package com.advance.supplier.tanx;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.ad.ITanxAd;
import com.alimm.tanx.core.ad.ad.template.rendering.feed.ITanxFeedExpressAd;
import com.alimm.tanx.core.ad.bean.TanxBiddingInfo;
import com.alimm.tanx.core.ad.listener.ITanxAdLoader;
import com.alimm.tanx.core.request.TanxAdSlot;
import com.alimm.tanx.core.request.TanxError;
import com.alimm.tanx.ui.TanxSdk;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYUtil;

import java.util.List;
import java.util.Map;

public class TanxNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    ITanxAdLoader iTanxAdLoader;
    ITanxFeedExpressAd iTanxFeedExpressAd;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        LogUtil.simple(TAG + "doDestroy");
        if (iTanxAdLoader != null) {
            iTanxAdLoader.destroy();
            LogUtil.devDebug(TAG + "iTanxAdLoader doDestroy");
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            TanxBiddingInfo biddingResult = new TanxBiddingInfo();
            biddingResult.setBidResult(true);
            iTanxFeedExpressAd.setBiddingResult(biddingResult);
            iTanxFeedExpressAd.setOnFeedAdListener(new ITanxFeedExpressAd.OnFeedAdListener() {
                @Override
                public void onAdClose(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onAdClose");

                    handleClose();
                }

                @Override
                public void onClick(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onClick");
                    handleClick();
                }

                @Override
                public void onAdShow(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onAdShow");

                    handleShow();
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
                public void onError(String s) {
                    LogUtil.simple(TAG + "onError ," + s);

                    handleFailed(AdvanceError.ERROR_TANX_FAILED, s);
                }
            });

//            Activity activity = BYUtil.getActivityFromView(mSetting.getAdContainer());
            View adView;
            if (activity != null) {
                adView = iTanxFeedExpressAd.getAdView(activity);
            } else {
                adView = iTanxFeedExpressAd.getAdView();
            }

            addADView(adView);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        TanxAdSlot adSlot = new TanxAdSlot.Builder()
                .adCount(sdkSupplier.adCount)
                .pid(sdkSupplier.adspotid)
                .build();

        iTanxAdLoader = TanxSdk.getSDKManager().createAdLoader(getRealContext());
        iTanxAdLoader.loadFeedAd(adSlot, new ITanxAdLoader.OnAdLoadListener<ITanxFeedExpressAd>() {

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
            public void onLoaded(List<ITanxFeedExpressAd> adList) {
                try {
                    if (adList.size() == 0 || adList.get(0) == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                        return;
                    }
                    LogUtil.simple(TAG + "onLoaded");
                    iTanxFeedExpressAd = adList.get(0);

                    long ecpm = 0;
                    try {
                        ecpm =(iTanxFeedExpressAd.getBidInfo().getBidPrice());
                    } catch (Throwable e) {
                    }
                    handleSucceed(ecpm);

                } catch (Throwable e) {
                    e.printStackTrace();
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                }
            }
        });
    }
}
