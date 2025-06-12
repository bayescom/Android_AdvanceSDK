package com.advance.supplier.mry;

import android.app.Activity;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.mercury.sdk.core.rewardvideo.MercuryRewardOptions;
import com.mercury.sdk.core.rewardvideo.MercuryRewardResult;
import com.mercury.sdk.core.rewardvideo.RewardVideoAD;
import com.mercury.sdk.core.rewardvideo.RewardVideoADListener;
import com.mercury.sdk.util.ADError;

public class MercuryRewardVideoAdapter extends AdvanceRewardCustomAdapter implements RewardVideoADListener {
    private RewardVideoSetting advanceRewardVideo;
    String TAG = "[MercuryRewardVideoAdapter] ";
    RewardVideoAD rewardVideoAD;

    public MercuryRewardVideoAdapter(Activity activity, RewardVideoSetting advanceRewardVideo) {
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
    public void onADLoad() {
        LogUtil.simple(TAG + "onADLoad");


        //旧版本SDK中不包含价格返回方法，catch住
        try {
            int cpm = rewardVideoAD.getEcpm();
            updateBidding(cpm);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        handleSucceed();


    }

    @Override
    public void onVideoCached() {
        LogUtil.simple(TAG + "onVideoCached");

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

    @Override
    public void onADShow() {
        LogUtil.simple(TAG + "onADShow");


    }

    @Override
    public void onADExposure() {
        LogUtil.simple(TAG + "onADExposure");

        handleShow();
    }

    @Override
    public void onADClicked() {
        LogUtil.simple(TAG + "onADClicked");

        handleClick();
    }

    @Override
    public void onReward() {
        LogUtil.simple(TAG + "onReward");

        try {
            MercuryRewardResult result = null;
            if (rewardVideoAD != null) {
                result = rewardVideoAD.getRewardResult();
            }
            String msg = "";
            //建议根据返回结果，来处理奖励发放逻辑
            if (result != null && !result.isRewardValid) {
                //可能是奖励验证超时或者服务端校验奖励不通过等原因
                msg = "奖励发放异常, errCode = " + result.errCode + " , errMsg = " + result.errMsg;
            } else {
                msg = "奖励正常发放";
            }

            RewardServerCallBackInf inf = new RewardServerCallBackInf();
            if (result != null) {
                inf.rewardVerify = result.isRewardValid;
                inf.rewardAmount = result.rewardAmount;
                inf.rewardName = result.rewardName;
                inf.errorCode = result.errCode;
                inf.errMsg = result.errMsg;
            }

            if (null != advanceRewardVideo) {
                if (sdkSupplier != null) {
                    inf.supId = sdkSupplier.id;
                }
                advanceRewardVideo.postRewardServerInf(inf);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (null != advanceRewardVideo) {
            advanceRewardVideo.adapterAdReward();
        }

    }


    @Override
    public void onVideoComplete() {
        LogUtil.simple(TAG + "onVideoComplete");

        if (null != advanceRewardVideo) {
            advanceRewardVideo.adapterVideoComplete();
        }

    }

    @Override
    public void onADClose() {
        LogUtil.simple(TAG + "onADClose");

        if (null != advanceRewardVideo) {
            advanceRewardVideo.adapterAdClose();
        }

    }


    @Override
    public void onNoAD(ADError adError) {

        int code = -1;
        String msg = "default onNoAD";
        if (adError != null) {
            code = adError.code;
            msg = adError.msg;
        }
        LogUtil.simple(TAG + "onNoAD");
        handleFailed(code, msg);
    }

    @Override
    protected void paraLoadAd() {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);
        rewardVideoAD = new RewardVideoAD(getRealContext(), sdkSupplier.adspotid, this);
        // (可选) 激励相关参数配置
        rewardVideoAD.setRewardOptions(new MercuryRewardOptions.Builder()
                .setUserID(advanceRewardVideo.getUserId()) //用户唯一id，服务端验证时必传
                .setRewardName(advanceRewardVideo.getRewardName()) // 发放奖励名称
                .setRewardAmount(advanceRewardVideo.getRewardCount()) // 发放奖励数量
                .setExtCustomInf(advanceRewardVideo.getExtraInfo()) // 额外自定义信息
                .build());
        MercuryRewardVideoAdItem mercuryRewardVideoAdItem = new MercuryRewardVideoAdItem(this, rewardVideoAD);
        mercuryRewardVideoAdItem.loadAD();
        rewardVideoItem = mercuryRewardVideoAdItem;
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
            rewardVideoAD.showAD(setting.getShowActivity());
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        if (rewardVideoAD != null) {
            return rewardVideoAD.isValid();
        }
        return super.isValid();
    }

//    @Override
//    public boolean isValid() {
//        if (rewardVideoAD == null) {
//            return false;
//        }
//        return rewardVideoAD.isValid();
//    }
}
