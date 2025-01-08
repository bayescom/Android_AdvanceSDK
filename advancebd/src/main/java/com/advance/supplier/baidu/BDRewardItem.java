package com.advance.supplier.baidu;

import com.advance.AdvanceConfig;
import com.advance.AdvanceRewardVideoItem;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.baidu.mobads.sdk.api.RewardVideoAd;
import com.bayes.sdk.basic.util.BYThreadUtil;

@Deprecated
public class BDRewardItem implements AdvanceRewardVideoItem {
    private RewardVideoAd rewardVideoAd;
    BDRewardAdapter adapter;

    BDRewardItem(BDRewardAdapter adapter, RewardVideoAd ad) {
        this.rewardVideoAd = ad;
        this.adapter = adapter;

    }

    @Override
    public String getSdkTag() {
        return BDUtil.BD_TAG;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_BAIDU;
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
            if (rewardVideoAd != null) {
                rewardVideoAd.show();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (adapter != null)
                    adapter.runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
