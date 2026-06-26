package com.advance.supplier.flink;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.utils.LogUtil;
import com.fl.saas.adx.api.FLVideo;
import com.fl.saas.adx.base.exception.FLError;
import com.fl.saas.adx.base.interfaces.AdViewVideoCheckListener;
import com.fl.saas.adx.base.interfaces.AdViewVideoListener;

import java.util.Map;

public class FLRewardAdapter extends AdvanceRewardCustomAdapter {
    FLVideo flAd;
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        FLVideo.Builder builder = new FLVideo.Builder(getRealContext());
        builder.setKey(getPosID());
        if (rewardSetting != null) {
            builder.setUserId(rewardSetting.getUserId());
            builder.setCustomData(rewardSetting.getExtraInfo());
            builder.setMute(rewardSetting.isMute());
        }
        builder.setAdViewVideoCheckListener(new AdViewVideoCheckListener() {//不使用服务端校验无须设置
            @Override
            public void onVideoCheckReward(String transID) {
//返回校验用订单id,如开启服务端校验，会在onVideoReward回调前给此回调，如未开启服务端校验无需设 置此监听
                LogUtil.simple(TAG + "onVideoCheckReward transID = " + transID);

                RewardServerCallBackInf callBackInf = new RewardServerCallBackInf();
                callBackInf.rewardVerify = true;

                handleRewardInf(callBackInf);
            }
        });
        builder.setVideoListener(new AdViewVideoListener() {
            @Override
            public void onAdShow() {
// 视频展示回调
                LogUtil.simple(TAG + "onAdShow");
                handleShow();
            }

            @Override
            public void onAdClose() {
// 视频关闭回调
                LogUtil.simple(TAG + "onAdClose");

                handleClose();
            }

            @Override
            public void onVideoPrepared() {
// 视频加载完成回调
// 需要在此回调后调用视频播放方法
                LogUtil.simple(TAG + "onVideoPrepared");

                handleSucceed(flAd.getEcpm());
            }

            @Override
            public void onVideoReward(double d) {
// 视频播放奖励回调
                LogUtil.simple(TAG + "onVideoReward");
                handleReward();

            }

            @Override
            public void onVideoCompleted() {
// 视频播放完毕回调
                LogUtil.simple(TAG + "onVideoCompleted");

                handleComplete();
            }

            @Override
            public void onSkipVideo() {
//                    跳过
                LogUtil.simple(TAG + "onSkipVideo");

                handleSkip();
            }

            @Override
            public void onAdFailed(FLError error) {
// 广告异常、失败，回调该方法
                LogUtil.simple(TAG + "onAdFailed , error = " + error);

                FLUtil.handleErr(FLRewardAdapter.this, error);
            }

            @Override
            public void onAdClick(String s) {
// 广告被点击时回调该方法
                LogUtil.simple(TAG + "onAdClick ,s = " + s);

                handleClick();
            }
        });

        flAd = builder.build();
        flAd.requestRewardVideo();
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (flAd != null) {
            flAd.destroy();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        if (flAd != null) {
            flAd.show(getRealActivity(null));
        }
    }

    @Override
    public boolean isValid() {
        if (flAd != null) {
            return flAd.isReady();
        }
           return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);


        if (flAd!=null){
            flAd.biddingResultUpload(isWin, (int) winPrice,0);
        }
    }
}
