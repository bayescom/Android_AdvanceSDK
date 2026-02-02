package com.advance;

import android.app.Activity;
import android.view.ViewGroup;

import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.bayes.sdk.basic.util.BYThreadUtil;


public class AdvanceBanner extends AdvanceBaseAdspot implements BannerSetting {
    private ViewGroup adContainer;
    private int csjAcceptedSizeWidth = 640;
    private int csjAcceptedSizeHeight = 100;
    private int refreshInterval = 0;
    private AdvanceBannerListener listener;

    private int csjExpressViewAcceptedWidth = 360;
    private int csjExpressViewAcceptedHeight = 100;

    /**
     * 推荐使用新的构造方式
     *
     * @param activity    上下文
     * @param adContainer 广告父布局
     * @param mediaId     媒体id
     * @param adspotId    广告位id
     * @see AdvanceBanner#AdvanceBanner(Activity, ViewGroup, String)
     */
    @Deprecated
    public AdvanceBanner(Activity activity, ViewGroup adContainer, String mediaId, String adspotId) {
        super(activity, mediaId, adspotId);
        this.adContainer = adContainer;
        initListener();
    }

    public AdvanceBanner(Activity activity, ViewGroup adContainer, String adspotId) {
        super(activity, "", adspotId);
        this.adContainer = adContainer;
        initListener();
    }

    public void initListener() {
        needForceLoadAndShow = true;
    }


    public AdvanceBanner setCsjAcceptedSize(int width, int height) {
        this.csjAcceptedSizeWidth = width;
        this.csjAcceptedSizeHeight = height;
        return this;
    }

    public AdvanceBanner setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
        return this;
    }

    public void setAdContainer(ViewGroup adContainer) {
        this.adContainer = adContainer;
    }

    public ViewGroup getContainer() {
        return adContainer;
    }

    public int getRefreshInterval() {
        return this.refreshInterval;
    }

    public int getCsjAcceptedSizeWidth() {
        return this.csjAcceptedSizeWidth;
    }

    public int getCsjAcceptedSizeHeight() {
        return this.csjAcceptedSizeHeight;
    }

    public AdvanceBanner setCsjExpressViewAcceptedSize(int width, int height) {
        this.csjExpressViewAcceptedWidth = width;
        this.csjExpressViewAcceptedHeight = height;
        return this;
    }

    public int getCsjExpressViewAcceptedWidth() {
        return this.csjExpressViewAcceptedWidth;
    }

    public int getCsjExpressViewAcceptedHeight() {
        return this.csjExpressViewAcceptedHeight;
    }


    public void setAdListener(AdvanceBannerListener listener) {
        advanceSelectListener = listener;
        this.listener = listener;
    }

    @Override
    public void initSdkSupplier() {
        try {
            initSupplierAdapterList();
            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_GDT, "gdt.GdtBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_MERCURY, "mry.MercuryBannerAdapter");
//            initAdapter(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TAP, "tap.TapBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_OPPO, "oppo.OppoBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_HW, "huawei.HWBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_XIAOMI, "mi.XMBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_HONOR, "honor.HonorBannerAdapter");
            initAdapter(AdvanceConfig.SDK_ID_VIVO, "vv.VivoBannerAdapter");

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getBannerAdapter(clzName, getADActivity(), this));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void selectSdkSupplierFailed() {
        onAdvanceError(listener, AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT_FAILED));
    }


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

    public void adapterDidShow(SdkSupplier supplier) {
        reportAdShow(supplier);
        if (null != listener) {
            listener.onAdShow();
        }

        //一旦有渠道展示成功了 则将其他渠道进行销毁操作
        if (currentSdkSupplier != null) {
//            destroyOtherSupplier(currentSdkSupplier.priority+"");
        }
    }


    public void adapterDidClicked(SdkSupplier supplier) {
        reportAdClicked(supplier);
        if (null != listener) {
            listener.onAdClicked();
        }
    }

    public void adapterDidDislike() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onDislike();
                }
            }
        });

    }

}
