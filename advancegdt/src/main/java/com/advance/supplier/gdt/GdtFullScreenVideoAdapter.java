package com.advance.supplier.gdt;

import android.app.Activity;

import com.advance.FullScreenVideoSetting;
import com.advance.custom.AdvanceFullScreenCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.util.AdError;

public class GdtFullScreenVideoAdapter extends AdvanceFullScreenCustomAdapter implements UnifiedInterstitialADListener {
    private FullScreenVideoSetting advanceFullScreenVideo;

    private UnifiedInterstitialAD iad;
    private long videoDuration;
    private long videoStartTime;
    String TAG = "[GdtFullScreenVideoAdapter] ";

    public GdtFullScreenVideoAdapter(Activity activity, FullScreenVideoSetting advanceFullScreenVideo) {
        super(activity, advanceFullScreenVideo);
        this.advanceFullScreenVideo = advanceFullScreenVideo;
    }

    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));

        }
    }

    @Override
    public void onADReceive() {
        try {
            LogUtil.simple(TAG + "onADReceive");
            if (iad != null) {
                updateBidding(iad.getECPM());
            }
            handleSucceed();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    //虽然有此回调，但是返回该事件的时机不固定。。。
    @Override
    public void onVideoCached() {
        LogUtil.simple(TAG + "onVideoCached");


        if (isParallel) {
            if (parallelListener != null) {
                parallelListener.onCached();
            }
        } else {
            if (null != advanceFullScreenVideo) {
                advanceFullScreenVideo.adapterVideoCached();
            }
        }
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

        if (advanceFullScreenVideo != null) {
            long costTime = System.currentTimeMillis() - videoStartTime;
            LogUtil.high(TAG + "costTime ==   " + costTime + " videoDuration == " + videoDuration);

            if (costTime < videoDuration) {
                LogUtil.high(TAG + " adapterVideoSkipped");
                advanceFullScreenVideo.adapterVideoSkipped();
            }
            LogUtil.high(TAG + " adapterClose");
            advanceFullScreenVideo.adapterClose();
        }
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


    @Override
    protected void paraLoadAd() {
        GdtUtil.initAD(this);

        iad = new UnifiedInterstitialAD(activity, sdkSupplier.adspotid, this);
        //用来获取视频时长
        iad.setMediaListener(new UnifiedInterstitialMediaListener() {
            @Override
            public void onVideoInit() {
                LogUtil.high(TAG + " onVideoInit");

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoInit();
            }

            @Override
            public void onVideoLoading() {
                LogUtil.high(TAG + " onVideoLoading");

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoLoading();
            }

            @Override
            public void onVideoReady(long l) {
                LogUtil.high(TAG + " onVideoReady, videoDuration = " + l);
                try {
                    if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                        advanceFullScreenVideo.getGdtMediaListener().onVideoReady(l);
                    videoStartTime = System.currentTimeMillis();
                    videoDuration = l;
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVideoStart() {
                LogUtil.high(TAG + " onVideoStart");

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoStart();
            }

            @Override
            public void onVideoPause() {
                LogUtil.high(TAG + " onVideoPause");

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoPause();
            }

            @Override
            public void onVideoComplete() {
                LogUtil.high(TAG + " onVideoComplete");

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoComplete();

                if (null != advanceFullScreenVideo) {
                    advanceFullScreenVideo.adapterVideoComplete();
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

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoError(adError);

                handleFailed(AdvanceError.ERROR_RENDER_FAILED, msgInf);
            }

            @Override
            public void onVideoPageOpen() {
                LogUtil.high(TAG + "onVideoPageOpen ");

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoPageOpen();
            }

            @Override
            public void onVideoPageClose() {
                LogUtil.high(TAG + " onVideoPageClose");

                if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtMediaListener() != null)
                    advanceFullScreenVideo.getGdtMediaListener().onVideoPageClose();
            }
        });
        //    private UnifiedInterstitialMediaListener mediaListener;
        VideoOption videoOption;
        if (advanceFullScreenVideo != null && advanceFullScreenVideo.getGdtVideoOption() != null) {
            videoOption = advanceFullScreenVideo.getGdtVideoOption();
        } else {
            videoOption = new VideoOption.Builder().setAutoPlayMuted(false)
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS)
                    .build();
        }

        iad.setMinVideoDuration(0);
        iad.setMaxVideoDuration(60);
        iad.setVideoOption(videoOption);
        iad.loadFullScreenAD();
        fullScreenItem = new GdtFullScreenVideoItem(activity, this, iad);
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {

    }

    @Override
    public void show() {

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
        return super.isValid();
    }
}
