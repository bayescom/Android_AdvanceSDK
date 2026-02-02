package com.advance;

import android.app.Activity;

import com.advance.model.SdkSupplier;

public interface RewardVideoSetting extends BaseSetting {
    void adapterAdDidLoaded(AdvanceRewardVideoItem mercuryRewardVideoAdItem, SdkSupplier supplier);

    void adapterVideoCached();

    void adapterDidShow(SdkSupplier supplier);

    void adapterDidClicked(SdkSupplier supplier);

    void adapterAdReward();

    void postRewardServerInf(RewardServerCallBackInf inf);

    void adapterVideoSkipped();

    void adapterVideoComplete();

    void adapterAdClose();

    int getCsjImageAcceptedSizeWidth();

    int getCsjImageAcceptedSizeHeight();

    int getCsjExpressHeight();

    int getCsjExpressWidth();

    int getOrientation();

    boolean isCsjExpress();

    @Deprecated
    boolean isGdtVolumeOn();

    boolean isMute();

    String getUserId();

    String getExtraInfo();

    Activity getShowActivity();

    String getRewardName();

    int getRewardCount();
}
