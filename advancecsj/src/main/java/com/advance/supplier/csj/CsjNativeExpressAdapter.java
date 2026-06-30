package com.advance.supplier.csj;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.AdvanceConfig;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYLog;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;
import java.util.Map;

public class CsjNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter implements TTAdNative.NativeExpressAdListener {

    TTNativeExpressAd ttNativeExpressAd;
    private String TAG = "[CsjNativeExpressAdapter] ";


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {


        final TTAdManager ttAdManager = TTAdSdk.getAdManager();
        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        BYLog.dev(TAG + "advanceNativeExpress.getExpressViewWidth() = " + nativeExpressSetting.getExpressViewWidth());
        TTAdNative ttAdNative = ttAdManager.createAdNative(activity);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(sdkSupplier.adspotid) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(sdkSupplier.adCount) //请求广告数量为1到3条
//                .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                .setExpressViewAcceptedSize(nativeExpressSetting.getExpressViewWidth(), nativeExpressSetting.getExpressViewHeight()) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(nativeExpressSetting.getCsjImageWidth(), nativeExpressSetting.getCsjImageHeight())
                .build();
        //加载广告
        ttAdNative.loadNativeExpressAd(adSlot, this);
    }

    @Override
    protected void adPrepared() {

    }


    @Override
    public void onError(int i, String s) {
        handleFailed(i, s);
    }

    @Override
    public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
        try {
            LogUtil.simple(TAG + "onNativeExpressAdLoad");
            if (ads == null || ads.size() == 0) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "ads empty");
            } else {
                ttNativeExpressAd = ads.get(0);

                if (ttNativeExpressAd == null) {
                    String nMsg = TAG + "ttNativeExpressAd  null";
                    AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, nMsg);
                    runParaFailed(error);
                    return;
                }

                handleSucceed(CsjUtil.getEcpmValue(TAG, ttNativeExpressAd.getMediaExtraInfo()));

            }
        } catch (Throwable e) {
            e.printStackTrace();
            handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
        }
    }

    public void onAdItemShow(View view) {
        LogUtil.simple(TAG + "onAdItemShow");
        nativeExpressADView = view;

        handleShow();
    }

    public void onAdItemClicked(View view) {
        LogUtil.simple(TAG + "onAdItemClicked");
        handleClick();

    }

    public void onAdItemRenderFailed(View view, String msg, int code) {
        LogUtil.simple(TAG + "onAdItemRenderFailed");


        handleRenderFailed(view,AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, TAG + code + "， " + msg));
    }

    public void onAdItemRenderSuccess(View view) {
        LogUtil.simple(TAG + "onAdItemRenderSuccess");

        handleRenderSuccess(view);
    }

    public void onAdItemClose(View view) {
        LogUtil.simple(TAG + "onAdItemClose");

        handleClose();
    }

    public void onAdItemErr(AdvanceError advanceError) {
        LogUtil.simple(TAG + "onAdItemErr ");

        runParaFailed(advanceError);
    }


    @Override
    public void destroyAd() {

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        if (ttNativeExpressAd == null) {
            LogUtil.e("无广告内容");
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL));
            return;
        }
        try {
            ttNativeExpressAd.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                @Override
                public void onAdClicked(View view, int i) {
                    onAdItemClicked(view);
                }

                @Override
                public void onAdShow(View view, int i) {
                    onAdItemShow(view);
                }

                @Override
                public void onRenderFail(View view, String s, int i) {

                    onAdItemRenderFailed(view, s, i);
                }

                @Override
                public void onRenderSuccess(View view, float v, float v1) {
                    onAdItemRenderSuccess(view);
                }
            });

            // 2024/4/15 分离加载是传入activity优化
            ttNativeExpressAd.setDislikeCallback(activity, new TTAdDislike.DislikeInteractionCallback() {
                @Override
                public void onShow() {

                }

                @Override
                public void onSelected(int i, String s, boolean enforce) {
                    LogUtil.simple(TAG + "DislikeInteractionCallback_onSelected , int i = +" + i + ", String s" + s + ", boolean enforce" + enforce + " ;");
                    onAdItemClose(null);
                }

                @Override
                public void onCancel() {

                }
            });
            addADView(ttNativeExpressAd.getExpressAdView());
            ttNativeExpressAd.render();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }

    }

    @Override
    public boolean isValid() {
        if (ttNativeExpressAd != null && ttNativeExpressAd.getMediationManager() != null) {
            return ttNativeExpressAd.getMediationManager().isReady();
        }

        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        CsjUtil.bid(ttNativeExpressAd,isWin,winPrice);

    }
}
