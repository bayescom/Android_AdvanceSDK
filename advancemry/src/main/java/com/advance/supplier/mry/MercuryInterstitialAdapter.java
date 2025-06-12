package com.advance.supplier.mry;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.mercury.sdk.core.interstitial.InterstitialAD;
import com.mercury.sdk.core.interstitial.InterstitialADListener;
import com.mercury.sdk.util.ADError;

public class MercuryInterstitialAdapter extends AdvanceInterstitialCustomAdapter implements InterstitialADListener {
    private InterstitialSetting advanceInterstitial;
    private InterstitialAD interstitialAD;
    String TAG = "[MercuryInterstitialAdapter] ";

    public MercuryInterstitialAdapter(Activity activity, InterstitialSetting advanceInterstitial) {
        super(activity, advanceInterstitial);
        this.advanceInterstitial = advanceInterstitial;
    }

    public void doDestroy() {
        if (null != interstitialAD) {
            interstitialAD.destroy();
        }
    }

    @Override
    public void show() {
        try {
            interstitialAD.show(getRealActivity(null));
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


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

            //旧版本SDK中不包含价格返回方法，catch住
            try {
                int cpm = interstitialAD.getEcpm();
                updateBidding(cpm);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            handleSucceed();
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

        if (null != advanceInterstitial) {
            advanceInterstitial.adapterDidClosed();
        }

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

    @Override
    protected void paraLoadAd() {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);
        boolean useNewAPI = true;
        if (sdkSupplier.versionTag == 1) {
            useNewAPI = false;
        }
        //根据配置选择使用新旧版本插屏广告，只有当versionTag 返回 1 才会执行旧版本插屏逻辑
        if (useNewAPI) {
            interstitialAD = new InterstitialAD(getRealActivity(null), sdkSupplier.adspotid);
        } else {
            interstitialAD = new InterstitialAD(getRealActivity(null), sdkSupplier.adspotid, this);
        }
        interstitialAD.setAdListener(this);
        interstitialAD.setVideoMute(true);
        interstitialAD.loadAD();
    }

    @Override
    protected void adReady() {

    }


    @Override
    public boolean isValid() {
        if (interstitialAD != null) {
            return interstitialAD.isValid();
        }
        return super.isValid();
    }
}
