package com.advance.supplier.flink;

import android.app.Activity;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.fl.saas.adx.api.FLVideo;
import com.fl.saas.adx.base.exception.FLError;
import com.fl.saas.adx.base.interfaces.AdViewVideoCheckListener;
import com.fl.saas.adx.base.interfaces.AdViewVideoListener;

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
        if (setting != null) {
            builder.setUserId(setting.getUserId());
            builder.setCustomData(setting.getExtraInfo());
            builder.setMute(setting.isMute());
        }
        builder.setAdViewVideoCheckListener(new AdViewVideoCheckListener() {//不使用服务端校验无须设置
            @Override
            public void onVideoCheckReward(String transID) {
//返回校验用订单id,如开启服务端校验，会在onVideoReward回调前给此回调，如未开启服务端校验无需设 置此监听
                LogUtil.simple(TAG + "onVideoCheckReward transID = " + transID);

                RewardServerCallBackInf callBackInf = new RewardServerCallBackInf();
                callBackInf.rewardVerify = true;

                handleRewardInf(callBackInf);
            }
        });
        builder.setVideoListener(new AdViewVideoListener() {
            @Override
            public void onAdShow() {
// 视频展示回调
                LogUtil.simple(TAG + "onAdShow");
                handleShow();
            }

            @Override
            public void onAdClose() {
// 视频关闭回调
                LogUtil.simple(TAG + "onAdClose");

                handleClose();
            }

            @Override
            public void onVideoPrepared() {
// 视频加载完成回调
// 需要在此回调后调用视频播放方法
                LogUtil.simple(TAG + "onVideoPrepared");

                updateBidding(flAd.getEcpm());

                handleSucceed(FLRewardAdapter.this);
            }

            @Override
            public void onVideoReward(double d) {
// 视频播放奖励回调
                LogUtil.simple(TAG + "onVideoReward");
                handleReward();

            }

            @Override
            public void onVideoCompleted() {
// 视频播放完毕回调
                LogUtil.simple(TAG + "onVideoCompleted");

                handleComplete();
            }

            @Override
            public void onSkipVideo() {
//                    跳过
                LogUtil.simple(TAG + "onSkipVideo");

                handleSkip();
            }

            @Override
            public void onAdFailed(FLError error) {
// 广告异常、失败，回调该方法
                LogUtil.simple(TAG + "onAdFailed , error = " + error);

                FLUtil.handleErr(FLRewardAdapter.this, error);
            }

            @Override
            public void onAdClick(String s) {
// 广告被点击时回调该方法
                LogUtil.simple(TAG + "onAdClick ,s = " + s);

                handleClick();
            }
        });

        flAd = builder.build();
        flAd.requestRewardVideo();
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
