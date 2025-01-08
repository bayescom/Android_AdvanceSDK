package com.advance.supplier.gdt;

import com.advance.AdvanceConfig;
import com.advance.AdvanceRewardVideoItem;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.qq.e.ads.rewardvideo.RewardVideoAD;

@Deprecated
public class GdtRewardVideoAdItem implements AdvanceRewardVideoItem {
    private GdtRewardVideoAdapter gdtRewardVideoAdapter;
    private RewardVideoAD rewardVideoAD;

    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_GDT;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_GDT;
    }


    @Override
    public void showAd() {
        try {
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (gdtRewardVideoAdapter != null) {
                        gdtRewardVideoAdapter.show();
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (gdtRewardVideoAdapter != null)
                    gdtRewardVideoAdapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    public GdtRewardVideoAdItem(GdtRewardVideoAdapter gdtRewardVideoAdapter) {
        this.gdtRewardVideoAdapter = gdtRewardVideoAdapter;
    }

    @Deprecated
    public GdtRewardVideoAdItem(GdtRewardVideoAdapter gdtRewardVideoAdapter, RewardVideoAD rewardVideoAd) {
        this.gdtRewardVideoAdapter = gdtRewardVideoAdapter;
        this.rewardVideoAD = rewardVideoAd;
    }

    @Deprecated
    public void loadAD() {
        if (rewardVideoAD != null) {
            rewardVideoAD.loadAD();
        }
    }

    @Deprecated
    public void showAD() {
        showAd();
    }




}
