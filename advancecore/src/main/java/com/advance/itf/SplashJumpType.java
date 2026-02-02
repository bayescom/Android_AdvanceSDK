package com.advance.itf;

/**
 * 开屏广告回调跳转时，可能的跳转行为原因
 */
public interface SplashJumpType {
    int TYPE_AD_FAILED = 1; //广告执行失败，对应onAdFailed回调
    int TYPE_CLICK_SKIP = 2; //用户点击了广告跳过，对应旧onAdSkip回调
    int TYPE_TIME_OVER = 3; //广告倒计时结束，对应旧onAdTimeOver回调
}
