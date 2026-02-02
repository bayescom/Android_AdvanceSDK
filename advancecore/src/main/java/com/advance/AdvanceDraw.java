package com.advance;

import android.app.Activity;
import android.view.ViewGroup;

import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.bayes.sdk.basic.util.BYThreadUtil;

public class AdvanceDraw extends AdvanceBaseAdspot implements AdvanceDrawSetting {

    AdvanceDrawListener listener;
    ViewGroup adContainer;
    private int csjExpressWidth;
    private int csjExpressHeight;

    public AdvanceDraw(Activity activity, String adspotId) {
        super(activity, "", adspotId);
        try {
            csjExpressWidth = BYDisplay.px2dp(BYDisplay.getScreenWPx());
            csjExpressHeight = BYDisplay.px2dp(BYDisplay.getScreenHPx());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void initAdapterData(SdkSupplier sdkSupplier,String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getDrawAdapter(clzName, getADActivity(), this));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initSdkSupplier() {
        try {
            initSupplierAdapterList();
            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjDrawAdapter");
            initAdapter(AdvanceConfig.SDK_ID_KS, "ks.KSDrawAdapter");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    @Override
    public void selectSdkSupplierFailed() {
        onAdvanceError(listener, AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT_FAILED));
    }


    @Override
    public void adapterDidSucceed(SdkSupplier supplier) {
        reportAdSucceed(supplier);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdLoaded();
                }
            }
        });

    }

    @Override
    public void adapterDidShow(SdkSupplier supplier) {
        reportAdShow(supplier);
        if (null != listener) {
            listener.onAdShow();
        }
    }

    @Override
    public void adapterDidClicked(SdkSupplier supplier) {
        reportAdClicked(supplier);
        if (null != listener) {
            listener.onAdClicked();
        }
    }

    public void setAdListener(AdvanceDrawListener listener) {
        advanceSelectListener = listener;
        this.listener = listener;
    }

    /**
     * 以下为setting回调项处理
     */

    public void setAdContainer(ViewGroup adContainer) {
        this.adContainer = adContainer;
    }


    @Override
    public ViewGroup getContainer() {
        return adContainer;
    }

    public void setCsjExpressSize(int width, int height) {
        this.csjExpressWidth = width;
        this.csjExpressHeight = height;
    }

    @Override
    public int getCsjExpressHeight() {
        return csjExpressHeight;
    }

    @Override
    public int getCsjExpressWidth() {
        return csjExpressWidth;
    }

}
