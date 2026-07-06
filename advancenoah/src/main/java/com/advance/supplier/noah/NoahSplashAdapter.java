package com.advance.supplier.noah;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.noah.api.AdError;
import com.noah.api.SplashAd;

import java.util.Map;

public class NoahSplashAdapter extends AdvanceSplashCustomAdapter {
    SplashAd noahAD;

    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        SplashAd.getAd(context, getPosID(), new SplashAd.AdListener() {
            @Override
            public void onAdError(final AdError adError) {
                LogUtil.simple(TAG+" onAdError , adError = "+ adError);
                //广告返回失败

                NoahUtil.loadErr(NoahSplashAdapter.this, adError);
            }

            @Override
            public void onAdLoaded(final SplashAd ad) {
                LogUtil.simple(TAG+" onAdLoaded , ad = "+ ad);

                //广告返回成功，展示广告
                if (ad == null) {
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "noahAD null");
                    return;
                }
                noahAD = ad;

                handleSucceed(ad.getPrice());
            }

            @Override
            public void onAdShown(final SplashAd ad) {
                //广告曝光
                LogUtil.simple(TAG+" onAdShown ");

                handleShow();
            }

            @Override
            public void onAdSkip(final SplashAd ad) {
                //广告点击Skip按钮
                LogUtil.simple(TAG+" onAdSkip ");

                handleSkip();
            }

            @Override
            public void onAdClicked(final SplashAd ad) {
                //广告点击
                LogUtil.simple(TAG+" onAdClicked ");

                handleClick();
            }

            @Override
            public void onAdTimeOver(final SplashAd splashAd) {
                //闪屏广告倒计时结束
                LogUtil.simple(TAG+" onAdTimeOver ");

                handleTimeOver();
            }
        });
    }

    @Override
    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        //splashViewGroup是app闪屏广告展示区域父组件
        noahAD.showSplashAd(getAdContainer());
    }

    @Override
    public boolean isValid() {
        if (noahAD != null)
            return noahAD.isValid();

        return false;
    }

    @Override
    public void destroyAd() {
        if (noahAD != null)
            noahAD.destroy();
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " + winPrice + ", referBidInfo = " + referBidInfo);

        NoahUtil.bid(noahAD, isWin, winPrice, referBidInfo);
    }
}
