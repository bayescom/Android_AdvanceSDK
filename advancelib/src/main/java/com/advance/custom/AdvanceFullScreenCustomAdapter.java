package com.advance.custom;

import android.app.Activity;

import com.advance.FullScreenVideoSetting;

public abstract class AdvanceFullScreenCustomAdapter extends AdvanceBaseCustomAdapter {
    FullScreenVideoSetting mSetting;
    public AdvanceFullScreenCustomAdapter(Activity activity, FullScreenVideoSetting setting) {
        super(activity, setting);
        mSetting = setting;
    }

    public void handleCached() {
        try {
            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onCached();
                }
            } else {
                if (null != mSetting) {
                    mSetting.adapterVideoCached();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
