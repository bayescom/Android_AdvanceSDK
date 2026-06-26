package com.advance.supplier.flink;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.fl.saas.adx.api.FLSpread;
import com.fl.saas.adx.base.exception.FLError;
import com.fl.saas.adx.base.interfaces.AdViewSpreadListener;
import com.fl.saas.adx.base.interfaces.SpreadLoadListener;

import java.util.Map;

public class FLSplashAdapter extends AdvanceSplashCustomAdapter {
    FLSpread flAd;
    SpreadLoadListener.SpreadAd adData;
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        FLSpread.Builder flBuilder = new FLSpread.Builder(getRealContext());
        flBuilder.setKey(getPosID())
                .setSpreadLoadListener(new SpreadLoadListener() {
                    @Override
                    public void onADLoaded(final SpreadAd spreadAd) {
                        LogUtil.simple(TAG + "onADLoaded");

// 广告请求成功回调该方法
                        if (spreadAd == null) {
                            String nMsg = TAG + " spreadAd null";
                            handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                            return;
                        }
                        adData = spreadAd;

                        handleSucceed(spreadAd.getEcpm());
                    }
                })
                .setSpreadListener(new AdViewSpreadListener() {
                    @Override
                    public void onAdDisplay() {
// 广告展示时回调该方法
                        try {
                            LogUtil.simple(TAG + "onAdDisplay");

                            handleShow();

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isCountingEnd = true;
                                }
                            }, 4800);
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onAdFailed(FLError error) {
// 广告异常、失败，回调该方法
                        LogUtil.simple(TAG + "onAdFailed , error = " + error);

                        FLUtil.handleErr(FLSplashAdapter.this,error);
                    }

                    @Override
                    public void onAdClick(String s) {
// 广告被点击时回调该方法
                        LogUtil.simple(TAG + "onAdClick ,s = "+s);

                        handleClick();
                    }

                    @Override
                    public void onAdClose() {
// 广告关闭时回调该方法
                        LogUtil.simple(TAG + "onAdClose  ");

                        handleClose();
                    }
                });

        if (splashSetting != null && splashSetting.getAdContainer() != null) {
            flBuilder.setContainer(splashSetting.getAdContainer());
        }
        flAd = flBuilder.build();
        flAd.requestSpread();
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (flAd != null) {
            flAd.destroy();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        if (adData != null) {
            adData.show(splashSetting.getAdContainer());
        }
    }

    @Override
    public boolean isValid() {
        if (adData != null) {
            return adData.isAdReady();
        }
           return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        if (flAd!=null){
            flAd.biddingResultUpload(isWin, (int) winPrice,0);
        }

    }
}
