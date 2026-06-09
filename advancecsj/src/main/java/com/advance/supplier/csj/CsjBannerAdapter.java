package com.advance.supplier.csj;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.advance.AdvanceConfig;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;
import java.util.Map;

/**
 * 如果网络异常，不会进行刷新行为，且不会回调失败。当网络正常，会继续定时刷新。视为内部闭环了刷新行为，一旦失败就流转下一优先级
 */
public class CsjBannerAdapter extends AdvanceBannerCustomAdapter implements TTAdNative.NativeExpressAdListener {
    private long startTime = 0;
    private String TAG = "[CsjBannerAdapter] ";
    private TTNativeExpressAd ad;


    @Override
    public void onError(int code, String message) {
        LogUtil.e(TAG + " onError: code = " + code + " msg = " + message);
        handleFailed(code + "", message);
    }

    @Override
    public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
        try {
            LogUtil.simple(TAG + "onNativeExpressAdLoad");
            if (ads == null || ads.size() == 0) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "广告列表数据为空");
                return;
            }
            ad = ads.get(0);
            // 加载成功的回调，接入方可在此处做广告的展示，请确保您的代码足够健壮，能够处理异常情况；
            if (null == ad) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "广告数据为空");
                return;
            }

            handleSucceed(CsjUtil.getEcpmValue(TAG, ad.getMediaExtraInfo()));
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    private void bindAdListener(TTNativeExpressAd ad) {
        try {
            if (null != bannerSetting) {
                ad.setSlideIntervalTime(bannerSetting.getRefreshInterval() * 1000);
            }
            ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                @Override
                public void onAdClicked(View view, int i) {
                    LogUtil.simple(TAG + "ExpressView onAdClicked , type :" + i);

                    handleClick();
                }

                @Override
                public void onAdShow(View view, int i) {
                    LogUtil.simple(TAG + "ExpressView onAdShow, type :" + i + ",cost time = " + (System.currentTimeMillis() - startTime));

                    handleShow();
                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    LogUtil.simple(TAG + "ExpressView render fail:" + (System.currentTimeMillis() - startTime));

                    doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, TAG + code + "， " + msg));
                }

                @Override
                public void onRenderSuccess(View view, float v, float v1) {
                    LogUtil.simple(TAG + "ExpressView render suc:" + (System.currentTimeMillis() - startTime));

                        ViewGroup adContainer = getAdContainer();
                        if (adContainer != null) {
//                            adContainer.removeAllViews();
                            boolean add = AdvanceUtil.addADView(adContainer, view);
                            if (!add) {
                                doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
                            }
//                            adContainer.addView(view);
                        }

                }
            });

            //使用默认模板中默认dislike弹出样式
            ad.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onShow() {

                }

                @Override
                public void onSelected(int position, String value, boolean enforce) {
//                    if (null != bannerSetting) {
//                        //用户选择不喜欢原因后，移除广告展示
//                        ViewGroup adContainer = bannerSetting.getContainer();
//                        if (adContainer != null) {
//                            adContainer.removeAllViews();
//                        }
//
//                        bannerSetting.adapterDidDislike();
//                    }

                    handleClose();
                }

                @Override
                public void onCancel() {
                }

//                @Override
//                public void onRefuse() {
//
//                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void destroyAd() {
        try {
            if (ad != null)
                ad.destroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        final TTAdManager ttAdManager = TTAdSdk.getAdManager();
        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        TTAdNative ttAdNative = ttAdManager.createAdNative(activity);
        AdSlot adSlot = new AdSlot.Builder()
                // 必选参数 设置您的CodeId
                .setCodeId(sdkSupplier.adspotid)
                //期望模板广告view的size,单位dp
                .setExpressViewAcceptedSize(bannerSetting.getCsjExpressViewAcceptedWidth(), bannerSetting.getCsjExpressViewAcceptedHeight())
                // 必选参数 设置广告图片的最大尺寸及期望的图片宽高比，单位Px
                .setImageAcceptedSize(bannerSetting.getCsjAcceptedSizeWidth(), bannerSetting.getCsjAcceptedSizeHeight())
                // 可选参数 设置是否支持deeplink
                .setSupportDeepLink(true)
                //请求原生广告时候需要设置，参数为TYPE_BANNER或TYPE_INTERACTION_AD
//                .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                .build();
        ttAdNative.loadBannerExpressAd(adSlot, this);
    }



    @Override
    protected void adPrepared() {
//        startTime = System.currentTimeMillis();
//        if (ad != null) {
//            ad.render();
//        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra){
        try {
            startTime = System.currentTimeMillis();
            bindAdListener(ad);
            ad.render();
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        if (ad != null && ad.getMediationManager() != null) {
            return ad.getMediationManager().isReady();
        }

        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }
}
