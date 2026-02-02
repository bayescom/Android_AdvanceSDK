package com.advance.model;

public class AdvanceSDKCacheModel {
    //缓存广告那一刻的时间戳
    public long cacheTime = 0;
    //缓存key
    public String cacheKey = "";
    //变现SDK的广告位id
    public String adID = "";
    //SDK端的reqid
    public String advanceReqID = "";
    //server端reqid
    public String serverReqID = "";


    //或许可以使用泛型？
    //缓存内容--具体广告对象
    public Object cacheValue = null;
    //补充缓存对象
    public Object cacheValueExt = null;

    @Override
    public String toString() {
        return "AdvanceSDKCacheModel{" +
                "cacheKey='" + cacheKey + '\'' +
                ", adID='" + adID + '\'' +
                ", advanceReqID='" + advanceReqID + '\'' +
                ", serverReqID='" + serverReqID + '\'' +
                ", cacheValue=" + cacheValue +
                '}';
    }
}
