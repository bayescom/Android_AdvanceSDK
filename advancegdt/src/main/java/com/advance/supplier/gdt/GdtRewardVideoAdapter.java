package com.advance.supplier.gdt;

import android.app.Activity;
import android.text.TextUtils;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.util.AdError;

import java.util.Map;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;
import static com.advance.model.AdvanceError.ERROR_EXCEPTION_SHOW;

public class GdtRewardVideoAdapter extends AdvanceRewardCustomAdapter implements RewardVideoADListener {

    private RewardVideoSetting advanceRewardVideo;
    public RewardVideoAD rewardVideoAD;
    String TAG = "[GdtRewardVideoAdapter] ";

    public GdtRewardVideoAdapter(Activity activity, RewardVideoSetting advanceRewardVideo) {
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

    private void rewardLoaded() {
        try {
            LogUtil.simple(TAG + "rewardLoaded");
            if (rewardVideoAD != null) {
                updateBidding(rewardVideoAD.getECPM());
            }
            handleSucceed();

        } catch (Throwable e) {
            e.printStackTrace();
            error(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
        }
    }

    private void rewardCached() {
        try {
            LogUtil.simple(TAG + "rewardCached");

            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onCached();
                }
            } else {
                if (null != advanceRewardVideo) {
                    advanceRewardVideo.adapterVideoCached();
                }
            }
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

            if (advanceRewardVideo != null) {
                advanceRewardVideo.adapterAdReward();

                RewardServerCallBackInf inf = new RewardServerCallBackInf();
                inf.rewardMap = map;
                inf.rewardVerify = true;
                if (sdkSupplier != null) {
                    inf.supId = sdkSupplier.id;
                }
                advanceRewardVideo.postRewardServerInf(inf);
            }
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

            if (advanceRewardVideo != null)
                advanceRewardVideo.adapterVideoComplete();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void rewardClose() {
        try {
            LogUtil.simple(TAG + "rewardClose");

            if (advanceRewardVideo != null)
                advanceRewardVideo.adapterAdClose();
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

    @Override
    public void paraLoadAd() {
        GdtUtil.initAD(this);

        boolean vo = false;
        String userId = "";
        String extraInfo = "";
        if (advanceRewardVideo != null) {

            vo = advanceRewardVideo.isGdtVolumeOn() || !advanceRewardVideo.isMute();
            userId = advanceRewardVideo.getUserId();
            extraInfo = advanceRewardVideo.getExtraInfo();
        }
        rewardVideoItem = new GdtRewardVideoAdItem(this);
        rewardVideoAD = new RewardVideoAD(getRealContext(), sdkSupplier.adspotid, this, vo);
        if (!TextUtils.isEmpty(userId) || !TextUtils.isEmpty(extraInfo)) {
            rewardVideoAD.setServerSideVerificationOptions(new ServerSideVerificationOptions.Builder().setUserId(userId).setCustomData(extraInfo).build());
        }
        rewardVideoAD.loadAD();
//        LogUtil.devDebug(TAG + " , sdk = " + sdkSupplier + " reqid:" + advanceRewardVideo.getAdvanceId());
    }


    @Override
    protected void adReady() {
//        if (advanceRewardVideo != null)
//            if (checkRewardOk()) {
//                advanceRewardVideo.adapterAdDidLoaded(gdtRewardVideoAdItem, sdkSupplier);
//            } else {
//                runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
//            }
    }

    @Override
    public void doDestroy() {

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

    @Override
    public void show() {
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
        return super.isValid();
    }
//
//    @Override
//    public boolean isValid() {
//        try {
//            if (rewardVideoAD == null) {
//                return false;
//            }
//            return rewardVideoAD.isValid();
//        } catch (Throwable e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
}
