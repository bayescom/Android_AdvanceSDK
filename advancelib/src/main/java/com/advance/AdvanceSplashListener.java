package com.advance;

public interface AdvanceSplashListener extends AdvanceBaseListener {

//    void onAdSkip();
//
//    void onAdTimeOver();

    void onAdLoaded();

    void jumpToMain();//跳转到首页方法，直接在此进行跳转行为
}
