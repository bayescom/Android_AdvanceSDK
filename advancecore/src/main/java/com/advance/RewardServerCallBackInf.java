package com.advance;

import java.util.Map;

// 2023/10/31 整合回调出去内容统一，将重要信息进行统一，比如奖励数量、名称；err信息等
public class RewardServerCallBackInf {

    //渠道id
    public String supId;
    //百度、mercury、快手 无以下任何信息，仅有rewardVerify
    public boolean rewardVerify;

    //tap、穿山甲 包含的返回内容
    public int rewardAmount;
    public String rewardName;
    public int errorCode;
    public String errMsg;


    //优量汇、tanx返回的map信息
    public Map<String, Object> rewardMap;

    //穿山甲返回的补充信息
    @Deprecated
    public CsjRewardInf csjInf;

    public static class CsjRewardInf {
        public boolean rewardVerify;
        public int rewardAmount;
        public String rewardName;
        public int errorCode;
        public float rewardPropose; //建议奖励百分比
        public int rewardType = 0; //其中rewardType 为枚举类型，包括：基础奖励 int REWARD_TYPE_DEFAULT = 0、进阶奖励-互动 int REWARD_TYPE_INTERACT = 1、进阶奖励-超过30s的视频播放完成 int REWARD_TYPE_VIDEO_COMPLETE = 2。
        public String errMsg;
    }

    @Override
    public String toString() {
        return "RewardServerCallBackInf{" +
                "supId='" + supId + '\'' +
                ", rewardVerify=" + rewardVerify +
                ", rewardAmount=" + rewardAmount +
                ", rewardName='" + rewardName + '\'' +
                ", errorCode=" + errorCode +
                ", errMsg='" + errMsg + '\'' +
                ", rewardMap=" + rewardMap +
                ", csjInf=" + csjInf +
                '}';
    }
}
