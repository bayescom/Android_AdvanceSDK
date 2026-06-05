package com.advance.supplier.tanx;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;


import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.ad.ITanxAd;
import com.alimm.tanx.core.ad.ad.template.rendering.splash.ITanxSplashExpressAd;
import com.alimm.tanx.core.ad.bean.TanxBiddingInfo;
import com.alimm.tanx.core.ad.listener.ITanxAdLoader;
import com.alimm.tanx.core.request.TanxAdSlot;
import com.alimm.tanx.core.request.TanxError;
import com.alimm.tanx.ui.TanxSdk;

import java.util.List;
import java.util.Map;

//根据tanx文档，开屏SDK内部有有效期逻辑，非品牌广告1天，品牌广告多天。每7天清理一次本地缓存。暂无对外有效性判断方法
public class TanxSplashAdapter extends AdvanceSplashCustomAdapter {
    ITanxAdLoader iTanxAdLoader;
    ITanxSplashExpressAd iTanxSplashExpressAd;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }


    @Override
    protected void adPrepared() {
//        showAD();
    }

    @Override
    public void destroyAd() {
        LogUtil.simple(TAG + "doDestroy");
        if (iTanxAdLoader != null) {
            iTanxAdLoader.destroy();
            LogUtil.simple(TAG + "iTanxAdLoader doDestroy");
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        TanxAdSlot adSlot = new TanxAdSlot.Builder()
                .adCount(sdkSupplier.adCount)
                .pid(sdkSupplier.adspotid)
                .build();
        iTanxAdLoader = TanxSdk.getSDKManager().createAdLoader(getRealContext());
        int timeout = sdkSupplier.timeout <= 0 ? 5000 : sdkSupplier.timeout;
        ITanxAdLoader.OnAdLoadListener<ITanxSplashExpressAd> loadListener = new ITanxAdLoader.OnAdLoadListener<ITanxSplashExpressAd>() {

            @Override
            public void onTimeOut() {
                String msg = "获取广告超时";
                LogUtil.simple(TAG + "onTimeOut   " + ", msg = " + msg);
                handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);

            }

            @Override
            public void onError(TanxError tanxError) {
                String msg = "广告请求失败";
                if (tanxError != null) {
                    msg = "[tanx onError] " + tanxError;
                }
                LogUtil.simple(TAG + "onError   " + ", msg = " + msg);
                handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);

            }

            @Override
            public void onLoaded(List<ITanxSplashExpressAd> adList) {
                try {
                    if (adList.size() == 0 || adList.get(0) == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                        return;
                    }
                    LogUtil.simple(TAG + "onLoaded");
                    iTanxSplashExpressAd = adList.get(0);

                    long ecpm = 0;
                    try {
                        ecpm =(iTanxSplashExpressAd.getBidInfo().getBidPrice());
                    } catch (Throwable e) {
                    }
                    handleSucceed(ecpm);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                }

            }
        };
        iTanxAdLoader.loadSplashAd(adSlot, loadListener, timeout);

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        if (iTanxSplashExpressAd == null) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "iTanxSplashExpressAd null"));
            return;
        }
        try {
//            iTanxAdLoader = TanxSdk.getSDKManager().createAdLoader(BYUtil.getActivityFromView(setting.getAdContainer()));

            TanxBiddingInfo biddingResult = new TanxBiddingInfo();
            biddingResult.setBidResult(true);
            //上报竞价成功
            iTanxSplashExpressAd.setBiddingResult(biddingResult);
            iTanxSplashExpressAd.setOnSplashAdListener(new ITanxSplashExpressAd.OnSplashAdListener() {
                @Override
                public void onAdRender(ITanxSplashExpressAd iTanxSplashExpressAd) {
                    LogUtil.simple(TAG + "onAdRender");

                }

                @Override
                public void onAdClicked() {
                    LogUtil.simple(TAG + "onAdClicked");

                    handleClick();
                }

                @Override
                public void onAdShake() {
                    LogUtil.simple(TAG + "onAdShake");

                    handleClick();
                }

                @Override
                public void onClickCommitSuccess(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onClickCommitSuccess");

                }

                @Override
                public void onExposureCommitSuccess(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onExposureCommitSuccess");

                }

                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + "onAdShow");

                    handleShow();
                }

                @Override
                public void onAdClosed() {
                    LogUtil.simple(TAG + "onAdClosed");

                    handleSkip();
                }

                @Override
                public void onAdFinish() {
                    LogUtil.simple(TAG + "onAdFinish");

                    handleTimeOver();
                }

                @Override
                public void onShowError(TanxError tanxError) {
                    String msg = "展示广告失败";
                    if (tanxError != null) {
                        msg = "[tanx onShowError] " + tanxError;
                    }
                    LogUtil.simple(TAG + "onShowError   " + ", msg = " + msg);
                    handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);
                }
            });
            // TODO: 2024/7/1 优化此处和信息流广告位得activity信息采集来源，优先使用承载view中获取的activity，其次使用初始化时传递得，最次使用当前展示的activity（待实现补充）；如果都没有则不使用带activity的getAdView方法
            //获取SplashView
            View view = iTanxSplashExpressAd.getAdView(activity);
            //渲染之前判断activity生命周期状态
//            if (!AdvanceUtil.isActivityDestroyed(softReferenceActivity)) {
//                adContainer.removeAllViews();
//                //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
//                adContainer.addView(view);
            boolean add = AdvanceUtil.addADView(getAdContainer(), view);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
            if (splashSetting!=null){
                TextView skipView = splashSetting.getSkipView();
                if (null != skipView) {
                    skipView.setVisibility(View.INVISIBLE);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
//            }

    }

}
