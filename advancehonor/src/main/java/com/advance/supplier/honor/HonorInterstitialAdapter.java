package com.advance.supplier.honor;

import android.app.Activity;
import android.content.Context;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.hihonor.adsdk.base.AdSlot;
import com.hihonor.adsdk.base.api.interstitial.InterstitialAdLoadListener;
import com.hihonor.adsdk.base.api.interstitial.InterstitialExpressAd;
import com.hihonor.adsdk.base.callback.AdListener;
import com.hihonor.adsdk.interstitial.InterstitialAdLoad;

import java.util.Map;

public class HonorInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    InterstitialExpressAd mInterstitialExpressAd;

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (mInterstitialExpressAd != null) {
            mInterstitialExpressAd.release();
        }
    }


    @Override
    public boolean isValid() {
        if (HonorUtil.isAdExpire(mInterstitialExpressAd)) {
            return false;
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        HonorUtil.sendBidResult(mInterstitialExpressAd, isWin, winPrice, referBidInfo);

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (mInterstitialExpressAd != null) {
                /**
                 * 广告事件监听器
                 */
                mInterstitialExpressAd.setAdListener(new AdListener() {
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
                     * 广告关闭时回调
                     */
                    @Override
                    public void onAdClosed() {
                        LogUtil.simple(TAG + "onAdClosed...");
                        handleClose();
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

                mInterstitialExpressAd.show(getRealActivity(null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        // 创建广告请求参数对象（AdSlot）
        AdSlot adSlot = new AdSlot.Builder()
                .setSlotId(sdkSupplier.adspotid) // 必传,设置您的广告位ID。
//                .setLoadType(-1) // 非必传，设置广告请求方式  -1：默认请求方式，不进行缓存  0：普通请求，优先去读缓存  1：预缓存请求，数据保存至缓存
                .build();

        // 构建广告加载器，传入已创建好的广告请求参数对象与广告加载状态监听器。
        InterstitialAdLoad load = new InterstitialAdLoad.Builder()
                .setInterstitialAdLoadListener(new InterstitialAdLoadListener() {
                    @Override
                    public void onAdLoaded(InterstitialExpressAd interstitialAD) {
                        LogUtil.d(TAG + "onLoadSuccess");

                        mInterstitialExpressAd = interstitialAD;

                        handleSucceed(HonorUtil.getECPM(mInterstitialExpressAd));
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
