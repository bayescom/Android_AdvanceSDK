package com.advance.supplier.honor;

import static com.advance.model.AdvanceError.ERROR_DATA_NULL;

import android.app.Activity;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.hihonor.adsdk.base.AdSlot;
import com.hihonor.adsdk.base.api.feed.PictureTextAdLoadListener;
import com.hihonor.adsdk.base.api.feed.PictureTextExpressAd;
import com.hihonor.adsdk.base.callback.AdListener;
import com.hihonor.adsdk.picturetextad.PictureTextAdLoad;

import java.util.List;

public class HonorNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    PictureTextExpressAd mExpressAd;

    public HonorNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
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

                addADView(mExpressAd.getExpressAdView());
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        HonorUtil.initAD(this);

        AdSlot.Builder builder = new AdSlot.Builder();
        builder.setSlotId(sdkSupplier.adspotid);

        //实际测试宽高设置无任何效果
//        if (mSetting != null) {
//            int widthDP = mSetting.getExpressViewWidth();
//            int heightDP = mSetting.getExpressViewHeight();
//            LogUtil.devDebug(TAG + "getExpressViewWidth = " + widthDP);
//            LogUtil.devDebug(TAG + "getExpressViewHeight = " + heightDP);
//            if (widthDP > 0) {
//                builder.setWidth(widthDP);
//            }
//            if (heightDP > 0) {
//                builder.setHeight((heightDP));
//            }
//        }

        // 创建广告请求参数对象（AdSlot）
         // 必传,设置您的广告位ID。
//                .setLoadType(-1) // 非必传，设置广告请求方式  -1：默认请求方式，不进行缓存  0：普通请求，优先去读缓存  1：预缓存请求，数据保存至缓存



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

                            handleSucceed();
                        }
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        LogUtil.d(TAG + "onFailed");

                        handleFailed(code, errorMsg);
                    }
                }) // 必传，注册广告加载状态监听器。
                .setAdSlot(builder.build()) // 必传，设置广告请求参数。
                .build();
// 加载广告
        load.loadAd();
    }
}
