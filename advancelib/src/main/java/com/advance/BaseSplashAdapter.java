package com.advance;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.TextView;

import com.advance.model.AdvanceReportModel;
import com.advance.model.SdkSupplier;

import java.lang.ref.SoftReference;

public abstract class BaseSplashAdapter extends BaseParallelAdapter {
    public SplashSetting setting;
//    public ViewGroup adContainer;
//    public TextView skipView;
    public String skipText = "跳过 %d";

    public BaseSplashAdapter(SoftReference<Activity> activity, final SplashSetting advanceSplash) {
        super(activity, advanceSplash);
        this.setting = advanceSplash;

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
