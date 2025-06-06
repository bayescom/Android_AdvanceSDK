package com.advance.supplier.csj;

import android.app.Activity;
import android.os.Bundle;

import com.advance.AdvanceConfig;
import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bykv.vk.openvk.TTRdVideoObject;
import com.bykv.vk.openvk.TTVfManager;
import com.bykv.vk.openvk.TTVfNative;
import com.bykv.vk.openvk.TTVfSdk;
import com.bykv.vk.openvk.VfSlot;

public class CsjRewardVideoAdapter extends AdvanceRewardCustomAdapter implements TTVfNative.RdVideoVfListener {

    private RewardVideoSetting advanceRewardVideo;
    private TTRdVideoObject ttRewardVideoAd;
    private String TAG = "[CsjRewardVideoAdapter] ";

    public CsjRewardVideoAdapter(Activity activity, RewardVideoSetting advanceRewardVideo) {
        super(activity, advanceRewardVideo);
        this.advanceRewardVideo = advanceRewardVideo;
    }

    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }


    @Override
    public void onError(int i, String s) {
        LogUtil.simple(TAG + "onError" + i + s);

        runParaFailed(AdvanceError.parseErr(i, s));
    }

    @Override
    public void onRdVideoVrLoad(TTRdVideoObject ttRewardVideoAd) {
        try {
            LogUtil.simple(TAG + "onRewardVideoAdLoad");

            if (ttRewardVideoAd == null) {
                String nMsg = TAG + " ttRewardVideoAd null";
                AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, nMsg);
                runParaFailed(error);
                return;
            }
            this.ttRewardVideoAd = ttRewardVideoAd;

            rewardVideoItem = new CsjRewardVideoAdItem(null, this, ttRewardVideoAd);

            updateBidding(CsjUtil.getEcpmValue(TAG, ttRewardVideoAd.getMediaExtraInfo()));

            handleSucceed();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onRdVideoCached() {
        LogUtil.simple(TAG + "onRewardVideoCached");

//        if (isParallel) {
//            if (parallelListener != null) {
//                parallelListener.onCached();
//            }
//        } else {
//            if (null != advanceRewardVideo) {
//                advanceRewardVideo.adapterVideoCached();
//            }
//        }
    }

    @Override
    public void onRdVideoCached(TTRdVideoObject ttRewardVideoAd) {
        try {
            String ad = "";
            if (ttRewardVideoAd != null) {
                ad = ttRewardVideoAd.toString();
            }
            LogUtil.simple(TAG + "onRewardVideoCached( " + ad + ")");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (isParallel) {
            if (parallelListener != null) {
                parallelListener.onCached();
            }
        } else {
            if (null != advanceRewardVideo) {
                advanceRewardVideo.adapterVideoCached();
            }
        }
    }


    public void onAdFailed(AdvanceError error) {
        runParaFailed(error);
    }

    public void onAdItemShow() {
        LogUtil.simple(TAG + "onAdItemShow");
        handleShow();
    }

    public void onAdItemClick() {
        LogUtil.simple(TAG + "onAdItemClick");
        handleClick();
    }

    public void onAdItemClose() {
        LogUtil.simple(TAG + "onAdItemClose");

        if (null != advanceRewardVideo) {
            advanceRewardVideo.adapterAdClose();
        }

    }

    public void onAdItemVideoComplete() {
        LogUtil.simple(TAG + "onAdItemVideoComplete");

        if (null != advanceRewardVideo) {
            advanceRewardVideo.adapterVideoComplete();
        }

    }

    public void onAdItemVideoSkipped() {
        LogUtil.simple(TAG + "onAdItemVideoSkipped");
        if (null != advanceRewardVideo) {
            advanceRewardVideo.adapterVideoSkipped();
        }
    }

    public void onAdItemRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errMsg) {
        onAdItemRewardVerify(rewardVerify, 0, rewardAmount, rewardName, errorCode, errMsg, 0f);
    }

    public void onAdItemRewardVerify(boolean rewardVerify, int rewardType, int rewardAmount, String rewardName, int errorCode, String errMsg, float mRewardPropose) {
        try {
            LogUtil.simple(TAG + "onAdItemRewardVerify; rewardVerify = " + rewardVerify + ",rewardAmount = " + rewardAmount + ",rewardName = " + rewardName + " errorCode:" + errorCode + " errMsg:" + errMsg);

            RewardServerCallBackInf inf = new RewardServerCallBackInf();
            inf.rewardVerify = rewardVerify;
            inf.rewardAmount = rewardAmount;
            inf.rewardName = rewardName;
            inf.errorCode = errorCode;
            inf.errMsg = errMsg;

            RewardServerCallBackInf.CsjRewardInf csjRewardInf = new RewardServerCallBackInf.CsjRewardInf();
            csjRewardInf.rewardVerify = rewardVerify;
            csjRewardInf.rewardAmount = rewardAmount;
            csjRewardInf.rewardName = rewardName;
            csjRewardInf.rewardType = rewardType;
            csjRewardInf.rewardPropose = mRewardPropose;
            csjRewardInf.errorCode = errorCode;
            csjRewardInf.errMsg = errMsg;
            inf.csjInf = csjRewardInf;
            inf.rewardVerify = rewardVerify;
            if (null != advanceRewardVideo) {
                if (sdkSupplier != null) {
                    inf.supId = sdkSupplier.id;
                }
                advanceRewardVideo.postRewardServerInf(inf);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (rewardVerify) {
            if (null != advanceRewardVideo) {
                advanceRewardVideo.adapterAdReward();
//                advanceRewardVideo.adapterDidFailed(AdvanceError.parseErr(errorCode, errMsg));
            }
        } else if (errorCode != 0) {//如果有异常信息，是否进行异常回调？
            LogUtil.e("onAdItemRewardVerify errorCode = " + errorCode + "  errMsg" + errMsg);
//                advanceRewardVideo.adapterDidFailed(AdvanceError.parseErr(errorCode, errMsg));
        }

    }

    public void onAdItemVideoError(AdvanceError advanceError) {
        LogUtil.simple(TAG + "onAdItemVideoError");

        runParaFailed(advanceError);
    }

    @Override
    public void paraLoadAd() {
        CsjUtil.initCsj(this, new CsjUtil.InitListener() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
                startLoad();
            }

            @Override
            public void fail(int code, String msg) {
                handleFailed(code, msg);
            }
        });
    }

    private void startLoad() {
        final TTVfManager ttAdManager = TTVfSdk.getVfManager();

        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(getRealContext());
        }
        TTVfNative ttAdNative = ttAdManager.createVfNative(getRealContext());

        VfSlot adSlot;
        if (advanceRewardVideo.isCsjExpress()) {
            //个性化模板广告需要传入期望广告view的宽、高，单位dp，
            adSlot = new VfSlot.Builder()
                    .setCodeId(sdkSupplier.adspotid)
                    .setSupportDeepLink(true)
                    .setAdCount(1)
                    //设置模板属性
                    .setExpressViewAcceptedSize(advanceRewardVideo.getCsjExpressWidth(), advanceRewardVideo.getCsjExpressHeight())
                    //必传参数，表来标识应用侧唯一用户；若非服务器回调模式或不需sdk透传
                    //可设置为空字符串
                    .setUserID(advanceRewardVideo.getUserId())
                    .setRewardAmount(advanceRewardVideo.getRewardCount())
                    .setRewardName(advanceRewardVideo.getRewardName())
                    .setOrientation(advanceRewardVideo.getOrientation())  //设置期望视频播放的方向，为TTVfConstant.HORIZONTAL或TTVfConstant.VERTICAL
                    .setMediaExtra(advanceRewardVideo.getExtraInfo()) //用户透传的信息，可不传
//                    .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                    .build();
        } else {
            //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
            adSlot = new VfSlot.Builder()
                    .setCodeId(sdkSupplier.adspotid)
                    .setSupportDeepLink(true)
                    .setAdCount(1)
                    .setImageAcceptedSize(advanceRewardVideo.getCsjImageAcceptedSizeWidth(), advanceRewardVideo.getCsjImageAcceptedSizeHeight())
                    //必传参数，表来标识应用侧唯一用户；若非服务器回调模式或不需sdk透传
                    //可设置为空字符串
                    .setUserID(advanceRewardVideo.getUserId())
                    .setRewardAmount(advanceRewardVideo.getRewardCount())
                    .setRewardName(advanceRewardVideo.getRewardName())
                    .setOrientation(advanceRewardVideo.getOrientation())  //设置期望视频播放的方向，为TTVfConstant.HORIZONTAL或TTVfConstant.VERTICAL
                    .setMediaExtra(advanceRewardVideo.getExtraInfo()) //用户透传的信息，可不传
//                    .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                    .build();
        }
        ttAdNative.loadRdVideoVr(adSlot, this);
    }

    @Override
    protected void adReady() {
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public void show() {
        try {
            ttRewardVideoAd.setRdVrInteractionListener(new TTRdVideoObject.RdVrInteractionListener() {
                @Override
                public void onShow() {
                    onAdItemShow();
                }

                @Override
                public void onVideoBarClick() {
                    onAdItemClick();
                }

                @Override
                public void onClose() {
                    onAdItemClose();
                }

                @Override
                public void onVideoComplete() {
                    onAdItemVideoComplete();
                }

                @Override
                public void onVideoError() {
                    onAdItemVideoError(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER, "onVideoError"));
                }

                @Override
                public void onRdVerify(boolean b, int i, String s, int i1, String s1) {
                    LogUtil.simple(TAG + " onRewardVerify");

//                onAdItemRewardVerify(b, i, s, i1, s1);
                }

                @Override
                public void onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo) {
                    LogUtil.simple(TAG + " onRewardArrived");
                    int mServerErrorCode = extraInfo.getInt(TTRdVideoObject.REWARD_EXTRA_KEY_ERROR_CODE);
                    String mServerErrorMsg = extraInfo.getString(TTRdVideoObject.REWARD_EXTRA_KEY_ERROR_MSG);
                    String mRewardName = extraInfo.getString(TTRdVideoObject.REWARD_EXTRA_KEY_REWARD_NAME);
                    int mRewardAmount = extraInfo.getInt(TTRdVideoObject.REWARD_EXTRA_KEY_REWARD_AMOUNT);
                    float mRewardPropose = extraInfo.getFloat(TTRdVideoObject.REWARD_EXTRA_KEY_REWARD_PROPOSE);
                    onAdItemRewardVerify(isRewardValid, rewardType, mRewardAmount, mRewardName, mServerErrorCode, mServerErrorMsg, mRewardPropose);
                }

                @Override
                public void onSkippedVideo() {
                    onAdItemVideoSkipped();
                }
            });
            ttRewardVideoAd.showRdVideoVr(activity);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

//    @Override
//    public boolean isValid() {
//        try {
//            if (ttRewardVideoAd == null) {
//                return false;
//            }
//            long expirationTime = ttRewardVideoAd.getExpirationTimestamp();
//            long currentTime = System.currentTimeMillis();
//            LogUtil.devDebug(TAG + "isReady check:expirationTime = " + expirationTime + ", currentTime = " + currentTime);
//            return expirationTime > currentTime;
//        } catch (Throwable e) {
//            e.printStackTrace();
//            return false;
//        }
//    }

    @Override
    public boolean isValid() {
        if (ttRewardVideoAd != null && ttRewardVideoAd.getMediationManager() != null) {
            return ttRewardVideoAd.getMediationManager().isReady();
        }
        return super.isValid();
    }
}
