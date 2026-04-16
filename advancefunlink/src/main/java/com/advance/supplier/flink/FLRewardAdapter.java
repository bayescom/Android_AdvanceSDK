package com.advance.supplier.flink;

import android.app.Activity;

import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.utils.AdvanceCacheUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.fl.saas.adx.api.FLVideo;

public class FLRewardAdapter extends AdvanceRewardCustomAdapter {
    FLVideo flAd;
    public FLRewardAdapter(Activity activity, RewardVideoSetting setting) {
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
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, FLRewardAdapter.class, new BYAbsCallBack<FLRewardAdapter>() {
            @Override
            public void invoke(FLRewardAdapter cacheAdapter) {
                //更新缓存广告得价格
                updateBidding(cacheAdapter.flAd.getECPM());
            }
        });
        if (hitCache) {
            return;
        }
        FLVideo.Builder builder = new FLVideo.Builder(getRealContext());

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
