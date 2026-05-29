package com.advance.supplier.vv;

import android.app.Activity;
import android.content.Context;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.callback.MediaListener;
import com.vivo.mobilead.unified.interstitial.UnifiedVivoInterstitialAd;
import com.vivo.mobilead.unified.interstitial.UnifiedVivoInterstitialAdListener;

import java.util.Map;

public class VivoInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    UnifiedVivoInterstitialAd vivoInterstitialAd;

    boolean loadVideo = false;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
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
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
//        if (vivoInterstitialAd!=null){
//            vivoInterstitialAd.
//        }
    }

    

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (vivoInterstitialAd == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "vivoInterstitialAd null"));
                return;
            }
            if (loadVideo) {
                vivoInterstitialAd.showVideoAd(activity);
            } else {
                vivoInterstitialAd.showAd();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        
        //检查是否命中使用缓存逻辑
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, VivoInterstitialAdapter.class, new BYAbsCallBack<VivoInterstitialAdapter>() {
            @Override
            public void invoke(VivoInterstitialAdapter cacheAdapter) {

                //更新缓存广告得价格
                updateBidding(VivoUtil.getPrice(cacheAdapter.vivoInterstitialAd));
            }
        });
        if (hitCache) {
            return;
        }
        
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
                handleSucceed(VivoInterstitialAdapter.this);
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
