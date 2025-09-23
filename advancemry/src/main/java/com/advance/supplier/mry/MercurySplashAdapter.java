package com.advance.supplier.mry;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;


import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.mercury.sdk.core.model.ADClickJumpInf;
import com.mercury.sdk.core.splash.MercurySplashData;
import com.mercury.sdk.core.splash.MercurySplashRenderListener;
import com.mercury.sdk.core.splash.MercurySplashRequestListener;
import com.mercury.sdk.core.splash.SplashAD;
import com.mercury.sdk.util.ADError;

import java.lang.ref.SoftReference;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

public class MercurySplashAdapter extends AdvanceSplashCustomAdapter {
    private long remainTime = 5000;
    private SplashAD mercurySplash;
    private String TAG = "[MercurySplashAdapter] ";

    public MercurySplashAdapter(SoftReference<Activity> activity, final SplashSetting setting) {
        super(activity, setting);
    }

    @Override
    public void show() {
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
            if ((null != splashSetting)) {
                mercurySplash.setAdContainer(splashSetting.getAdContainer());
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
                    if (splashSetting != null) {
                        splashSetting.adapterDidSkip();
                    }
                }

                @Override
                public void onCountDown() {
                    LogUtil.simple(TAG + "onCountDown ");

                    if (splashSetting != null) {
                        splashSetting.adapterDidTimeOver();
                    }
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

            mercurySplash.showAd(getRealActivity(splashSetting.getAdContainer()), splashSetting.getAdContainer());

        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void orderLoadAd() {
        try {
            initAD();
            if (mercurySplash != null) {
                mercurySplash.fetchAdOnly();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            String tag = "MercurySplashAdapter Throwable ";
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD, tag));
            String cause = e.getCause() != null ? e.getCause().toString() : "no cause";
            reportCodeErr(tag + cause);
        }
    }

    @Override
    public void paraLoadAd() {
        initAD();
//        if (null != skipView) {
//            skipView.setVisibility(View.VISIBLE);
//        }
        if (mercurySplash != null) {
            mercurySplash.fetchAdOnly();
        }
    }


    //调用展示方法
    @Override
    public void adReady() {
//        if (mercurySplash != null && isParallel) {
//            mercurySplash.showAd(adContainer);
//        }
    }

    @Override
    public void doDestroy() {

    }


    private void initAD() {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);
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
                handleSucceed();
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
//        mercurySplash = new SplashAD(getRealActivity(setting.getAdContainer()), sdkSupplier.adspotid, skipView, timeout, new SplashADListener() {
//            @Override
//            public void onADDismissed() {
//                LogUtil.simple(TAG + "onADDismissed ");
//
//                if (null != setting) {
//                    if (remainTime < 1000) {
//                        setting.adapterDidTimeOver();
//                    } else {
//                        setting.adapterDidSkip();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onADPresent() {
//                LogUtil.simple(TAG + "onADPresent ");
//
//                //旧版本SDK中不包含价格返回方法，catch住
//                try {
//                    int cpm = mercurySplash.getEcpm();
////                    if (AdvanceUtil.isDev()) {//todo 测试逻辑，正式上线需移除
////                        cpm = 600;
////                    }
//                    updateBidding(cpm);
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
//                handleSucceed();
//            }
//
//            @Override
//            public void onADTick(long l) {
//                LogUtil.simple(TAG + "onADTick :" + l);
//                remainTime = l;
//                if (null != skipView) {
//                    skipView.setText(String.format(skipText, Math.round(l / 1000f)));
//                }
//
//            }
//
//            @Override
//            public void onADExposure() {
//                LogUtil.simple(TAG + "onADExposure ");
//
//                handleShow();
//            }
//
//            @Override
//            public void onADClicked() {
//                LogUtil.simple(TAG + "onADClicked ");
//
//                handleClick();
//            }
//
//            @Override
//            public void onNoAD(ADError adError) {
//                int code = -1;
//                String msg = "default onNoAD";
//                if (adError != null) {
//                    code = adError.code;
//                    msg = adError.msg;
//                }
//                LogUtil.simple(TAG + "onNoAD");
//                handleFailed(code, msg);
//            }
//        });
        if (mercurySplash != null) {
            mercurySplash.setRequestTimeout(timeout);
            if (null != splashSetting) {
                mercurySplash.setAdContainer(splashSetting.getAdContainer());
//                if (setting.getLogoLayoutRes() != 0) {
//                    mercurySplash.setLogoLayout(setting.getLogoLayoutRes(), setting.getLogoLayoutHeight());
//                }
                if (splashSetting.getHolderImage() != null) {
                    mercurySplash.setSplashHolderImage(splashSetting.getHolderImage());
                }
            }
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
        return super.isValid();
    }

}
