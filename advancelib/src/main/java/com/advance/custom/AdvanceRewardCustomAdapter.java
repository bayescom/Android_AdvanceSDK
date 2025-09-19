package com.advance.custom;

import android.app.Activity;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;

public abstract class AdvanceRewardCustomAdapter extends AdvanceBaseCustomAdapter {
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


    public void handleClose() {
        try {
            if (null != setting) {
                setting.adapterAdClose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleComplete() {
        try {
            if (null != setting) {
                setting.adapterVideoComplete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleSkip() {
        try {
            if (null != setting) {
                setting.adapterVideoSkipped();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleReward() {
        try {
            if (null != setting) {
                setting.adapterAdReward();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRewardInf(RewardServerCallBackInf serverCallBackInf) {
        try {
            if (null != setting) {
                if (sdkSupplier != null && serverCallBackInf != null) {
                    serverCallBackInf.supId = sdkSupplier.id;
                }
                setting.postRewardServerInf(serverCallBackInf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
