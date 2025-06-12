package com.advance.supplier.gdt;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

public class GdtInterstitialAdapter extends AdvanceInterstitialCustomAdapter implements UnifiedInterstitialADListener {
    private InterstitialSetting advanceInterstitial;
    private UnifiedInterstitialAD interstitialAD;

    String TAG = "[GdtInterstitialAdapter] ";

    public GdtInterstitialAdapter(Activity activity, InterstitialSetting advanceInterstitial) {
        super(activity, advanceInterstitial);
        this.advanceInterstitial = advanceInterstitial;
    }

    @Override
    public void doDestroy() {
        if (null != interstitialAD) {
            interstitialAD.destroy();
        }

    }

    @Override
    public void show() {
        try {
            interstitialAD.show();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable t) {
            t.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }

    }

    @Override
    public void onADReceive() {
        try {
            LogUtil.simple(TAG + "onADReceive");
            if (interstitialAD != null) {
                updateBidding(interstitialAD.getECPM());
            }
            handleSucceed();
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

        if (null != advanceInterstitial) {
            advanceInterstitial.adapterDidClosed();
        }
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

    @Override
    protected void paraLoadAd() {
        GdtUtil.initAD(this);

        interstitialAD = new UnifiedInterstitialAD(activity, sdkSupplier.adspotid, this);
        interstitialAD.loadAD();
    }

    @Override
    protected void adReady() {
        if (null != advanceInterstitial) {
            // onADReceive之后才能调用getAdPatternType()
            if (interstitialAD != null && interstitialAD.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
                interstitialAD.setMediaListener(advanceInterstitial.getGdtMediaListener());
            }
        }
    }

    @Override
    public boolean isValid() {
        if (interstitialAD != null) {
            return interstitialAD.isValid();
        }
        return super.isValid();
    }
}
