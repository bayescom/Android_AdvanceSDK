package com.advance.supplier.flink;

import android.app.Activity;
import android.content.Context;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.fl.saas.adx.api.FLInterstitial;
import com.fl.saas.adx.base.exception.FLError;
import com.fl.saas.adx.base.interfaces.AdViewInterstitialListener;

import java.util.Map;

public class FLInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    FLInterstitial flAd;
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        flAd = new FLInterstitial.Builder(getRealContext())
                .setKey(getPosID())
                .setInterstitialListener(new AdViewInterstitialListener() {
                    @Override
                    public void onAdReady() {
                        LogUtil.simple(TAG + "onAdReady  ");
//请求成功时回调。可以在此处调用展示
                        handleSucceed(flAd.getEcpm());
                    }

                    @Override
                    public void onAdDisplay() {
                        LogUtil.simple(TAG + "onAdDisplay  ");
//广告展示时回调
                        handleShow();
                    }

                    @Override
                    public void onAdFailed(FLError error) {
// 广告异常、失败，回调该方法
                        LogUtil.simple(TAG + "onAdFailed , error = " + error);

                        FLUtil.handleErr(FLInterstitialAdapter.this, error);
                    }

                    @Override
                    public void onAdClick(String s) {
// 广告被点击时回调该方法
                        LogUtil.simple(TAG + "onAdClick ,s = " + s);

                        handleClick();
                    }

                    @Override
                    public void onAdClosed() {
// 广告关闭时回调该方法
                        LogUtil.simple(TAG + "onAdClosed  ");

                        handleClose();
                    }
                })
                .build();
        flAd.requestInterstitial();
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (flAd != null) {
            flAd.destroy();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        if (flAd != null) {
            flAd.show(getRealActivity(null));
        }
    }

    @Override
    public boolean isValid() {
        if (flAd != null) {
            return flAd.isReady();
        }
           return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }
}
