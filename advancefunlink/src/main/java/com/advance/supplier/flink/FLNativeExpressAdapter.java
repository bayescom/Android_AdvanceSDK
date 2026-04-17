package com.advance.supplier.flink;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.fl.saas.adx.api.AdParams;
import com.fl.saas.adx.api.FLSDK;
import com.fl.saas.adx.api.mixNative.NativeAd;
import com.fl.saas.adx.api.mixNative.NativeAdView;
import com.fl.saas.adx.api.mixNative.NativeEventListener;
import com.fl.saas.adx.api.mixNative.NativeLoadListener;
import com.fl.saas.adx.api.mixNative.NativePrepareInfo;
import com.fl.saas.adx.base.exception.FLError;

public class FLNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    NativeAd flAd;

    public FLNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
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
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, FLNativeExpressAdapter.class, new BYAbsCallBack<FLNativeExpressAdapter>() {
            @Override
            public void invoke(FLNativeExpressAdapter cacheAdapter) {
                //更新缓存广告得价格
                updateBidding(cacheAdapter.flAd.getECPM());
            }
        });
        if (hitCache) {
            return;
        }

        int width = mSetting.getExpressViewWidth();
        int height = mSetting.getExpressViewHeight();
//        if (mSetting.getGdtAutoHeight()) {
//        }
//        //如果宽度为默认值，也按照填满配置，避免出现截断现象
//        if (mSetting.getGdtFullWidth() || 360 == width) {
//        }

        AdParams params = new AdParams.Builder(getPosID())
                .setExpressHeight(height) // 期望模板高度，单位dp。
                .setExpressWidth(width) // 期望模板宽度，单位dp。此参数设置为广告容器的 宽度
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
                handleSucceed(FLNativeExpressAdapter.this);

            }

            @Override
            public void onAdFailed(FLError error) {
// 4. 请求失败处理
                LogUtil.simple(TAG + "onAdFailed , error = " + error);

                FLUtil.handleErr(FLNativeExpressAdapter.this, error);

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
        if (flAd != null) {
            flAd.setNativeEventListener(new NativeEventListener() {
                @Override
                public void onAdImpressed(NativeAdView nativeAdView) {
                    LogUtil.simple(TAG + "onAdImpressed");

                    handleShow();
                }

                @Override
                public void onAdClicked(NativeAdView nativeAdView) {
                    LogUtil.simple(TAG + "onAdClicked");

                    handleClick();
                }

                @Override
                public void onAdClose(NativeAdView nativeAdView) {
                    LogUtil.simple(TAG + "onAdClose");

                    handleClose();
                }

                @Override
                public void onAdVideoStart(NativeAdView nativeAdView) {
                    LogUtil.simple(TAG + "onAdVideoStart");

                }

                @Override
                public void onAdVideoEnd(NativeAdView nativeAdView) {
                    LogUtil.simple(TAG + "onAdVideoEnd");

                }

                @Override
                public void onAdVideoProgress(NativeAdView nativeAdView, long l) {
                    LogUtil.simple(TAG + "onAdVideoProgress");

                }

                @Override
                public void onAdFailed(NativeAdView nativeAdView, FLError flError) {
                    LogUtil.simple(TAG + "show onAdFailed , error = " + flError);

                    FLUtil.handleErr(FLNativeExpressAdapter.this, flError);


                }
            });

            // 对于模板广告只需获取MediaView填充到广告容器中即可,模版广告展示时高度最好设置为自适应。
// 因为返回的模版高度可能不是传入的高度，避免广告显示不全导致产生问题
            View mediaView = flAd.getAdMaterial().getAdMediaView();
            Activity activity = getRealActivity(mSetting.getAdContainer());


            NativeAdView nativeAdView = new NativeAdView(activity);
            nativeAdView.addView(mediaView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
// 绑定模板广告容器，对于模板广告，第二参数selfRenderView可为null
            flAd.renderAdContainer(nativeAdView, null);
            NativePrepareInfo prepareInfo = new NativePrepareInfo();
// 渲染模板需绑定Activity
            prepareInfo.setActivity(activity);
// 渲染模板广告
            flAd.prepare(prepareInfo);

            //添加布局
            addADView(nativeAdView);
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
