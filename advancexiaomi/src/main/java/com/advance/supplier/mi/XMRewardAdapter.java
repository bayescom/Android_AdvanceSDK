package com.advance.supplier.mi;

import android.app.Activity;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.RewardVideoAd;

public class XMRewardAdapter extends AdvanceRewardCustomAdapter {
    RewardVideoAd rewardVideoAd;

    public XMRewardAdapter(Activity activity, RewardVideoSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        XMUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                loadAd();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {

        if (rewardVideoAd != null) {
            rewardVideoAd.destroy();
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            if (setting != null) {
                rewardVideoAd.setMute(setting.isMute());
            }
            rewardVideoAd.showAd(getRealActivity(null), new RewardVideoAd.RewardVideoInteractionListener() {

                @Override
                public void onAdPresent() {
                    // 广告被曝光
                    LogUtil.d(TAG+"onAdPresent");

                    handleShow();
                }

                @Override
                public void onAdClick() {
                    LogUtil.d(TAG+"onAdClick");
                    // 广告被点击
                    handleClick();
                }

                @Override
                public void onAdDismissed() {
                    //点击关闭按钮广告消失回调
                    LogUtil.d(TAG+"onAdDismissed");
                    handleClose();
                }

                @Override
                public void onAdFailed(String message) {
                    LogUtil.d(TAG+"onAdFailed");
                    // 渲染失败
                    handleFailed(AdvanceError.ERROR_EXCEPTION_SHOW, message);
                }

                @Override
                public void onVideoStart() {
                    LogUtil.d(TAG+"onVideoStart");
                    //视频开始播放
                }

                @Override
                public void onVideoPause() {
                    LogUtil.d(TAG+"onVideoPause");
                    //视频暂停
                }

                @Override
                public void onVideoSkip() {
                    LogUtil.d(TAG+"onVideoSkip");
                    //跳过视频播放
                }

                @Override
                public void onVideoComplete() {
                    LogUtil.d(TAG+"onVideoComplete");
                    // 视频播放完成
                    handleComplete();
                }

                @Override
                public void onPicAdEnd() {
                    LogUtil.d(TAG+"onPicAdEnd");
                    //图片类型广告播放完成
                    handleComplete();
                }

                @Override
                public void onReward() {
                    LogUtil.d(TAG+"onReward");
                    //激励回调

                    handleReward();

                    //回调服务端回调信息
                    RewardServerCallBackInf inf = new RewardServerCallBackInf();
                    inf.rewardVerify = true;
                    handleRewardInf(inf);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        rewardVideoAd = new RewardVideoAd();

        //(5.3.4新增接口) 请使用最新接口集成
        ADParams params = new ADParams.Builder().setUpId(getPosID()).build();
        rewardVideoAd.loadAd(params, new RewardVideoAd.RewardVideoLoadListener() {

            @Override
            public void onAdRequestSuccess() {
                //广告请求成功
                LogUtil.d(TAG+"onAdRequestSuccess");

                updateBidding(XMUtil.getPrice(rewardVideoAd.getMediaExtraInfo()));

                handleSucceed();
            }

            @Override
            public void onAdLoadSuccess() {
                //广告加载（缓存）成功，在需要的时候在此处展示广告
                LogUtil.d(TAG+"onAdLoadSuccess");
                handleCached();
            }

            @Override
            public void onAdLoadFailed(int errorCode, String errorMsg) {
                //广告加载失败
                LogUtil.d(TAG+"onAdLoadFailed");
                handleFailed(errorCode, errorMsg);
            }

        });
    }
}
