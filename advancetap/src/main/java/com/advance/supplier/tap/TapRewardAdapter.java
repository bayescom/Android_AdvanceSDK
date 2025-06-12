package com.advance.supplier.tap;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapRewardVideoAd;

public class TapRewardAdapter extends AdvanceRewardCustomAdapter {
    TapAdNative tapAdNative;
    TapRewardVideoAd adData;


    public TapRewardAdapter(Activity activity, RewardVideoSetting setting) {
        super(activity, setting);
    }

    @Override
    public void orderLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });

    }

    @Override
    protected void paraLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });

    }


    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (adData != null) {
                adData.dispose();
            }
            Context ctx = getRealActivity(null);
            if (ctx == null) {
                Activity showAct = null;
                if (setting != null) {
                    showAct = setting.getShowActivity();
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

    @Override
    public boolean isValid() {
        return super.isValid();
    }

    @Override
    public void show() {
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
                        if (setting != null) {
                            setting.adapterAdClose();
                        }

                    }

                    @Override
                    public void onVideoComplete() {
                        //                        经测试，并不会回调此方法
                        LogUtil.simple(TAG + " onVideoComplete");

                        if (setting != null) {
                            setting.adapterVideoComplete();
                        }
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
                            if (setting != null) {
                                setting.adapterAdReward();

                                RewardServerCallBackInf inf = new RewardServerCallBackInf();
                                inf.rewardVerify = rewardVerify;
                                inf.rewardAmount = rewardAmount;
                                inf.rewardName = rewardName;

                                inf.errorCode = code;
                                inf.errMsg = msg;
                                if (sdkSupplier != null) {
                                    inf.supId = sdkSupplier.id;
                                }
                                setting.postRewardServerInf(inf);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onSkippedVideo() {
//                        经测试，并不会回调此方法
                        LogUtil.simple(TAG + " onSkippedVideo");

                        if (setting != null) {
                            setting.adapterVideoSkipped();
                        }
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

                adData.showRewardVideoAd(setting.getShowActivity());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW, ""));
        }
    }


    private void loadAD() {
        try {
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
                userID = setting.getUserId();
            }
            AdRequest request = new AdRequest.Builder().withSpaceId(spaceId)
                    .withUserId(userID)
                    .withRewardAmount(setting.getRewardCount())
                    .withRewardName(setting.getRewardName())
                    .withExtra1(setting.getExtraInfo())
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

                        handleSucceed();


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

                        if (isParallel) {
                            if (parallelListener != null) {
                                parallelListener.onCached();
                            }
                        } else {
                            if (null != setting) {
                                setting.adapterVideoCached();
                            }
                        }
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
