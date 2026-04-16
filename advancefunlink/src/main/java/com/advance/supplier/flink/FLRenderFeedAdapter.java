package com.advance.supplier.flink;

import android.content.Context;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.utils.AdvanceCacheUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;

public class FLRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    public FLRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
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
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, FLRenderFeedAdapter.class, new BYAbsCallBack<FLRenderFeedAdapter>() {
            @Override
            public void invoke(FLRenderFeedAdapter cacheAdapter) {
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
