package com.advance;

import android.app.Activity;

import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.itf.InterstitialGMCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;

public class AdvanceInterstitial extends AdvanceBaseAdspot implements InterstitialSetting {
    private AdvanceInterstitialListener listener;
    private UnifiedInterstitialMediaListener gdtlistener;
    private float csjExpressViewWidth = 300;
    private float csjExpressViewHeight = 300;
    private boolean isCsjNew = true;
    private InterstitialGMCallBack gmCallBack;

    @Deprecated
    public AdvanceInterstitial(Activity activity, String mediaId, String adspotId) {
        super(activity, mediaId, adspotId);
    }

    public AdvanceInterstitial(Activity activity, String adspotId) {
        super(activity, "", adspotId);
    }


    public void setCsjExpressViewAcceptedSize(float expressViewWidth, float expressViewHeight) {
        this.csjExpressViewWidth = expressViewWidth;
        this.csjExpressViewHeight = expressViewHeight;
    }

    public void setGdtMediaListener(UnifiedInterstitialMediaListener listener) {
        gdtlistener = listener;
    }

    public UnifiedInterstitialMediaListener getGdtMediaListener() {
        return gdtlistener;
    }


    public float getCsjExpressViewWidth() {
        return csjExpressViewWidth;
    }

    public float getCsjExpressViewHeight() {
        return csjExpressViewHeight;
    }

    public void setAdListener(AdvanceInterstitialListener listener) {
        advanceSelectListener = listener;
        this.listener = listener;
    }

    public void setGMCallBack(InterstitialGMCallBack gmCallBack) {
        this.gmCallBack = gmCallBack;
    }

    @Override
    public void initSdkSupplier() {
        try {
            initSupplierAdapterList();

            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_GDT, "gdt.GdtInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_MERCURY, "mry.MercuryInterstitialAdapter");

            initAdapter(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_KS, "ks.KSInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TANX, "tanx.TanxInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TAP, "tap.TapInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_OPPO, "oppo.OppoInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_SIG, "sigmob.SigmobInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_HW, "huawei.HWInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_XIAOMI, "mi.XMInterstitialAdapter");

            initAdapter(AdvanceConfig.SDK_ID_HONOR, "honor.HonorInterstitialAdapter");
            initAdapter(AdvanceConfig.SDK_ID_VIVO, "vv.VivoInterstitialAdapter");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getInterstitialAdapter(clzName, getADActivity(), this));
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
                    listener.onAdReady();
                }
                if (null != gmCallBack) {
                    gmCallBack.onAdSuccess();
                }
            }
        });

    }

    public void adapterDidShow(SdkSupplier supplier) {
        reportAdShow(supplier);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdShow();
                }
                if (null != gmCallBack) {
                    gmCallBack.onAdShow();
                }
            }
        });

    }


    public void adapterDidClicked(SdkSupplier supplier) {
        reportAdClicked(supplier);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdClicked();
                }
                if (null != gmCallBack) {
                    gmCallBack.onAdClick();
                }
            }
        });
    }

    public void adapterDidClosed() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdClose();
                }
                if (null != gmCallBack) {
                    gmCallBack.onAdClose();
                }
                destroy();
            }
        });
    }

    @Override
    @Deprecated
    public boolean isCsjNew() {
        return isCsjNew;
    }

    @Deprecated
    public void setCsjNew(boolean csjNew) {
        isCsjNew = csjNew;
    }
}
