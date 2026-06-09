package com.advance.supplier.baidu;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.SplashAd;
import com.baidu.mobads.sdk.api.SplashInteractionListener;
import com.bayes.sdk.basic.util.BYUtil;

import java.util.Map;

public class BDSplashAdapter extends AdvanceSplashCustomAdapter implements SplashInteractionListener {
    private SplashAd splashAd;
    private RequestParameters parameters;

    private final String TAG = "[BDSplashAdapter] ";


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        parameters = AdvanceBDManager.getInstance().splashParameters;


        splashAd = new SplashAd(BYUtil.getCtx(), sdkSupplier.adspotid, parameters, this);
        //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
        int bidFloor = AdvanceBDManager.getInstance().splashBidFloor;
        if (bidFloor > 0) {
            splashAd.setBidFloor(bidFloor);
        }
        splashAd.load();
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
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
    public boolean isValid() {
        if (splashAd != null) {
            return splashAd.isReady();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

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
        double ecpm = 0;
        try { //避免方法有异常，catch一下，不影响success逻辑
            if (splashAd != null) {
                ecpm = (BDUtil.getEcpmValue(splashAd.getECPMLevel()));

                if (BYUtil.isDev()) {//测试bidding
                    //                sdkSupplier.price = 2700;
                    //                sdkSupplier.bidResultPrice = sdkSupplier.price;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        handleSucceed(ecpm);
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


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
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
