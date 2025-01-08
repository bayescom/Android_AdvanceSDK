package com.advance.supplier.mry;

import com.advance.AdvanceConfig;
import com.advance.AdvanceRewardVideoItem;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.mercury.sdk.core.rewardvideo.RewardVideoAD;
@Deprecated
public class MercuryRewardVideoAdItem implements AdvanceRewardVideoItem {
    private MercuryRewardVideoAdapter mercuryRewardVideoAdapter;
    private RewardVideoAD rewardVideoAD;

    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_MERCURY;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_MERCURY;
    }

    @Override
    public void showAd() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                doShow();
            }
        });

    }

    private void doShow() {
        try {
                rewardVideoAD.showAD();
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (mercuryRewardVideoAdapter != null) {
                    mercuryRewardVideoAdapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    public MercuryRewardVideoAdItem(MercuryRewardVideoAdapter mercuryRewardVideoAdapter, RewardVideoAD rewardVideoAd) {
        this.mercuryRewardVideoAdapter = mercuryRewardVideoAdapter;
        this.rewardVideoAD = rewardVideoAd;
    }

    public void loadAD() {
        try {
            if (null != rewardVideoAD) {
                rewardVideoAD.loadAD();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    @Deprecated
    public void showAD() {
        if (null != rewardVideoAD) {
            rewardVideoAD.showAD();
        }
    }


    public boolean hasShown() {
        if (null != rewardVideoAD) {
            return rewardVideoAD.hasShown();
        }
        return false;

    }

}
