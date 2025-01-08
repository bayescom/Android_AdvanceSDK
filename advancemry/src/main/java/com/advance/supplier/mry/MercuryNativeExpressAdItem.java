package com.advance.supplier.mry;

import android.view.View;

import com.advance.AdvanceConfig;
import com.advance.AdvanceNativeExpressAdItem;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.mercury.sdk.core.config.ADSize;
import com.mercury.sdk.core.nativ.NativeExpressADView;
import com.mercury.sdk.core.nativ.NativeExpressMediaListener;
@Deprecated
public class MercuryNativeExpressAdItem implements AdvanceNativeExpressAdItem {
    private MercuryNativeExpressAdapter mercuryNativeExpressAdapter;
    private NativeExpressADView nativeExpressADView;


    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_MERCURY;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_MERCURY;
    }

    public MercuryNativeExpressAdItem(MercuryNativeExpressAdapter mercuryNativeExpressAdapter, NativeExpressADView nativeExpressADView) {
        this.mercuryNativeExpressAdapter = mercuryNativeExpressAdapter;
        this.nativeExpressADView = nativeExpressADView;
    }

    public NativeExpressADView getNativeExpressADView() {
        return nativeExpressADView;
    }

    @Override
    public void render() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                doRender();
            }
        });

    }

    private void doRender() {
        try {
            if (null != nativeExpressADView)
                nativeExpressADView.render();
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (mercuryNativeExpressAdapter != null) {
                    mercuryNativeExpressAdapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER));
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setAdSize(ADSize adSize) {
        if (null != nativeExpressADView)

            nativeExpressADView.setAdSize(adSize);


    }

    public int getAdPatternType() {
        try {
            return nativeExpressADView.getAdPatternType();
        } catch (Throwable e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getAdInfo() {
        try {
            return nativeExpressADView.getAdInfo();
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void destroy() {
        if (null != nativeExpressADView) {
            nativeExpressADView.destroy();
        }


    }

    @Override
    public View getExpressAdView() {
        return getNativeExpressADView();
    }

    public void setMediaListener(NativeExpressMediaListener mediaListener) {
        if (null != nativeExpressADView)
            nativeExpressADView.setMediaListener(mediaListener);
    }

}
