package com.advance.supplier.oppo;

import static com.advance.model.AdvanceError.ERROR_DATA_NULL;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.heytap.msp.mobad.api.ad.NativeTempletAd;
import com.heytap.msp.mobad.api.listener.INativeTempletAdListener;
import com.heytap.msp.mobad.api.params.INativeTempletAdView;
import com.heytap.msp.mobad.api.params.NativeAdError;
import com.heytap.msp.mobad.api.params.NativeAdParams;
import com.heytap.msp.mobad.api.params.NativeAdSize;

import java.util.List;
import java.util.Map;

public class OppoNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    NativeTempletAd mNativeTempletAd;
    INativeTempletAdView adView;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            /**
             * 通过构造NativeAdSize对象，在NativeTempletAd初始化时传入、可以指定原生模板广告的大小，单位为dp
             * 在接入、调试模板广告的过程中，可以利用这个字段调整广告View的大小，找到与自己的App需求适合的最佳广告位尺寸
             * 确定最佳尺寸后，应该把这个ADSize固定下来，并在构造NativeExpressAD的时候传入
             * 也可以传入null，展示默认的大小
             */
            //  2025/2/21 测试高度为0时表现？？？  测试看下来设置宽高信息，广告不会根据设置的值来渲染。。。。
            int width = nativeExpressSetting.getExpressViewWidth();
            int height = nativeExpressSetting.getExpressViewHeight();
            LogUtil.devDebug(TAG + " width = " + width + " , height = " + height);
            NativeAdSize nativeAdSize = new NativeAdSize.Builder()
                    .setWidthInDp(width)
                    .setHeightInDp(height)
                    .build();
            mNativeTempletAd = new NativeTempletAd(getRealContext(), sdkSupplier.adspotid, nativeAdSize, new INativeTempletAdListener() {
                @Override
                public void onAdSuccess(List<INativeTempletAdView> list) {
                    LogUtil.simple(TAG + " onAdSuccess ");

                    if (list == null || list.isEmpty() || list.get(0) == null) {
                        handleFailed(ERROR_DATA_NULL, "");
                    } else {
                        adView = list.get(0);
                        int ecpm = 0;
                        try {
                            ecpm = (adView.getECPM());
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        nativeExpressADView = adView.getAdView();
                        handleSucceed(ecpm);
                    }
                }

                @Override
                public void onAdFailed(NativeAdError nativeAdError) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    int code = -1;
                    String msg = "default onNoAD";
                    if (nativeAdError != null) {
                        code = nativeAdError.code;
                        msg = nativeAdError.msg;
                    }
                    handleFailed(code, msg);
                }

                @Override
                public void onAdClick(INativeTempletAdView iNativeTempletAdView) {
                    LogUtil.simple(TAG + " onAdClick ");

                    handleClick();
                }

                @Override
                public void onAdShow(INativeTempletAdView iNativeTempletAdView) {
                    LogUtil.simple(TAG + " onAdShow ");

                    handleShow();
                }

                @Override
                public void onAdClose(INativeTempletAdView iNativeTempletAdView) {
                    LogUtil.simple(TAG + " onAdClose ");

                    handleClose();
                }

                @Override
                public void onRenderSuccess(INativeTempletAdView iNativeTempletAdView) {
                    LogUtil.simple(TAG + " onRenderSuccess ");

                    handleRenderSuccess(nativeExpressADView);
                }

                @Override
                public void onRenderFailed(NativeAdError nativeAdError, INativeTempletAdView iNativeTempletAdView) {
                    LogUtil.simple(TAG + "onRenderFailed  ");

                    handleRenderFailed(nativeExpressADView, AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED));
                }
            });

            NativeAdParams params = new NativeAdParams.Builder().setFetchTimeout(sdkSupplier.timeout).build();
            mNativeTempletAd.loadAd(params);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        try {
            if (null != adView) {
                adView.destroy();
            }
            if (mNativeTempletAd != null) {
                mNativeTempletAd.destroyAd();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            addADView(adView.getAdView());
            adView.render();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
