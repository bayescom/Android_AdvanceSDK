package com.advance.supplier.oppo;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;


import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.heytap.msp.mobad.api.ad.HotSplashAd;
import com.heytap.msp.mobad.api.listener.IHotSplashListener;
import com.heytap.msp.mobad.api.params.SplashAdParams;

import java.util.Map;

public class OppoSplashAdapter extends AdvanceSplashCustomAdapter {
    private final String TAG = "[OppoSplashAdapter] ";
    private HotSplashAd splashAd;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        try {
            if (splashAd != null) {
                splashAd.destroyAd();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (splashAd == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "splashAd null"));
                return;
            }

            splashAd.showAd(activity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {

            //可以自定义跳过按钮样式结束
            SplashAdParams.Builder builder = new SplashAdParams.Builder()
                    .setFetchTimeout(sdkSupplier.timeout);

            //如果请求时未赋值，将无法展示自定义跳过view
//            if (splashSetting.getSkipView() != null) {
//                builder.setSplashSkipView(splashSetting.getSkipView());
//            }

            splashAd = new HotSplashAd(getRealContext(), sdkSupplier.adspotid, "", new IHotSplashListener() {
                @Override
                public void onAdReady() {
                    LogUtil.simple(TAG + " onAdReady ");

                    int ecpm = splashAd == null ? 0 : splashAd.getECPM();
                    handleSucceed(ecpm);
                }

                @Override
                public void onAdDismissed() {
                    LogUtil.simple(TAG + " onAdDismissed ");

                    if (isCountingEnd) {
                        handleTimeOver();
                    } else {
                        handleSkip();
                    }
                }

                @Override
                public void onAdShow(String transportData) {//请求时透传的信息
                    LogUtil.simple(TAG + " onAdShow ");


                    handleShow();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isCountingEnd = true;
                        }
                    }, 4800);
                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);

                }

                @Override
                public void onAdClick() {
                    LogUtil.simple(TAG + " onAdClick ");
                    handleClick();


                }
            }, builder.build());
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }


}
