package com.advance.supplier.custom;

import android.app.Activity;
import android.content.Context;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class CustomADNYLHSplashAdapter extends AdvanceSplashCustomAdapter {

    private long remainTime = 5000;
    private boolean isClicked = false;
    protected SplashAD splashAD;
    private final String TAG = "[CustomADNYLHSplashAdapter:" + this + "] ";


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        LogUtil.devDebug(TAG + " show");
        try {
            if (splashAD == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, "splashAd null"));
                return;
            }
            if (isParallel) {
                splashAD.showAd(getAdContainer());
            }

            TextView skipView = splashSetting.getSkipView();
            if (null != skipView) {
                skipView.setVisibility(View.INVISIBLE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    @Override
    public void destroyAd() {

    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        int timeout = sdkSupplier.timeout <= 0 ? 5000 : sdkSupplier.timeout;


        SplashADListener listener = new SplashADListener() {

            @Override
            public void onADDismissed() {
                LogUtil.simple(TAG + "onADDismissed ");

                //剩余时长在600ms以上，且未点击才按照跳过
                if (remainTime >= 600 && !isClicked) {
                    handleSkip();
                } else {
                    handleTimeOver();
                }
            }

            @Override
            public void onNoAD(AdError adError) {
                int code = -1;
                String msg = "default onNoAD";
                if (adError != null) {
                    code = adError.getErrorCode();
                    msg = adError.getErrorMsg();
                }
                LogUtil.simple(TAG + "onNoAD");

                handleFailed(code, msg);
                preLoad();
            }

            @Override
            public void onADPresent() {
                LogUtil.simple(TAG + "onADPresent ");

            }

            @Override
            public void onADClicked() {
                LogUtil.simple(TAG + "onADClicked ");

                handleClick();
                isClicked = true;
            }

            @Override
            public void onADTick(long l) {
                LogUtil.simple(TAG + "onADTick :" + l);
                remainTime = l;
            }

            @Override
            public void onADExposure() {
                LogUtil.simple(TAG + "onADExposure ");
                handleShow();

                preLoad();
            }

            @Override
            public void onADLoaded(long expireTimestamp) {
                try {
                    LogUtil.simple(TAG + "onADLoaded expireTimestamp:" + expireTimestamp);
                    double ecpm = 0;

                    if (splashAD != null) {
                        LogUtil.devDebug(TAG + "getECPMLevel = " + splashAD.getECPMLevel() + ", getECPM = " + splashAD.getECPM());
                        ecpm = (splashAD.getECPM());
                    }

                    handleSucceed(ecpm);

                    long rt = SystemClock.elapsedRealtime();
                    long expire = expireTimestamp - rt;
                    LogUtil.high(TAG + "ad will expired in :" + expire + " ms");

                } catch (Throwable e) {
                    e.printStackTrace();
                    runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                }

            }
        };
        splashAD = new SplashAD(getRealContext(), sdkSupplier.adspotid, listener, timeout);

        LogUtil.simple(TAG + "fetchAdOnly ");

        if (splashAD != null) {
            splashAD.fetchAdOnly();
        }
    }


    //当广告曝光、广告失败的时候再执行广告预加载，避免影响当前展示
    private void preLoad() {
        try {
            //预加载素材，会有频次限制，目前是交给广点通自己来控制，不做额外频次控制
            if (splashAD != null)
                splashAD.preLoad();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        if (splashAD != null) {
            return splashAD.isValid();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " + winPrice + ", referBidInfo = " + referBidInfo);


    }
}
