package com.advance.supplier.oppo;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.heytap.msp.mobad.api.ad.RewardVideoAd;
import com.heytap.msp.mobad.api.listener.IRewardVideoAdListener;
import com.heytap.msp.mobad.api.params.RewardVideoAdParams;

import java.util.Map;

public class OppoRewardAdapter extends AdvanceRewardCustomAdapter {
    private final String TAG = "[OppoRewardAdapter] ";
    RewardVideoAd mRewardVideoAd;

    @Override
    protected void adPrepared() {
    }

    @Override
    public void destroyAd() {
        try {
            if (mRewardVideoAd != null)
                mRewardVideoAd.destroyAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        try {
            if (mRewardVideoAd != null) {
                return mRewardVideoAd.isReady();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
           return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (mRewardVideoAd != null)
                mRewardVideoAd.showAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            mRewardVideoAd = new RewardVideoAd(getRealContext(), sdkSupplier.adspotid, new IRewardVideoAdListener() {
                @Override
                public void onAdSuccess() {
                    LogUtil.simple(TAG + "onAdSuccess ");

                    int ecpm = mRewardVideoAd == null ? 0 : mRewardVideoAd.getECPM();
                    handleSucceed(ecpm);
                }

                @Override
                public void onAdFailed(String s) {
                    //                已废弃，

                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);

                }

                @Override
                public void onAdClick(long currentPosition) {
                    LogUtil.simple(TAG + "onAdClick  ，currentPosition = " + currentPosition);

                    handleClick();
                }

                @Override
                public void onVideoPlayStart() {
                    LogUtil.simple(TAG + " onVideoPlayStart");

                    handleShow();

                }

                @Override
                public void onVideoPlayComplete() {
                    LogUtil.simple(TAG + "onVideoPlayComplete");

                    handleComplete();
                }

                @Override
                public void onVideoPlayError(String msg) {
                    LogUtil.simple(TAG + " onVideoPlayError ,  msg = " + msg);

                    handleFailed(AdvanceError.ERROR_VIDEO_RENDER_ERR, msg);
                }

                @Override
                public void onVideoPlayClose(long currentPosition) {
//                    当视频播放过程中被关闭时回调
                    LogUtil.simple(TAG + "onVideoPlayClose  ,currentPosition =" + currentPosition);

                    handleClose();
                }

                @Override
                public void onLandingPageOpen() {
//                    当视频播放完毕落地页打开时回调
                    LogUtil.simple(TAG + " onLandingPageOpen");

                }

                @Override
                public void onLandingPageClose() {
//                    当视频落地页关闭时回调
                    LogUtil.simple(TAG + "onLandingPageClose ");


                }

                @Override
                public void onReward(Object... objects) {
                    LogUtil.simple(TAG + " onReward");

                    handleReward();
                }
            });

            RewardVideoAdParams rewardVideoAdParams = new RewardVideoAdParams.Builder()
                    .setFetchTimeout(sdkSupplier.timeout)
                    .build();
            mRewardVideoAd.loadAd(rewardVideoAdParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
