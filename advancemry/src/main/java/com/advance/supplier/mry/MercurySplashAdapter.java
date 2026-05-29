package com.advance.supplier.mry;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.TextView;


import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.mercury.sdk.core.model.ADClickJumpInf;
import com.mercury.sdk.core.splash.MercurySplashData;
import com.mercury.sdk.core.splash.MercurySplashRenderListener;
import com.mercury.sdk.core.splash.MercurySplashRequestListener;
import com.mercury.sdk.core.splash.SplashAD;
import com.mercury.sdk.util.ADError;

import java.lang.ref.SoftReference;
import java.util.Map;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

public class MercurySplashAdapter extends AdvanceSplashCustomAdapter {
    private long remainTime = 5000;
    private SplashAD mercurySplash;
    private String TAG = "[MercurySplashAdapter] ";

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        if (BYUtil.isDev()) {//todo 测试逻辑，正式上线需移除
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "测试mry渲染异常");
//                }
//            },200);
////            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "测试渲染异常");
//            return;
//        }
        try {
            if (mercurySplash == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "splashAd null"));
                return;
            }
            mercurySplash.setAdContainer(getAdContainer());
            if ((null != splashSetting)) {
//                if (setting.getLogoLayoutRes() != 0) {
//                    mercurySplash.setLogoLayout(setting.getLogoLayoutRes(), setting.getLogoLayoutHeight());
//                }
                if (splashSetting.getHolderImage() != null) {
                    mercurySplash.setSplashHolderImage(splashSetting.getHolderImage());
                }
                mercurySplash.setCustomSkipView(splashSetting.getSkipView());
                TextView skipView = splashSetting.getSkipView();
                if (null != skipView) {
                    skipView.setVisibility(View.VISIBLE);
                }
            }

            mercurySplash.getMercurySplashData().setRenderListener(new MercurySplashRenderListener() {
                @Override
                public void onSkip() {
                    LogUtil.simple(TAG + "onSkip ");

                    handleSkip();
                }

                @Override
                public void onCountDown() {
                    LogUtil.simple(TAG + "onCountDown ");

                    handleTimeOver();
                }

                @Override
                public void onRenderSuccess() {
                    LogUtil.simple(TAG + "onRenderSuccess ");

                    handleShow();

                }

                @Override
                public void onClicked(ADClickJumpInf adClickJumpInf) {
                    LogUtil.simple(TAG + "onClicked ");
//
                    handleClick();
                }

                @Override
                public void onRenderFail(ADError adError) {
                    LogUtil.simple(TAG + "onRenderFail ");

                    int code = -1;
                    String msg = "default onRenderFail";
                    if (adError != null) {
                        code = adError.code;
                        msg = adError.msg;
                    }
                    LogUtil.simple(TAG + "onAdFailed");
                    handleFailed(code, msg);
                }
            });

            mercurySplash.showAd(activity, getAdContainer());

        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    //调用展示方法
    @Override
    public void adPrepared() {
//        if (mercurySplash != null && isParallel) {
//            mercurySplash.showAd(adContainer);
//        }
    }

    @Override
    public void destroyAd() {

    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);

        //检查是否命中使用缓存逻辑
        boolean hitCache = AdvanceCacheUtil.loadWithCacheData(this, SplashAD.class, new BYAbsCallBack<SplashAD>() {
            @Override
            public void invoke(SplashAD cacheAD) {
                mercurySplash = cacheAD;
                //自渲染需要转换返回广告model为聚合通用model
//                dataConverter = new KSRenderDataConverter(cacheAD, sdkSupplier);

                updateBidding(cacheAD.getEcpm());
            }
        });
        if (hitCache) {
            return;
        }

        int timeout = sdkSupplier.timeout <= 0 ? 5000 : sdkSupplier.timeout;
//  2023/9/5 替换为分离加载模式
        mercurySplash = new SplashAD(getRealContext(), sdkSupplier.adspotid);
        mercurySplash.setRequestListener(new MercurySplashRequestListener() {
            @Override
            public void onAdSuccess(MercurySplashData mercurySplashData) {
                LogUtil.simple(TAG + "onAdSuccess ");

                //旧版本SDK中不包含价格返回方法，catch住
                try {
                    int cpm = mercurySplash.getEcpm();
//                    if (AdvanceUtil.isDev()) {//todo 测试逻辑，正式上线需移除
//                        cpm = 600;
//                    }
                    updateBidding(cpm);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                handleSucceed(mercurySplash);
            }

            @Override
            public void onMaterialCached() {
                LogUtil.simple(TAG + "onMaterialCached ");

            }

            @Override
            public void onAdFailed(ADError adError) {
                int code = -1;
                String msg = "default onNoAD";
                if (adError != null) {
                    code = adError.code;
                    msg = adError.msg;
                }
                LogUtil.simple(TAG + "onAdFailed");
                handleFailed(code, msg);
            }
        });
//
        if (mercurySplash != null) {
            mercurySplash.setRequestTimeout(timeout);
            mercurySplash.setAdContainer(getAdContainer());
//                if (setting.getLogoLayoutRes() != 0) {
//                    mercurySplash.setLogoLayout(setting.getLogoLayoutRes(), setting.getLogoLayoutHeight());
//                }
            if (splashSetting != null && splashSetting.getHolderImage() != null) {
                mercurySplash.setSplashHolderImage(splashSetting.getHolderImage());
            }

        }
        if (mercurySplash != null) {
            mercurySplash.fetchAdOnly();
        }
        //跳过载体要可见状态，不然如果载体设置了默认隐藏按钮会无法展示
//        if (null != setting && setting.getGdtSkipContainer() != null) {
//            setting.getGdtSkipContainer().setVisibility(View.VISIBLE);
//        }
    }


    @Override
    public boolean isValid() {
        if (mercurySplash != null) {
            return mercurySplash.isValid();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }

}
