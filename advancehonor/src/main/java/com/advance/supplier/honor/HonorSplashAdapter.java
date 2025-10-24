package com.advance.supplier.honor;

import android.app.Activity;
import android.view.View;

import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.hihonor.adsdk.base.AdSlot;
import com.hihonor.adsdk.base.api.splash.SplashAdLoadListener;
import com.hihonor.adsdk.base.api.splash.SplashExpressAd;
import com.hihonor.adsdk.base.callback.AdListener;
import com.hihonor.adsdk.splash.SplashAdLoad;

import java.lang.ref.SoftReference;

public class HonorSplashAdapter extends AdvanceSplashCustomAdapter {
    SplashExpressAd mSplashExpressAd;
    SplashAdLoad splashAdLoad;

    public HonorSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting splashSetting) {
        super(softReferenceActivity, splashSetting);
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
        if (mSplashExpressAd != null) {
            mSplashExpressAd.release();
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public boolean isValid() {
        if (HonorUtil.isAdExpire(mSplashExpressAd)) {
            return false;
        }
        return super.isValid();
    }

    @Override
    public void show() {
        try {
            if (mSplashExpressAd != null) {

                /**
                 * 广告事件监听器
                 */
                mSplashExpressAd.setAdListener(new AdListener() {
                    /**
                     * 开屏广告点击跳过或倒计时结束时回调
                     *
                     * @param type 0：点击跳过、1：倒计时结束
                     */
                    @Override
                    public void onAdSkip(int type) {
                        LogUtil.simple(TAG + "onAdSkip, type: " + type);
                        // 可以跳转您的启动页或首页
                        if (splashSetting != null) {
                            if (type == 0) {
                                splashSetting.adapterDidSkip();
                            } else {
                                splashSetting.adapterDidTimeOver();
                            }
                        }

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
                View view = mSplashExpressAd.getExpressAdView();
                //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
                boolean add = AdvanceUtil.addADView(splashSetting.getAdContainer(), view);
                if (!add) {
                    runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
                }
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
        SplashAdLoad load = new SplashAdLoad.Builder()
                .setSplashAdLoadListener(new SplashAdLoadListener() {
                    @Override
                    public void onLoadSuccess(SplashExpressAd splashExpressAd) {
                        LogUtil.d(TAG + "onLoadSuccess");

                        mSplashExpressAd = splashExpressAd;

                        updateBidding(HonorUtil.getECPM(mSplashExpressAd));

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
