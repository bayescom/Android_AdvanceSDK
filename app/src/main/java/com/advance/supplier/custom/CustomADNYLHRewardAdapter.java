package com.advance.supplier.custom;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;
import static com.advance.model.AdvanceError.ERROR_EXCEPTION_SHOW;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class CustomADNYLHRewardAdapter extends AdvanceRewardCustomAdapter implements RewardVideoADListener {

    public RewardVideoAD rewardVideoAD;
    String TAG = "[CustomADNYLHRewardAdapter] ";



    private void rewardLoaded() {
        try {
            LogUtil.simple(TAG + "rewardLoaded");
            double ecpm = 0;

            if (rewardVideoAD != null) {
                ecpm = (rewardVideoAD.getECPM());
            }
            handleSucceed(ecpm);

        } catch (Throwable e) {
            e.printStackTrace();
            error(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
        }
    }

    private void rewardCached() {
        try {
            LogUtil.simple(TAG + "rewardCached");

            handleCached();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rewardShow() {
        LogUtil.simple(TAG + "rewardShow");

    }

    private void rewardExpose() {
        try {
            LogUtil.simple(TAG + "rewardExpose");

            handleShow();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rewardReward(Map<String, Object> map) {
        try {
            LogUtil.simple(TAG + "rewardReward");

            handleReward();
                RewardServerCallBackInf inf = new RewardServerCallBackInf();
                inf.rewardMap = map;
                inf.rewardVerify = true;
                if (sdkSupplier != null) {
                    inf.supId = sdkSupplier.id;
                }

                handleRewardInf(inf);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rewardClick() {
        try {
            LogUtil.simple(TAG + "rewardClick");

            handleClick();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rewardComplete() {
        try {
            LogUtil.simple(TAG + "rewardComplete");

            handleComplete();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rewardClose() {
        try {
            LogUtil.simple(TAG + "rewardClose");

           handleClose();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rewardError(AdError adError) {
        try {
            int code = -1;
            String msg = "default onNoAD";
            if (adError != null) {
                code = adError.getErrorCode();
                msg = adError.getErrorMsg();
            }
            LogUtil.simple(TAG + "rewardError");
            handleFailed(code, msg);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onADLoad() {
        rewardLoaded();
    }

    @Override
    public void onVideoCached() {
        rewardCached();
    }

    @Override
    public void onADShow() {
        rewardShow();
    }

    @Override
    public void onADExpose() {
        rewardExpose();
    }

    @Override
    public void onReward(Map<String, Object> map) {
        rewardReward(map);
    }

    @Override
    public void onADClick() {
        rewardClick();
    }

    @Override
    public void onVideoComplete() {
        rewardComplete();
    }

    @Override
    public void onADClose() {
        rewardClose();
    }

    @Override
    public void onError(AdError adError) {
        rewardError(adError);
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        boolean vo = false;
        String userId = "";
        String extraInfo = "";
        if (rewardSetting != null) {

            vo = rewardSetting.isGdtVolumeOn() || !rewardSetting.isMute();
            userId = rewardSetting.getUserId();
            extraInfo = rewardSetting.getExtraInfo();
        }
        rewardVideoAD = new RewardVideoAD(getRealContext(), sdkSupplier.adspotid, this, vo);
        if (!TextUtils.isEmpty(userId) || !TextUtils.isEmpty(extraInfo)) {
            rewardVideoAD.setServerSideVerificationOptions(new ServerSideVerificationOptions.Builder().setUserId(userId).setCustomData(extraInfo).build());
        }
        rewardVideoAD.loadAD();
    }


    @Override
    protected void adPrepared() {
    }

    @Override
    public void destroyAd() {

    }


    public boolean checkRewardOk() {
        try {
            return rewardVideoAD.isValid();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    public void error(AdvanceError advanceError) {
        runParaFailed(advanceError);
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (checkRewardOk()) {
                rewardVideoAD.showAD();
            } else {
                error(AdvanceError.parseErr(ERROR_EXCEPTION_SHOW, "RewardNotVis"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            error(AdvanceError.parseErr(ERROR_EXCEPTION_SHOW));
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
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {

    }

}
