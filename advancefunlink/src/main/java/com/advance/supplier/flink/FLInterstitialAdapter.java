package com.advance.supplier.flink;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.fl.saas.adx.api.FLInterstitial;
import com.fl.saas.adx.base.exception.FLError;
import com.fl.saas.adx.base.interfaces.AdViewInterstitialListener;

public class FLInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    FLInterstitial flAd;

    public FLInterstitialAdapter(Activity activity, InterstitialSetting setting) {
        super(activity, setting);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        FLUtil.initAD(this);
        loadAd();
        reportStart();
    }

    private void loadAd() {

        //检查是否命中使用缓存逻辑
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, FLInterstitialAdapter.class, new BYAbsCallBack<FLInterstitialAdapter>() {
            @Override
            public void invoke(FLInterstitialAdapter cacheAdapter) {
                //更新缓存广告得价格
                updateBidding(cacheAdapter.flAd.getEcpm());
            }
        });
        if (hitCache) {
            return;
        }
        flAd = new FLInterstitial.Builder(getRealContext())
                .setKey(getPosID())
                .setInterstitialListener(new AdViewInterstitialListener() {
                    @Override
                    public void onAdReady() {
                        LogUtil.simple(TAG + "onAdReady  ");
//请求成功时回调。可以在此处调用展示
                        updateBidding(flAd.getEcpm());

                        handleSucceed(FLInterstitialAdapter.this);
                    }

                    @Override
                    public void onAdDisplay() {
                        LogUtil.simple(TAG + "onAdDisplay  ");
//广告展示时回调
                        handleShow();
                    }

                    @Override
                    public void onAdFailed(FLError error) {
// 广告异常、失败，回调该方法
                        LogUtil.simple(TAG + "onAdFailed , error = " + error);

                        FLUtil.handleErr(FLInterstitialAdapter.this, error);
                    }

                    @Override
                    public void onAdClick(String s) {
// 广告被点击时回调该方法
                        LogUtil.simple(TAG + "onAdClick ,s = " + s);

                        handleClick();
                    }

                    @Override
                    public void onAdClosed() {
// 广告关闭时回调该方法
                        LogUtil.simple(TAG + "onAdClosed  ");

                        handleClose();
                    }
                })
                .build();
        flAd.requestInterstitial();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (flAd != null) {
            flAd.destroy();
        }
    }


    @Override
    public void show() {
        if (flAd != null) {
            flAd.show(getRealActivity(null));
        }
    }

    @Override
    public boolean isValid() {
        if (flAd != null) {
            return flAd.isReady();
        }
        return super.isValid();
    }
}
