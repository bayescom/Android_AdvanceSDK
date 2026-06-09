package com.advance.supplier.ks;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;

import java.util.Map;

public class KSSplashAdapter extends AdvanceSplashCustomAdapter implements KsSplashScreenAd.SplashScreenAdInteractionListener {
    private String TAG = "[KSSplashAdapter] ";
    private KsSplashScreenAd splashAd;


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        if (BYUtil.isDev()) {// 测试逻辑，正式上线需移除
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "测试ks渲染异常");
//                }
//            },200);
////            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "测试渲染异常");
//            return;
//        }

        if (splashAd == null) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "splashAd null"));
            return;
        }

        try {
            activity = getRealActivity(getAdContainer());

            //获取SplashView
            View view = splashAd.getView(activity, this);
            //渲染之前判断activity生命周期状态
            boolean isDestroy = AdvanceUtil.isActivityDestroyed(activity);
            if (isDestroy) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "ActivityDestroyed"));
                return;
            }
            //                adContainer.removeAllViews();
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽

            boolean add = AdvanceUtil.addADView(getAdContainer(), view);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
            //                adContainer.addView(view);
//                if (skipView != null) {
//                    skipView.setVisibility(View.INVISIBLE);
//                }
            TextView skipView = splashSetting.getSkipView();
            if (null != skipView) {
                skipView.setVisibility(View.INVISIBLE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }

    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        //场景设置
        KsScene scene = new KsScene.Builder(KSUtil.getADID(sdkSupplier)).build(); // 此为测试posId，请联系快手平台申请正式posId
        KsAdSDK.getLoadManager().loadSplashScreenAd(scene, new KsLoadManager.SplashScreenAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogUtil.simple(TAG + " onError ");

                handleFailed(code, msg);


            }

            @Override
            public void onRequestResult(int adNumber) {
                LogUtil.simple(TAG + "onRequestResult，广告填充数量：" + adNumber);
            }

            @Override
            public void onSplashScreenAdLoad(KsSplashScreenAd splashScreenAd) {
                LogUtil.simple(TAG + "onSplashScreenAdLoad");

                try {
                    if (splashScreenAd == null) {
                        String nMsg = TAG + " KsSplashScreenAd null";
                        handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                        return;
                    }
                    splashAd = splashScreenAd;

                    handleSucceed(splashAd.getECPM());

                } catch (Throwable e) {
                    e.printStackTrace();
                    runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                }
            }
        });

    }


    @Override
    protected void adPrepared() {

//        if (splashAd != null) {
//            //获取SplashView
//            View view = splashAd.getView(getADActivity(), this);
//            //渲染之前判断activity生命周期状态
//            if (!AdvanceUtil.isActivityDestroyed(softReferenceActivity)) {
//                adContainer.removeAllViews();
//                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT));
//                //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
//                adContainer.addView(view);
//                if (skipView != null) {
//                    skipView.setVisibility(View.INVISIBLE);
//                }
//
//            }
//        }

    }

    @Override
    public void destroyAd() {

    }


    //------广告回调事件------

    @Override
    public void onAdClicked() {
        LogUtil.simple(TAG + "onAdClicked");

        handleClick();
    }

    @Override
    public void onAdShowError(int code, String extra) {
        String msg = ",开屏广告显示错误 ,code =" + code + " extra " + extra;
        LogUtil.e(TAG + "onAdShowError" + msg);

        //异常时不触发显示miniWindow
        splashAd = null;
        //按照渲染异常进行异常回调
        handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, msg);
    }

    @Override
    public void onAdShowEnd() {
        LogUtil.simple(TAG + "onAdShowEnd");

        handleTimeOver();
    }

    @Override
    public void onAdShowStart() {
        LogUtil.simple(TAG + "onAdShowStart");

        handleShow();
    }

    @Override
    public void onSkippedAd() {
        LogUtil.simple(TAG + "onSkippedAd");
        handleSkip();
    }

    @Override
    public void onDownloadTipsDialogShow() {
        LogUtil.simple(TAG + "onDownloadTipsDialogShow");

    }

    @Override
    public void onDownloadTipsDialogDismiss() {
        LogUtil.simple(TAG + "onDownloadTipsDialogDismiss");

    }

    @Override
    public void onDownloadTipsDialogCancel() {
        LogUtil.simple(TAG + "onDownloadTipsDialogCancel");

    }

    @Override
    public boolean isValid() {
        try {
            if (splashAd != null) {
                return splashAd.isAdEnable();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }
}
