package com.advance.supplier.huawei;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.reward.Reward;
import com.huawei.hms.ads.reward.RewardAd;
import com.huawei.hms.ads.reward.RewardAdLoadListener;
import com.huawei.hms.ads.reward.RewardAdStatusListener;

import java.util.Map;

public class HWRewardAdapter extends AdvanceRewardCustomAdapter {
    private RewardAd rewardedAd;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        try {
            if (rewardedAd != null) {
                rewardedAd.destroy();
            }
        } catch (Exception e) {
        }
    }

    

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (rewardedAd == null || !rewardedAd.isLoaded()) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "未获取到广告信息"));
                return;
            }
            rewardedAd.show(getRealActivity(null), new RewardAdStatusListener() {
                @Override
                public void onRewardAdClosed() {
                    LogUtil.simple(TAG + "onRewardAdClosed");

                    handleClose();
                }

                @Override
                public void onRewardAdFailedToShow(int errorCode) {
                    LogUtil.simple(TAG + "onRewardAdFailedToShow " + "errorCode is :" + errorCode);

                    handleFailed(errorCode, " onRewardAdFailedToShow");
                }

                @Override
                public void onRewardAdOpened() {
                    LogUtil.simple(TAG + "onRewardAdOpened");

                    handleShow();
                }

                @Override
                public void onRewarded(Reward reward) {
                    // You are advised to grant a reward immediately and at the same time, check whether the reward
                    // takes effect on the server. If no reward information is configured, grant a reward based on the
                    // actual scenario.
                    int amount = reward == null ? 0 : reward.getAmount();
                    LogUtil.simple(TAG + "Watch video show finished , getAmount " + amount + " scores");

                    //回调激励事件
                    handleReward();

                    //回调服务端回调信息
                    RewardServerCallBackInf inf = new RewardServerCallBackInf();
                    inf.rewardVerify = true;
                    handleRewardInf(inf);
                }
            });

        } catch (Throwable e) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
            e.printStackTrace();
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        if (rewardedAd == null) {
            rewardedAd = new RewardAd(getRealContext(), sdkSupplier.adspotid);
        }

        RewardAdLoadListener rewardAdLoadListener = new RewardAdLoadListener() {
            @Override
            public void onRewardAdFailedToLoad(int errorCode) {
                LogUtil.simple(TAG + "onRewardAdFailedToLoad " + "errorCode is :" + errorCode);

                handleFailed(errorCode, " onRewardAdFailedToLoad");

            }

            @Override
            public void onRewardedLoaded() {
                LogUtil.simple(TAG + "onRewardedLoaded");

                double ecpm = 0;

                if (rewardedAd != null) {
                    ecpm=(HWUtil.getPrice(rewardedAd.getBiddingInfo()));
                }

                handleSucceed(ecpm);
            }
        };
        AdParam.Builder adParam = AdvanceHWManager.getInstance().globalAdParamBuilder;
        if (adParam == null) {
            adParam = new AdParam.Builder();
        }
        rewardedAd.loadAd(adParam.build(), rewardAdLoadListener);
    }

}
