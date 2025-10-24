package com.advance.supplier.huawei;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.huawei.hms.ads.AdCloseBtnClickListener;
import com.huawei.hms.ads.AdFeedbackListener;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.VideoConfiguration;
import com.huawei.hms.ads.nativead.MediaView;
import com.huawei.hms.ads.nativead.NativeAd;
import com.huawei.hms.ads.nativead.NativeAdConfiguration;
import com.huawei.hms.ads.nativead.NativeAdLoader;
import com.huawei.hms.ads.nativead.NativeView;
import com.huawei.hms.ads.utils.NativeListener;

import java.util.ArrayList;

public class HWRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
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
            if (mNativeAd != null) {
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
            mNativeAd.setNativeListener(new NativeListener() {
                @Override
                public void onAdClicked() {
                    LogUtil.simple(TAG + "点击回调");
                    handleClick();
                }

                @Override
                public void onAdImpression() {
                    LogUtil.simple(TAG + "曝光回调");
                    handleShow();
                }
            });
            NativeView nativeView = new NativeView(getRealContext());
            nativeView.setNativeAd(mNativeAd);
            nativeView.setAdFeedbackListener(new AdFeedbackListener() {
                @Override
                public void onAdFeedbackShowFailed() {
                    LogUtil.simple(TAG + "AdFeedbackListener, showFeedbackFailed");
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

                    handleClose();
                }
            });
//            nativeView.set
//        addADView(nativeView);

//            需要先拿到根布局信息
            AdvRFRootView rootView = mAdvanceRFBridge.getMaterialProvider().rootView;
            Activity activity = getRealActivity(rootView);
//            -----------方案B copy全部子布局，复制子控件至新布局，并将新布局添加至旧父布局中
            AdvanceRFUtil.copyChild(rootView, nativeView);

            //添加action交互按钮绑定
            ArrayList<View> creativeViews = mAdvanceRFBridge.getMaterialProvider().creativeViews;
            if (!creativeViews.isEmpty()) {
                nativeView.setCallToActionView(creativeViews.get(0));
            }

            //绑定素材、视频信息
            AdvRFVideoView videoView = mAdvanceRFBridge.getMaterialProvider().videoView;
            if (videoView != null) {

                MediaView mediaView = new MediaView(getRealContext());
                videoView.addView(mediaView);
                nativeView.setMediaView(mediaView);
                nativeView.getMediaView().setMediaContent(mNativeAd.getMediaContent());
            }

            //关闭广告事件绑定
            View dislikeView = mAdvanceRFBridge.getMaterialProvider().disLikeView;
            if (dislikeView != null) {
                dislikeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.simple(TAG + " dislikeView onClick");

                        try {
                            nativeView.removeAllViews();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        handleClose();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));

        }

    }

    private void loadAd() {
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

