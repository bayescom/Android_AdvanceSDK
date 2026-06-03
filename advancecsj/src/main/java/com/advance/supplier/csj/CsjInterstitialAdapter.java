package com.advance.supplier.csj;

import android.app.Activity;
import android.content.Context;

import com.advance.AdvanceConfig;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

import java.util.Map;

public class CsjInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    private final String TAG = "[CsjInterstitialAdapter] ";

    public TTFullScreenVideoAd newVersionAd;

    @Override
    public void destroyAd() {

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra){
        try {
//            if (AdvanceUtil.isDev()) {//todo 测试逻辑，正式上线需移除
//                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "测试渲染异常");
//                    }
//                }, 1000);
//                return;
//            }
            newVersionAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + "newVersionAd onAdShow");
                    handleShow();
                }

                @Override
                public void onAdVideoBarClick() {
                    LogUtil.simple(TAG + "newVersionAd onAdVideoBarClick");
                    handleClick();
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + "newVersionAd onAdClose");

                    handleClose();
                }

                @Override
                public void onVideoComplete() {
                    LogUtil.simple(TAG + "newVersionAd onVideoComplete");
                }

                @Override
                public void onSkippedVideo() {
                    LogUtil.simple(TAG + "newVersionAd onSkippedVideo");
                }
            });

            String nullTip = TAG + "请先加载广告或者广告已经展示过";
            if (newVersionAd != null) {
                newVersionAd.showFullScreenVideoAd(activity, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
                newVersionAd = null;
            } else {
                LogUtil.e(nullTip);
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        CsjUtil.initCsj(this, new CsjUtil.InitListener() {
//            @Override
//            public void success() {
//                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
//                startLoad();
//
//            }
//
//            @Override
//            public void fail(int code, String msg) {
//                handleFailed(code, msg);
//            }
//        });
//
//
//        //检查是否命中使用缓存逻辑
//        boolean hitCache = AdvanceCacheUtil.loadWithCacheData(this, TTFullScreenVideoAd.class, new BYAbsCallBack<TTFullScreenVideoAd>() {
//            @Override
//            public void invoke(TTFullScreenVideoAd cacheAD) {
//                newVersionAd = cacheAD;
//                updateBidding(CsjUtil.getEcpmValue(TAG, cacheAD.getMediaExtraInfo()));
//            }
//        });
//        if (hitCache) {
//            return;
//        }
        final TTAdManager ttAdManager = TTAdSdk.getAdManager();
        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        TTAdNative ttAdNative = ttAdManager.createAdNative(activity);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(sdkSupplier.adspotid)
                .setSupportDeepLink(true)
                .setExpressViewAcceptedSize(interstitialSetting.getCsjExpressViewWidth(), interstitialSetting.getCsjExpressViewHeight())
                .setImageAcceptedSize(600, 600) //根据广告平台选择的尺寸，传入同比例尺寸
//                .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                .build();
        ttAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int i, String s) {
                handleFailed(i, s);
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {
                try {
                    LogUtil.simple(TAG + "onFullScreenVideoAdLoad");

                    newVersionAd = ttFullScreenVideoAd;
                    if (newVersionAd == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "new ints ad null");
                        return;
                    }

                    updateBidding(CsjUtil.getEcpmValue(TAG, newVersionAd.getMediaExtraInfo()));


                    handleSucceed(newVersionAd);

                } catch (Throwable e) {
                    e.printStackTrace();
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                }
            }

            @Override
            public void onFullScreenVideoCached() {
                LogUtil.simple(TAG + "onFullScreenVideoCached");

            }

            @Override
            public void onFullScreenVideoCached(TTFullScreenVideoAd ttFullScreenVideoAd) {
                try {
                    String ad = "";
                    if (ttFullScreenVideoAd != null) {
                        ad = ttFullScreenVideoAd.toString();
                    }
                    LogUtil.simple(TAG + "onFullScreenVideoCached( " + ad + ")");
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    protected void adPrepared() {
        //新版本调用的是全屏视频的方法

    }

    @Override
    public boolean isValid() {
        if (newVersionAd != null && newVersionAd.getMediationManager() != null) {
            return newVersionAd.getMediationManager().isReady();
        }

        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }
}
