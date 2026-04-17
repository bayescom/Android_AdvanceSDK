package com.advance.supplier.flink;

import static com.fl.saas.adx.api.mixNative.NativeAdConst.AD_TYPE_VIDEO;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.widget.AdvRFLogoView;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.fl.saas.adx.api.AdParams;
import com.fl.saas.adx.api.FLSDK;
import com.fl.saas.adx.api.mixNative.NativeAd;
import com.fl.saas.adx.api.mixNative.NativeAdView;
import com.fl.saas.adx.api.mixNative.NativeLoadListener;
import com.fl.saas.adx.api.mixNative.NativeMaterial;
import com.fl.saas.adx.api.mixNative.NativePrepareInfo;
import com.fl.saas.adx.base.exception.FLError;
import com.mercury.sdk.util.MercuryTool;

import java.util.ArrayList;

public class FLRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    NativeAd flAd;

    public FLRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        FLUtil.initAD(this);
        loadAd();
        reportStart();
    }

    private void loadAd() {

        //检查是否命中使用缓存逻辑
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, FLRenderFeedAdapter.class, new BYAbsCallBack<FLRenderFeedAdapter>() {
            @Override
            public void invoke(FLRenderFeedAdapter cacheAdapter) {
                //更新缓存广告得价格
                updateBidding(cacheAdapter.flAd.getECPM());
            }
        });
        if (hitCache) {
            return;
        }

        AdParams params = new AdParams.Builder(getPosID())
//                .setExpressHeight(height) // 期望模板高度，单位dp。
//                .setExpressWidth(width) // 期望模板宽度，单位dp。此参数设置为广告容器的 宽度
                .build();


// 2. 发起请求
        FLSDK.loadMixNative(getRealContext(), params, new NativeLoadListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                LogUtil.simple(TAG + "onNativeAdLoaded");

// 3. 渲染请求成功广告
                if (nativeAd == null) {
                    String nMsg = TAG + " nativeAd null";
                    handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                    return;
                }
                if (!nativeAd.isNativeExpress()) {
                    handleFailed(AdvanceError.ERROR_DATA_NULL, "类型错误，期望模板广告，但返回了自渲染广告");
                    return;
                }
                flAd = nativeAd;

                updateBidding(nativeAd.getECPM());
                dataConverter = new FLRenderDataConverter(nativeAd, sdkSupplier);

                handleSucceed(FLRenderFeedAdapter.this);

            }

            @Override
            public void onAdFailed(FLError error) {
// 4. 请求失败处理
                LogUtil.simple(TAG + "onAdFailed , error = " + error);

                FLUtil.handleErr(FLRenderFeedAdapter.this, error);

            }
        });
        LogUtil.simple(TAG + "loadAd");
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (flAd != null) {
            flAd.destroy();
        }
    }


    @Override
    public void show() {
        LogUtil.simple(TAG + "call show ");
        if (mAdvanceRFBridge == null || flAd == null) {
            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "advanceRFBridge or flAd null");
            return;
        }

        final AdvanceRFMaterialProvider rfMaterialProvider = mAdvanceRFBridge.getMaterialProvider();

        if (rfMaterialProvider == null) {
            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "getMaterialProvider  null");
            return;
        }
        if (rfMaterialProvider.rootView == null) {
            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "请设置  rootView 信息");
            return;
        }

//            需要先拿到根布局信息
        AdvRFRootView rootView = rfMaterialProvider.rootView;
        Activity activity = getRealActivity(rootView);

        // 构造NativeAdViewd对象，广告View均需在此ViewGroup中
        NativeAdView nativeAdView = new NativeAdView(activity);
        ViewGroup adView = new FrameLayout(activity);

//        -----------方案B copy全部子布局，复制子控件至新布局，且’不会‘将新布局添加至旧父布局中
        AdvanceRFUtil.copyChild(rootView, adView, false);

        //广告view（已copy了开发者的自定义布局）添加至容器
        nativeAdView.addView(adView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //广告容器添加至根布局中
        rootView.addView(nativeAdView);

        // 绑定广告容器，第一个参数NativeAdView必须为广告的父容器；第二参数为自定义的广告view，不可为空
        flAd.renderAdContainer(nativeAdView, adView);

        NativeMaterial nativeMaterial = flAd.getAdMaterial();
// 判断广告素材类型，对于视频广告需获取MediaView填充
        if (nativeMaterial.getAdType() == AD_TYPE_VIDEO &&
                nativeMaterial.getAdMediaView() != null) {
            rfMaterialProvider.videoView.addView(nativeMaterial.getAdMediaView());
        }

        bindLogo();

        // 准备渲染广告时需要绑定的控件
        NativePrepareInfo prepareInfo = new NativePrepareInfo();
        prepareInfo.setActivity(activity);

        ArrayList<View> creativeViews = rfMaterialProvider.creativeViews;
        View[] views = new View[creativeViews.size()];
        creativeViews.toArray(views);
        prepareInfo.setCtaView(views);

        ArrayList<View> clickViews = rfMaterialProvider.clickViews;
        View[] clks = new View[clickViews.size()];
        clickViews.toArray(clks);
        prepareInfo.setClickView(clks);

        ArrayList<ImageView> imageViews = rfMaterialProvider.imageViews;
        View[] img = new View[imageViews.size()];
        imageViews.toArray(img);
        prepareInfo.setImageView(img);
// 渲染广告
        flAd.prepare(prepareInfo);
    }

    private void bindLogo() {
        try {
            AdvRFLogoView logoView = mAdvanceRFBridge.getMaterialProvider().logoView;
            if (logoView!= null){
                ImageView recLogo = new ImageView(getRealContext());
                int maxW = BYDisplay.dp2px((25));
                int h = BYDisplay.dp2px((12));
                recLogo.setMaxWidth(maxW);
                recLogo.setAdjustViewBounds(true);

                NativeMaterial nativeMaterial = flAd.getAdMaterial();

                if (nativeMaterial.getAdLogoBitmap() != null) {
                    recLogo.setImageBitmap(nativeMaterial.getAdLogoBitmap());
                } else if (!TextUtils.isEmpty(nativeMaterial.getAdLogoUrl())) {
                    //调用渲染图片方法
                    MercuryTool.renderNetImg(nativeMaterial.getAdLogoUrl(), recLogo);
                }
                logoView.addView(recLogo);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public boolean isValid() {
        if (flAd != null) {
            return flAd.isReady();
        }
        return super.isValid();
    }
}
