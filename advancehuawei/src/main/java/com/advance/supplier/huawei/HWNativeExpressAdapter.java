package com.advance.supplier.huawei;

import android.app.Activity;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.huawei.hms.ads.AdCloseBtnClickListener;
import com.huawei.hms.ads.AdFeedbackListener;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.VideoConfiguration;
import com.huawei.hms.ads.nativead.NativeAd;
import com.huawei.hms.ads.nativead.NativeAdConfiguration;
import com.huawei.hms.ads.nativead.NativeAdLoader;
import com.huawei.hms.ads.nativead.NativeView;
import com.huawei.hms.ads.utils.NativeListener;

public class HWNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    private NativeAd mNativeAd;

    public HWNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
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
        try {
            if (mNativeAd == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "未获取到广告信息"));
            }
            mNativeAd.setNativeListener(new NativeListener(){
                @Override
                public void onAdClicked() {
                    LogUtil.simple(TAG +"点击回调");
                    handleClick();
                }

                @Override
                public void onAdImpression() {
                    LogUtil.simple(TAG +"曝光回调");
                    handleShow();
                }
            });
            NativeView nativeView = new NativeView(getRealContext());
            nativeView.setNativeAd(mNativeAd);
            nativeView.setAdFeedbackListener(new AdFeedbackListener() {
                @Override
                public void onAdFeedbackShowFailed() {
                    LogUtil.simple(TAG+ "AdFeedbackListener, showFeedbackFailed");
                    // ***feedback view弹出失败，可选择自行弹窗负反馈界面
                    //   ***若需要上报负反馈事件，调用NativeView.onClose()接口上报

                    // 也可选择直接移除广告画面
//                    mSetting.adapterDidClosed(nativeView);
                }
                @Override
                public void onAdLiked() {
                    LogUtil.simple(TAG + "AdFeedbackListener, onAdLiked");
                }
                @Override
                public void onAdDisliked() {
                    LogUtil.simple(TAG + "AdFeedbackListener, onAdDisliked");

                }
            });
            nativeView.setAdCloseBtnClickListener(new AdCloseBtnClickListener() {
                @Override
                public void onCloseBtnClick() {
                    LogUtil.simple(TAG + "AdCloseBtnClickListener, onCloseBtnClick");

                    if (mSetting!=null) {
                        mSetting.adapterDidClosed(nativeView);
                    }

                    removeADView();
                }
            });
            addADView(nativeView);

        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        //先执行SDK初始化
        HWUtil.initAD(this);


        String adId = sdkSupplier.adspotid;
//        adId = "testb65czjivt9"; // 原生小图广告
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
        //设置为模板类型
        adParam.setSupportTemplate(true);

        nativeAdLoader.loadAd(adParam.build());


    }

}
