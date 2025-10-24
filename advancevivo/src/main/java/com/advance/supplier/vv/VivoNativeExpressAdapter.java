package com.advance.supplier.vv;

import android.app.Activity;
import android.view.View;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.nativead.UnifiedVivoNativeExpressAd;
import com.vivo.mobilead.unified.nativead.UnifiedVivoNativeExpressAdListener;
import com.vivo.mobilead.unified.nativead.VivoNativeExpressView;

public class VivoNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    UnifiedVivoNativeExpressAd nativeExpressAd;
    VivoNativeExpressView expressView;

    public VivoNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
    }

    @Override
    protected void paraLoadAd() {
        VivoUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                loadAd();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });

    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (expressView != null) {
            expressView.destroy();
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
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

    private void loadAd() {
        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);

        if (builder != null) {
            int widthDP = mSetting.getExpressViewWidth();
            int heightDP = mSetting.getExpressViewHeight();
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

        View container = null;
        if (mSetting != null) {
            container = mSetting.getAdContainer();
        }
        nativeExpressAd = new UnifiedVivoNativeExpressAd(getRealActivity(container), adParams, new UnifiedVivoNativeExpressAdListener() {
            @Override
            public void onAdReady(VivoNativeExpressView vivoNativeExpressView) {
                expressView = vivoNativeExpressView;
//                expressView.setMediaListener();
                LogUtil.simple(TAG + "onAdReady...");

                updateBidding(VivoUtil.getPrice(expressView));
                handleSucceed();
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
