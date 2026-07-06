package com.advance.supplier.noah;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.noah.api.AdError;
import com.noah.api.InterstitialAd;

import java.util.Map;

public class NoahInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    InterstitialAd noahAD;

    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        InterstitialAd.getAd(context, getPosID(), new InterstitialAd.AdListener() {
            @Override
            public void onAdError(final AdError adError) {
                LogUtil.simple(TAG + " onAdError , adError = " + adError);
                //广告返回失败

                NoahUtil.loadErr(NoahInterstitialAdapter.this, adError);
            }

            @Override
            public void onAdLoaded(final InterstitialAd ad) {
                //广告请求成功，展示广告
                LogUtil.simple(TAG+" onAdLoaded , ad = "+ ad);

                if (ad == null) {
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "noahAD null");
                    return;
                }
                noahAD = ad;

                handleSucceed(ad.getPrice());
            }

            @Override
            public void onAdShown(final InterstitialAd ad) {
                //广告曝光成功

                LogUtil.simple(TAG+" onAdShown ");

                handleShow();

            }

            @Override
            public void onAdClosed(final InterstitialAd ad) {
                //广告关闭

                LogUtil.simple(TAG+" onAdClosed ");

                handleClose();
            }

            @Override
            public void onAdClicked(final InterstitialAd ad) {
                //广告点击

                LogUtil.simple(TAG+" onAdClicked ");

                handleClick();
            }

            @Override
            public void onVideoStart(final InterstitialAd ad) {
                //广告视频播放开始
                LogUtil.simple(TAG+" onVideoStart ");

            }

            @Override
            public void onVideoEnd(final InterstitialAd ad) {
                //广告视频播放结束
                LogUtil.simple(TAG+" onVideoEnd ");

            }
        });
    }

    @Override
    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        noahAD.show();
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
