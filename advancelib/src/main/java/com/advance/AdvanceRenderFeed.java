package com.advance;

import android.content.Context;

import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFEventListener;
import com.advance.core.srender.AdvanceRFLoadListener;
import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYThreadUtil;

public class AdvanceRenderFeed extends AdvanceBaseAdspot implements AdvanceRFBridge {
    //广告尺寸像素默认值
    private int adSizePxW = 640;
    private int adSizePxH = 320;
    AdvanceRFLoadListener mLoadListener;
    AdvanceRFEventListener mRenderListener;
    AdvanceRFMaterialProvider rfMaterialProvider;
    AdvanceRFADData mRenderADData;

    public AdvanceRenderFeed(Context context, String adspotid) {
        super(context, adspotid);

    }

    //    监听广告返回
    public void setLoadListener(final AdvanceRFLoadListener loadListener) {
        this.mLoadListener = loadListener;

        advanceSelectListener = new AdvanceSelectListener() {
            @Override
            public void onSdkSelected(String id) {

            }

            @Override
            public void onAdFailed(AdvanceError advanceError) {
                if (loadListener != null) {
                    loadListener.onAdFailed(advanceError);
                }
            }
        };
    }

    public void setRenderEventListener(AdvanceRFEventListener renderEventListener) {
        this.mRenderListener = renderEventListener;
    }

    //    设置view信息，透传给广告SDK用来渲染
    public void setRfMaterialProvider(AdvanceRFMaterialProvider provider) {
        this.rfMaterialProvider = provider;
    }

    //    图片期望大小信息，主要是设置给穿山甲使用，不设置将使用默认值
    public void setCsjImgSize(int w, int h) {
        this.adSizePxW = w;
        this.adSizePxH = h;
    }

    @Override
    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getRenderFeedAdapter(clzName, mContext, this));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initSdkSupplier() {
        try {
            initSupplierAdapterList();

            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_GDT, "gdt.GdtRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_MERCURY, "mry.MercuryRenderFeedAdapter");

            initAdapter(AdvanceConfig.SDK_ID_KS, "ks.KSRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TANX, "tanx.TanxRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_OPPO, "oppo.OppoRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_SIG, "sigmob.SigmobRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_HW, "huawei.HWRenderFeedAdapter");

            initAdapter(AdvanceConfig.SDK_ID_HONOR, "honor.HonorRenderFeedAdapter");
            initAdapter(AdvanceConfig.SDK_ID_VIVO, "vv.VivoRenderFeedAdapter");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void selectSdkSupplierFailed() {
        onAdvanceError(mLoadListener, AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT_FAILED));
    }


    @Override
    public int getADSizeW() {
        return adSizePxW;
    }

    @Override
    public int getADSizeH() {
        return adSizePxH;
    }

    @Override
    public void adapterDidLoaded(AdvanceRFADData renderADData) {
        try {
            this.mRenderADData = renderADData;
            //返回时保证在主线程
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (mLoadListener != null) {
                        mLoadListener.onADLoaded(mRenderADData);
                    }
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void adapterDidClose(SdkSupplier sdkSupplier) {
//返回时保证在主线程
        try {
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (mRenderListener != null) {
                        mRenderListener.onAdClose(mRenderADData);
                    }

                    //广告关闭，将执行view清除以及广告destroy逻辑
                    destroy();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public AdvanceRFMaterialProvider getMaterialProvider() {
        return rfMaterialProvider;
    }

    @Override
    public void adapterDidSucceed(SdkSupplier supplier) {
        //此事件 在adapterDidLoaded 回调之后执行得
        reportAdSucceed(supplier);
    }

    @Override
    public void adapterDidShow(SdkSupplier supplier) {
        try {
            reportAdShow(supplier);
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (mRenderListener != null) {
                        mRenderListener.onAdShow(mRenderADData);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void adapterDidClicked(SdkSupplier supplier) {
        try {
            reportAdClicked(supplier);
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (mRenderListener != null) {
                        mRenderListener.onAdClicked(mRenderADData);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
