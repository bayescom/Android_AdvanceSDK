package com.advance.supplier.vv;

import android.app.Activity;
import android.view.View;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.callback.MediaListener;
import com.vivo.mobilead.unified.interstitial.UnifiedVivoInterstitialAd;
import com.vivo.mobilead.unified.interstitial.UnifiedVivoInterstitialAdListener;

public class VivoInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    UnifiedVivoInterstitialAd vivoInterstitialAd;

    boolean loadVideo = false;

    public VivoInterstitialAdapter(Activity activity, InterstitialSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        VivoUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                loadAd();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });

    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
//        if (vivoInterstitialAd!=null){
//            vivoInterstitialAd.
//        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            if (vivoInterstitialAd == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "vivoInterstitialAd null"));
                return;
            }
            if (loadVideo) {
                vivoInterstitialAd.showVideoAd(getRealActivity(null));
            } else {
                vivoInterstitialAd.showAd();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);
        if (builder != null) {
            adParams = builder.build();
        }

        vivoInterstitialAd = new UnifiedVivoInterstitialAd(getRealActivity(null), adParams, new UnifiedVivoInterstitialAdListener() {
            @Override
            public void onAdShow() {
                LogUtil.simple(TAG + "onAdShow...");

                handleShow();
            }

            @Override
            public void onAdFailed(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onAdFailed... , vivoAdError = " + vivoAdError);

                VivoUtil.handleErr(VivoInterstitialAdapter.this, vivoAdError, AdvanceError.ERROR_LOAD_SDK, "onAdFailed");
            }

            @Override
            public void onAdReady() {
                LogUtil.simple(TAG + "onAdReady...");

                updateBidding(VivoUtil.getPrice(vivoInterstitialAd));
                handleSucceed();
            }

            @Override
            public void onAdClick() {
                LogUtil.simple(TAG + "onAdClick...");

                handleClick();
            }

            @Override
            public void onAdClose() {
                LogUtil.simple(TAG + "onAdClose...");

                handleClose();
            }
        });
//插屏半屏视频类广告会收到这个回调
        vivoInterstitialAd.setMediaListener(new MediaListener() {
            @Override
            public void onVideoStart() {

            }

            @Override
            public void onVideoPause() {

            }

            @Override
            public void onVideoPlay() {

            }

            @Override
            public void onVideoError(VivoAdError vivoAdError) {

            }

            @Override
            public void onVideoCompletion() {

            }

            @Override
            public void onVideoCached() {

            }
        });

        loadVideo = sdkSupplier.versionTag == 2;
        if (loadVideo) {
            vivoInterstitialAd.loadVideoAd();
        } else {
            vivoInterstitialAd.loadAd();
        }
    }


}
