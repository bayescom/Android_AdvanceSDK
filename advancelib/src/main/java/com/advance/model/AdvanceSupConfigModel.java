package com.advance.model;

public class AdvanceSupConfigModel {
    //初始化配置信息，SDKid为唯一SDK标记，className为需要反射获取的配置类名
    public AdvanceSupConfigModel(String sdkID, String className) {
        this.sdkID = sdkID;
        this.className = className;
    }

    public String sdkID = "";
    public String className = "";
//    public String extInf = "";

}
