package com.advance.supplier.tap;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapInterstitialAd;

public class TapInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    TapAdNative tapAdNative;
    TapInterstitialAd adData;

    private final InterstitialSetting setting;


    public TapInterstitialAdapter(Activity activity, InterstitialSetting setting) {
        super(activity, setting);
        this.setting = setting;
    }

    @Override
    public void orderLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });

    }

    @Override
    protected void paraLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });

    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (adData != null) {
                adData.dispose();
            }
            TapUtil.removeTapMap(getRealContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            if (adData != null) {
                adData.show(getRealActivity(null));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW, ""));
        }

    }


    private void loadAD() {
        try {
//            tapAdNative = TapAdManager.get().createAdNative(getRealContext());
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

                        updateBidding(TapUtil.getBiddingPrice(adData.getMediaExtraInfo()));

                        handleSucceed();

                        adData.setInteractionListener(new TapInterstitialAd.InterstitialAdInteractionListener() {

                            @Override
                            public void onAdShow() {
                                LogUtil.simple(TAG + " onAdShow");

                                handleShow();
                            }

                            @Override
                            public void onAdClose() {
                                LogUtil.simple(TAG + " onAdClose");
                                if (setting != null) {
                                    setting.adapterDidClosed();
                                }
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
