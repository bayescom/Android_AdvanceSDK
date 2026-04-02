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
                result.banner = "10003091";
                result.splash = "10003083";
                result.reward = "10003100";
                result.interstitial = "10003097";
                result.nativeExpress = "10003094";
                result.nativeCustom = "10003120";
                result.fullscreen = "10012468";
                result.draw = "10005123";
                break;
            case "优量汇":
                result.banner = "10014710";
                result.splash = "10014707";
                result.reward = "10014713";
                result.interstitial = "10014711";
                result.nativeExpress = "10014708";
                result.nativeCustom = "10014709";
                result.fullscreen = "10014712";
                result.draw = "";
                break;
        }
        return result;
    }
}
