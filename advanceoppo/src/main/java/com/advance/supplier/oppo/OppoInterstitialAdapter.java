package com.advance.supplier.oppo;

import android.app.Activity;
import android.content.Context;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.heytap.msp.mobad.api.ad.InterstitialAd;
import com.heytap.msp.mobad.api.listener.IInterstitialAdListener;

import java.util.Map;

public class OppoInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    InterstitialAd mInterstitialAd;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        OppoUtil.bid(mInterstitialAd,isWin,winPrice,referBidInfo);
    }
    

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {

            /**
             * 构造 InterstitialAd.
             */
            mInterstitialAd = new InterstitialAd(getRealActivity(null), sdkSupplier.adspotid);
            /**
             * 设置插屏广告行为监听器.
             */
            mInterstitialAd.setAdListener(new IInterstitialAdListener() {
                @Override
                public void onAdReady() {
                    LogUtil.simple(TAG + "onAdReady ");

                    int ecpm = mInterstitialAd == null ? 0 : mInterstitialAd.getECPM();
                    handleSucceed(ecpm);
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + " onAdClose");


                    handleClose();
                }

                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + " onAdShow");

                    handleShow();
                }

                @Override
                public void onAdFailed(String s) {
                    //                已废弃，

                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);

                }

                @Override
                public void onAdClick() {
                    LogUtil.simple(TAG + "onAdClick  ，   ");

                    handleClick();
                }

            });
            /**
             * 调用 loadAd() 方法请求广告.
             */
            mInterstitialAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adPrepared() {

    }


    @Override
    public void destroyAd() {
        try {
            if (mInterstitialAd != null)
                mInterstitialAd.destroyAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (mInterstitialAd != null)
                mInterstitialAd.showAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
