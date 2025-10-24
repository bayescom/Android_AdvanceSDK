package com.advance.supplier.honor;

import static com.advance.model.AdvanceError.ERROR_DATA_NULL;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.hihonor.adsdk.base.AdSlot;
import com.hihonor.adsdk.base.api.AdVideo;
import com.hihonor.adsdk.base.api.feed.PictureTextAdLoadListener;
import com.hihonor.adsdk.base.api.feed.PictureTextExpressAd;
import com.hihonor.adsdk.base.callback.AdListener;
import com.hihonor.adsdk.picturetextad.PictureTextAdLoad;
import com.hihonor.adsdk.picturetextad.PictureTextAdRootView;

import java.util.List;

public class HonorRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    PictureTextExpressAd mExpressAd;

    public HonorRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    protected void paraLoadAd() {
        loadAd();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (mExpressAd != null) {
            mExpressAd.release();
        }
    }

    @Override
    public boolean isValid() {
        if (HonorUtil.isAdExpire(mExpressAd)) {
            return false;
        }
        return super.isValid();
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {

        try {
            if (mExpressAd != null) {


                /**
                 * 广告事件监听器
                 */
                mExpressAd.setAdListener(new AdListener() {
                    /**
                     * 开屏广告点击跳过或倒计时结束时回调
                     *
                     * @param type 0：点击跳过、1：倒计时结束
                     */
                    @Override
                    public void onAdSkip(int type) {
                        LogUtil.simple(TAG + "onAdSkip, type: " + type);
                    }

                    /**
                     * 广告关闭时回调
                     */
                    @Override
                    public void onAdClosed() {
                        LogUtil.simple(TAG + "onAdClosed...");
                        handleClose();
                    }


                    /**
                     * 广告曝光时回调
                     */
                    @Override
                    public void onAdImpression() {
                        LogUtil.simple(TAG + "onAdImpression...");

                        handleShow();
                    }

                    /**
                     * 广告曝光失败时回调
                     *
                     * @param errCode 错误码
                     * @param msg 曝光失败信息
                     */
                    @Override
                    public void onAdImpressionFailed(int errCode, String msg) {
                        super.onAdImpressionFailed(errCode, msg);
                        LogUtil.simple(TAG + "onAdImpressionFailed, errCode: " + errCode + ", msg: " + msg);

                        handleFailed(errCode, msg);
                    }

                    /**
                     * 广告被点击时回调
                     */
                    @Override
                    public void onAdClicked() {
                        LogUtil.simple(TAG + "onAdClicked...");

                        handleClick();
                    }

                    /**
                     * 广告成功跳转小程序时回调
                     */
                    @Override
                    public void onMiniAppStarted() {
                        LogUtil.simple(TAG + "onMiniAppStarted...");

                    }
                });


                PictureTextAdRootView nativeView = new PictureTextAdRootView(getRealContext());

                //赋值广告信息
                nativeView.setAd(mExpressAd);

//            需要先拿到根布局信息
                AdvRFRootView rootView = mAdvanceRFBridge.getMaterialProvider().rootView;
                Activity activity = getRealActivity(rootView);
//            -----------方案B copy全部子布局，复制子控件至新布局，并将新布局添加至旧父布局中
                AdvanceRFUtil.copyChild(rootView, nativeView);


                //注册点击
                nativeView.registerViewForInteraction(mAdvanceRFBridge.getMaterialProvider().clickViews);

                //渲染视频。
                AdVideo video = mExpressAd.getAdVideo();
                AdvRFVideoView videoView = mAdvanceRFBridge.getMaterialProvider().videoView;
                if (videoView != null) {
                    videoView.addView(video.getVideoView());
                }

                //关闭广告事件绑定
                View dislikeView = mAdvanceRFBridge.getMaterialProvider().disLikeView;
                if (dislikeView != null) {
                    dislikeView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            LogUtil.simple(TAG + " dislikeView onClick");

                            try {
                                nativeView.removeAllViews();
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            handleClose();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        HonorUtil.initAD(this);

        // 创建广告请求参数对象（AdSlot）
        AdSlot adSlot = new AdSlot.Builder()
                .setSlotId(sdkSupplier.adspotid) // 必传,设置您的广告位ID。
//                .setLoadType(-1) // 非必传，设置广告请求方式  -1：默认请求方式，不进行缓存  0：普通请求，优先去读缓存  1：预缓存请求，数据保存至缓存
                .build();

        // 构建广告加载器，传入已创建好的广告请求参数对象与广告加载状态监听器。
        PictureTextAdLoad load = new PictureTextAdLoad.Builder()
                .setPictureTextAdLoadListener(new PictureTextAdLoadListener() {
                    @Override
                    public void onAdLoaded(List<PictureTextExpressAd> list) {
                        LogUtil.d(TAG + "onLoadSuccess");
                        if (list == null || list.isEmpty() || list.get(0) == null) {
                            handleFailed(ERROR_DATA_NULL, "");
                        } else {
                            mExpressAd = list.get(0);

                            updateBidding(HonorUtil.getECPM(mExpressAd));

                            dataConverter = new HonorRenderDataConverter(mExpressAd, HonorRenderFeedAdapter.this);

                            handleSucceed();
                        }
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        LogUtil.d(TAG + "onFailed");

                        handleFailed(code, errorMsg);
                    }
                }) // 必传，注册广告加载状态监听器。
                .setAdSlot(adSlot) // 必传，设置广告请求参数。
                .build();
// 加载广告
        load.loadAd();
    }
}
