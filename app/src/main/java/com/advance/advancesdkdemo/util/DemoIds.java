package com.advance.advancesdkdemo.util;

public class DemoIds {
    //    各个位置的广告位id
    public String banner;
    public String splash;
    public String reward;
    public String interstitial;
    public String nativeExpress;
    public String nativeCustom;
    public String fullscreen;
    public String draw;


    public static DemoIds getDemoIds(String sdkName) {
        DemoIds result = new DemoIds();
        switch (sdkName) {
            case "Mercury":
                result.banner = "10007785";
                result.splash = "10007770";
                result.reward = "10003102";
                result.interstitial = "10007786";
                result.nativeExpress = "10007784";
                result.nativeCustom = "10003122";
                result.fullscreen = "";
                result.draw = "";
                break;
            case "穿山甲":
                result.banner = "10007795";
                result.splash = "10007771";
                result.reward = "10007797";
                result.interstitial = "10007796";
                result.nativeExpress = "10007794";
                result.nativeCustom = "10003120";
                result.fullscreen = "10007825";
                result.draw = "10005123";
                break;
            case "广点通":
                result.banner = "10003092";
                result.splash = "10003079";
                result.reward = "10003101";
                result.interstitial = "10003098";
                result.nativeExpress = "10003095";
                result.nativeCustom = "10003121";
                result.fullscreen = "10003104";
                result.draw = "";
                break;
            case "百度":
                result.banner = "10007822";
                result.splash = "10007774";
                result.reward = "10007824";
                result.interstitial = "10007823";
                result.nativeExpress = "10007821";
                result.nativeCustom = "10009945";
                result.fullscreen = "10007828";
                result.draw = "";
                break;
            case "快手":
                result.banner = "";
                result.splash = "10007812";
                result.reward = "10007815";
                result.interstitial = "10007814";
                result.nativeExpress = "10007813";
                result.nativeCustom = "10009934";
                result.fullscreen = "10007827";
                result.draw = "10005127";
                break;
            case "tanx":
                result.banner = "";
                result.splash = "10009437";
                result.reward = "10009441";
                result.interstitial = "10009440";
                result.nativeExpress = "10009438";
                result.nativeCustom = "10009946";
                result.fullscreen = "";
                result.draw = "";
                break;
            case "taptap":
                result.banner = "10008664";
                result.splash = "10008661";
                result.reward = "10008662";
                result.interstitial = "10008663";
                result.nativeExpress = "";
                result.nativeCustom = "";
                result.fullscreen = "";
                result.draw = "";
                break;
            case "oppo":
                result.banner = "10011818";
                result.splash = "10011814";
                result.reward = "10011816";
                result.interstitial = "10011817";
                result.nativeExpress = "10011822";
                result.nativeCustom = "10011815";
                result.fullscreen = "";
                result.draw = "";
                break;
            case "sigmob":
                result.banner = "";
                result.splash = "10011940";
                result.reward = "10011943";
                result.interstitial = "10011942";
                result.nativeExpress = "10007821";
                result.nativeCustom = "10011941";
                result.fullscreen = "";
                result.draw = "";
                break;
        }
        return result;
    }
}
