package com.advance.supplier.csj;

import android.app.Activity;
import android.os.Bundle;

import com.advance.AdvanceConfig;
import com.advance.AdvanceRewardVideoItem;
import com.advance.model.AdvanceError;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bykv.vk.openvk.TTAppDownloadListener;
import com.bykv.vk.openvk.TTRdVideoObject;

@Deprecated
public class CsjRewardVideoAdItem implements AdvanceRewardVideoItem {
    private CsjRewardVideoAdapter csjRewardVideoAdapter;
    private TTRdVideoObject ttRewardVideoAd;
    private Activity activity;
    private TTRdVideoObject.RdVrInteractionListener rewardAdInteractionListener;

    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_CSJ;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_CSJ;
    }

    @Override
    public void showAd() {
        showRewardVideoAd();
    }

    public CsjRewardVideoAdItem(Activity activity, CsjRewardVideoAdapter csjRewardVideoAdapter, TTRdVideoObject ttRewardVideoAd) {
        this.csjRewardVideoAdapter = csjRewardVideoAdapter;
        this.ttRewardVideoAd = ttRewardVideoAd;
        this.activity = activity;
    }

    public void setRewardAdInteractionListener(final TTRdVideoObject.RdVrInteractionListener rewardAdInteractionListener) {
        try {
            this.rewardAdInteractionListener = rewardAdInteractionListener;
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void setDownloadListener(TTAppDownloadListener appDownloadListener) {
        try {

            ttRewardVideoAd.setDownloadListener(appDownloadListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public int getInteractionType() {
        try {
            return ttRewardVideoAd.getInteractionType();
        } catch (Throwable e) {
            e.printStackTrace();
            return 0;
        }

    }

    public void showRewardVideoAd() {
        try {
            if (ttRewardVideoAd == null) {
                if (null != csjRewardVideoAdapter) {
                    csjRewardVideoAdapter.onAdItemVideoError(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, "ttRewardVideoAd null"));
                }
                return;
            }
            ttRewardVideoAd.setRdVrInteractionListener(new TTRdVideoObject.RdVrInteractionListener() {
                @Override
                public void onShow() {
                    if (null != csjRewardVideoAdapter) {
                        csjRewardVideoAdapter.onAdItemShow();
                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onShow();
                    }

                }

                @Override
                public void onVideoBarClick() {
                    if (null != csjRewardVideoAdapter) {
                        csjRewardVideoAdapter.onAdItemClick();
                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onVideoBarClick();
                    }


                }

                @Override
                public void onClose() {
                    if (null != csjRewardVideoAdapter) {
                        csjRewardVideoAdapter.onAdItemClose();
                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onClose();
                    }

                }

                @Override
                public void onVideoComplete() {
                    if (null != csjRewardVideoAdapter) {
                        csjRewardVideoAdapter.onAdItemVideoComplete();
                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onVideoComplete();
                    }


                }

                @Override
                public void onVideoError() {
                    if (null != csjRewardVideoAdapter) {
                        csjRewardVideoAdapter.onAdItemVideoError(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER, "onVideoError"));
                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onVideoError();
                    }

                }

                @Override
                public void onRdVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errMsg) {

//                    String msg = "verify:" + rewardVerify + " amount:" + rewardAmount + " name:" + rewardName + " errorCode:" + errorCode + " errMsg:" + errMsg;
//                    LogUtil.simple(msg);
//                    if (null != csjRewardVideoAdapter) {
//                        csjRewardVideoAdapter.onAdItemRewardVerify(rewardVerify, rewardAmount, rewardName, errorCode, errMsg);
//                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onRdVerify(rewardVerify, rewardAmount, rewardName, errorCode, errMsg);
                    }

                }

                @Override
                public void onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo) {
                    if (null != csjRewardVideoAdapter) {
                        int mServerErrorCode = extraInfo.getInt(TTRdVideoObject.REWARD_EXTRA_KEY_ERROR_CODE);
                        String mServerErrorMsg = extraInfo.getString(TTRdVideoObject.REWARD_EXTRA_KEY_ERROR_MSG);
                        String mRewardName = extraInfo.getString(TTRdVideoObject.REWARD_EXTRA_KEY_REWARD_NAME);
                        int mRewardAmount = extraInfo.getInt(TTRdVideoObject.REWARD_EXTRA_KEY_REWARD_AMOUNT);
                        float mRewardPropose = extraInfo.getFloat(TTRdVideoObject.REWARD_EXTRA_KEY_REWARD_PROPOSE);
                        csjRewardVideoAdapter.onAdItemRewardVerify(isRewardValid, rewardType, mRewardAmount, mRewardName, mServerErrorCode, mServerErrorMsg, mRewardPropose);
                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onRewardArrived(isRewardValid, rewardType, extraInfo);
                    }
                }

//                @Override
//                public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName) {
//
//                    String msg = "verify:" + rewardVerify + " amount:" + rewardAmount + " name:" + rewardName;
//                    LogUtil.AdvanceLog(msg);
//                    if (null != csjRewardVideoAdapter) {
//                        csjRewardVideoAdapter.onAdItemRewardVerify(rewardVerify, rewardAmount, rewardName, 0, "");
//                    }
//                    if (null != rewardAdInteractionListener) {
//                        rewardAdInteractionListener.onRewardVerify(rewardVerify, rewardAmount, rewardName);
//                    }
//
//                }

                @Override
                public void onSkippedVideo() {
                    if (null != csjRewardVideoAdapter) {
                        csjRewardVideoAdapter.onAdItemVideoSkipped();
                    }
                    if (null != rewardAdInteractionListener) {
                        rewardAdInteractionListener.onSkippedVideo();
                    }


                }
            });

            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    ttRewardVideoAd.showRdVideoVr(csjRewardVideoAdapter.setting.getShowActivity());
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (csjRewardVideoAdapter != null)
                    csjRewardVideoAdapter.onAdFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setShowDownLoadBar(boolean isShow) {
        try {

//            ttRewardVideoAd.setShowDownLoadBar(isShow);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
