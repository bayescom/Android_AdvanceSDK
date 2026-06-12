package com.advance.supplier.custom;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class CustomADNYLHInterstitialAdapter extends AdvanceInterstitialCustomAdapter implements UnifiedInterstitialADListener {
    private UnifiedInterstitialAD interstitialAD;

    String TAG = "[CustomADNYLHInterstitialAdapter] ";

    @Override
    public void destroyAd() {
        if (null != interstitialAD) {
            interstitialAD.destroy();
        }

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            interstitialAD.show();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    @Override
    public void onADReceive() {
        try {
            LogUtil.simple(TAG + "onADReceive");
            double ecpm = 0;

            if (interstitialAD != null) {
                ecpm = (interstitialAD.getECPM());
            }
            handleSucceed(ecpm);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onVideoCached() {
        LogUtil.simple(TAG + "onVideoCached");

    }

    @Override
    public void onNoAD(AdError adError) {
        try {
            int code = -1;
            String msg = "default onNoAD";
            if (adError != null) {
                code = adError.getErrorCode();
                msg = adError.getErrorMsg();
            }
            LogUtil.e(TAG + "onNoAD " + code + msg);
            AdvanceError error = AdvanceError.parseErr(code, msg);
            runParaFailed(error);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onADOpened() {
        LogUtil.simple(TAG + "onADOpened");

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
    public void onADLeftApplication() {
        LogUtil.simple(TAG + "onADLeftApplication");

    }

    @Override
    public void onADClosed() {
        LogUtil.simple(TAG + "onADClosed");

        handleClose();
    }

    @Override
    public void onRenderSuccess() {
        LogUtil.simple(TAG + "onRenderSuccess");
    }

    @Override
    public void onRenderFail() {
        LogUtil.simple(TAG + "onRenderFail");
        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED));
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        interstitialAD = new UnifiedInterstitialAD(activity, sdkSupplier.adspotid, this);
        interstitialAD.loadAD();
    }

    @Override
    protected void adPrepared() {
        if (null != interstitialSetting) {
            // onADReceive之后才能调用getAdPatternType()
            if (interstitialAD != null && interstitialAD.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                interstitialAD.setMediaListener(interstitialSetting.getGdtMediaListener());
            }
        }
    }

    @Override
    public boolean isValid() {
        if (interstitialAD != null) {
            return interstitialAD.isValid();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);
        

    }
}
