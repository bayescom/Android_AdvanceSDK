package com.advance.supplier.tanx;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.ad.ad.reward.ITanxRewardVideoAd;
import com.alimm.tanx.core.ad.ad.reward.model.VideoParam;
import com.alimm.tanx.core.ad.ad.template.rendering.reward.ITanxRewardExpressAd;
import com.alimm.tanx.core.ad.bean.TanxBiddingInfo;
import com.alimm.tanx.core.ad.listener.ITanxAdLoader;
import com.alimm.tanx.core.ad.view.TanxAdView;
import com.alimm.tanx.core.request.TanxAdSlot;
import com.alimm.tanx.core.request.TanxError;
import com.alimm.tanx.core.request.TanxPlayerError;
import com.alimm.tanx.ui.TanxSdk;
import com.bayes.sdk.basic.util.BYStringUtil;

import java.util.List;
import java.util.Map;

public class TanxRewardAdapter extends AdvanceRewardCustomAdapter {
    private final String TAG = "[TanxRewardAdapter] ";
    ITanxAdLoader iTanxAdLoader;
    ITanxRewardExpressAd iTanxRewardVideoExpressAd;
    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        LogUtil.simple(TAG + "doDestroy");
        if (iTanxAdLoader != null) {
            iTanxAdLoader.destroy();
            LogUtil.simple(TAG + "iTanxAdLoader doDestroy");
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        if (iTanxRewardVideoExpressAd == null) {
            handleFailed(AdvanceError.ERROR_EXCEPTION_SHOW, "无广告内容");
            return;
        }
        try {
            TanxBiddingInfo biddingResult = new TanxBiddingInfo();
            biddingResult.setBidResult(true);
            //上报竞价成功
            iTanxRewardVideoExpressAd.setBiddingResult(biddingResult);

            VideoParam videoParam = new VideoParam();
            videoParam.mute = rewardSetting.isMute();
            iTanxRewardVideoExpressAd.showAd(activity, videoParam);

            iTanxRewardVideoExpressAd.setOnRewardAdListener(new ITanxRewardExpressAd.OnRewardAdListener() {

                @Override
                public void onAdClicked(TanxAdView tanxAdView, ITanxRewardVideoAd iTanxRewardVideoAd) {
                    LogUtil.simple(TAG + "onAdClicked");
                    handleClick();
                }

                @Override
                public void onClickCommitSuccess(ITanxRewardVideoAd iTanxRewardVideoAd) {
                    LogUtil.simple(TAG + "onClickCommitSuccess");

                }

                @Override
                public void onAdShow(ITanxRewardVideoAd iTanxRewardVideoAd) {
                    LogUtil.simple(TAG + "onAdShow");

                    handleShow();
                }

                @Override
                public void onExposureCommitSuccess(ITanxRewardVideoAd iTanxRewardVideoAd) {
                    LogUtil.simple(TAG + "onExposureCommitSuccess");

                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + "onAdClose");
                    handleClose();
                }

                @Override
                public void onVideoComplete() {
                    LogUtil.simple(TAG + "onVideoComplete");

                    handleComplete();
                }

                @Override
                public void onVideoError(TanxPlayerError tanxPlayerError) {
                    LogUtil.simple(TAG + "onVideoError");
                    String errInf = "";
                    if (tanxPlayerError != null) {
                        errInf = tanxPlayerError.toString();
                    }
                    AdvanceError advanceError = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER, "onVideoError" + errInf);
                    runParaFailed(advanceError);
                }

//onRewardArrived	boolean isRewardValid	 当前通知是否有效
//int rewardType	0:基础奖励 >0:进阶奖励

//Map<String, Object> extraInfo ：
//pid  	奖励的广告位pid
//taskType  	暂时可不关注
//sessionId  	奖励对应的某个广告的id
//completeTime  	任务完成时间
//rewardName  	媒体在平台配置的奖励名称
//rewardCount  	媒体在平台配置的奖励数量

                @Override
                public void onRewardArrived(boolean b, int i, Map<String, Object> map) {
                    LogUtil.simple(TAG + "onRewardArrived，rewardVerify = " + b);
                    RewardServerCallBackInf inf = new RewardServerCallBackInf();

                    inf.rewardVerify = b;
                    inf.rewardMap = map;
                        if (b) {
                            handleReward();
                        }

                        if (sdkSupplier != null) {
                            inf.supId = sdkSupplier.id;
                        }
                        handleRewardInf( inf);
                }

                @Override
                public void onSkippedVideo() {
                    LogUtil.simple(TAG + "onSkippedVideo");

                    handleSkip();
                }

                @Override
                public void onError(TanxError tanxError) {
                    String errInf = "on show Error";
                    LogUtil.simple(TAG + errInf);
                    if (tanxError != null) {
                        errInf = tanxError.toString();
                    }
                    AdvanceError advanceError = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER, "tanxError , " + errInf);
                    runParaFailed(advanceError);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        //获取用户id，首先检查tanx通用配置
        String uid = AdvanceTanxSetting.getInstance().mediaUID;
        //为空则获取广告位上配置的uid
        if (BYStringUtil.isEmpty(uid)){
            uid = rewardSetting.getUserId();
        }
        if (BYStringUtil.isEmpty(uid)){
            LogUtil.e(TAG+"tanx激励需要配置用户id信息，否则可能拉取不到广告。");
        }
        TanxAdSlot adSlot = new TanxAdSlot.Builder()
                .adCount(sdkSupplier.adCount)
                .pid(sdkSupplier.adspotid)
                .setMediaUid(uid)
                .setRewardParam(AdvanceTanxSetting.getInstance().rewardParam)
                .setUserId(uid)
                .build();
        int timeout = sdkSupplier.timeout <= 0 ? 5000 : sdkSupplier.timeout;

        iTanxAdLoader = TanxSdk.getSDKManager().createAdLoader(getRealContext());

        iTanxAdLoader.loadRewardAd(adSlot, new ITanxAdLoader.OnRewardAdLoadListener<ITanxRewardExpressAd>() {
            @Override
            public void onLoaded(List<ITanxRewardExpressAd> adList) {
                try {
                    if (adList.size() == 0 || adList.get(0) == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                        return;
                    }
                    LogUtil.simple(TAG + "onLoaded");
                    iTanxRewardVideoExpressAd = adList.get(0);
                    long ecpm = 0;
                    try {
                        ecpm =(iTanxRewardVideoExpressAd.getBidInfo().getBidPrice());
                    } catch (Throwable e) {
                    }
                    handleSucceed(ecpm);
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                }

            }

            @Override
            public void onRewardVideoCached(ITanxRewardExpressAd ad) {
                //这里启动广告 视频流畅不卡顿
                LogUtil.simple(TAG + "onRewardVideoCached");

                handleCached();
            }

            @Override
            public void onTimeOut() {
                String msg = "获取广告超时";
                LogUtil.simple(TAG + "onTimeOut   " + ", msg = " + msg);
                handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);

            }

            @Override
            public void onError(TanxError tanxError) {
                String msg = "广告请求失败";
                if (tanxError != null) {
                    msg = "[tanx onError] " + tanxError;
                }
                LogUtil.simple(TAG + "onError   " + ", msg = " + msg);
                handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);

            }
        }, timeout);
    }
}
