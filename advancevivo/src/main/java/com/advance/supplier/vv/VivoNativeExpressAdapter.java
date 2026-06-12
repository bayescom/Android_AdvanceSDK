package com.advance.supplier.vv;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.nativead.UnifiedVivoNativeExpressAd;
import com.vivo.mobilead.unified.nativead.UnifiedVivoNativeExpressAdListener;
import com.vivo.mobilead.unified.nativead.VivoNativeExpressView;

import java.util.Map;

public class VivoNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    UnifiedVivoNativeExpressAd nativeExpressAd;
    VivoNativeExpressView expressView;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);
        

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (expressView != null) {
            expressView.destroy();
        }
    }

    

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (expressView == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "expressView null"));
                return;
            }

            addADView(expressView);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);

        if (builder != null) {
            int widthDP = nativeExpressSetting.getExpressViewWidth();
            int heightDP = nativeExpressSetting.getExpressViewHeight();
            LogUtil.devDebug(TAG + "getExpressViewWidth = " + widthDP);
            LogUtil.devDebug(TAG + "getExpressViewHeight = " + heightDP);
            if (widthDP > 0) {
                builder.setNativeExpressWidth(widthDP);
            }
            if (heightDP > 0) {
                builder.setNativeExpressHegiht(heightDP);
            }

            adParams = builder.build();
        }

        View container = getAdContainer();

        nativeExpressAd = new UnifiedVivoNativeExpressAd(getRealActivity(container), adParams, new UnifiedVivoNativeExpressAdListener() {
            @Override
            public void onAdReady(VivoNativeExpressView vivoNativeExpressView) {
                expressView = vivoNativeExpressView;
//                expressView.setMediaListener();
                LogUtil.simple(TAG + "onAdReady...");

                handleSucceed(VivoUtil.getPrice(expressView));
            }

            @Override
            public void onAdFailed(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onAdFailed... , vivoAdError = " + vivoAdError);

                VivoUtil.handleErr(VivoNativeExpressAdapter.this, vivoAdError, AdvanceError.ERROR_LOAD_SDK, "onAdFailed");
            }

            @Override
            public void onAdClick(VivoNativeExpressView vivoNativeExpressView) {
                LogUtil.simple(TAG + "onAdClick...");

                handleClick();
            }

            @Override
            public void onAdShow(VivoNativeExpressView vivoNativeExpressView) {
                LogUtil.simple(TAG + "onAdShow...");

                handleShow();
            }

            @Override
            public void onAdClose(VivoNativeExpressView vivoNativeExpressView) {
                LogUtil.simple(TAG + "onAdClose...");

                handleClose();
            }
        });

//设置视频监听
//        nativeExpressAd.setMediaListener(mediaListener);

//开始加载广告
        nativeExpressAd.loadAd();


    }
}
