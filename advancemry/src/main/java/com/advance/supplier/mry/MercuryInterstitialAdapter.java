package com.advance.supplier.mry;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.mercury.sdk.core.interstitial.InterstitialAD;
import com.mercury.sdk.core.interstitial.InterstitialADListener;
import com.mercury.sdk.util.ADError;

import java.util.Map;

public class MercuryInterstitialAdapter extends AdvanceInterstitialCustomAdapter implements InterstitialADListener {
    private InterstitialAD interstitialAD;
    String TAG = "[MercuryInterstitialAdapter] ";


    public void doDestroy() {
        if (null != interstitialAD) {
            interstitialAD.destroy();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            interstitialAD.show(getRealActivity(null));
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    @Override
    public void onADReceive() {
        try {
            LogUtil.simple(TAG + "onADReceive");

            //旧版本SDK中不包含价格返回方法，catch住
            int cpm = 0;
            try {
                cpm = interstitialAD.getEcpm();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            handleSucceed(cpm);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }

    }

    @Override
    public void onADOpened() {
        LogUtil.simple(TAG + "onADOpened");


    }

    @Override
    public void onADClosed() {
        LogUtil.simple(TAG + "onADClosed");

        handleClose();

    }

    @Override
    public void onADLeftApplication() {
        LogUtil.simple(TAG + "onADLeftApplication");

    }

    @Override
    public void onADExposure() {
        LogUtil.simple(TAG + "onADExposure");

        handleShow();
    }

    @Override
    public void onADClicked() {
        LogUtil.simple(TAG + "onADClicked");

        handleClick();
    }

    @Override
    public void onNoAD(ADError adError) {
        int code = -1;
        String msg = "default onNoAD";
        if (adError != null) {
            code = adError.code;
            msg = adError.msg;
        }
        LogUtil.e(code + msg);
        AdvanceError error = AdvanceError.parseErr(code, msg);
        if (isParallel) {
            if (parallelListener != null) {
                parallelListener.onFailed(error);
            }
        } else {
            doBannerFailed(error);
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        interstitialAD = new InterstitialAD(context, sdkSupplier.adspotid);
        interstitialAD.setAdListener(this);
        interstitialAD.loadAD();
    }

    @Override
    protected void adPrepared() {

    }


    @Override
    public boolean isValid() {
        if (interstitialAD != null) {
            return interstitialAD.isValid();
        }
        return true;
    }

    @Override
    public void destroyAd() {
        if (interstitialAD != null) {
            interstitialAD.destroy();
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }
}
