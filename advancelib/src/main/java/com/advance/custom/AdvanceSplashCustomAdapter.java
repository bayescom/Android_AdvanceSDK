package com.advance.custom;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advance.SplashSetting;

import java.lang.ref.SoftReference;

public abstract class AdvanceSplashCustomAdapter extends AdvanceBaseCustomAdapter {
    public SplashSetting splashSetting;
    public ViewGroup adContainer;
    public TextView skipView;
    public String skipText = "跳过 %d";

    public AdvanceSplashCustomAdapter(Activity activity, SplashSetting splashSetting) {
        super(new SoftReference<>(activity), splashSetting);
        this.splashSetting = splashSetting;

        try {
            if (splashSetting != null) {
                skipView = splashSetting.getSkipView();
                adContainer = splashSetting.getAdContainer();
                String st = splashSetting.getSkipText();
                if (st != null && !"".equals(st)) {
                    this.skipText = st;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void show() {
//
//    }
}
