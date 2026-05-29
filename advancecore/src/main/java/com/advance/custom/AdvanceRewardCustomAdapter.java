package com.advance.custom;

import com.advance.RewardServerCallBackInf;

public abstract class AdvanceRewardCustomAdapter extends AdvanceBaseCustomAdapter {



    public void handleCached() {
        try {
            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onCached();
                }
            } else {
                if (null != rewardSetting) {
                    rewardSetting.adapterVideoCached();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void handleClose() {
        try {
            if (null != rewardSetting) {
                rewardSetting.adapterAdClose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleComplete() {
        try {
            if (null != rewardSetting) {
                rewardSetting.adapterVideoComplete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleSkip() {
        try {
            if (null != rewardSetting) {
                rewardSetting.adapterVideoSkipped();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleReward() {
        try {
            if (null != rewardSetting) {
                rewardSetting.adapterAdReward();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleRewardInf(RewardServerCallBackInf serverCallBackInf) {
        try {
            if (null != rewardSetting) {
                if (sdkSupplier != null && serverCallBackInf != null) {
                    serverCallBackInf.supId = sdkSupplier.id;
                }
                rewardSetting.postRewardServerInf(serverCallBackInf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
