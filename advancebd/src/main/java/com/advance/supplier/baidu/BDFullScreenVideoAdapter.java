package com.advance.supplier.baidu;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceFullScreenCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.FullScreenVideoAd;

import java.util.Map;

public class BDFullScreenVideoAdapter extends AdvanceFullScreenCustomAdapter implements FullScreenVideoAd.FullScreenVideoAdListener {
    private String TAG = "[BDFullScreenVideoAdapter] ";

    private FullScreenVideoAd mFullScreenVideoAd;


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        BDUtil.initBDAccount(this);
//
//        //检查是否命中使用缓存逻辑
//        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, BDFullScreenVideoAdapter.class, new BYAbsCallBack<BDFullScreenVideoAdapter>() {
//            @Override
//            public void invoke(BDFullScreenVideoAdapter cacheAdapter) {
//
//                //更新缓存广告得价格
//                updateBidding(BDUtil.getEcpmValue(cacheAdapter.mFullScreenVideoAd.getECPMLevel()));
//            }
//        });
//        if (hitCache) {
//            return;
//        }

        // 全屏视频产品可以选择是否使用SurfaceView进行渲染视频
        mFullScreenVideoAd = new FullScreenVideoAd(activity, sdkSupplier.adspotid
                , this, AdvanceBDManager.getInstance().fullScreenUseSurfaceView);
        //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
        int bidFloor = AdvanceBDManager.getInstance().fullScreenBidFloor;
        if (bidFloor > 0) {
            mFullScreenVideoAd.setBidFloor(bidFloor);
        }
        mFullScreenVideoAd.load();

    }

    @Override
    protected void adPrepared() {
    }

    @Override
    public void destroyAd() {

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


        handleClose();
    }

    @Override
    public void onAdFailed(String s) {
        String msg = "onAdFailed" + s;

        handleFailed(AdvanceError.ERROR_BD_FAILED, msg);
    }

    @Override
    public void onVideoDownloadSuccess() {
        LogUtil.simple(TAG + "onVideoDownloadSuccess");

        handleCached();
    }

    @Override
    public void onVideoDownloadFailed() {
        handleFailed(AdvanceError.ERROR_BD_FAILED, "onVideoDownloadFailed");
    }

    @Override
    public void playCompletion() {
        LogUtil.simple(TAG + "playCompletion");

        handleComplete();
    }

    @Override
    public void onAdSkip(float playScale) {
        // 用户跳过了广告
        // playScale[0.0-1.0],1.0表示播放完成，媒体可以按照自己的设计给予奖励
        LogUtil.simple(TAG + "onAdSkip" + playScale);

        handleSkip();
    }

    @Override
    public void onAdLoaded() {
        LogUtil.simple(TAG + "onAdLoaded");
        double ecpm = 0;
        try { //避免方法有异常，catch一下，不影响success逻辑
            if (mFullScreenVideoAd != null) {
                ecpm = (BDUtil.getEcpmValue(mFullScreenVideoAd.getECPMLevel()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        handleSucceed(ecpm);
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            boolean isReady = mFullScreenVideoAd != null && mFullScreenVideoAd.isReady();
            LogUtil.simple(TAG + " isReady = " + isReady);
            mFullScreenVideoAd.show();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        if (mFullScreenVideoAd!=null){
            return mFullScreenVideoAd.isReady();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }
}
