package com.advance;

import android.app.Activity;

import java.lang.ref.SoftReference;

public abstract class BaseSplashAdapter extends BaseParallelAdapter {
    public SplashSetting splashSetting;
//    public ViewGroup adContainer;
//    public TextView skipView;
    public String skipText = "跳过 %d";
    public boolean isCountingEnd = false;//用来判断是否倒计时走到了最后，false 回调dismiss的话代表是跳过，否则倒计时结束

    public BaseSplashAdapter(SoftReference<Activity> activity, final SplashSetting advanceSplash) {
        super(activity, advanceSplash);
        this.splashSetting = advanceSplash;

        try {
            if (advanceSplash != null) {
//                skipView = advanceSplash.getSkipView();
//                adContainer = advanceSplash.getAdContainer();
                String st = advanceSplash.getSkipText();
                if (st != null && !"".equals(st)) {
                    this.skipText = st;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
