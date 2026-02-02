package com.advance.itf;

//用来桥接 各个SDK公共配置方法，主要包含了隐私开关配置、版本号获取、个性化广告推送开关等信息
public interface AdvanceSupplierBridge {

    //隐私配置
    void setCustomPrivacy(AdvancePrivacyController advancePrivacyController);

    //获取SDK版本号信息
    String getSDKVersion();

    //允许个性化广告推送开关
    void setPersonalRecommend(boolean allow);

}
