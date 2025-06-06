package com.advance.supplier.ks;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.annotation.Nullable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFConstant;
import com.advance.core.srender.AdvanceRFDownloadListener;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.core.srender.AdvanceRFVideoOption;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsAdVideoPlayConfig;
import com.kwad.sdk.api.KsApkDownloadListener;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsNativeAd;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.model.AdSourceLogoType;
import com.kwad.sdk.api.model.KsNativeConvertType;
import com.mercury.sdk.util.MercuryTool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 重要！！！ 快手无法在同一并发层设置2个及以上【快手SDK】渠道，否则返回的广告信息会出现错乱
 */
public class KSRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    KsNativeAd nativeAd;

    public KSRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
            String cause = e.getCause() != null ? e.getCause().toString() : "no cause";
            reportCodeErr(TAG + " Throwable" + cause);
        }
    }

    @Override
    protected void paraLoadAd() {
        KSUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
                startLoad();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });

    }

    private void startLoad() {
        //场景设置
        KsScene scene = new KsScene.Builder(KSUtil.getADID(sdkSupplier)).build(); // 此为测试posId，请联系快手平台申请正式posId
        KsAdSDK.getLoadManager().loadNativeAd(scene, new KsLoadManager.NativeAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogUtil.simple(TAG + " onError ");

                handleFailed(code, msg);
            }

            @Override
            public void onNativeAdLoad(@Nullable List<KsNativeAd> list) {
                try {
                    LogUtil.simple(TAG + "onNativeAdLoad");

                    if (list == null || list.size() == 0 || list.get(0) == null) {
                        String nMsg = TAG + " KsSplashScreenAd null";
                        handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                        return;
                    }
                    nativeAd = list.get(0);
                    updateBidding(nativeAd.getECPM());
                    //转换广告model为聚合通用model
                    dataConverter = new KSRenderDataConverter(nativeAd, sdkSupplier);

                    handleSucceed();

                } catch (Throwable e) {
                    e.printStackTrace();
                    runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                }
            }
        });
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {


    }

    @Override
    public void show() {
        try {

            if (AdvanceRFUtil.skipRender(this)) {
                LogUtil.d(TAG + " skipRender");
                return;
            }
            final AdvanceRFMaterialProvider rfMaterialProvider = mAdvanceRFBridge.getMaterialProvider();

            if (nativeAd == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "未获取到广告信息");
                return;
            }
            //打印当前渲染的广告信息
//            if (BYUtil.isDev()){
//                KSRenderDataConverter converter = (KSRenderDataConverter)dataConverter;
//                converter.printAdInf();
//            }

            Activity activity = getRealActivity(rfMaterialProvider.rootView);

            //核心：注册view及响应事件
            nativeAd.registerViewForInteraction(activity, rfMaterialProvider.rootView, getClickMap(rfMaterialProvider), new KsNativeAd.AdInteractionListener() {
                @Override
                public void onAdClicked(View view, KsNativeAd ksNativeAd) {
                    LogUtil.simple(TAG + "onAdClicked");

                    handleClick();
                }

                @Override
                public void onAdShow(KsNativeAd ksNativeAd) {
                    LogUtil.simple(TAG + "onShow");

                    handleShow();
                }

                @Override
                public boolean handleDownloadDialog(DialogInterface.OnClickListener onClickListener) {
                    LogUtil.simple(TAG + "handleDownloadDialog");

                    return false;
                }

                @Override
                public void onDownloadTipsDialogShow() {
                    LogUtil.simple(TAG + "onDownloadTipsDialogShow");
                }

                @Override
                public void onDownloadTipsDialogDismiss() {
                    LogUtil.simple(TAG + "onDownloadTipsDialogDismiss");

                }
            });

            //核心：视频广告渲染
            bindVideo(rfMaterialProvider);

            //下载事件监听
            bindDownload(rfMaterialProvider);

            //广告标识内容渲染
            bindSourceLogo(rfMaterialProvider);

            //绑定关闭按钮
            bindCloseView(rfMaterialProvider);


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void bindDownload(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            final AdvanceRFDownloadListener downloadListener = rfMaterialProvider.downloadListener;
            if (dataConverter.isDownloadAD() && downloadListener != null) {

                String appName = nativeAd.getAppName();
                String fileName = "";
                final long totalBytes = nativeAd.getAppPackageSize();
                final AdvanceRFDownloadListener.AdvanceRFDownloadInf downloadInf = new AdvanceRFDownloadListener.AdvanceRFDownloadInf(AdvanceRFConstant.AD_DOWNLOAD_STATUS_ACTIVE, totalBytes, 0, fileName, appName);
                nativeAd.setDownloadListener(new KsApkDownloadListener() {
                    @Override
                    public void onPaused(int progress) {
                        LogUtil.simple(TAG + " onPaused , progress = " + progress);

                        downloadInf.currBytes = progress * totalBytes / 100;
                        downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_PAUSED;
                        downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);
                    }

                    @Override
                    public void onIdle() {
                        LogUtil.simple(TAG + " onIdle");

                        downloadListener.onIdle(dataConverter);
                    }

                    @Override
                    public void onDownloadStarted() {
                        LogUtil.simple(TAG + " onDownloadStarted");

                        downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_ACTIVE;
                        downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);

                    }

                    @Override
                    public void onProgressUpdate(int progress) {
                        LogUtil.simple(TAG + " onProgressUpdate , progress = " + progress);

                        downloadInf.currBytes = progress * totalBytes / 100;
                        downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_ACTIVE;

                        downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);

                    }

                    @Override
                    public void onDownloadFinished() {
                        LogUtil.simple(TAG + " onDownloadFinished");

                        downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_FINISHED;
                        downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);

                    }

                    @Override
                    public void onInstalled() {
                        LogUtil.simple(TAG + " onInstalled");

                    }

                    @Override
                    public void onDownloadFailed() {
                        LogUtil.simple(TAG + " onDownloadFailed");

                        downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_FAILED;
                        downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);

                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void bindVideo(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            if (dataConverter.isVideo()) {
                //设置播放相关配置
                AdvanceRFVideoOption advanceRFVideoOption = rfMaterialProvider.videoOption;

                boolean isMute = true;
                int videoAutoPlayType = KsAdVideoPlayConfig.VideoAutoPlayType.AUTO_PLAY;
                if (advanceRFVideoOption != null) {
                    isMute = advanceRFVideoOption.isMute;
                    switch (advanceRFVideoOption.autoPlayNetStatus) {
                        case AdvanceRFConstant.VIDEO_AUTO_PLAY_WIFI:
                            videoAutoPlayType = KsAdVideoPlayConfig.VideoAutoPlayType.AUTO_PLAY_WIFI;
                            break;
                        case AdvanceRFConstant.VIDEO_AUTO_PLAY_ALWAYS:
                            videoAutoPlayType = KsAdVideoPlayConfig.VideoAutoPlayType.AUTO_PLAY;
                            break;
                        case AdvanceRFConstant.VIDEO_AUTO_PLAY_NONE:
                            videoAutoPlayType = KsAdVideoPlayConfig.VideoAutoPlayType.NO_AUTO_PLAY;
                            break;
                    }
                }
                LogUtil.simple(TAG + "videoPlayConfig,  isMute = " + isMute + " , videoAutoPlayType = " + videoAutoPlayType);

                // SDK默认渲染的视频view
                KsAdVideoPlayConfig videoPlayConfig = new KsAdVideoPlayConfig.Builder()
                        .videoAutoPlayType(videoAutoPlayType) // 设置在有wifi
                        // 时视频自动播放，当与dataFlowAutoStart()同时设置时，以最后一个传入的值为准
                        .videoSoundEnable(!isMute)
                        .build();

                View video = nativeAd.getVideoView(getRealContext(), videoPlayConfig);
//                if (video != null) {
//                    if (video.getParent() == null) {
//                        rfMaterialProvider.videoView.removeAllViews();
//                        rfMaterialProvider.videoView.addView(video);
//                    }
//                }

                AdvanceRFUtil.addVideoView(rfMaterialProvider, video, getVideoMaterialWH());


                final AdvanceRFVideoEventListener videoEventListener = rfMaterialProvider.videoEventListener;
                nativeAd.setVideoPlayListener(new KsNativeAd.VideoPlayListener() {
                    @Override
                    public void onVideoPlayReady() {
                        LogUtil.simple(TAG + " onVideoPlayReady");

                        if (videoEventListener != null) {
                            videoEventListener.onReady(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoPlayStart() {
                        LogUtil.simple(TAG + " onVideoPlayStart");

                        if (videoEventListener != null) {
                            videoEventListener.onPlayStart(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoPlayComplete() {
                        LogUtil.simple(TAG + " onVideoPlayComplete");

                        if (videoEventListener != null) {
                            videoEventListener.onComplete(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoPlayError(int what, int extra) {
                        String eMsg = "what ： " + what + "， extraCode = " + extra;
                        handleFailed(what, eMsg);

                        if (videoEventListener != null) {
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(what, eMsg));
                        }
                    }

                    @Override
                    public void onVideoPlayPause() {
                        LogUtil.simple(TAG + " onVideoPlayPause");

                        if (videoEventListener != null) {
                            videoEventListener.onPause(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoPlayResume() {
                        LogUtil.simple(TAG + " onVideoPlayResume");

                        if (videoEventListener != null) {
                            videoEventListener.onResume(dataConverter);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private float getVideoMaterialWH() {
        float videoMaterialWH = 0;
        try {
            int videoW = nativeAd.getVideoWidth();
            int videoH = nativeAd.getVideoHeight();
            videoMaterialWH = videoW / (float) videoH;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return videoMaterialWH;

    }


    private Map<View, Integer> getClickMap(AdvanceRFMaterialProvider rfMaterialProvider) {
        //可点击view传递
        Map<View, Integer> clickViewMap = new HashMap<>();
        try {
            ArrayList<View> clickViews = rfMaterialProvider.clickViews;
            if (clickViews != null) {
                for (View cv : clickViews) {
                    clickViewMap.put(cv, KsNativeConvertType.SHOW_DOWNLOAD_TIPS_DIALOG);
                }
            }
            //如果是创意按钮，需要特定点击标记值
            ArrayList<View> creativeViews = rfMaterialProvider.creativeViews;
            if (creativeViews != null) {
                for (View cv : creativeViews) {
                    clickViewMap.put(cv, KsNativeConvertType.CONVERT);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return clickViewMap;
    }

    private void bindCloseView(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            View dislikeView = rfMaterialProvider.disLikeView;
            final AdvRFRootView adContainer = rfMaterialProvider.rootView;
            if (dislikeView != null) {
                dislikeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.simple(TAG + " dislikeView onClick");

                        try {
                            adContainer.removeAllViews();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        handleClose();
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void bindSourceLogo(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            LinearLayout logoLayout = new LinearLayout(getRealContext());
            logoLayout.setOrientation(LinearLayout.HORIZONTAL);
            logoLayout.setGravity(Gravity.CENTER_VERTICAL);
            //设置背景
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(Color.GRAY);
            gd.setCornerRadius(BYDisplay.dp2px(3));
            gd.setAlpha(100);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                logoLayout.setBackground(gd);
            } else {
                logoLayout.setBackgroundDrawable(gd);
            }
            int lrPadding = BYDisplay.dp2px(3);
            int tbPadding = BYDisplay.dp2px(2);
            logoLayout.setPadding(lrPadding, tbPadding, lrPadding, tbPadding);

            String sourceLogoUrl = nativeAd.getAdSourceLogoUrl(AdSourceLogoType.NORMAL);
            String sourceText = nativeAd.getAdSource();

            if (BYStringUtil.isEmpty(sourceText)) {
                sourceText = "广告";
            }

            //logo 图标
            try {
                if (!BYStringUtil.isEmpty(sourceLogoUrl)) {
                    ImageView recLogo = new ImageView(getRealContext());
                    int maxW = BYDisplay.dp2px((25));
                    int h = BYDisplay.dp2px((12));
                    recLogo.setMaxWidth(maxW);
                    recLogo.setAdjustViewBounds(true);
                    //调用渲染图片方法
                    AdvanceRFUtil.renderSourceLogo(recLogo, sourceLogoUrl, R.drawable.ic_ks_ad_source_logo);
                    LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
                    imgLp.setMargins(0, 0, BYDisplay.dp2px(3), 0);
                    logoLayout.addView(recLogo, imgLp);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            //文字一般是"广告"二字
            TextView tv = new TextView(getRealContext());
            tv.setText(sourceText);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

            LinearLayout.LayoutParams txtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            logoLayout.addView(tv, txtLp);

            LogUtil.d(TAG + "add logoLayout ; sourceText = " + sourceText);
            rfMaterialProvider.logoView.addView(logoLayout);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
