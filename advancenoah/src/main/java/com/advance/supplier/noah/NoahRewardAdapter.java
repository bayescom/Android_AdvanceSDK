package com.advance.supplier.noah;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.noah.api.AdError;
import com.noah.api.RewardedVideoAd;

import java.util.Map;

public class NoahRewardAdapter extends AdvanceRewardCustomAdapter {
    RewardedVideoAd noahAD;
    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        RewardedVideoAd.getAd(context, getPosID(), new RewardedVideoAd.AdListener() {
            @Override
            public void onAdError ( final AdError adError){
                LogUtil.simple(TAG+" onAdError , adError = "+ adError);
                //广告返回失败

                NoahUtil.loadErr(NoahRewardAdapter.this, adError);
            }
            @Override
            public void onAdLoaded ( final RewardedVideoAd ad){
                //广告请求成功，展示广告
                LogUtil.simple(TAG+" onAdLoaded , ad = "+ ad);

                //广告返回成功，展示广告
                if (ad == null) {
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "noahAD null");
                    return;
                }
                noahAD = ad;

                handleSucceed(ad.getPrice());
            }
            @Override
            public void onAdShown ( final RewardedVideoAd ad){
                //广告曝光
                LogUtil.simple(TAG+" onAdShown ");

                handleShow();
            }
            @Override
            public void onAdClosed ( final RewardedVideoAd ad){
                //广告关闭
                LogUtil.simple(TAG+" onAdClosed ");

                handleClose();
            }
            @Override
            public void onAdClicked ( final RewardedVideoAd ad){
                //广告点击
                LogUtil.simple(TAG+" onAdClicked ");

                handleClick();
            }
            @Override
            public void onVideoStart ( final RewardedVideoAd ad){
                //激励视频开始播放
                LogUtil.simple(TAG+" onVideoStart ");

            }
            @Override
            public void onVideoEnd ( final RewardedVideoAd ad){
                //激励视频播放完成
                LogUtil.simple(TAG+" onVideoEnd ");

                handleComplete();
            }

            @Override
            public void onRewarded(final RewardedVideoAd ad) {
                LogUtil.simple(TAG+" onRewarded ");
//激励发放
                //如果开通了进阶发奖功能，该接口会回调多次，具体开通请联系SDK商务同学
                //可以通过ad.getRewardType()查看当前是哪个类型的奖，默认基础奖励
                //未开通进阶发奖，可不必关注
                handleReward();

                RewardServerCallBackInf inf = new RewardServerCallBackInf();
                inf.rewardVerify = true;
                handleRewardInf(inf);

//
//                int rewardType = ad.getRewardType();
//                if (rewardType == ExternalKey.RewardType.BASE) {
//                    //发放基础奖励
//                } else if (rewardType == ExternalKey.RewardType.ADVANCED) {
//                    //发放高级奖励
//                }
            }
        });
    }

    @Override
    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        noahAD.show();
    }

    @Override
    public boolean isValid() {
        if (noahAD != null)
            return noahAD.isValid();

        return false;
    }

    @Override
    public void destroyAd() {
        if (noahAD != null)
            noahAD.destroy();
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        NoahUtil.bid(noahAD,isWin,winPrice,referBidInfo);
    }
}
