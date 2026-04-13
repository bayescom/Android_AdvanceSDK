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
            case "sigmob":
                result.banner = "";
                result.splash = "10011940";
                result.reward = "10011943";
                result.interstitial = "10011942";
                result.nativeExpress = "";
                result.nativeCustom = "10011941";
                result.fullscreen = "";
                result.draw = "";
                break;
        }
        return result;
    }
}
