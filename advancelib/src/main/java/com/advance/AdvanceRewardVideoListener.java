package com.advance;

import androidx.annotation.Nullable;

public interface AdvanceRewardVideoListener extends AdvanceBaseListener {
    void onAdLoaded(@Nullable AdvanceRewardVideoItem advanceRewardVideoItem);

    void onVideoCached();

    void onVideoComplete();

    void onVideoSkip();

    void onAdClose();

    void onAdReward();

    //激励视频返回的服务器回调信息，穿山甲一直支持，广点通自v4.330.1200 开始支持,百度9.13开始支持
    void onRewardServerInf(RewardServerCallBackInf inf);
}
