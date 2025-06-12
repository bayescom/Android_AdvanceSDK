package com.advance.supplier.baidu;

import android.app.Activity;

import com.advance.FullScreenVideoSetting;
import com.advance.custom.AdvanceFullScreenCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.FullScreenVideoAd;

public class BDFullScreenVideoAdapter extends AdvanceFullScreenCustomAdapter implements FullScreenVideoAd.FullScreenVideoAdListener {
    private FullScreenVideoSetting advanceFullScreenVideo;
    private String TAG = "[BDFullScreenVideoAdapter] ";

    private FullScreenVideoAd mFullScreenVideoAd;

    public BDFullScreenVideoAdapter(Activity activity, FullScreenVideoSetting advanceFullScreenVideo) {
        super(activity, advanceFullScreenVideo);
        this.advanceFullScreenVideo = advanceFullScreenVideo;
    }

    @Override
    protected void paraLoadAd() {
        BDUtil.initBDAccount(this);

        // 全屏视频产品可以选择是否使用SurfaceView进行渲染视频
        mFullScreenVideoAd = new FullScreenVideoAd(activity, sdkSupplier.adspotid
                , this, AdvanceBDManager.getInstance().fullScreenUseSurfaceView);
        //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
        int bidFloor = AdvanceBDManager.getInstance().fullScreenBidFloor;
        if (bidFloor > 0) {
            mFullScreenVideoAd.setBidFloor(bidFloor);
        }
        mFullScreenVideoAd.load();

        fullScreenItem = new BDFullScreenVideoItem(activity, this, mFullScreenVideoAd);
    }

    @Override
    protected void adReady() {
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }


    /**
     * 广告事件回调
     */

    @Override
    public void onAdShow() {
        LogUtil.simple(TAG + "onAdShow");

        handleShow();
    }

    @Override
    public void onAdClick() {
        LogUtil.simple(TAG + "onAdClick");

        handleShow();
    }

    @Override
    public void onAdClose(float playScale) {
        // 用户关闭了广告
        // 说明：关闭按钮在mssp上可以动态配置，媒体通过mssp配置，可以选择广告一开始就展示关闭按钮，还是播放结束展示关闭按钮
        // 建议：收到该回调之后，可以重新load下一条广告,最好限制load次数（4-5次即可）
        // playScale[0.0-1.0],1.0表示播放完成，媒体可以按照自己的设计给予奖励
        LogUtil.simple(TAG + "onAdClose" + playScale);


        if (advanceFullScreenVideo != null)
            advanceFullScreenVideo.adapterClose();
    }

    @Override
    public void onAdFailed(String s) {
        String msg = "onAdFailed" + s;

        handleFailed(AdvanceError.ERROR_BD_FAILED, msg);
    }

    @Override
    public void onVideoDownloadSuccess() {
        LogUtil.simple(TAG + "onVideoDownloadSuccess");
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
    public void onVideoDownloadFailed() {
        handleFailed(AdvanceError.ERROR_BD_FAILED, "onVideoDownloadFailed");
    }

    @Override
    public void playCompletion() {
        LogUtil.simple(TAG + "playCompletion");

        if (advanceFullScreenVideo != null)
            advanceFullScreenVideo.adapterVideoComplete();
    }

    @Override
    public void onAdSkip(float playScale) {
        // 用户跳过了广告
        // playScale[0.0-1.0],1.0表示播放完成，媒体可以按照自己的设计给予奖励
        LogUtil.simple(TAG + "onAdSkip" + playScale);
        if (advanceFullScreenVideo != null)
            advanceFullScreenVideo.adapterVideoSkipped();
    }

    @Override
    public void onAdLoaded() {
        LogUtil.simple(TAG + "onAdLoaded");
        try { //避免方法有异常，catch一下，不影响success逻辑
            if (mFullScreenVideoAd != null) {
                updateBidding(BDUtil.getEcpmValue(mFullScreenVideoAd.getECPMLevel()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        handleSucceed();
    }

//    @Override
//    public boolean isValid() {
//        if (mFullScreenVideoAd != null) {
//            return mFullScreenVideoAd.isReady();
//        }
//        return super.isValid();
//    }

    @Override
    public void show() {
        try {
            boolean isReady = mFullScreenVideoAd != null && mFullScreenVideoAd.isReady();
            LogUtil.simple(TAG + " isReady = " + isReady);
            mFullScreenVideoAd.show();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
