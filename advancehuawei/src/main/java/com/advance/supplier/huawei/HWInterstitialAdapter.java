package com.advance.supplier.huawei;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.InterstitialAd;
import com.huawei.hms.ads.VideoConfiguration;

import java.util.Locale;

public class HWInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    InterstitialAd interstitialAd;

    public HWInterstitialAdapter(Activity activity, InterstitialSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        loadAd();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
//            if (interstitialAd != null) {
//                interstitialAd.();
//            }
        } catch (Exception e) {
        }
    }

    @Override
    public void orderLoadAd() {
        loadAd();
    }

    @Override
    public void show() {
        try {
            // Display an interstitial ad.
            if (interstitialAd != null && interstitialAd.isLoaded()) {
                interstitialAd.show(getRealActivity(null));
            } else {
//                LogUtil.simple(TAG + "Ad did not load");
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "未获取到插屏广告"));
            }
        } catch (Throwable e) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
            e.printStackTrace();
        }
    }

    private void loadAd() {
        //先执行SDK初始化
        HWUtil.initAD(this);

        interstitialAd = new InterstitialAd(getRealContext());
        interstitialAd.setAdId(sdkSupplier.adspotid);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Called when an ad is loaded successfully.
                LogUtil.simple(TAG + "Ad loaded.");

                if (interstitialAd != null) {
                    updateBidding(HWUtil.getPrice(interstitialAd.getBiddingInfo()));
                }

                handleSucceed();
            }

            @Override
            public void onAdFailed(int errorCode) {
                // Called when an ad fails to be loaded.
                LogUtil.simple(TAG + String.format(Locale.ROOT, "Ad failed to load with error code %d.", errorCode));

                handleFailed(errorCode, " onAdFailed");
            }

            @Override
            public void onAdOpened() {
                // Called when an ad is opened.
                LogUtil.simple(TAG + String.format("Ad opened "));

                handleShow();
            }

            @Override
            public void onAdClicked() {
                // Called when a user taps an ad.
                LogUtil.simple(TAG + "Ad clicked");
                handleClick();
            }

            @Override
            public void onAdLeave() {
                // Called when a user has left the app.
                LogUtil.simple(TAG + "Ad Leave");
            }

            @Override
            public void onAdClosed() {
                // Called when an ad is closed.
                LogUtil.simple(TAG + "Ad closed");

                handleClose();
            }
        });

        AdParam.Builder adParam = AdvanceHWManager.getInstance().globalAdParamBuilder;
        if (adParam == null) {
            adParam = new AdParam.Builder();
        }
        VideoConfiguration.Builder configuration = AdvanceHWManager.getInstance().globalVideoConfigBuilder;
        if (configuration != null) {
            interstitialAd.setVideoConfiguration(configuration.build());
        }
        interstitialAd.loadAd(adParam.build());
    }
}
