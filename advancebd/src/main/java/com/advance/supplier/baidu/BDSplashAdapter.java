package com.advance.supplier.baidu;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;


import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.SplashAd;
import com.baidu.mobads.sdk.api.SplashInteractionListener;
import com.bayes.sdk.basic.util.BYUtil;

import java.lang.ref.SoftReference;

public class BDSplashAdapter extends AdvanceSplashCustomAdapter implements SplashInteractionListener {
    private SplashAd splashAd;
    private final RequestParameters parameters;

    private final String TAG = "[BDSplashAdapter] ";

    public BDSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting setting) {
        super(softReferenceActivity, setting);

        parameters = AdvanceBDManager.getInstance().splashParameters;
    }

    @Override
    protected void paraLoadAd() {
        initSplash();
        splashAd.load();
    }

    private void initSplash() {
        BDUtil.initBDAccount(this);

        splashAd = new SplashAd(BYUtil.getCtx(), sdkSupplier.adspotid, parameters, this);
        //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
        int bidFloor = AdvanceBDManager.getInstance().splashBidFloor;
        if (bidFloor > 0) {
            splashAd.setBidFloor(bidFloor);
        }
    }

    @Override
    protected void adReady() {
//        if (null != setting) {
//            setting.adapterDidSucceed(sdkSupplier);
//        }

//使用百度自己的skipview，自定义view隐藏
//        if (null != skipView) {
//            skipView.setVisibility(View.INVISIBLE);
//        }

    }

    @Override
    public void doDestroy() {
        try {
            if (splashAd != null) {
                splashAd.destroy();
                splashAd = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void orderLoadAd() {
        try {
            initSplash();
            splashAd.loadAndShow(splashSetting.getAdContainer());
        } catch (Throwable e) {
            e.printStackTrace();
            String tag = TAG + "Throwable ";
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD, tag));
            String cause = e.getCause() != null ? e.getCause().toString() : "no cause";
            reportCodeErr(tag + cause);
        }
    }


    /**
     * 以下为监听的广告事件
     */

    @Override
    public void onLpClosed() {
//落地页关闭回调
        LogUtil.simple(TAG + "onLpClosed");
    }

    @Override
    public void onAdPresent() {
        LogUtil.simple(TAG + "onAdPresent");

//        //进行辅助判断倒计时操作的定时任务
//        try {
//            handleShow();
//
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    isCountingEnd = true;
//                }
//            }, 4800);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

    }

    @Override
    public void onAdExposed() {
        LogUtil.simple(TAG + "onAdExposed");

        //进行辅助判断倒计时操作的定时任务
        try {
            handleShow();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isCountingEnd = true;
                }
            }, 4800);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onADLoaded() {
        LogUtil.simple(TAG + "onADLoaded , isParallel = " + isParallel);
        try { //避免方法有异常，catch一下，不影响success逻辑
            if (splashAd != null) {
                updateBidding(BDUtil.getEcpmValue(splashAd.getECPMLevel()));

                if (BYUtil.isDev()) {//测试bidding
                    //                sdkSupplier.price = 2700;
                    //                sdkSupplier.bidResultPrice = sdkSupplier.price;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        handleSucceed();
    }

    @Override
    public void onAdClick() {
        LogUtil.simple(TAG + "onAdClick");
        handleClick();
    }

    @Override
    public void onAdCacheSuccess() {
        LogUtil.simple(TAG + "onAdCacheSuccess");

    }

    @Override
    public void onAdCacheFailed() {
        LogUtil.simple(TAG + "onAdCacheFailed");

    }

    @Override
    public void onAdDismissed() {
        LogUtil.simple(TAG + "onAdDismissed");

        if (splashSetting != null) {
            if (isCountingEnd) {
                splashSetting.adapterDidTimeOver();
            } else {
                splashSetting.adapterDidSkip();
            }
        }

//        doDestroy();

    }

    @Override
    public void onAdSkip() {
        LogUtil.simple(TAG + "onAdSkip");

        if (splashSetting != null)
            splashSetting.adapterDidSkip();
    }

    @Override
    public void onAdFailed(String s) {
        LogUtil.e(TAG + "onAdFailed reason:" + s);

        handleFailed(AdvanceError.ERROR_BD_FAILED, s);
    }


//    @Override
//    public boolean isValid() {
//        if (splashAd != null) {
//            return splashAd.isReady();
//        }
//        return super.isValid();
//    }

    @Override
    public void show() {
        try {
            //并行时需要单独进行show
            if (isParallel) {
                splashAd.show(splashSetting.getAdContainer());
            }
            TextView skipView = splashSetting.getSkipView();
            if (null != skipView) {
                skipView.setVisibility(View.INVISIBLE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
