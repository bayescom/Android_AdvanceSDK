package com.advance.custom;

import android.app.Activity;

import com.advance.InterstitialSetting;

public abstract class AdvanceInterstitialCustomAdapter extends AdvanceBaseCustomAdapter {
    protected InterstitialSetting mSetting;

    public AdvanceInterstitialCustomAdapter(Activity activity, InterstitialSetting setting) {
        super(activity, setting);
        this.mSetting = setting;
    }

    public void handleClose() {
        try {
            if (mSetting != null) {
                mSetting.adapterDidClosed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
