package com.advance.supplier.oppo;

import android.app.Activity;

import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.heytap.msp.mobad.api.ad.RewardVideoAd;
import com.heytap.msp.mobad.api.listener.IRewardVideoAdListener;
import com.heytap.msp.mobad.api.params.RewardVideoAdParams;

public class OppoRewardAdapter extends AdvanceRewardCustomAdapter {
    private final String TAG = "[OppoRewardAdapter] ";
    RewardVideoAd mRewardVideoAd;

    public OppoRewardAdapter(Activity activity, RewardVideoSetting advanceRewardVideo) {
        super(activity, advanceRewardVideo);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        OppoUtil.initAD(this);
        loadAd();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (mRewardVideoAd != null)
                mRewardVideoAd.destroyAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        try {
            if (mRewardVideoAd != null) {
                return mRewardVideoAd.isReady();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.isValid();
    }

    @Override
    public void show() {
        try {
            if (mRewardVideoAd != null)
                mRewardVideoAd.showAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void loadAd() {
        try {
            mRewardVideoAd = new RewardVideoAd(getRealContext(), sdkSupplier.adspotid, new IRewardVideoAdListener() {
                @Override
                public void onAdSuccess() {
                    LogUtil.simple(TAG + "onAdSuccess ");

                    updateBidding(mRewardVideoAd.getECPM());

                    handleSucceed();
                }

                @Override
                public void onAdFailed(String s) {
                    //                已废弃，

                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);

                }

                @Override
                public void onAdClick(long currentPosition) {
                    LogUtil.simple(TAG + "onAdClick  ，currentPosition = " + currentPosition);

                    handleClick();
                }

                @Override
                public void onVideoPlayStart() {
                    LogUtil.simple(TAG + " onVideoPlayStart");

                    handleShow();

                }

                @Override
                public void onVideoPlayComplete() {
                    LogUtil.simple(TAG + "onVideoPlayComplete");

                    if (null != setting) {
                        setting.adapterVideoComplete();
                    }
                }

                @Override
                public void onVideoPlayError(String msg) {
                    LogUtil.simple(TAG + " onVideoPlayError ,  msg = " + msg);

                    handleFailed(AdvanceError.ERROR_VIDEO_RENDER_ERR, msg);
                }

                @Override
                public void onVideoPlayClose(long currentPosition) {
//                    当视频播放过程中被关闭时回调
                    LogUtil.simple(TAG + "onVideoPlayClose  ,currentPosition =" + currentPosition);

                    if (null != setting) {
                        setting.adapterAdClose();
                    }
                }

                @Override
                public void onLandingPageOpen() {
//                    当视频播放完毕落地页打开时回调
                    LogUtil.simple(TAG + " onLandingPageOpen");

                }

                @Override
                public void onLandingPageClose() {
//                    当视频落地页关闭时回调
                    LogUtil.simple(TAG + "onLandingPageClose ");

                    if (null != setting) {
                        setting.adapterAdClose();
                    }
                }

                @Override
                public void onReward(Object... objects) {
                    LogUtil.simple(TAG + " onReward");

                    if (null != setting) {
                        setting.adapterAdReward();
                    }
                }
            });

            RewardVideoAdParams rewardVideoAdParams = new RewardVideoAdParams.Builder()
                    .setFetchTimeout(sdkSupplier.timeout)
                    .build();
            mRewardVideoAd.loadAd(rewardVideoAdParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
