package com.advance;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import androidx.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.advance.core.splash.AdvanceSplashRenderListener;
import com.advance.itf.AdvanceLifecycleCallback;
import com.advance.itf.SplashJumpType;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.itf.SplashGMCallBack;
import com.advance.itf.SplashJumpListener;
import com.advance.model.AdStatus;
import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceLoader;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;

import java.lang.ref.SoftReference;


public class AdvanceSplash extends AdvanceBaseAdspot implements SplashSetting {
    private static String TAG = "[" + AdvanceSplash.class.getSimpleName() + "] ";
    private AdvanceSplashListener listener;
    private ViewGroup adContainer;
    private ViewGroup adRealContainer;
    private TextView skipView;
    //    private View gdtSkipContainer;
    private String skipText = "跳过 %d";
    private int csjAcceptedSizeWidth = 1080;
    private int csjAcceptedSizeHeight = 1920;
    private float csjExpressViewWidth = 1080;
    private float csjExpressViewHeight = 1920;
    private boolean csjShowAsExpress = false; //设置穿山甲广告是否以个性化模板形式加载广告
    private String oaid = "";
    private Drawable logoImage;
    private Drawable holderImage;
    private boolean gdtClickAsSkip = true; //广点通点击广告后会自动关闭广告，基于爱尚需求这里默认点击导致的关闭为倒计时回调。23-05-23 修改默认值为true
//    private boolean gdtCustomSkipHide = false; //广点通自定义跳过是否提前隐藏。
//    private boolean csjTimeOutQuit = false; //穿山甲返回time out回调时，是否退出策略，true 退出false 继续加载下个策略，false。

    public boolean canJump = false;
    public boolean hasJump = false;
    public boolean immediatelyJump = false; //todo 对于点击了广告，返回开屏页面时，部分adn（穿山甲、倍业、百度、快手）可能还在倒计时状态。此时是否需要直接执行跳转首页
    private boolean showInSingleActivity = false;
    private SplashGMCallBack splashGMCallBack;
    private ImageView gdtHolderView;

    public SplashJumpListener jumpListener;
    private int logoLayout = 0;
    //    传入布局载体的高度
    private int logoLayoutHeight = 0;
    //    logo布局载体
    private View logoLayoutView = null;
    private boolean onlyMercuryLogoShow = false;
    //广告跳转类型，默认是广告失败
    private int jumpType = SplashJumpType.TYPE_AD_FAILED;


    public AdvanceSplash(Activity activity, String adspotId, ViewGroup adContainer, TextView skipView) {
        super(new SoftReference<>(activity), "", adspotId);
        initSplash(adContainer, skipView);
    }

    //     分离模式优化新增内容 ------------start -----------  todo 后续可能需要废弃掉旧接口方法，全部改用新接口方法
    private AdvanceSplashRenderListener mSplashRenderListener;

    /**
     * 分离模式优化，可以不强依赖activity信息，先进行初始化以及策略请求
     *  todo 测试各个SDK，在application中子线程下进行广告load是否有问题
     */

    public AdvanceSplash(String adspotId) {
        super(adspotId);
        initSplash(null, null);
    }


    //避免调用出错，需要额外进行检测并打印日志提示开发者，使用正确的调用广告
    @Override
    public void show() {
        try {
            if (adContainer == null) {
                LogUtil.e("未检测到广告布局，请使用 show(ViewGroup adContainer) 方法调用展示，或者检查初始化是否传递了广告布局信息。");
                return;
            }
            super.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //分离模式下的广告展示方法，需要传递广告布局信息
    public void show(ViewGroup adContainer) {
        this.adContainer = adContainer;
        this.adRealContainer = adContainer;

        if (isSplitLoad) {
            reInit(adContainer);
            //需要等view确定好了之后，再去show
            initView(new BYBaseCallBack() {
                @Override
                public void call() {
                    AdvanceSplash.super.show();
                }
            });
            return;
        }
        super.show();
    }

    //分离模式下设置自定义得跳过view，目前仅倍业SDK生效
    public void setCustomSkipView(TextView skipView) {
        this.skipView = skipView;
    }

//    public void setRenderListener(AdvanceSplashRenderListener renderListener){
//        mSplashRenderListener  = renderListener;
//    }
//     分离模式优化新增方法 ------------start -----------

    private void initSplash(ViewGroup adContainer, TextView skipView) {
        try {
            this.adContainer = adContainer;
            this.adRealContainer = adContainer;
            this.skipView = skipView;

            this.needDelay = true;
            needForceLoadAndShow = true;
            isSplash = true;
            ownLifecycleCallback = new AdvanceLifecycleCallback() {
                @Override
                public void onActivityResumed() {
                    if (canJump) {
                        doJump();
                    }
                    canJump = true;
                }

                @Override
                public void onActivityPaused() {
                    //穿山甲和tanx 都需要重置跳转状态
                    canJump = TextUtils.equals(currentSDKId, AdvanceConfig.SDK_ID_TANX);
                }

                @Override
                public void onActivityDestroyed() {
                    reportCloseEvent();
//                        清除回调
                    listener = null;
                    gdtHolderView = null;
                }
            };

            //初始化宽高设置为全屏尺寸
            csjAcceptedSizeWidth = BYDisplay.getScreenWPx();
            csjAcceptedSizeHeight = BYDisplay.getScreenHPx();

            csjExpressViewWidth = BYDisplay.px2dp(csjAcceptedSizeWidth);
            csjExpressViewHeight = BYDisplay.px2dp(csjAcceptedSizeHeight);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void reportCloseEvent() {
        try {
            if (adStatus == AdStatus.DEFAULT || adStatus == AdStatus.START) {//广告仍在请求中、页面被关闭属于强关？
                String msg = "检测到app强关事件";
                LogUtil.devDebug(TAG + msg);
                AdvanceReportModel report = new AdvanceReportModel();
                report.code = AdvanceConstant.TRACE_SPLASH_FORCE_CLOSE;
                report.msg = msg;
                //开屏需要延迟进行上报
                report.needDelay = true;
                doTrackReport(report);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setSplashGMCallBack(SplashGMCallBack callBack) {
        splashGMCallBack = callBack;
        setBaseGMCall(splashGMCallBack);
    }

    public void setLoadAsync(boolean isLoadAsync) {
        loadWithAsync = isLoadAsync;
    }

    public AdvanceSplash setSkipText(String skipText) {
        this.skipText = skipText;
        return this;
    }


    public void setGDTSplashHolder(ImageView splashHolder) {
        gdtHolderView = splashHolder;
    }

    public ImageView getGDTHolderView() {
        return gdtHolderView;
    }


    @Deprecated
    public AdvanceSplash setOaid(String oaid) {
        this.oaid = oaid;
        return this;
    }

    @Deprecated
    public String getOaid() {
        return this.oaid;
    }

    @Deprecated
    public AdvanceSplash setLogoImage(Drawable logoImage) {
        this.logoImage = logoImage;
        return this;
    }

    @Deprecated
    public Drawable getLogoImage() {
        return logoImage;
    }

    @Deprecated
    public AdvanceSplash setHolderImage(Drawable holderImage) {
        this.holderImage = holderImage;
        return this;
    }

    public Drawable getHolderImage() {
        return this.holderImage;
    }


    //   4.1.2 新增  开屏底部logo区域设置，使用layout资源引入，固定好具体高度
    public void setLogoLayout(@LayoutRes int logoLayout, int heightPX) {
        this.logoLayout = logoLayout;
        logoLayoutHeight = heightPX;
    }

    //   4.1.2 新增 是否仅Mercury渠道使用logo配置，其他adn将使用全屏展示
    public void setOnlyMercuryUserLogo(boolean onlyMercuryLogoShow) {
        this.onlyMercuryLogoShow = onlyMercuryLogoShow;
    }

    public boolean isOnlyMercuryLogoShow() {
        return onlyMercuryLogoShow;
    }

    public void setShowInSingleActivity(boolean single) {
        showInSingleActivity = single;
//        AdvanceSetting.getInstance().isSplashShowInSingleActivity = single;
        canJump = single;
    }

    public AdvanceSplash setCsjAcceptedSize(int width, int height) {
        this.csjAcceptedSizeWidth = width;
        this.csjAcceptedSizeHeight = height;
        return this;
    }

    //穿山甲后台暂时不支持开屏模板广告，代码先加上相关判断，不影响现有展示。
    public AdvanceSplash setCsjExpressViewAcceptedSize(float expressViewWidth, float expressViewHeight) {
        csjShowAsExpress = true;
        this.csjExpressViewWidth = expressViewWidth;
        this.csjExpressViewHeight = expressViewHeight;
        LogUtil.devDebug(TAG + "setCsjExpressViewAcceptedSize : expressViewWidth = " + expressViewWidth + " , expressViewHeight = " + expressViewHeight);
        return this;
    }

    public void setAdListener(AdvanceSplashListener listener) {
        advanceSelectListener = listener;
        this.listener = listener;
    }

    //获取跳转类型，在SDK回调jumpToMain时调用，可以用来判断用户是否点击了跳过广告
    public int getJumpType() {
        return jumpType;
    }

    public void setGdtClickAsSkip(boolean gdtClickAsSkip) {
        this.gdtClickAsSkip = gdtClickAsSkip;
    }

    @Override
    public void initSdkSupplier() {
        try {
            initSupplierAdapterList();

            initAdapter(AdvanceConfig.SDK_ID_CSJ, "csj.CsjSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_GDT, "gdt.GdtSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_MERCURY, "mry.MercurySplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_KS, "ks.KSSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TANX, "tanx.TanxSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_TAP, "tap.TapSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_OPPO, "oppo.OppoSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_SIG, "sigmob.SigmobSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_HW, "huawei.HWSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_XIAOMI, "mi.XMSplashAdapter");

            initAdapter(AdvanceConfig.SDK_ID_HONOR, "honor.HonorSplashAdapter");
            initAdapter(AdvanceConfig.SDK_ID_VIVO, "vv.VivoSplashAdapter");
        } catch (Throwable e) {
//            e.printStackTrace();
        }
    }

    @Override
    public void loadAd(int adCount) {
        canJump = false;
        super.loadAd(adCount);
//        初始化logo信息，以及广告尺寸信息
        initView(null);
    }

    private void initView(final BYBaseCallBack callBack) {
        try {
            //给宽高赋值为父布局宽高（如果>0）
            if (adContainer != null) {
                adContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            try {
                                //                    承载布局原始宽高
                                int oriW = adContainer.getWidth();
                                int oriH = adContainer.getHeight();


                                Activity activity = getRealActivity(adContainer);

                                RelativeLayout wrapC = new RelativeLayout(activity);
                                if (oriW > 0) {
                                    csjAcceptedSizeWidth = oriW;
                                    LogUtil.devDebug(TAG + "use adContainer width ,csjAcceptedSizeWidth:" + csjAcceptedSizeWidth);
                                }
                                if (logoLayoutHeight > 0 && !isOnlyMercuryLogoShow()) { //设置了广告logo，并行需要进行logo的展示
                                    int adHeight = ViewGroup.LayoutParams.MATCH_PARENT;
                                    if (oriH > 0) {
                                        adHeight = oriH - logoLayoutHeight;
                                        if (adHeight > 0) {
                                            //赋值新的高度像素值
                                            csjAcceptedSizeHeight = adHeight;
                                            LogUtil.devDebug(TAG + "use new height ,csjAcceptedSizeHeight:" + csjAcceptedSizeHeight);

                                            //赋值新的承载布局
                                            adRealContainer = new FrameLayout(activity);
                                        } else {
                                            adHeight = ViewGroup.LayoutParams.MATCH_PARENT;
                                        }
                                    }
                                    RelativeLayout.LayoutParams adLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, adHeight);
                                    wrapC.addView(adRealContainer, adLp);

                                    //                            添加广告布局至底部
                                    RelativeLayout.LayoutParams logoLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, logoLayoutHeight);
                                    logoLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                    if (logoLayoutView == null) {
                                        logoLayoutView = LayoutInflater.from(activity).inflate(logoLayout, null);
                                    }
                                    wrapC.addView(logoLayoutView, logoLp);

                                    adContainer.addView(wrapC, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                } else { //未设置logo ,使用原始得布局以及宽高信息
                                    if (oriH > 0) {
                                        csjAcceptedSizeHeight = oriH;
                                        LogUtil.devDebug(TAG + "use adContainer height ,csjAcceptedSizeHeight:" + csjAcceptedSizeHeight);
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            if (callBack != null)
                                callBack.call();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                if (callBack != null)
                    callBack.call();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (callBack != null)
                callBack.call();
        }
    }


    @Override
    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {
        try {
            supplierAdapters.put(sdkSupplier.priority + "", AdvanceLoader.getSplashAdapter(clzName, softReferenceActivity, this));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 统一处理跳转逻辑
     */
    private synchronized void doJump() {
        try {
            BYBaseCallBack ensureListener = new BYBaseCallBack() {
                @Override
                public void call() {
                    LogUtil.high("[AdvanceSplash] canJump = " + canJump);
                    if (canJump) {
                        LogUtil.high("[AdvanceSplash] hasJump = " + hasJump);
                        //避免重复回调
                        if (hasJump) {
                            return;
                        }
                        hasJump = true;
                        if (listener != null)
                            listener.jumpToMain();
                        //全部失败是不进行事件回调，因为gm可能并没有失败，不需要跳转主页
                        if (jumpListener != null) {
                            jumpListener.jumpMain();
                        }
                    } else {
                        canJump = true;
                    }
                }
            };
            BYThreadUtil.switchMainThread(ensureListener);
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
        //防止无法跳转
        if (!isSplitLoad) {
            canJump = true;
        }
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != listener) {
                    listener.onAdLoaded();
                }
//                if (mLoadListener != null) {
//                    mLoadListener.onLoaded(mSuccessData);
//                }
                if (splashGMCallBack != null) {
                    splashGMCallBack.onAdSuccess();
                }
            }
        });

    }

    @Override
    public void adapterDidShow(SdkSupplier supplier) {
        reportAdShow(supplier);
        //防止无法跳转
        canJump = true;
        if (null != listener) {
            listener.onAdShow();
        }
        if (splashGMCallBack != null) {
            splashGMCallBack.onAdShow();
        }
    }

    @Override
    public void adapterDidSkip() {
        jumpType = SplashJumpType.TYPE_CLICK_SKIP;
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
//                if (null != listener) {
//                    listener.onAdSkip();
//                }
                if (splashGMCallBack != null) {
                    splashGMCallBack.onAdSkip();
                }
            }
        });

        doJump();
    }

    @Override
    public void adapterDidQuit() {
        adapterDidFailed(AdvanceError.parseErr(AdvanceError.ERROR_CSJ_SKIP));
    }

    @Override
    public void adapterDidTimeOver() {
        jumpType = SplashJumpType.TYPE_TIME_OVER;
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
//                if (null != listener) {
//                    listener.onAdTimeOver();
//                }
                if (splashGMCallBack != null) {
                    splashGMCallBack.onAdTimeOver();
                }
            }
        });

        doJump();
    }

    @Override
    public void adapterDidClicked(SdkSupplier supplier) {
        reportAdClicked(supplier);
        if (null != listener) {
            listener.onAdClicked();
        }
        if (splashGMCallBack != null) {
            splashGMCallBack.onAdClick();
        }
    }

    @Override
    public int getLogoLayoutRes() {
        return logoLayout;
    }

    @Override
    public int getLogoLayoutHeight() {
        return logoLayoutHeight;
    }

    @Override
    public boolean isShowInSingleActivity() {
        return showInSingleActivity;
    }

    @Override
    public boolean getCsjShowAsExpress() {
        return csjShowAsExpress;
    }

    @Override
    public int getCsjAcceptedSizeWidth() {
        return csjAcceptedSizeWidth;
    }

    @Override
    public int getCsjAcceptedSizeHeight() {
        return csjAcceptedSizeHeight;
    }

    @Override
    public float getCsjExpressViewWidth() {
        return csjExpressViewWidth;
    }

    @Override
    public float getCsjExpressViewHeight() {
        return csjExpressViewHeight;
    }

    @Override
    public boolean isGdtClickAsSkip() {
        return gdtClickAsSkip;
    }

    @Override
    public ViewGroup getAdContainer() {
        if (onlyMercuryLogoShow) {
            return adContainer;
        }
        return adRealContainer;
    }

    @Override
    public ViewGroup getAdContainerOri() {
        return adContainer;
    }

    @Override
    public TextView getSkipView() {
        return skipView;
    }

    @Override
    public String getSkipText() {
        return skipText;
    }


//   以下为 v4.2.3 及之后版本中移除接口

//    public boolean isGdtCustomSkipHide() {
//        return gdtCustomSkipHide;
//    }

//    public void setGdtCustomSkipHide(boolean gdtCustomSkipHide) {
//        this.gdtCustomSkipHide = gdtCustomSkipHide;
//    }
//
//    @Override
//    public boolean isCsjTimeOutQuit() {
//        return csjTimeOutQuit;
//    }
//
//
//    public void setCsjTimeOutQuit(boolean csjTimeOutQuit) {
//        this.csjTimeOutQuit = csjTimeOutQuit;
//    }


//    public View getGdtSkipContainer() {
//        return gdtSkipContainer;
//    }
//
//    public void setGdtSkipContainer(View gdtSkipContainer) {
//        this.gdtSkipContainer = gdtSkipContainer;
//    }

}
