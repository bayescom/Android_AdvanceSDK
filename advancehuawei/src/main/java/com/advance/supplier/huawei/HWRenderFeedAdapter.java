package com.advance.supplier.huawei;

import android.content.Context;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.utils.LogUtil;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.VideoConfiguration;
import com.huawei.hms.ads.nativead.NativeAd;
import com.huawei.hms.ads.nativead.NativeAdConfiguration;
import com.huawei.hms.ads.nativead.NativeAdLoader;

public class HWRenderFeedAdapter  extends AdvanceSelfRenderCustomAdapter {
    private NativeAd mNativeAd;

    public HWRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    protected void paraLoadAd() {
        loadAd();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (mNativeAd!=null){
                mNativeAd.destroy();
            }
            removeADView();
        } catch (Exception e) {
        }
    }

    @Override
    public void orderLoadAd() {
        loadAd();
    }

    @Override
    public void show() {



    }

    private void loadAd(){
        //先执行SDK初始化
        HWUtil.initAD(this);

        String adId = sdkSupplier.adspotid;
        NativeAdLoader.Builder builder = new NativeAdLoader.Builder(getRealContext(), adId);
        builder.setNativeAdLoadedListener(new NativeAd.NativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                mNativeAd = nativeAd;
                // Call this method when an ad is successfully loaded.
                LogUtil.simple(TAG + " onNativeAdLoaded , nativeAd = " + nativeAd);

                if (nativeAd != null) {
                    updateBidding(HWUtil.getPrice(nativeAd.getBiddingInfo()));

                    //原生模板广告为 99
                    int createType = nativeAd.getCreativeType();
                    LogUtil.simple(TAG + "Native ad createType is " + createType);
                }

                //转换广告model为聚合通用model
                dataConverter = new HWRenderDataConverter(nativeAd, HWRenderFeedAdapter.this);

                handleSucceed();


            }
        }).setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {

                LogUtil.simple(TAG + "  onAdLoaded");

            }

            @Override
            public void onAdFailed(int errorCode) {
                // Call this method when an ad fails to be loaded.

                LogUtil.simple(TAG + " onAdFailed , errorCode = " + errorCode);
                handleFailed(errorCode, " onAdFailed");

            }
        });

        VideoConfiguration.Builder videoConfiguration = AdvanceHWManager.getInstance().globalVideoConfigBuilder;
        if (videoConfiguration == null) {
            videoConfiguration = new VideoConfiguration.Builder()
                    .setStartMuted(true);
        }

        NativeAdConfiguration.Builder nativeConfig = AdvanceHWManager.getInstance().nativeConfigBuilder;
        if (nativeConfig == null) {
            nativeConfig = new NativeAdConfiguration.Builder()
                    .setChoicesPosition(NativeAdConfiguration.ChoicesPosition.BOTTOM_RIGHT) // Set custom attributes.
                    .setVideoConfiguration(videoConfiguration.build());
        }

        nativeConfig.setVideoConfiguration(videoConfiguration.build());

        NativeAdLoader nativeAdLoader = builder
                .setNativeAdOptions(nativeConfig.build())
                .build();

        AdParam.Builder adParam = AdvanceHWManager.getInstance().globalAdParamBuilder;
        if (adParam == null) {
            adParam = new AdParam.Builder();
        }
        //设置为非模板类型
        adParam.setSupportTemplate(false);

        nativeAdLoader.loadAd(adParam.build());
    }
}

