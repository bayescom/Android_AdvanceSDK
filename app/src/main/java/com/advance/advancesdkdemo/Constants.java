package com.advance.advancesdkdemo;

public class Constants {

    //应用id（仅测试用，不可用来上线正式使用！）
    public static final String APP_ID = "100171";

    //示例代码位id（仅测试用，不可用来上线正式使用！），具体展现的是哪个广告, 由后台策略来决定。实际使用请使用自己的广告位id，一定不要使用这里的测试id上线发布，否则无收益！
    public static class TestIds {
        //以下测试id默认配置了穿山甲广告
        public static final String splashAdspotId = "10007770"; //开屏-mercury
//        public static final String splashAdspotId = "10011940"; //开屏-Sigmob
        public static final String bannerAdspotId = "10007795"; //banner
//        public static final String interstitialAdspotId = "10007796"; //插屏
        public static final String interstitialAdspotId = "10011942"; //插屏-Sigmob
        public static final String nativeExpressAdspotId = "10007794"; //原生模板信息流
//        public static final String rewardAdspotId = "10007797"; //激励视频
//        public static final String rewardAdspotId = "10011943"; //激励视频-Sigmob
        public static final String rewardAdspotId = "10003101"; //激励视频-优量汇测试
        public static final String fullScreenVideoAdspotId = "10007825"; //全屏视频

        public static final String drawAdspotId = "10005127"; //draw信息流 -快手

    }


    public static final String SP_AGREE_PRIVACY = "agree_privacy";
    public static final String SP_NAME = "preference";
}
