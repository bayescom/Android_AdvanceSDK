package com.advance.supplier.baidu;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import android.app.Activity;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.RewardVideoAd;


public class BDRewardAdapter extends AdvanceRewardCustomAdapter implements RewardVideoAd.RewardVideoAdListener {
    private RewardVideoAd mRewardVideoAd;

    private final String TAG = "[BDRewardAdapter] ";

    public BDRewardAdapter(Activity activity, RewardVideoSetting setting) {
        super(activity, setting);
    }


    @Override
    protected void paraLoadAd() {
        BDUtil.initBDAccount(this);

        mRewardVideoAd = new RewardVideoAd(getRealContext(), sdkSupplier.adspotid, this, AdvanceBDManager.getInstance().rewardUseSurfaceView);
        //服务端校验透传参数
        if (setting != null) {
            mRewardVideoAd.setUserId(setting.getUserId());
            mRewardVideoAd.setExtraInfo(setting.getExtraInfo());
        }
        //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
        int bidFloor = AdvanceBDManager.getInstance().rewardBidFloor;
        if (bidFloor > 0) {
            mRewardVideoAd.setBidFloor(bidFloor);
        }
        mRewardVideoAd.setDownloadAppConfirmPolicy(AdvanceBDManager.getInstance().rewardDownloadAppConfirmPolicy);
        mRewardVideoAd.load();

        rewardVideoItem = new BDRewardItem(this, mRewardVideoAd);
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
        }
    }

//    @Override
//    public boolean isValid() {
//        if (mRewardVideoAd != null) {
//            return mRewardVideoAd.isReady();
//        }
//        return super.isValid();
//    }


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
        if (null != setting) {
            setting.adapterAdClose();
        }

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
        if (null != setting) {
            setting.adapterVideoComplete();
        }
    }

    @Override
    public void onAdSkip(float playScale) {
        // 用户点击跳过, 展示尾帧
        // 建议：媒体可以按照自己的设计给予奖励
        LogUtil.simple(TAG + " onSkip: playScale = " + playScale);
        if (null != setting) {
            setting.adapterVideoSkipped();
        }
    }

    @Override
    public void onRewardVerify(boolean rewardVerify) {
        try {
            LogUtil.simple(TAG + " onRewardVerify : rewardVerify = " + rewardVerify);

            RewardServerCallBackInf inf = new RewardServerCallBackInf();
            inf.rewardVerify = rewardVerify;
            if (null != setting) {
                if (rewardVerify) {
                    //激励达成回调
                    setting.adapterAdReward();
                }

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
    public void onAdLoaded() {
        LogUtil.simple(TAG + "onAdLoaded");
        try { //避免方法有异常，catch一下，不影响success逻辑
            if (mRewardVideoAd != null) {
                updateBidding(BDUtil.getEcpmValue(mRewardVideoAd.getECPMLevel()));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        handleSucceed();
    }

    @Override
    public void show() {
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
