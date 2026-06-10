package com.advance.supplier.gdt;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceFullScreenCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class GdtFullScreenVideoAdapter extends AdvanceFullScreenCustomAdapter implements UnifiedInterstitialADListener {

    private UnifiedInterstitialAD iad;
    private long videoDuration;
    private long videoStartTime;
    String TAG = "[GdtFullScreenVideoAdapter] ";


    @Override
    public void onADReceive() {
        try {
            LogUtil.simple(TAG + "onADReceive");
            double ecpm = 0;

            if (iad != null) {
                ecpm = (iad.getECPM());
            }
            handleSucceed(ecpm);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    //虽然有此回调，但是返回该事件的时机不固定。。。
    @Override
    public void onVideoCached() {
        LogUtil.simple(TAG + "onVideoCached");


        handleCached();
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
            LogUtil.simple(TAG + " onNoAD");
            handleFailed(code, msg);
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

        long costTime = System.currentTimeMillis() - videoStartTime;
        LogUtil.high(TAG + "costTime ==   " + costTime + " videoDuration == " + videoDuration);

        if (costTime < videoDuration) {
            LogUtil.high(TAG + " adapterVideoSkipped");
            handleSkip();
        }
        LogUtil.high(TAG + " adapterClose");
        handleClose();
    }

    @Override
    public void onRenderSuccess() {
        LogUtil.simple(TAG + "onRenderSuccess");

    }

    @Override
    public void onRenderFail() {
        LogUtil.simple(TAG + "onRenderFail");
        handleFailed(AdvanceError.ERROR_RENDER_FAILED, "");
    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        iad = new UnifiedInterstitialAD(activity, sdkSupplier.adspotid, this);
        //用来获取视频时长
        iad.setMediaListener(new UnifiedInterstitialMediaListener() {
            @Override
            public void onVideoInit() {
                LogUtil.high(TAG + " onVideoInit");

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoInit();
            }

            @Override
            public void onVideoLoading() {
                LogUtil.high(TAG + " onVideoLoading");

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoLoading();
            }

            @Override
            public void onVideoReady(long l) {
                LogUtil.high(TAG + " onVideoReady, videoDuration = " + l);
                try {
                    if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                        fullScreenVideoSetting.getGdtMediaListener().onVideoReady(l);
                    videoStartTime = System.currentTimeMillis();
                    videoDuration = l;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVideoStart() {
                LogUtil.high(TAG + " onVideoStart");

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoStart();
            }

            @Override
            public void onVideoPause() {
                LogUtil.high(TAG + " onVideoPause");

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoPause();
            }

            @Override
            public void onVideoComplete() {
                LogUtil.high(TAG + " onVideoComplete");

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoComplete();

                if (null != fullScreenVideoSetting) {
                    fullScreenVideoSetting.adapterVideoComplete();
                }

            }

            @Override
            public void onVideoError(AdError adError) {
                LogUtil.simple(TAG + " onVideoError ");
                String msgInf = "";
                if (adError != null) {
                    LogUtil.high(TAG + " ErrorCode: " + adError.getErrorCode() + ", ErrorMsg: " + adError.getErrorMsg());
                    msgInf = TAG + adError.getErrorCode() + "， " + adError.getErrorMsg();
                }

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoError(adError);

                handleFailed(AdvanceError.ERROR_RENDER_FAILED, msgInf);
            }

            @Override
            public void onVideoPageOpen() {
                LogUtil.high(TAG + "onVideoPageOpen ");

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoPageOpen();
            }

            @Override
            public void onVideoPageClose() {
                LogUtil.high(TAG + " onVideoPageClose");

                if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtMediaListener() != null)
                    fullScreenVideoSetting.getGdtMediaListener().onVideoPageClose();
            }
        });
        //    private UnifiedInterstitialMediaListener mediaListener;
        VideoOption videoOption;
        if (fullScreenVideoSetting != null && fullScreenVideoSetting.getGdtVideoOption() != null) {
            videoOption = fullScreenVideoSetting.getGdtVideoOption();
        } else {
            videoOption = new VideoOption.Builder().setAutoPlayMuted(false)
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS)
                    .build();
        }

        iad.setMinVideoDuration(0);
        iad.setMaxVideoDuration(60);
        iad.setVideoOption(videoOption);
        iad.loadFullScreenAD();
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        try {
            iad.showFullScreenAD(activity);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }

    }

    @Override
    public boolean isValid() {
        if (iad != null) {
            return iad.isValid();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {

    }
}
