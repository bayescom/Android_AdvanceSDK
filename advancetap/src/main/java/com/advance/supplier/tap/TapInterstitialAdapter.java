package com.advance.supplier.tap;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapInterstitialAd;

import java.util.Map;

public class TapInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    TapAdNative tapAdNative;
    TapInterstitialAd adData;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        try {
            if (adData != null) {
                adData.dispose();
            }
            TapUtil.removeTapMap(getRealContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (adData != null) {
                adData.setInteractionListener(new TapInterstitialAd.InterstitialAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        LogUtil.simple(TAG + " onAdShow");

                        handleShow();
                    }

                    @Override
                    public void onAdClose() {
                        LogUtil.simple(TAG + " onAdClose");

                        handleClose();
                    }

                    @Override
                    public void onAdError() {
                        LogUtil.simple(TAG + " onAdError");

                        handleFailed(AdvanceError.ERROR_TAP_RENDER_ERR, "InterstitialAdInteractionListener onAdError");
                    }

                    @Override
                    public void onAdValidShow() {
                        LogUtil.simple(TAG + " onAdValidShow");

                    }

                    @Override
                    public void onAdClick() {
                        LogUtil.simple(TAG + " onAdClick");


                        handleClick();
                    }
                });
                adData.show(getRealActivity(null));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW, ""));
        }

    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            tapAdNative = TapUtil.getTapADManger(getRealContext());

            int spaceId = TapUtil.getPlaceId(getPosID());
            AdRequest request = new AdRequest.Builder().withSpaceId(spaceId)
                    .withUserId(AdvanceTapManger.getInstance().customTapUserId)
                    .build();

            tapAdNative.loadInterstitialAd(request, new TapAdNative.InterstitialAdListener() {
                @Override
                public void onInterstitialAdLoad(TapInterstitialAd tapInterstitialAd) {
                    try {
                        if (tapInterstitialAd == null) {
                            String nMsg = TAG + " tapInterstitialAd null";
                            handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                            return;
                        }
                        adData = tapInterstitialAd;
                        handleSucceed(TapUtil.getBiddingPrice(adData.getMediaExtraInfo()));

                    } catch (Throwable e) {
                        e.printStackTrace();
                        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                    }
                }

                @Override
                public void onError(int code, String message) {
                    LogUtil.e(TAG + " onError ");

                    handleFailed(code, message);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD, "out"));
        }
    }

}
