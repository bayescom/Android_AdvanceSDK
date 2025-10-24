package com.advance.supplier.honor;

import android.app.Activity;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.hihonor.adsdk.base.AdSlot;
import com.hihonor.adsdk.base.api.reward.RewardAdLoadListener;
import com.hihonor.adsdk.base.api.reward.RewardExpressAd;
import com.hihonor.adsdk.base.api.reward.RewardItem;
import com.hihonor.adsdk.base.callback.AdListener;
import com.hihonor.adsdk.reward.RewardAdLoad;

public class HonorRewardAdapter extends AdvanceRewardCustomAdapter {
    RewardExpressAd mRewardExpressAd;

    public HonorRewardAdapter(Activity activity, RewardVideoSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        loadAd();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (mRewardExpressAd != null) {
            mRewardExpressAd.release();
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public boolean isValid() {
        if (HonorUtil.isAdExpire(mRewardExpressAd)) {
            return false;
        }
        return super.isValid();
    }

    @Override
    public void show() {
        try {
            if (mRewardExpressAd != null) {
                /**
                 * 广告事件监听器
                 */
                mRewardExpressAd.setAdListener(new AdListener() {
                    /**
                     * 开屏广告点击跳过或倒计时结束时回调
                     *
                     * @param type 0：点击跳过、1：倒计时结束
                     */
                    @Override
                    public void onAdSkip(int type) {
                        LogUtil.simple(TAG + "onAdSkip, type: " + type);

                    }

                    /**
                     * 广告曝光时回调
                     */
                    @Override
                    public void onAdImpression() {
                        LogUtil.simple(TAG + "onAdImpression...");

                        handleShow();
                    }

                    /**
                     * 广告曝光失败时回调
                     *
                     * @param errCode 错误码
                     * @param msg 曝光失败信息
                     */
                    @Override
                    public void onAdImpressionFailed(int errCode, String msg) {
                        super.onAdImpressionFailed(errCode, msg);
                        LogUtil.simple(TAG + "onAdImpressionFailed, errCode: " + errCode + ", msg: " + msg);

                        handleFailed(errCode, msg);
                    }

                    /**
                     * 广告被点击时回调
                     */
                    @Override
                    public void onAdClicked() {
                        LogUtil.simple(TAG + "onAdClicked...");

                        handleClick();
                    }

                    /**
                     * 广告成功跳转小程序时回调
                     */
                    @Override
                    public void onMiniAppStarted() {
                        LogUtil.simple(TAG + "onMiniAppStarted...");

                    }
                });
// 必须调用show方法显示广告，否则广告无法显示
                mRewardExpressAd.show(getRealActivity(null), new RewardExpressAd.RewardAdStatusListener() {

                    /**
                     * 激励广告关闭
                     */
                    @Override
                    public void onRewardAdClosed(boolean isVideoEnd) {
                        LogUtil.simple(TAG + "onRewardAdClosed");

                        handleClose();
                    }

                    /**
                     * 激励广告曝光失败
                     *
                     * @param errorCode 曝光失败错误码
                     */
                    @Override
                    public void onVideoError(int errorCode) {
                        LogUtil.simple(TAG + "onVideoError, errorCode: " + errorCode);


//                        handleFailed(errorCode,"onVideoError");
                    }

                    /**
                     * 激励广告打开时
                     */
                    @Override
                    public void onRewardAdOpened() {
                        LogUtil.simple(TAG + "onRewardAdOpened");
                    }

                    /**
                     * 获得奖励
                     *
                     * @param rewardItem 激励奖励类
                     */
                    @Override
                    public void onRewarded(RewardItem rewardItem) {
                        LogUtil.simple(TAG + "onRewarded , rewardItem = " + rewardItem);


                        RewardServerCallBackInf inf = new RewardServerCallBackInf();
                        if (rewardItem != null) {
                            inf.rewardVerify = true;
                            inf.rewardAmount = (int) rewardItem.getAmount();
                            inf.rewardName = rewardItem.getType();
                        }
                        handleRewardInf(inf);

                        handleReward();

                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        HonorUtil.initAD(this);

        // 创建广告请求参数对象（AdSlot）
        AdSlot adSlot = new AdSlot.Builder()
                .setSlotId(sdkSupplier.adspotid) // 必传,设置您的广告位ID。
//                .setLoadType(-1) // 非必传，设置广告请求方式  -1：默认请求方式，不进行缓存  0：普通请求，优先去读缓存  1：预缓存请求，数据保存至缓存
                .build();

        // 构建广告加载器，传入已创建好的广告请求参数对象与广告加载状态监听器。
        RewardAdLoad load = new RewardAdLoad.Builder()
                .setRewardAdLoadListener(new RewardAdLoadListener() {
                    @Override
                    public void onLoadSuccess(RewardExpressAd rewardExpressAd) {
                        LogUtil.d(TAG + "onLoadSuccess");

                        mRewardExpressAd = rewardExpressAd;

                        updateBidding(HonorUtil.getECPM(mRewardExpressAd));

                        handleSucceed();
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        LogUtil.d(TAG + "onFailed");

                        handleFailed(code, errorMsg);
                    }
                }) // 必传，注册广告加载状态监听器。
                .setAdSlot(adSlot) // 必传，设置广告请求参数。
                .build();
// 加载广告
        load.loadAd();
    }
}
