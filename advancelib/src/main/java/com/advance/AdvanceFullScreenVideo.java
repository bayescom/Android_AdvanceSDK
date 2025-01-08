package com.advance;

import android.app.Activity;

import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;

public class AdvanceFullScreenVideo extends AdvanceBaseAdspot implements FullScreenVideoSetting {
    private AdvanceFullScreenVideoListener listener;
    private UnifiedInterstitialMediaListener mediaListener;
    private VideoOption videoOption;
    private int csjExpressWidth = 500;
    private int csjExpressHeight = 500;
    private boolean isCsjExpress = true;
    private String paraCachedSupId = ""; //并行时当前缓存成功的渠道id

    @Deprecated
    public AdvanceFullScreenVideo(Activity activity, String mediaId, String adspotId) {
        super(activity, mediaId, adspotId);
    }

    public AdvanceFullScreenVideo(Activity activity, String adspotId) {
        super(activity, "", adspotId);
    }

    public void setAdListener(AdvanceFullScreenVideoListener listener) {
        advanceSelectListener = listener;
        this.listener = listener;
    }

    public void setCsjExpressSize(int width, int height) {
        this.csjExpressWidth = width;
        this.csjExpressHeight = height;
        isCsjExpress = true;
    }

    @Override
    public void initSdkSupplier() {
        try {
            //配置渠道信息
            initSupplierAdapterList();

//            supplierAdapters.put(AdvanceConfig.SDK_ID_CSJ, new CsjFullScreenVideoAdapter(activity, this));
//            supplierAdapters.put(AdvanceConfig.SDK_ID_GDT, new GdtFullScreenVideoAdapter(activity, this));

            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjFullScreenVideoAdapter");
            initAdapter(AdvanceConfig.SDK_ID_GDT, "gdt.GdtFullScreenVideoAdapter");
            initAdapter(AdvanceConfig.SDK_ID_KS, "ks.KSFullScreenVideoAdapter");
            initAdapter(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDFullScreenVideoAdapter");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getFullVideoAdapter(clzName, getADActivity(), this));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public UnifiedInterstitialMediaListener getGdtMediaListener() {
        return mediaListener;
    }

    @Override
    public VideoOption getGdtVideoOption() {
        return videoOption;
    }

    public void setGdtMediaListener(UnifiedInterstitialMediaListener mediaListener) {
        this.mediaListener = mediaListener;
    }

    public void setGdtVideoOption(VideoOption videoOption) {
        this.videoOption = videoOption;
    }

    public String getParaCachedSupId() {
        return paraCachedSupId;
    }
//    @Override
//    public void setParaCachedSupId(String id){
//        paraCachedSupId = id;
//    }

    @Override
    public void selectSdkSupplierFailed() {
        onAdvanceError(listener, AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT_FAILED));
    }

    public void adapterDidShow(SdkSupplier supplier) {
        try {
            reportAdShow(supplier);
            if (null != listener) {
                listener.onAdShow();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void paraEvent(int type, AdvanceError advanceError, SdkSupplier sdkSupplier) {
        LogUtil.max("[AdvanceFullScreenVideo] paraEvent: type = " + type);
        switch (type) {
            case AdvanceConstant.EVENT_TYPE_CACHED:
                if (canNextStep(sdkSupplier)) {
                    if (advanceError != null) {
                        paraCachedSupId = advanceError.msg;
                    }
                    adapterVideoCached();
                }
                break;
            case AdvanceConstant.EVENT_TYPE_ORDER:
            case AdvanceConstant.EVENT_TYPE_ERROR:
            case AdvanceConstant.EVENT_TYPE_SUCCEED:
                super.paraEvent(type, advanceError, sdkSupplier);
                break;
        }
    }

    public void adapterDidClicked(SdkSupplier supplier) {
        try {
            reportAdClicked(supplier);
            if (null != listener) {
                listener.onAdClicked();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void adapterAdDidLoaded(final AdvanceFullScreenItem advanceFullScreenItem, SdkSupplier supplier) {
        try {
            reportAdSucceed(supplier);
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (null != listener) {
                        listener.onAdLoaded(advanceFullScreenItem);
                    }
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void adapterVideoCached() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onVideoCached();
                }
            }
        });

    }

    public void adapterVideoComplete() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onVideoComplete();
                }
            }
        });

    }

    public void adapterClose() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdClose();
                }
            }
        });

    }

    public void adapterVideoSkipped() {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onVideoSkipped();
                }
            }
        });

    }

    @Override
    public boolean isCsjExpress() {
        return isCsjExpress;
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
