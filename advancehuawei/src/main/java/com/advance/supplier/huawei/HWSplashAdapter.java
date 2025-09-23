package com.advance.supplier.huawei;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;

import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.AudioFocusType;
import com.huawei.hms.ads.splash.SplashAdDisplayListener;
import com.huawei.hms.ads.splash.SplashView;

import java.lang.ref.SoftReference;

public class HWSplashAdapter extends AdvanceSplashCustomAdapter {
    SplashView splashView;

    public HWSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting splashSetting) {
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
        try {
            if (splashView != null) {
                splashView.destroyView();
            }
        } catch (Exception e) {
        }

    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            splashView.setAdDisplayListener(new SplashAdDisplayListener() {
                @Override
                public void onAdShowed() {
                    // Call this method when an ad is displayed.
                    LogUtil.simple(TAG + "SplashAdDisplayListener onAdShowed.");
                    handleShow();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isCountingEnd = true;
                        }
                    }, 4800);
                }

                @Override
                public void onAdClick() {
                    // Call this method when an ad is clicked.
                    LogUtil.simple(TAG + "SplashAdDisplayListener onAdClick.");
                    handleClick();


                    //必须要点击后回调跳过，否则不会有结束回调，且要延迟一定时间，否则就会出现落地页先打开，接着进入首页逻辑，导致落地页无法栈顶展示。
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (splashSetting != null) {
                                splashSetting.adapterDidSkip();
                            }
                        }
                    }, 300);

                }
            });
//            设置宽高
//            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            boolean add = AdvanceUtil.addADView(splashSetting.getAdContainer(), splashView);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        //先执行SDK初始化
        HWUtil.initAD(this);

        AdParam.Builder adParam = AdvanceHWManager.getInstance().globalAdParamBuilder;
        if (adParam == null) {
            adParam = new AdParam.Builder();
        }

        SplashView.SplashAdLoadListener splashAdLoadListener = new SplashView.SplashAdLoadListener() {
            @Override
            public void onAdLoaded() {
                // 广告加载成功时调用
                LogUtil.simple(TAG + "SplashAdLoadListener onAdLoaded");

                if (splashView != null) {
                    updateBidding(HWUtil.getPrice(splashView.getBiddingInfo()));
                }

                handleSucceed();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // 广告加载失败时调用, 跳转至App主界面
                LogUtil.simple(TAG + "SplashAdLoadListener onAdFailedToLoad , errorCode = " + errorCode);

                handleFailed(errorCode, "hw onAdFailedToLoad");
            }

            @Override
            public void onAdDismissed() {
                // 广告展示完毕时调用, 跳转至App主界面
                LogUtil.simple(TAG + "SplashAdLoadListener onAdDismissed");


                if (splashSetting != null) {
                    if (isCountingEnd) {
                        splashSetting.adapterDidTimeOver();
                    } else {
                        splashSetting.adapterDidSkip();
                    }
                }
            }
        };
        String slotId = sdkSupplier.adspotid;
        // 锁定设备当前屏幕方向，自适应横竖屏方向
//        getRealActivity(null).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        int orientation = getScreenOrientation();
        // 获取SplashView
        splashView = new SplashView(getRealContext());
        // 设置视频类开屏广告的音频焦点类型
        splashView.setAudioFocusType(AudioFocusType.NOT_GAIN_AUDIO_FOCUS_WHEN_MUTE);
        // 加载广告
        splashView.load(slotId, orientation, adParam.build(), splashAdLoadListener);
    }

    private int getScreenOrientation() {
        try {
            Configuration config = getRealContext().getResources().getConfiguration();
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            } else {
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            }
        } catch (Throwable e) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }
}
