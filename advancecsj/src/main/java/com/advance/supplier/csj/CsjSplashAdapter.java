package com.advance.supplier.csj;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;

import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.CSJAdError;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
import com.bytedance.sdk.openadsdk.CSJSplashCloseType;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;

import java.lang.ref.SoftReference;

public class CsjSplashAdapter extends AdvanceSplashCustomAdapter {
    private CSJSplashAd newSplashAd;
    private String TAG = "[CsjSplashAdapter] ";
//    boolean useOldApi = false;

    public CsjSplashAdapter(SoftReference<Activity> activity, SplashSetting setting) {
        super(activity, setting);
    }

    @Override
    public void show() {
        showAD();
    }

    @Override
    protected void paraLoadAd() {
        initAD();
    }

    @Override
    protected void adReady() {
//        showAD();
    }

    @Override
    public void doDestroy() {

    }

    private void showAD() {
        try {
            //获取SplashView
            View view;

            if (newSplashAd == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "newSplashAd null"));
                return;
            }
            view = newSplashAd.getSplashView();
            initNewSplashClickEyeData(newSplashAd, view);
            boolean isDestroy = AdvanceUtil.isActivityDestroyed(getRealActivity(splashSetting.getAdContainer()));
            if (isDestroy) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "ActivityDestroyed"));
                return;
            }
            //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
            boolean add = AdvanceUtil.addADView(splashSetting.getAdContainer(), view);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
            TextView skipView = splashSetting.getSkipView();
            if (null != skipView) {
                skipView.setVisibility(View.INVISIBLE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }


//        if (AdvanceSetting.getInstance().isDev) {
//            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "模拟测试展示失败");
//        }
    }


    public void orderLoadAd() {
        try {
//            if (setting != null && setting.getGdtSkipContainer() != null) {
//                setting.getGdtSkipContainer().setVisibility(View.GONE);
//            }
            initAD();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
            String cause = e.getCause() != null ? e.getCause().toString() : "no cause";
            String msg = "CsjSplashAdapter Throwable" + cause;
            reportCodeErr(msg);
        }
    }

    private void initAD() {
        //初始化值
        CSJSplashClickEyeManager.getInstance().setSupportSplashClickEye(false);

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
//        当后台下发 versionTag 为1时，使用旧版本api调用方法


        if (BYUtil.isDev()) {
//           客户端 bidding 测试广告位
//            sdkSupplier.adspotid = "888483708";
//            useOldApi = true;
        }
        final TTAdManager ttAdManager = TTAdSdk.getAdManager();
        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(getRealContext());
        }
        LogUtil.devDebug(TAG + " startLoad sdkSupplier.adspotid = " + sdkSupplier.adspotid);
        AdSlot adSlot;
        //穿山甲后台暂时不支持开屏模板广告，代码先加上相关判断，不影响现有展示。
        if (splashSetting.getCsjShowAsExpress()) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(sdkSupplier.adspotid)
                    .setSupportDeepLink(true)
                    .setExpressViewAcceptedSize(splashSetting.getCsjExpressViewWidth(), splashSetting.getCsjExpressViewHeight())
                    .setImageAcceptedSize(splashSetting.getCsjAcceptedSizeWidth(), splashSetting.getCsjAcceptedSizeHeight())
//                    .setSplashButtonType(AdvanceSetting.getInstance().csj_splashButtonType)
//                    .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                    .build();
            LogUtil.devDebug(TAG + "getCsjShowAsExpress .  setting.getCsjExpressViewWidth() = " + splashSetting.getCsjExpressViewWidth()
                    + ", setting.getCsjExpressViewHeight()  = " + splashSetting.getCsjExpressViewHeight());
        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(sdkSupplier.adspotid)
                    .setSupportDeepLink(true)
                    .setImageAcceptedSize(splashSetting.getCsjAcceptedSizeWidth(), splashSetting.getCsjAcceptedSizeHeight())
//                    .setSplashButtonType(AdvanceSetting.getInstance().csj_splashButtonType)
//                    .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                    .build();
        }
        TTAdNative ttAdNative = ttAdManager.createAdNative(getRealContext());
        int timeout = sdkSupplier.timeout <= 0 ? 5000 : sdkSupplier.timeout;


        ttAdNative.loadSplashAd(adSlot, new TTAdNative.CSJSplashAdListener() {
            @Override
            public void onSplashLoadSuccess(CSJSplashAd csjSplashAd) {
                LogUtil.simple(TAG + "onSplashLoadSuccess");

            }

            @Override
            public void onSplashLoadFail(CSJAdError csjAdError) {
                LogUtil.simple(TAG + "onSplashLoadFail");

                newApiAdFailed(csjAdError, AdvanceError.ERROR_EXCEPTION_LOAD, "onSplashLoadFail");
            }

            @Override
            public void onSplashRenderSuccess(CSJSplashAd csjSplashAd) {
                LogUtil.simple(TAG + "onSplashRenderSuccess");

                if (csjSplashAd == null) {
                    String nMsg = TAG + " TTSplashAd null";
                    AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, nMsg);
                    runParaFailed(error);
                    return;
                }
                newSplashAd = csjSplashAd;

                updateBidding(CsjUtil.getEcpmValue(TAG, csjSplashAd.getMediaExtraInfo()));
                handleSucceed();
                newSplashAd.setSplashAdListener(new CSJSplashAd.SplashAdListener() {
                    @Override
                    public void onSplashAdShow(CSJSplashAd csjSplashAd) {
                        LogUtil.simple(TAG + "onSplashAdShow");

                        handleShow();
                    }

                    @Override
                    public void onSplashAdClick(CSJSplashAd csjSplashAd) {
                        LogUtil.simple(TAG + "onSplashAdClick");
                        handleClick();
                    }

                    @Override
                    public void onSplashAdClose(CSJSplashAd csjSplashAd, int closeType) {
                        LogUtil.simple(TAG + "onSplashAdClose , closeType = " + closeType);
                        if (splashSetting != null) {
                            if (closeType == CSJSplashCloseType.CLICK_SKIP) {
                                splashSetting.adapterDidSkip();
                            } else if (closeType == CSJSplashCloseType.COUNT_DOWN_OVER) {
                                splashSetting.adapterDidTimeOver();
                            } else {
                                splashSetting.adapterDidSkip();
                            }
                        }
                    }
                });

            }

            @Override
            public void onSplashRenderFail(CSJSplashAd csjSplashAd, CSJAdError csjAdError) {
                LogUtil.simple(TAG + "onSplashRenderFail");

                newApiAdFailed(csjAdError, AdvanceError.ERROR_RENDER_FAILED, "onSplashRenderFail");
            }
        }, timeout);

    }

    private void newApiAdFailed(CSJAdError csjAdError, String errCodeDefault, String errExt) {
        try {
            AdvanceError error;
            if (csjAdError == null) {
                error = AdvanceError.parseErr(errCodeDefault, errExt);
            } else {
                error = AdvanceError.parseErr(csjAdError.getCode(), csjAdError.getMsg());
            }
            runParaFailed(error);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 以下为点睛广告特殊处理
     */

    //是否进行点睛广告的展示
    private void switchSplashClickShow() {
        try {
            if (splashSetting == null) {
                return;
            }
            if (splashSetting.isShowInSingleActivity()) {
                new CsjUtil().zoomOut(getRealActivity(splashSetting.getAdContainer()));
            } else {
                AdvanceSetting.getInstance().isSplashSupportZoomOut = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }





//    NewSplashClickEyeListener mSplashClickEyeListener;

    private void initNewSplashClickEyeData(CSJSplashAd splashAd, View splashView) {
        try {
//            if (splashAd == null || splashView == null) {
//                return;
//            }
//            Activity adAct = getRealActivity(setting.getAdContainer());
//            mSplashClickEyeListener = new NewSplashClickEyeListener(adAct, splashAd, setting.getAdContainer(), splashView);
//
//            splashAd.setSplashClickEyeListener(mSplashClickEyeListener);
//            CSJSplashClickEyeManager.getInstance().init(adAct);
//            CSJSplashClickEyeManager.getInstance().setCSJSplashInfo(splashAd, splashView, adAct.getWindow().getDecorView());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean isValid() {
        if (newSplashAd != null && newSplashAd.getMediationManager() != null) {
            return newSplashAd.getMediationManager().isReady();
        }
        return super.isValid();
    }
}
