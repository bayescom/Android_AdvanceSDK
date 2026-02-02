package com.advance;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.itf.NativeExpressGMCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;

import java.util.List;

public class AdvanceNativeExpress extends AdvanceBaseAdspot implements NativeExpressSetting {
    private AdvanceNativeExpressListener listener;
    private int csjImageWidth = 640;
    private int csjImageHeight = 320;
    private int expressViewWidth = 360; //4.2.3 宽度默认dp修改为360
    private boolean userSetWh = false;
    private int expressViewHeight = 0;
    //无用设置，推荐仅设置expressViewsize
    @Deprecated
    private boolean gdtfullWidth = false;
    @Deprecated
    private boolean gdtAutoHeight = false;
    // TODO: 2023/5/24 聚合静音部分设置、自动播放设置等方法
    private boolean videoMute = true;
    private int gdtMaxVideoDuration = 60;
    private ViewGroup adContainer;
    private NativeExpressGMCallBack expressGMCallBack;

    @Deprecated
    public AdvanceNativeExpress(Activity activity, String mediaId, String adspotId) {
        super(activity, mediaId, adspotId);
    }

    public AdvanceNativeExpress(Activity activity, String adspotId) {
        this(activity, "", adspotId);
    }


    public AdvanceNativeExpress setCsjImageAcceptedSize(int width, int height) {
        this.csjImageWidth = width;
        this.csjImageHeight = height;
        return this;
    }

    //    v3.5.3新增 设置用来展示广告的父布局
    public void setAdContainer(ViewGroup container) {
        adContainer = container;

        try {
            if (userSetWh) {
                LogUtil.devDebug("用户设置了具体宽高，不再自动检查布局的宽高了 ");
                return;
            }
            adContainer.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (userSetWh) {
                            LogUtil.devDebug("用户设置了具体宽高，不再自动检查布局的宽高了 2 ");
                            return;
                        }
                        //     宽高度需考虑含padding自适应布局方式。
                        int paddingH = adContainer.getPaddingBottom() + adContainer.getPaddingTop();
                        int paddingW = adContainer.getPaddingLeft() + adContainer.getPaddingRight();
                        LogUtil.devDebug("paddingW = " + paddingW + " , paddingH = " + paddingH);


                        int showW = adContainer.getWidth() - paddingW;
                        int showH = adContainer.getHeight() - paddingH;
                        if (showW > 0) {
                            expressViewWidth = BYDisplay.px2dp(showW);
                            LogUtil.devDebug("set expressViewWidth as adContainer Width= " + expressViewWidth);
                        }
                        if (showH > 0) {
                            expressViewHeight = BYDisplay.px2dp(showH);
                            LogUtil.devDebug("set expressViewHeight as adContainer Height= " + expressViewHeight);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public int getCsjImageWidth() {
        return csjImageWidth;
    }

    public int getCsjImageHeight() {
        return csjImageHeight;
    }

    @Override
    public ViewGroup getAdContainer() {
        return adContainer;
    }

    public AdvanceNativeExpress setExpressViewAcceptedSize(int width, int height) {
        userSetWh = true;
        this.expressViewWidth = width;
        this.expressViewHeight = height;
        return this;
    }

    public int getExpressViewWidth() {
        return expressViewWidth;
    }

    public int getExpressViewHeight() {
        return expressViewHeight;
    }

    @Deprecated
    public AdvanceNativeExpress setGdtFullWidth(boolean isFullWidth) {
        this.gdtfullWidth = isFullWidth;
        return this;
    }

    @Deprecated
    public AdvanceNativeExpress setGdtAutoHeight(boolean isAutoHeight) {
        this.gdtAutoHeight = isAutoHeight;
        return this;
    }

    public boolean getGdtAutoHeight() {
        return this.gdtAutoHeight;
    }

    public boolean getGdtFullWidth() {
        return this.gdtfullWidth;
    }


    @Override
    public boolean isVideoMute() {
        return videoMute;
    }

    //v3.5.3新增 设置视频类广告是否静音，默认true
    public AdvanceNativeExpress setVideoMute(boolean mute) {
        this.videoMute = mute;
        return this;
    }

    public AdvanceNativeExpress setGdtMaxVideoDuration(int gdtMaxVideoDuration) {
        this.gdtMaxVideoDuration = gdtMaxVideoDuration;
        return this;
    }

    public int getGdtMaxVideoDuration() {
        return gdtMaxVideoDuration;
    }

    public void setAdListener(AdvanceNativeExpressListener listener) {
        advanceSelectListener = listener;
        this.listener = listener;
    }

    @Override
    public void initSdkSupplier() {
        try {
            //配置渠道信息
            initSupplierAdapterList();

            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_GDT, "gdt.GdtNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_MERCURY, "mry.MercuryNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_KS, "ks.KSNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TANX, "tanx.TanxNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_OPPO, "oppo.OppoNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_HW, "huawei.HWNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_XIAOMI, "mi.XMNativeExpressAdapter");

            initAdapter(AdvanceConfig.SDK_ID_HONOR, "honor.HonorNativeExpressAdapter");
            initAdapter(AdvanceConfig.SDK_ID_VIVO, "vv.VivoNativeExpressAdapter");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getNativeAdapter(clzName, getADActivity(), this));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void selectSdkSupplierFailed() {
        onAdvanceError(listener, AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT_FAILED));
    }

    public void adapterDidShow(final View view, SdkSupplier supplier) {
        reportAdShow(supplier);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdShow(view);
                }
                if (expressGMCallBack != null) {
                    expressGMCallBack.onAdShow();
                }
            }
        });

    }

    public void adapterDidClosed(final View view) {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdClose(view);
                }
            }
        });

    }

    public void adapterRenderFailed(final View view) {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdRenderFailed(view);
                }
            }
        });

    }

    public void adapterRenderSuccess(final View view) {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdRenderSuccess(view);
                }
                if (expressGMCallBack != null) {
                    expressGMCallBack.onAdRenderSuccess();
                }
            }
        });

    }

    public void adapterDidClicked(final View view, SdkSupplier supplier) {
        reportAdClicked(supplier);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdClicked(view);
                }
                if (expressGMCallBack != null) {
                    expressGMCallBack.onAdClick();
                }

            }
        });

    }

    public void adapterAdDidLoaded(final List<AdvanceNativeExpressAdItem> advanceNativeExpressAdItemList, SdkSupplier supplier) {
        reportAdSucceed(supplier);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdLoaded(advanceNativeExpressAdItemList);
                }

                if (expressGMCallBack != null) {
                    expressGMCallBack.onAdSuccess();
                }
            }
        });
    }

    //设置gm监听回调
    public void setExpressGMCallBack(NativeExpressGMCallBack expressGMCallBack) {
        this.expressGMCallBack = expressGMCallBack;
        setBaseGMCall(expressGMCallBack);
    }
}
