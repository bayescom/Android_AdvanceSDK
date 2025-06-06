package com.advance.supplier.csj;

import android.app.Activity;

import com.advance.AdvanceConfig;
import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bykv.vk.openvk.TTFullVideoObject;
import com.bykv.vk.openvk.TTVfConstant;
import com.bykv.vk.openvk.TTVfManager;
import com.bykv.vk.openvk.TTVfNative;
import com.bykv.vk.openvk.TTVfSdk;
import com.bykv.vk.openvk.VfSlot;

public class CsjInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    private InterstitialSetting advanceInterstitial;
    private final String TAG = "[CsjInterstitialAdapter] ";

    public TTFullVideoObject newVersionAd;

    public CsjInterstitialAdapter(Activity activity, InterstitialSetting advanceInterstitial) {
        super(activity, advanceInterstitial);
        this.advanceInterstitial = advanceInterstitial;
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public void show() {
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
            String nullTip = TAG + "请先加载广告或者广告已经展示过";
            if (newVersionAd != null) {
                newVersionAd.showFullVideoVs(activity, TTVfConstant.RitScenes.GAME_GIFT_BONUS, null);
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

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable t) {
            t.printStackTrace();

            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }

    }

    @Override
    protected void paraLoadAd() {
        CsjUtil.initCsj(this, new CsjUtil.InitListener() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
                startLoad();
            }

            @Override
            public void fail(int code, String msg) {
                handleFailed(code, msg);
            }
        });
    }

    private void startLoad() {

        final TTVfManager ttAdManager = TTVfSdk.getVfManager();

        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        TTVfNative ttAdNative = ttAdManager.createVfNative(activity);
        VfSlot adSlot = new VfSlot.Builder()
                .setCodeId(sdkSupplier.adspotid)
                .setSupportDeepLink(true)
                .setExpressViewAcceptedSize(advanceInterstitial.getCsjExpressViewWidth(), advanceInterstitial.getCsjExpressViewHeight())
                .setImageAcceptedSize(600, 600) //根据广告平台选择的尺寸，传入同比例尺寸
//                .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                .build();
        ttAdNative.loadFullVideoVs(adSlot, new TTVfNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int i, String s) {
                handleFailed(i, s);
            }

            @Override
            public void onFullVideoVsLoad(TTFullVideoObject ttFullScreenVideoAd) {
                try {
                    LogUtil.simple(TAG + "onFullScreenVideoAdLoad");

                    newVersionAd = ttFullScreenVideoAd;
                    if (newVersionAd == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "new ints ad null");
                        return;
                    }

                    updateBidding(CsjUtil.getEcpmValue(TAG, newVersionAd.getMediaExtraInfo()));

                    newVersionAd.setFullScreenVideoAdInteractionListener(new TTFullVideoObject.FullVideoVsInteractionListener() {
                        @Override
                        public void onShow() {
                            LogUtil.simple(TAG + "newVersionAd onAdShow");
                            handleShow();
                        }

                        @Override
                        public void onVideoBarClick() {
                            LogUtil.simple(TAG + "newVersionAd onAdVideoBarClick");
                            handleClick();
                        }

                        @Override
                        public void onClose() {
                            LogUtil.simple(TAG + "newVersionAd onAdClose");

                            if (advanceInterstitial != null)
                                advanceInterstitial.adapterDidClosed();
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
                    handleSucceed();

                } catch (Throwable e) {
                    e.printStackTrace();
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                }
            }

            @Override
            public void onFullVideoCached() {
                LogUtil.simple(TAG + "onFullScreenVideoCached");

            }

            @Override
            public void onFullVideoCached(TTFullVideoObject ttFullScreenVideoAd) {
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
    protected void adReady() {
        //新版本调用的是全屏视频的方法

    }


    @Override
    public boolean isValid() {
        //        return  false;
        if (newVersionAd != null && newVersionAd.getMediationManager() != null) {
            return newVersionAd.getMediationManager().isReady();
        }
        return super.isValid();
    }
}
