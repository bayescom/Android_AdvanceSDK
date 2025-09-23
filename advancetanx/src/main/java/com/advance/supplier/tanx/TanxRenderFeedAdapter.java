package com.advance.supplier.tanx;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.ad.ad.feed.ITanxFeedAd;
import com.alimm.tanx.core.ad.ad.feed.ITanxFeedInteractionListener;
import com.alimm.tanx.core.ad.ad.feed.ITanxFeedVideoAdListener;
import com.alimm.tanx.core.ad.ad.feed.ITanxFeedVideoPlayer;
import com.alimm.tanx.core.ad.listener.ITanxAdLoader;
import com.alimm.tanx.core.ad.loader.ITanxRequestLoader;
import com.alimm.tanx.core.ad.view.TanxAdView;
import com.alimm.tanx.core.request.TanxAdSlot;
import com.alimm.tanx.core.request.TanxError;
import com.alimm.tanx.core.request.TanxPlayerError;
import com.alimm.tanx.core.utils.LogUtils;
import com.alimm.tanx.ui.TanxSdk;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.util.BYStringUtil;

import java.util.List;

public class TanxRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    private final String TAG = "[TanxRenderFeedAdapter] ";
    ITanxAdLoader iTanxAdLoader;
    ITanxFeedAd nativeAD;

    public TanxRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        initAD();
    }

    @Override
    protected void paraLoadAd() {
        initAD();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        LogUtil.simple(TAG + "doDestroy");
        if (iTanxAdLoader != null) {
            iTanxAdLoader.destroy();
            LogUtil.simple(TAG + "iTanxAdLoader doDestroy");
        }
    }

    @Override
    public void show() {
        try {
            if (AdvanceRFUtil.skipRender(this)) {
                return;
            }
            if (nativeAD == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "未获取到广告信息");
                return;
            }
            final AdvanceRFMaterialProvider rfMaterialProvider = mAdvanceRFBridge.getMaterialProvider();
            Activity activity = getRealActivity(rfMaterialProvider.rootView);

            TanxAdView tanxAdView;
            if (activity == null) {
                tanxAdView = new TanxAdView(getRealContext());
            } else {
                tanxAdView = new TanxAdView(activity);
            }

            //必须，核心方法
            nativeAD.bindFeedAdView(tanxAdView, rfMaterialProvider.clickViews, rfMaterialProvider.disLikeView, new ITanxFeedInteractionListener() {
                @Override
                public void onAdDislike() {
                    LogUtil.simple(TAG + "onAdDislike");
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + "onAdClose");

                    handleClose();
                }

                @Override
                public void onAdClicked(TanxAdView tanxAdView, ITanxFeedAd feedAd) {
                    LogUtil.simple(TAG + "onAdClicked");

                    handleClick();
                }

                @Override
                public void onClickCommitSuccess(ITanxFeedAd feedAd) {
                    LogUtil.simple(TAG + "onClickCommitSuccess");

                }

                @Override
                public void onAdShow(ITanxFeedAd feedAd) {
                    LogUtil.simple(TAG + "onAdShow");

                    handleShow();
                }

                @Override
                public void onExposureCommitSuccess(ITanxFeedAd feedAd) {
                    LogUtil.simple(TAG + "onExposureCommitSuccess");

                }
            });
            //核心：视频广告渲染
            bindVideo(rfMaterialProvider);

            //广告标识内容渲染
            bindSourceLogo(rfMaterialProvider);

            //绑定关闭按钮
            bindCloseView(rfMaterialProvider);

            //添加view必不可少，且必须在 bindFeedAdView 之后进行
            rfMaterialProvider.rootView.addView(tanxAdView);

        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }

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


    private void initAD() {
        TanxUtil.initTanx(this, new TanxUtil.InitListener() {
            @Override
            public void success() {
                startLoadAD();
            }

            @Override
            public void fail(int code, String msg) {
                handleFailed(code, msg);
            }
        });
    }

    private void startLoadAD() {
        try {
//            if (BYUtil.isDev()) {
//                int a = 0 / 0;
//            }
            TanxAdSlot.Builder builder = new TanxAdSlot.Builder()
                    .adCount(sdkSupplier.adCount)
                    .pid(sdkSupplier.adspotid);
//        VideoParam videoParam = new VideoParam();
//        if (advanceRFVideoOption != null) {
//            videoParam.mute = advanceRFVideoOption.isMute;
//        }
//        builder.setVideoParam(videoParam);

//        todo 测试尺寸设置后表现
//        builder.adSize()


            iTanxAdLoader = TanxSdk.getSDKManager().createAdLoader(getRealContext());

            iTanxAdLoader.request(builder.build(), new ITanxRequestLoader.ITanxRequestListener<ITanxFeedAd>() {
                @Override
                public void onSuccess(List<ITanxFeedAd> adList) {
                    try {
                        if (adList.size() == 0 || adList.get(0) == null) {
                            handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                            return;
                        }
                        LogUtil.simple(TAG + "onSuccess");
                        nativeAD = adList.get(0);

                        updateBidding(nativeAD.getBidInfo().getBidPrice());

                        //生成通用数据接口
                        dataConverter = new TanxRenderDataConverter(nativeAD, sdkSupplier);

//                    回调成功事件
                        handleSucceed();

                    } catch (Throwable e) {
                        e.printStackTrace();
                        handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                    }
                }

                @Override
                public void onError(TanxError tanxError) {
                    //广告请求异常
                    String msg = "广告请求失败";
                    if (tanxError != null) {
                        msg = "[tanx onError] " + tanxError;
                    }
                    LogUtil.simple(TAG + "onError   " + ", msg = " + msg);
                    handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);
                }

                @Override
                public void onTimeOut() {
                    //广告请求超时
                    String msg = "获取广告超时";
                    LogUtil.simple(TAG + "onTimeOut   " + ", msg = " + msg);
                    handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
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

            String sourceLogoUrl = nativeAD.getBidInfo().getAdvLogo();
            String sourceText = "广告";

//            if (BYStringUtil.isEmpty(sourceText)) {
//                sourceText =;
//            }

            //logo 图标
            try {
                if (!BYStringUtil.isEmpty(sourceLogoUrl)) {
                    ImageView recLogo = new ImageView(getRealContext());
                    int maxW = BYDisplay.dp2px((25));
                    int h = BYDisplay.dp2px((12));
                    recLogo.setMaxWidth(maxW);
                    recLogo.setAdjustViewBounds(true);
                    //调用渲染图片方法
                    AdvanceRFUtil.renderSourceLogo(recLogo, sourceLogoUrl, R.drawable.ic_tanx_source_logo);
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

    private void bindVideo(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            if (dataConverter.isVideo()) {
                Context context = getRealContext();;
                ITanxFeedVideoPlayer iTanxVideoView = nativeAD.getITanxVideoView(context);

                final AdvanceRFVideoEventListener videoEventListener = rfMaterialProvider.videoEventListener;
                View tanxVideoView = iTanxVideoView.getVideoAdView(new ITanxFeedVideoAdListener<ITanxFeedAd>() {
                    @Override
                    public void onVideoLoad(ITanxFeedAd ad) {
                        LogUtil.simple(TAG + "onVideoLoad ：");
                        if (videoEventListener != null) {
                            videoEventListener.onReady(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoAdStartPlay(ITanxFeedAd ad) {
                        LogUtil.simple(TAG + "onVideoAdStartPlay ：");
                        if (videoEventListener != null) {
                            videoEventListener.onPlayStart(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoAdPaused(ITanxFeedAd ad) {
                        LogUtil.simple(TAG + "onVideoAdPaused ：");
                        if (videoEventListener != null) {
                            videoEventListener.onPause(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoComplete() {
                        LogUtil.simple(TAG + "onVideoComplete ：");
                        if (videoEventListener != null) {
                            videoEventListener.onComplete(dataConverter);
                        }
                    }

                    @Override
                    public void onProgressUpdate(long current, long duration) {
                        LogUtil.simple(TAG + "onProgressUpdate ：current：" + current + " duration：" + duration);

                        if (videoEventListener != null) {
                            videoEventListener.onPlaying(dataConverter, current, duration);
                        }
                    }

                    @Override
                    public void onVideoError(TanxPlayerError e) {
                        LogUtil.simple(TAG + "播放错误：" + LogUtils.getStackTraceMessage(e));
                        String eMsg = "TanxPlayerError = " + e;
                        String errCode = AdvanceError.ERROR_VIDEO_RENDER_ERR;
                        handleFailed(errCode, eMsg);

                        if (videoEventListener != null) {
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(errCode, eMsg));
                        }
                    }

                    @Override
                    public void onError(TanxError e) {
                        LogUtil.simple(TAG + "错误：" + LogUtils.getStackTraceMessage(e));

                        String eMsg = "TanxError = " + e;
                        String errCode = AdvanceError.ERROR_VIDEO_RENDER_ERR;
                        handleFailed(errCode, eMsg);

                        if (videoEventListener != null) {
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(errCode, eMsg));
                        }
                    }

                    @Override
                    public View onCustomPlayIcon() {
//                        ImageView ivDefaultPlayer = new ImageView(getContext());
//                        int px = DimenUtil.dp2px(getContext(), 40);
//                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(px, px);
//                        layoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
//                        ivDefaultPlayer.setImageResource(R.mipmap.start);
//                        ivDefaultPlayer.setLayoutParams(layoutParams);
                        return null;
                    }

                    @Override
                    public View onCustomLoadingIcon() {
                        return null;
                    }

                });

                AdvanceRFUtil.addVideoView(rfMaterialProvider, tanxVideoView, getVideoMaterialWH());

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private float getVideoMaterialWH() {
        float videoMaterialWH = 0;
        try {
            String videoW = nativeAD.getBidInfo().getCreativeItem().getVideoWidth();
            String videoH = nativeAD.getBidInfo().getCreativeItem().getVideoHeight();
            int vw = Integer.parseInt(videoW);
            int vh = Integer.parseInt(videoH);
            videoMaterialWH = vw / (float) vh;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return videoMaterialWH;

    }

}
