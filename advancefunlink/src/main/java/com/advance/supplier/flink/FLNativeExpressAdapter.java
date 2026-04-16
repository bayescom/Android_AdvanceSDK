package com.advance.supplier.flink;

import android.app.Activity;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.utils.AdvanceCacheUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;

public class FLNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    public FLNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
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
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, FLNativeExpressAdapter.class, new BYAbsCallBack<FLNativeExpressAdapter>() {
            @Override
            public void invoke(FLNativeExpressAdapter cacheAdapter) {
                //更新缓存广告得价格
//                updateBidding(cacheAdapter.splashAD.getECPM());
            }
        });
        if (hitCache) {
            return;
        }

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

    }

    @Override
    public boolean isValid() {
        if (spreadAd != null) {
            return spreadAd.isAdReady();
        }
        return super.isValid();
    }
}
