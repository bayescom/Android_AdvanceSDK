package com.advance.model;

import com.bayes.sdk.basic.model.BYBaseModel;

//后端下发的自定义adn配置项
public class AdvanceCustomADNModel extends BYBaseModel {
    //该自定义adn唯一id信息
    public String sdkID = "";
    public String sdkName = "";

    //初始化类名
    public String initClzName = "";

    //各广告位类名
    public String splashClzName = "";
    public String bannerClzName = "";
    public String interstitialClzName = "";
    public String rewardClzName = "";
    public String nativeExpressClzName = "";
    public String nativeCustomClzName = "";
}
