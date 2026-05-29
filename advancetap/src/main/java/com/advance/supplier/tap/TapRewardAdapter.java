package com.advance.supplier.tap;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapRewardVideoAd;

import java.util.Map;

public class TapRewardAdapter extends AdvanceRewardCustomAdapter {
    TapAdNative tapAdNative;
    TapRewardVideoAd adData;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        try {
            if (adData != null) {
                adData.dispose();
            }
            Context ctx = getRealActivity(null);
            if (ctx == null) {
                Activity showAct = null;
                if (rewardSetting != null) {
                    showAct = rewardSetting.getShowActivity();
                    if (showAct != null) {
                        ctx = showAct;
                    }
                }
                if (ctx == null) {
                    LogUtil.high(TAG + "--doDestroy-- use ctx cause activity null");

                    ctx = getRealContext();
                }
            }

            TapUtil.removeTapMap(ctx);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (adData != null) {
                adData.setRewardAdInteractionListener(new TapRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        LogUtil.simple(TAG + " onAdShow");

                        handleShow();
                    }

                    @Override
                    public void onAdClose() {
                        LogUtil.simple(TAG + " onAdClose");

                        handleClose();

                    }

                    @Override
                    public void onVideoComplete() {
                        //                        经测试，并不会回调此方法
                        LogUtil.simple(TAG + " onVideoComplete");

                        handleComplete();
                    }

                    @Override
                    public void onVideoError() {
                        LogUtil.simple(TAG + " onVideoError");

                        handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, " onVideoError");
                    }

                    //视频播放完成后，奖励验证回调，rewardVerify：是否有效，code：错误码，msg：错误信息
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int code, String msg) {
                        try {
                            LogUtil.simple(TAG + " onRewardVerify : rewardVerify = " + rewardVerify + ", rewardAmount = " + rewardAmount + ", rewardName = " + rewardName + ", code = " + code + ", msg = " + msg);

                            handleReward();


                            RewardServerCallBackInf inf = new RewardServerCallBackInf();
                            inf.rewardVerify = rewardVerify;
                            inf.rewardAmount = rewardAmount;
                            inf.rewardName = rewardName;

                            inf.errorCode = code;
                            inf.errMsg = msg;
                            if (sdkSupplier != null) {
                                inf.supId = sdkSupplier.id;
                            }

                            handleRewardInf(inf);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
//                        经测试，并不会回调此方法
                        LogUtil.simple(TAG + " onSkippedVideo");

                        handleSkip();
                    }

                    @Override
                    public void onAdClick() {
                        LogUtil.simple(TAG + "onAdClick");

                        handleClick();
                    }


                    @Override
                    public void onAdValidShow() {
                        LogUtil.simple(TAG + "onAdValidShow");

                    }

                });

                adData.showRewardVideoAd(activity);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW, ""));
        }
    }


    private void loadAD() {
        try {

            //检查是否命中使用缓存逻辑
            boolean hitCache = AdvanceCacheUtil.loadWithCacheData(this, TapRewardVideoAd.class, new BYAbsCallBack<TapRewardVideoAd>() {
                @Override
                public void invoke(TapRewardVideoAd cacheAD) {
                    adData = cacheAD;

                    updateBidding(TapUtil.getBiddingPrice(cacheAD.getMediaExtraInfo()));
                }
            });
            if (hitCache) {
                return;
            }


            Context ctx = getRealActivity(null);
            if (ctx == null) {
                ctx = getRealContext();
                LogUtil.high(TAG + " use ctx cause activity null");
            }

//            tapAdNative = TapAdManager.get().createAdNative(ctx);
            tapAdNative = TapUtil.getTapADManger(ctx);

            int spaceId = TapUtil.getPlaceId(getPosID());
            String userID = AdvanceTapManger.getInstance().customTapUserId;
            if (BYStringUtil.isEmpty(userID)) {
                userID = rewardSetting.getUserId();
            }
            AdRequest request = new AdRequest.Builder().withSpaceId(spaceId)
                    .withUserId(userID)
                    .withRewardAmount(rewardSetting.getRewardCount())
                    .withRewardName(rewardSetting.getRewardName())
                    .withExtra1(rewardSetting.getExtraInfo())
                    .build();

            tapAdNative.loadRewardVideoAd(request, new TapAdNative.RewardVideoAdListener() {
                @Override
                public void onRewardVideoAdLoad(TapRewardVideoAd tapRewardVideoAd) {
                    try {
                        if (tapRewardVideoAd == null) {
                            String nMsg = TAG + " tapRewardVideoAd null";
                            handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                            return;
                        }
                        adData = tapRewardVideoAd;

                        updateBidding(TapUtil.getBiddingPrice(adData.getMediaExtraInfo()));

                        handleSucceed(adData);


                    } catch (Throwable e) {
                        e.printStackTrace();
                        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                    }
                }

                @Override
                public void onRewardVideoCached(TapRewardVideoAd tapRewardVideoAd) {
                    try {
                        LogUtil.simple(TAG + "onRewardVideoCached");

                        if (tapRewardVideoAd != null) {
                            adData = tapRewardVideoAd;
                        }

                        handleCached();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(int code, String message) {
                    LogUtil.e(TAG + " onError ");

                    handleFailed(code, message);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD, "out"));
        }
    }

}
