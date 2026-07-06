package com.advance.supplier.noah;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.noah.api.AdError;
import com.noah.api.NativeAd;
import com.noah.api.RequestInfo;

import java.util.List;
import java.util.Map;

public class NoahNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    NativeAd noahAD;

    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        RequestInfo requestInfo = new RequestInfo();
        NativeAd.getAd(context, getPosID(), requestInfo, new NativeAd.AdListener() {
            @Override
            public void onAdError(final AdError adError) {
                LogUtil.simple(TAG + " onAdError , adError = " + adError);
                //广告返回失败

                NoahUtil.loadErr(NoahNativeExpressAdapter.this, adError);
            }

            @Override
            public void onAdLoaded(final List<NativeAd> ads) {

                //广告请求成功，展示广告
                LogUtil.simple(TAG + " onAdLoaded , ad = " + ads);
                //广告返回成功，展示广告
                if (ads == null || ads.isEmpty() || ads.get(0) == null) {
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "noahAD null");
                    return;
                }
                //广告返回成功，如果requestInfo中要求多条广告返回，会返回多条广告，默认返回1条
                NativeAd ad = ads.get(0);
                noahAD = ad;

                handleSucceed(ad.getPrice());
            }

            @Override
            public void onAdShown(final NativeAd ad) {
                //广告曝光
                LogUtil.simple(TAG + " onAdShown ");

                handleShow();
            }

            @Override
            public void onAdClosed(final NativeAd ad) {
                //广告关闭，只有使用模板渲染方式才会有这个回调
                LogUtil.simple(TAG + " onAdClosed ");

                handleClose();
            }

            @Override
            public void onAdClicked(final NativeAd ad) {
                //广告点击
                LogUtil.simple(TAG + " onAdClicked ");

                handleClick();
            }

            @Override
            public void onDownloadStatusChanged(final NativeAd ad, final int apkDownloadStatus) {
                //下载类广告下载进度回调
                LogUtil.simple("onDownloadStatusChanged, return status: " + apkDownloadStatus);
            }

            @Override
            public void onAdEvent(final NativeAd ad, final int eventId, final Object extInfo) {
                //其他广告事件回调
                LogUtil.simple("onAdEvent,   eventId: " + eventId + " extInfo = " + extInfo);

            }
        });
    }

    @Override
    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        //开发者可选择展示SDK模板广告，也可以选择自渲染广告，
        addADView(noahAD.getView(activity));
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
