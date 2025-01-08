package com.advance.custom;

import android.app.Activity;

import com.advance.RewardVideoSetting;
import com.advance.itf.AdvanceRewardExtInterface;

public abstract class AdvanceRewardCustomAdapter extends AdvanceBaseCustomAdapter implements AdvanceRewardExtInterface {
    public RewardVideoSetting setting;

    public AdvanceRewardCustomAdapter(Activity activity, RewardVideoSetting setting) {
        super(activity, setting);
        this.setting = setting;
    }

    public void handleCached() {
        try {
            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onCached();
                }
            } else {
                if (null != setting) {
                    setting.adapterVideoCached();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
