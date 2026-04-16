package com.advance.supplier.flink;

import android.app.Activity;
import android.os.Handler;

import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.fl.saas.adx.api.FLSpread;
import com.fl.saas.adx.base.exception.FLError;
import com.fl.saas.adx.base.interfaces.AdViewSpreadListener;
import com.fl.saas.adx.base.interfaces.SpreadLoadListener;

import java.lang.ref.SoftReference;

public class FLSplashAdapter extends AdvanceSplashCustomAdapter {
    FLSpread flAd;
    SpreadLoadListener.SpreadAd adData;

    public FLSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting splashSetting) {
        super(softReferenceActivity, splashSetting);
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
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, FLSplashAdapter.class, new BYAbsCallBack<FLSplashAdapter>() {
            @Override
            public void invoke(FLSplashAdapter cacheAdapter) {
                //更新缓存广告得价格
                updateBidding(cacheAdapter.adData.getEcpm());
            }
        });
        if (hitCache) {
            return;
        }
        FLSpread.Builder flBuilder = new FLSpread.Builder(getRealContext());
        flBuilder.setKey(getPosID())
                .setSpreadLoadListener(new SpreadLoadListener() {
                    @Override
                    public void onADLoaded(final SpreadAd spreadAd) {
                        LogUtil.simple(TAG + "onADLoaded");

// 广告请求成功回调该方法
                        if (spreadAd == null) {
                            String nMsg = TAG + " spreadAd null";
                            handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                            return;
                        }
                        adData = spreadAd;
                        updateBidding(spreadAd.getEcpm());

                        handleSucceed(FLSplashAdapter.this);
                    }
                })
                .setSpreadListener(new AdViewSpreadListener() {
                    @Override
                    public void onAdDisplay() {
// 广告展示时回调该方法
                        try {
                            LogUtil.simple(TAG + "onAdDisplay");

                            handleShow();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isCountingEnd = true;
                                }
                            }, 4800);
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onAdFailed(FLError error) {
// 广告异常、失败，回调该方法
                        LogUtil.simple(TAG + "onAdFailed , error = " + error);

                        FLUtil.handleErr(FLSplashAdapter.this,error);
                    }

                    @Override
                    public void onAdClick(String s) {
// 广告被点击时回调该方法
                        LogUtil.simple(TAG + "onAdClick ,s = "+s);

                        handleClick();
                    }

                    @Override
                    public void onAdClose() {
// 广告关闭时回调该方法
                        LogUtil.simple(TAG + "onAdClose  ");

                        handleClose();
                    }
                });

        if (splashSetting != null && splashSetting.getAdContainer() != null) {
            flBuilder.setContainer(splashSetting.getAdContainer());
        }
        flAd = flBuilder.build();
        flAd.requestSpread();
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
        if (adData != null) {
            adData.show(splashSetting.getAdContainer());
        }
    }

    @Override
    public boolean isValid() {
        if (adData != null) {
            return adData.isAdReady();
        }
        return super.isValid();
    }
}
