package com.advance.supplier.mi;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.SplashAd;

import java.util.Map;

public class XMSplashAdapter extends AdvanceSplashCustomAdapter {
    SplashAd splashAd;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);
        

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (splashAd != null)
            splashAd.destroy();
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            splashAd.showAd(getAdContainer(), new SplashAd.SplashAdInteractionListener() {

                @Override
                public void onAdShow() {
                    // 广告展示
                    LogUtil.d(TAG + "onAdShow");
                    handleShow();
                }

                @Override
                public void onAdClick() {
                    // 广告被点击
                    LogUtil.d(TAG + "onAdClick");
                    handleClick();
                }

                @Override
                public void onAdDismissed() {
                    // 点击关闭按钮广告消失回调

                    LogUtil.d(TAG + "onAdDismissed");
                    if (isCountingEnd) {
                        handleTimeOver();
                    } else {
                        handleSkip();

                    }
                }

                @Override
                public void onAdRenderFailed(int errorCode, String errorMsg) {
                    //广告渲染失败
//                container.setVisibility(View.GONE)
                    LogUtil.d(TAG + "onAdRenderFailed");
                    handleFailed(errorCode, errorMsg);

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        splashAd = new SplashAd();
        ADParams params = new ADParams.Builder().setUpId(sdkSupplier.adspotid).build();
        splashAd.loadAd(params, new SplashAd.SplashAdLoadListener() {

            @Override
            public void onAdRequestSuccess() {
                // 广告请求成功
                LogUtil.d(TAG + "onAdRequestSuccess");

                handleSucceed(splashAd == null ? 0 : XMUtil.getPrice(splashAd.getMediaExtraInfo()));
            }

            @Override
            public void onAdLoaded() {
                LogUtil.d(TAG + "onAdLoaded");
                // 广告加载成功，在需要的时候在此处展示广告
            }

            @Override
            public void onAdLoadFailed(int errorCode, String errorMsg) {
                // 广告加载失败
                LogUtil.d(TAG + "onAdLoadFailed");
                handleFailed(errorCode, errorMsg);
            }
        });
    }
}
