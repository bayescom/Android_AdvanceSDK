package com.advance.supplier.mry;

import static com.advance.model.AdvanceError.ERROR_DATA_NULL;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYLog;
import com.mercury.sdk.core.config.ADSize;
import com.mercury.sdk.core.nativ.NativeExpressAD;
import com.mercury.sdk.core.nativ.NativeExpressADListener;
import com.mercury.sdk.core.nativ.NativeExpressADView;
import com.mercury.sdk.util.ADError;

import java.util.List;
import java.util.Map;

public class MercuryNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    String TAG = "[MercuryNativeExpressAdapter] ";
    NativeExpressADView adView;
    NativeExpressAD nativeExpressAd;

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        BYLog.dev(TAG + "advanceNativeExpress.getExpressViewWidth() = " + nativeExpressSetting.getExpressViewWidth());

        int width = nativeExpressSetting.getExpressViewWidth();
        int height = nativeExpressSetting.getExpressViewHeight();
        if (nativeExpressSetting.getGdtAutoHeight()) {
            height = ADSize.AUTO_HEIGHT;
        }
        //如果宽度为默认值，也按照填满配置，避免出现截断现象
        if (nativeExpressSetting.getGdtFullWidth() || 360 == width) {
            width = ADSize.FULL_WIDTH;
        }
        ADSize adSize = new ADSize(width, height);
//        LogUtil.devDebug("paraLoadAd init");
        nativeExpressAd = new NativeExpressAD(activity, sdkSupplier.adspotid, adSize, new NativeExpressADListener() {

            @Override
            public void onADLoaded(List<NativeExpressADView> list) {
                LogUtil.simple(TAG + "onADLoaded");

                if (list == null || list.isEmpty()) {
                    handleFailed(ERROR_DATA_NULL, "");
                } else {
                    adView = list.get(0);

                    //旧版本SDK中不包含价格返回方法，catch住
                    int cpm = 0;
                    try {
                        cpm = adView.getEcpm();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    handleSucceed(cpm);
                }
            }

            @Override
            public void onRenderFail(NativeExpressADView nativeExpressADView) {
                LogUtil.simple(TAG + "onRenderFail");

                handleRenderFailed(nativeExpressADView, AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED));
            }

            @Override
            public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
                LogUtil.simple(TAG + "onRenderSuccess");

                handleRenderSuccess(nativeExpressADView);

            }

            @Override
            public void onADExposure(NativeExpressADView adView) {
                nativeExpressADView = adView;
                LogUtil.simple(TAG + "onADExposure");

                handleShow();
            }

            @Override
            public void onADClicked(NativeExpressADView nativeExpressADView) {
                LogUtil.simple(TAG + "onADClicked");

                handleClick();
            }

            @Override
            public void onADClosed(NativeExpressADView nativeExpressADView) {
                LogUtil.simple(TAG + "onADClosed");

                handleClose();
            }

            @Override
            public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
                LogUtil.simple(TAG + "onADLeftApplication");

            }

            @Override
            public void onNoAD(ADError adError) {
                int code = -1;
                String msg = "default onNoAD";
                if (adError != null) {
                    code = adError.code;
                    msg = adError.msg;
                }
                LogUtil.simple(TAG + "onNoAD");
                handleFailed(code, msg);
            }

        }); // 这里的Context必须为Activity
        //设置播放属性
//        nativeExpressAd.setVideoOption(new VideoOption.Builder().setAutoPlayMuted(nativeExpressSetting.isVideoMute()).build());
//        LogUtil.devDebug("paraLoadAd loadAD");
//        nativeExpressAd.setWidthBlankDP();

        nativeExpressAd.loadAD(sdkSupplier.adCount);
//        LogUtil.devDebug("paraLoadAd finish");
    }

    @Override
    protected void adPrepared() {
    }


    @Override
    public void destroyAd() {
        if (null != adView) {
            adView.destroy();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            addADView(adView);
            adView.render();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    @Override
    public boolean isValid() {
        if (nativeExpressAd != null) {
            return nativeExpressAd.isValid();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " + winPrice + ", referBidInfo = " + referBidInfo);

        if (nativeExpressAd != null && !isWin) {
            nativeExpressAd.sendLossWin(winPrice);
        }
    }
}
