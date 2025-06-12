package com.advance.supplier.oppo;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.utils.LogUtil;
import com.heytap.msp.mobad.api.ad.InterstitialAd;
import com.heytap.msp.mobad.api.listener.IInterstitialAdListener;

public class OppoInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    private final InterstitialSetting setting;
    InterstitialAd mInterstitialAd;

    public OppoInterstitialAdapter(Activity activity, InterstitialSetting setting) {
        super(activity, setting);
        this.setting = setting;
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        OppoUtil.initAD(this);
        startLoad();
    }

    private void startLoad() {
        try {
            /**
             * 构造 InterstitialAd.
             */
            mInterstitialAd = new InterstitialAd(getRealActivity(null), sdkSupplier.adspotid);
            /**
             * 设置插屏广告行为监听器.
             */
            mInterstitialAd.setAdListener(new IInterstitialAdListener() {
                @Override
                public void onAdReady() {
                    LogUtil.simple(TAG + "onAdReady ");

                    updateBidding(mInterstitialAd.getECPM());

                    handleSucceed();
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + " onAdClose");

                    if (setting != null)
                        setting.adapterDidClosed();
                }

                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + " onAdShow");

                    handleShow();
                }

                @Override
                public void onAdFailed(String s) {
                    //                已废弃，

                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);

                }

                @Override
                public void onAdClick() {
                    LogUtil.simple(TAG + "onAdClick  ，   ");

                    handleClick();
                }

            });
            /**
             * 调用 loadAd() 方法请求广告.
             */
            mInterstitialAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adReady() {

    }


    @Override
    public void doDestroy() {
        try {
            if (mInterstitialAd != null)
                mInterstitialAd.destroyAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void show() {
        try {
            if (mInterstitialAd != null)
                mInterstitialAd.showAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
