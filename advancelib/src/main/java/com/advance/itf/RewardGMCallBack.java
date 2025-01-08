package com.advance.itf;

public interface RewardGMCallBack extends BaseGMCallBackListener {

    void onVideoCached();

    void onVideoComplete();

    void onVideoSkip();

    void onAdClose();

    void onAdReward();
}
