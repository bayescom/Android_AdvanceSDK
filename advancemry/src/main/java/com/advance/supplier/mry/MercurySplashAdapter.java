package com.advance.supplier.mry;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.advance.AdvanceSetting;
import com.advance.BaseSplashAdapter;
import com.advance.SplashSetting;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.mercury.sdk.core.model.ADClickJumpInf;
import com.mercury.sdk.core.splash.MercurySplashData;
import com.mercury.sdk.core.splash.MercurySplashRenderListener;
import com.mercury.sdk.core.splash.MercurySplashRequestListener;
import com.mercury.sdk.core.splash.SplashAD;
import com.mercury.sdk.core.splash.SplashADListener;
import com.mercury.sdk.util.ADError;

import java.lang.ref.SoftReference;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

public class MercurySplashAdapter extends BaseSplashAdapter {
    private long remainTime = 5000;
    private SplashAD mercurySplash;
    private String TAG = "[MercurySplashAdapter] ";

    public MercurySplashAdapter(SoftReference<Activity> activity, final SplashSetting setting) {
        super(activity, setting);
    }

    @Override
    public void show() {
        try {
            if (mercurySplash == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "splashAd null"));
                return;
            }
            if ((null != setting)) {
                mercurySplash.setAdContainer(setting.getAdContainer());
//                if (setting.getLogoLayoutRes() != 0) {
//                    mercurySplash.setLogoLayout(setting.getLogoLayoutRes(), setting.getLogoLayoutHeight());
//                }
                if (setting.getHolderImage() != null) {
                    mercurySplash.setSplashHolderImage(setting.getHolderImage());
                }
                mercurySplash.setCustomSkipView(setting.getSkipView());
                TextView skipView = setting.getSkipView();
                if (null != skipView) {
                    skipView.setVisibility(View.VISIBLE);
                }
            }

            mercurySplash.getMercurySplashData().setRenderListener(new MercurySplashRenderListener() {
                @Override
                public void onSkip() {
                    LogUtil.simple(TAG + "onSkip ");
                    if (setting != null) {
                        setting.adapterDidSkip();
                    }
                }

                @Override
                public void onCountDown() {
                    LogUtil.simple(TAG + "onCountDown ");

                    if (setting != null) {
                        setting.adapterDidTimeOver();
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

            mercurySplash.showAd(getRealActivity(setting.getAdContainer()), setting.getAdContainer());

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
        if (mercurySplash != null) {
            mercurySplash.setRequestTimeout(timeout);
            if (null != setting) {
                mercurySplash.setAdContainer(setting.getAdContainer());
//                if (setting.getLogoLayoutRes() != 0) {
//                    mercurySplash.setLogoLayout(setting.getLogoLayoutRes(), setting.getLogoLayoutHeight());
//                }
                if (setting.getHolderImage() != null) {
                    mercurySplash.setSplashHolderImage(setting.getHolderImage());
                }
            }
        }
    }


}
