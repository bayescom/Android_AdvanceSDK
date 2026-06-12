package com.advance.supplier.baidu;


import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.BiddingListener;
import com.baidu.mobads.sdk.api.RewardVideoAd;

import java.util.HashMap;
import java.util.Map;


public class BDRewardAdapter extends AdvanceRewardCustomAdapter implements RewardVideoAd.RewardVideoAdListener {
    private RewardVideoAd mRewardVideoAd;

    private final String TAG = "[BDRewardAdapter] ";


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        mRewardVideoAd = new RewardVideoAd(getRealContext(), sdkSupplier.adspotid, this, AdvanceBDManager.getInstance().rewardUseSurfaceView);
        //服务端校验透传参数
        if (rewardSetting != null) {
            mRewardVideoAd.setUserId(rewardSetting.getUserId());
            mRewardVideoAd.setExtraInfo(rewardSetting.getExtraInfo());
        }
        //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
        int bidFloor = AdvanceBDManager.getInstance().rewardBidFloor;
        if (bidFloor > 0) {
            mRewardVideoAd.setBidFloor(bidFloor);
        }
//        mRewardVideoAd.setDownloadAppConfirmPolicy(AdvanceBDManager.getInstance().rewardDownloadAppConfirmPolicy);
        mRewardVideoAd.load();

    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
    }

    @Override
    public boolean isValid() {
        if (mRewardVideoAd != null) {
            return mRewardVideoAd.isReady();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        if (isWin) {
            mRewardVideoAd.biddingSuccess(null, new BiddingListener() {
                @Override
                public void onBiddingResult(boolean result, String message, HashMap<String, Object> ext) {
                }
            });
        } else {
            // 调用反馈竞价失败及原因
            mRewardVideoAd.biddingFail(BDUtil.getFailedObj(winPrice, referBidInfo), new BiddingListener() {
                @Override
                public void onBiddingResult(boolean result, String message, HashMap<String, Object> ext) {
                }
            });
        }
    }


    //以下为广告回调事件

    @Override
    public void onAdShow() {
        LogUtil.simple(TAG + "onAdShow");
        handleShow();
    }

    @Override
    public void onAdClick() {
        LogUtil.simple(TAG + "onAdClick");
        handleClick();
    }

    @Override
    public void onAdClose(float v) {
        LogUtil.simple(TAG + "onAdClose " + v);

        handleClose();

    }

    @Override
    public void onAdFailed(String s) {
        LogUtil.e(TAG + "onAdFailed " + s);
        handleFailed(AdvanceError.ERROR_BD_FAILED, s);
    }

    @Override
    public void onVideoDownloadSuccess() {
        LogUtil.simple(TAG + "onVideoDownloadSuccess");

        handleCached();
    }

    @Override
    public void onVideoDownloadFailed() {
        LogUtil.e(TAG + "onVideoDownloadFailed");
        handleFailed(AdvanceError.ERROR_BD_FAILED, "onVideoDownloadFailed");

    }

    @Override
    public void playCompletion() {
        LogUtil.simple(TAG + "playCompletion");

        handleComplete();
    }

    @Override
    public void onAdSkip(float playScale) {
        // 用户点击跳过, 展示尾帧
        // 建议：媒体可以按照自己的设计给予奖励
        LogUtil.simple(TAG + " onSkip: playScale = " + playScale);


        handleSkip();
    }

    @Override
    public void onRewardVerify(boolean rewardVerify) {
        try {
            LogUtil.simple(TAG + " onRewardVerify : rewardVerify = " + rewardVerify);

            RewardServerCallBackInf inf = new RewardServerCallBackInf();
            inf.rewardVerify = rewardVerify;
            if (rewardVerify) {
                //激励达成回调
                handleReward();

            }


            if (sdkSupplier != null) {
                inf.supId = sdkSupplier.id;
            }
            handleRewardInf(inf);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdLoaded() {
        LogUtil.simple(TAG + "onAdLoaded");
        double ecpm = 0;
        try { //避免方法有异常，catch一下，不影响success逻辑
            if (mRewardVideoAd != null) {
                ecpm = (BDUtil.getEcpmValue(mRewardVideoAd.getECPMLevel()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        handleSucceed(ecpm);
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            mRewardVideoAd.show();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

//    @Override
//    public boolean isValid() {
//        try {
//            if (mRewardVideoAd == null) {
//                return false;
//            }
//            return mRewardVideoAd.isReady();
//        } catch (Throwable e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
