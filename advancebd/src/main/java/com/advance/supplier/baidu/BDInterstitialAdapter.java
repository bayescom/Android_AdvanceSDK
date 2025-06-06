package com.advance.supplier.baidu;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;
import static com.advance.model.AdvanceError.ERROR_EXCEPTION_SHOW;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.ExpressInterstitialAd;
import com.baidu.mobads.sdk.api.ExpressInterstitialListener;

public class BDInterstitialAdapter extends AdvanceInterstitialCustomAdapter implements ExpressInterstitialListener {
    private final InterstitialSetting setting;
    private ExpressInterstitialAd mInterAd;            // 插屏广告实例
    private String TAG = "[BDInterstitialAdapter] ";

    public BDInterstitialAdapter(Activity activity, InterstitialSetting baseSetting) {
        super(activity, baseSetting);
        this.setting = baseSetting;
    }

    @Override
    protected void paraLoadAd() {
        if (sdkSupplier != null) {
            BDUtil.initBDAccount(this);

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
    protected void adReady() {
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

    @Override
    public void show() {
        try {
            mInterAd.show();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public void doDestroy() {
        if (mInterAd != null) {
            mInterAd.destroy();
        }
    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable t) {
            t.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
        }

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
        handleSucceed();
    }

    @Override
    public void onAdClick() {
        LogUtil.simple(TAG + "onAdClick");
        handleClick();
    }

    @Override
    public void onAdClose() {
        LogUtil.simple(TAG + "onAdClose");
        if (null != setting) {
            setting.adapterDidClosed();
        }
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
