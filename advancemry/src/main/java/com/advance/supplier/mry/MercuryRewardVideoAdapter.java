package com.advance.supplier.mry;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.mercury.sdk.core.rewardvideo.MercuryRewardOptions;
import com.mercury.sdk.core.rewardvideo.MercuryRewardResult;
import com.mercury.sdk.core.rewardvideo.RewardVideoAD;
import com.mercury.sdk.core.rewardvideo.RewardVideoADListener;
import com.mercury.sdk.util.ADError;

import java.util.Map;

public class MercuryRewardVideoAdapter extends AdvanceRewardCustomAdapter implements RewardVideoADListener {
    String TAG = "[MercuryRewardVideoAdapter] ";
    RewardVideoAD rewardVideoAD;


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

        handleSucceed(this);


    }

    @Override
    public void onVideoCached() {
        LogUtil.simple(TAG + "onVideoCached");

        handleCached();

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


            handleRewardInf(inf);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        handleReward();


    }


    @Override
    public void onVideoComplete() {
        LogUtil.simple(TAG + "onVideoComplete");


        handleComplete();
    }

    @Override
    public void onADClose() {
        LogUtil.simple(TAG + "onADClose");


        handleClose();

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

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);

        //检查是否命中使用缓存逻辑
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, MercuryRewardVideoAdapter.class, new BYAbsCallBack<MercuryRewardVideoAdapter>() {
            @Override
            public void invoke(MercuryRewardVideoAdapter cacheAdapter) {

                //更新缓存广告得价格
                updateBidding(cacheAdapter.rewardVideoAD.getEcpm());
            }
        });
        if (hitCache) {
            return;
        }

        rewardVideoAD = new RewardVideoAD(getRealContext(), sdkSupplier.adspotid, this);
        // (可选) 激励相关参数配置
        rewardVideoAD.setRewardOptions(new MercuryRewardOptions.Builder()
                .setUserID(rewardSetting.getUserId()) //用户唯一id，服务端验证时必传
                .setRewardName(rewardSetting.getRewardName()) // 发放奖励名称
                .setRewardAmount(rewardSetting.getRewardCount()) // 发放奖励数量
                .setExtCustomInf(rewardSetting.getExtraInfo()) // 额外自定义信息
                .build());
        rewardVideoAD.loadAD();
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            rewardVideoAD.showAD(activity);
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
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }

//    @Override
//    public boolean isValid() {
//        if (rewardVideoAD == null) {
//            return false;
//        }
//        return rewardVideoAD.isValid();
//    }
}
