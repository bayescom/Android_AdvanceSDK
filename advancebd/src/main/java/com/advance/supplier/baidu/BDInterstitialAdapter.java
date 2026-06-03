package com.advance.supplier.baidu;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;
import static com.advance.model.AdvanceError.ERROR_EXCEPTION_SHOW;

import android.app.Activity;
import android.content.Context;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.ExpressInterstitialAd;
import com.baidu.mobads.sdk.api.ExpressInterstitialListener;
import com.bayes.sdk.basic.itf.BYAbsCallBack;

import java.util.Map;

public class BDInterstitialAdapter extends AdvanceInterstitialCustomAdapter implements ExpressInterstitialListener {
    private ExpressInterstitialAd mInterAd;            // 插屏广告实例
    private String TAG = "[BDInterstitialAdapter] ";



    
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        BDUtil.initBDAccount(this);
//
//        //检查是否命中使用缓存逻辑
//        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, BDInterstitialAdapter.class, new BYAbsCallBack<BDInterstitialAdapter>() {
//            @Override
//            public void invoke(BDInterstitialAdapter cacheAdapter) {
//
//                //更新缓存广告得价格
//                updateBidding(BDUtil.getEcpmValue(cacheAdapter.mInterAd.getECPMLevel()));
//            }
//        });
//        if (hitCache) {
//            return;
//        }
        if (sdkSupplier != null) {

            String adPlaceId = sdkSupplier.adspotid;
            mInterAd = new ExpressInterstitialAd(activity, adPlaceId);
            mInterAd.setDialogFrame(AdvanceBDManager.getInstance().interstitialUseDialogFrame);
            mInterAd.setLoadListener(this);
            //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
            int bidFloor = AdvanceBDManager.getInstance().interstitialBidFloor;
            if (bidFloor > 0) {
                mInterAd.setBidFloor(bidFloor);
            }
        }

        mInterAd.load();
    }

    @Override
    protected void adPrepared() {
//        if (null != setting) {
//            setting.adapterDidSucceed(sdkSupplier);
//        }
    }
//
//
//    @Override
//    public boolean isValid() {
//        if (mInterAd != null) {
//            return mInterAd.isReady();
//        }
//        return super.isValid();
//    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            mInterAd.show();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public void destroyAd() {
        if (mInterAd != null) {
            mInterAd.destroy();
        }
    }


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }


    //广告的回调事件
    @Override
    public void onADLoaded() {
        LogUtil.simple(TAG + "onADLoaded");
        try { //避免方法有异常，catch一下，不影响success逻辑
            if (mInterAd != null) {
                updateBidding(BDUtil.getEcpmValue(mInterAd.getECPMLevel()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        handleSucceed(this);
    }

    @Override
    public void onAdClick() {
        LogUtil.simple(TAG + "onAdClick");
        handleClick();
    }

    @Override
    public void onAdClose() {
        LogUtil.simple(TAG + "onAdClose");

        handleClose();
    }

    @Override
    public void onAdFailed(int i, String s) {
        LogUtil.simple(TAG + "onAdFailed ，[" + i + "] " + s);
        handleFailed(i, s);
    }

    @Override
    public void onNoAd(int i, String s) {
        LogUtil.simple(TAG + "onNoAd ，[" + i + "] " + s);
        handleFailed(i, s);
    }

    @Override
    public void onADExposed() {
        LogUtil.simple(TAG + "onADExposed");
        handleShow();
    }

    @Override
    public void onADExposureFailed() {
        LogUtil.simple(TAG + "onADExposureFailed");

        handleFailed(AdvanceError.ERROR_BD_FAILED, "onADExposureFailed");
    }

    @Override
    public void onAdCacheSuccess() {
        LogUtil.simple(TAG + "onAdCacheSuccess");

    }

    @Override
    public void onAdCacheFailed() {
        LogUtil.simple(TAG + "onAdCacheFailed");

    }

    @Override
    public void onLpClosed() {
        LogUtil.simple(TAG + "onLpClosed");

    }

//    @Override
//    public void onVideoDownloadSuccess() {
//        LogUtil.simple(TAG + "onVideoDownloadSuccess");
//
//    }
//
//    @Override
//    public void onVideoDownloadFailed() {
//        LogUtil.simple(TAG + "onVideoDownloadFailed");
//
//    }
}
