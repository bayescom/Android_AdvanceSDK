package com.advance.supplier.mry;

import android.app.Activity;

import com.advance.AdvanceNativeExpressAdItem;
import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYLog;
import com.mercury.sdk.core.config.ADSize;
import com.mercury.sdk.core.config.VideoOption;
import com.mercury.sdk.core.nativ.NativeExpressAD;
import com.mercury.sdk.core.nativ.NativeExpressADListener;
import com.mercury.sdk.core.nativ.NativeExpressADView;
import com.mercury.sdk.util.ADError;

import java.util.ArrayList;
import java.util.List;

import static com.advance.model.AdvanceError.ERROR_DATA_NULL;

public class MercuryNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter implements NativeExpressADListener {
    private List<NativeExpressADView> list;
    private NativeExpressSetting advanceNativeExpress;
    String TAG = "[MercuryNativeExpressAdapter] ";
    NativeExpressADView adView;
    NativeExpressAD nativeExpressAd;

    public MercuryNativeExpressAdapter(Activity activity, NativeExpressSetting advanceNativeExpress) {
        super(activity, advanceNativeExpress);
        this.advanceNativeExpress = advanceNativeExpress;

    }

    @Override
    protected void paraLoadAd() {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);
        BYLog.dev(TAG + "advanceNativeExpress.getExpressViewWidth() = " + advanceNativeExpress.getExpressViewWidth());

        int width = advanceNativeExpress.getExpressViewWidth();
        int height = advanceNativeExpress.getExpressViewHeight();
        if (advanceNativeExpress.getGdtAutoHeight()) {
            height = ADSize.AUTO_HEIGHT;
        }
        //如果宽度为默认值，也按照填满配置，避免出现截断现象
        if (advanceNativeExpress.getGdtFullWidth() || 360 == width) {
            width = ADSize.FULL_WIDTH;
        }
        ADSize adSize = new ADSize(width, height);
//        LogUtil.devDebug("paraLoadAd init");
        nativeExpressAd = new NativeExpressAD(activity, sdkSupplier.adspotid, adSize, this); // 这里的Context必须为Activity
        //设置播放属性
        nativeExpressAd.setVideoOption(new VideoOption.Builder().setAutoPlayMuted(advanceNativeExpress.isVideoMute()).build());
//        LogUtil.devDebug("paraLoadAd loadAD");
//        nativeExpressAd.setWidthBlankDP();

        nativeExpressAd.loadAD(sdkSupplier.adCount);
//        LogUtil.devDebug("paraLoadAd finish");
    }

    @Override
    protected void adReady() {
    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));

        }

    }

    @Override
    public void onADLoaded(List<NativeExpressADView> list) {
        LogUtil.simple(TAG + "onADLoaded");

        if (list == null || list.isEmpty()) {
            handleFailed(ERROR_DATA_NULL, "");
        } else {
            nativeExpressAdItemList = new ArrayList<>();
            adView = list.get(0);
            for (NativeExpressADView nativeExpressADView : list) {
                AdvanceNativeExpressAdItem advanceNativeExpressAdItem = new MercuryNativeExpressAdItem(this, nativeExpressADView);
                nativeExpressAdItemList.add(advanceNativeExpressAdItem);
            }

            //旧版本SDK中不包含价格返回方法，catch住
            try {
                int cpm = adView.getEcpm();
                updateBidding(cpm);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            handleSucceed();
        }
    }

    @Override
    public void onRenderFail(NativeExpressADView nativeExpressADView) {
        LogUtil.simple(TAG + "onRenderFail");

        if (advanceNativeExpress != null)
            advanceNativeExpress.adapterRenderFailed(nativeExpressADView);

        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED));
    }

    @Override
    public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
        LogUtil.simple(TAG + "onRenderSuccess");

        if (advanceNativeExpress != null)
            advanceNativeExpress.adapterRenderSuccess(nativeExpressADView);

    }

    @Override
    public void onADExposure(NativeExpressADView nativeExpressADView) {
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

        if (advanceNativeExpress != null)
            advanceNativeExpress.adapterDidClosed(nativeExpressADView);

        removeADView();
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

    @Override
    public void doDestroy() {
        if (null != adView) {
            adView.destroy();
        }
    }

    @Override
    public void show() {
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
        return super.isValid();
    }
}
