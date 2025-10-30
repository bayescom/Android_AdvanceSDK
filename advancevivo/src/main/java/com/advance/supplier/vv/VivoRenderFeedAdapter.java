package com.advance.supplier.vv;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advance.AdvanceSetting;
import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.core.srender.widget.AdvRFLogoView;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.mercury.sdk.util.MercuryTool;
import com.vivo.ad.model.AdError;
import com.vivo.ad.nativead.ClosePosition;
import com.vivo.ad.nativead.NativeAdListener;
import com.vivo.ad.nativead.NativeResponse;
import com.vivo.mobilead.nativead.NativeAdParams;
import com.vivo.mobilead.nativead.VivoNativeAd;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.callback.MediaListener;
import com.vivo.mobilead.unified.base.view.NativeVideoView;
import com.vivo.mobilead.unified.base.view.VivoNativeAdContainer;
import com.vivo.mobilead.unified.vnative.ProVivoNativeAd;
import com.vivo.mobilead.unified.vnative.ProVivoNativeAdListener;
import com.vivo.mobilead.unified.vnative.VNativeAd;

import java.util.List;

public class VivoRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    VivoNativeAd nativeAd;
    ProVivoNativeAd nativeAdPro;
    NativeResponse adData;
    VNativeAd adDataPro;
    boolean usePro = true; //是否使用2.0版本开屏广告
    View container = null;
    AdvanceRFMaterialProvider rfMaterialProvider = null;
    VivoNativeAdContainer adContainer;

    ClosePosition closePosition = ClosePosition.RIGHT_TOP;

    public VivoRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    protected void paraLoadAd() {
        VivoUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                loadAd();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });

    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (usePro) {
            if (adDataPro != null) {
                adDataPro.destroy();
            }
        } else {
//            if (adData!=null){
//                adData.
//            }
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {

        try {
            if (mAdvanceRFBridge != null) {
                rfMaterialProvider = mAdvanceRFBridge.getMaterialProvider();
            }
            if (rfMaterialProvider == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "未获取到 MaterialProvider 信息，展示前请参考demo调用 advanceRenderFeed.setRfMaterialProvider(materialProvider)方法");
                return;
            }
            if (rfMaterialProvider.rootView == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "请设置  rootView 信息");
                return;
            }
            bindView();

            renderAdLogoAndTag();

            bindCloseView();

            bindPrivacy();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void bindPrivacy() {
        try {
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.topMargin = BYDisplay.dp2px(15);
            layoutParams.leftMargin = BYDisplay.dp2px(15);
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            if (!usePro) {
                adData.bindPrivacyView(layoutParams);
            }
        } catch (Exception e) {
        }
    }

    private void bindCloseView() {
        try {
            View disLikeView = rfMaterialProvider.disLikeView;

            LogUtil.simple(TAG + "bindCloseView , disLikeView = " + disLikeView);

            if (disLikeView != null) {
                disLikeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.simple(TAG + " dislikeView onClick");

                        try {
                            if (adContainer != null)
                                adContainer.removeAllViews();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        handleClose();
                    }
                });

//                adDataPro
            } else {
                if (usePro) {
                    adDataPro.bindCloseView(closePosition);
                } else {
                    adData.bindCloseView(closePosition);
                }
            }
        } catch (Exception e) {
        }
    }

    private void bindView() {

//            需要先拿到根布局信息
        AdvRFRootView rootView = rfMaterialProvider.rootView;
        Activity activity = getRealActivity(rfMaterialProvider.rootView);

        adContainer = new VivoNativeAdContainer(activity);

//            -----------方案B copy全部子布局，复制子控件至新布局，并将新布局添加至旧父布局中
        AdvanceRFUtil.copyChild(rootView, adContainer);

        if (rfMaterialProvider.clickViews.contains(rootView)) {
            LogUtil.devDebug(TAG + "contains rootView");
            rfMaterialProvider.clickViews.add(adContainer);
        }

        //将创意view添加至可点击view中
        for (View v : rfMaterialProvider.creativeViews) {
            if (!rfMaterialProvider.clickViews.contains(v)) {
                rfMaterialProvider.clickViews.add(v);
            }
        }

        //添加视频控件
        AdvRFVideoView videoView = rfMaterialProvider.videoView;
        NativeVideoView vivoVideo = null;
        if (videoView != null) {
            vivoVideo = new NativeVideoView(activity);
            AdvanceRFVideoEventListener videoEventListener = rfMaterialProvider.videoEventListener;

            vivoVideo.setMediaListener(new MediaListener() {
                @Override
                public void onVideoStart() {
                    LogUtil.simple(TAG + "onVideoStart : ");

                    if (videoEventListener != null) {
                        videoEventListener.onPlayStart(dataConverter);
                    }

                }

                @Override
                public void onVideoPause() {
                    LogUtil.simple(TAG + "onVideoPause : ");

                    if (videoEventListener != null) {
                        videoEventListener.onPause(dataConverter);
                    }
                }

                @Override
                public void onVideoPlay() {
                    LogUtil.simple(TAG + "onVideoPlay : ");


                    if (videoEventListener != null) {
                        videoEventListener.onPlaying(dataConverter, 0, 0);
                    }
                }

                @Override
                public void onVideoError(VivoAdError vivoAdError) {
                    LogUtil.simple(TAG + "onVideoError, error: " + vivoAdError);
                    int code = -1;
                    String msg = "default video err";

                    if (vivoAdError != null) {
                        code = vivoAdError.getCode();
                        msg = vivoAdError.getMsg();
                    }
                    handleFailed(code, msg);

                    if (videoEventListener != null) {
                        videoEventListener.onError(dataConverter, AdvanceError.parseErr(code, msg));
                    }
                }

                @Override
                public void onVideoCompletion() {
                    LogUtil.simple(TAG + "onVideoCompletion : ");


                    if (videoEventListener != null) {
                        videoEventListener.onComplete(dataConverter);
                    }
                }

                @Override
                public void onVideoCached() {
                    LogUtil.simple(TAG + "onVideoCached : ");


//                        if (videoEventListener != null){
//                            videoEventListener.o(dataConverter);
//                        }
                }
            });
            videoView.addView(vivoVideo);
        }


        //绑定主控件
        if (usePro) {
            adDataPro.registerView(activity, adContainer, rfMaterialProvider.clickViews, rfMaterialProvider.creativeViews);
        } else {
            View createV = null;
            if (!rfMaterialProvider.creativeViews.isEmpty()) {
                createV = rfMaterialProvider.creativeViews.get(0);
            }

            adData.registerView(adContainer, createV, vivoVideo);
        }

    }


    //渲染广告来源view
    public void renderAdLogoAndTag() {
        try {
            Context context = getRealContext();
            AdvRFLogoView logoView = rfMaterialProvider.logoView;
            LogUtil.simple(TAG + "renderAdLogoAndTag , logoView = " + logoView);

//            if (logoView == null) {
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
            lp.setMargins(BYDisplay.dp2px(8), 0, 0, BYDisplay.dp2px(5));
            if (usePro) {
                adDataPro.bindLogoView(lp);
            } else {
                adData.bindLogoView(lp);
            }
            return;
//            }

//            LinearLayout logoLayout = new LinearLayout(context);
//            ImageView ivAdMarkLogo = new ImageView(context);
//            TextView tvAdMarkText = new TextView(context);
//
//            //父容器样式
//            logoLayout.setOrientation(LinearLayout.HORIZONTAL);
//            logoLayout.setGravity(Gravity.CENTER_VERTICAL);
//            //设置背景
//            GradientDrawable gd = new GradientDrawable();
//            gd.setColor(Color.GRAY);
//            gd.setCornerRadius(BYDisplay.dp2px(3));
//            gd.setAlpha(100);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                logoLayout.setBackground(gd);
//            } else {
//                logoLayout.setBackgroundDrawable(gd);
//            }
//            int lrPadding = BYDisplay.dp2px(3);
//            int tbPadding = BYDisplay.dp2px(2);
//            logoLayout.setPadding(lrPadding, tbPadding, lrPadding, tbPadding);
//
//
//            Bitmap logoBit;
//            String logoUrl;
//            String logoMarkText;
//            String logoTagText;
//            if (usePro) {
//                logoBit = adDataPro.getAdLogo();
//                logoUrl = adDataPro.getAdMarkUrl();
//                logoMarkText = adDataPro.getAdMarkText();
//                logoTagText = adDataPro.getAdTag();
//            } else {
//                logoBit = adData.getAdLogo();
//                logoUrl = adData.getAdMarkUrl();
//                logoMarkText = adData.getAdMarkText();
//                logoTagText = adData.getAdTag();
//            }
//
//            if (logoBit != null) {
//                ivAdMarkLogo.setVisibility(View.VISIBLE);
//                tvAdMarkText.setVisibility(View.GONE);
//                ivAdMarkLogo.setImageBitmap(logoBit);
//            } else if (!TextUtils.isEmpty(logoUrl)) {
//                ivAdMarkLogo.setVisibility(View.VISIBLE);
//                tvAdMarkText.setVisibility(View.GONE);
//                MercuryTool.renderNetImg(logoUrl, ivAdMarkLogo);
//
//
//            } else {
//                String adMark;
//                if (!TextUtils.isEmpty(logoMarkText)) {
//                    adMark = logoMarkText;
//                } else if (!TextUtils.isEmpty(logoTagText)) {
//                    adMark = logoTagText;
//                } else {
//                    adMark = "广告";
//                }
//
//                tvAdMarkText.setVisibility(View.VISIBLE);
//                ivAdMarkLogo.setVisibility(View.GONE);
//                tvAdMarkText.setText(adMark);
//                tvAdMarkText.setTextColor(Color.WHITE);
//                tvAdMarkText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
//            }
//
//            int maxW = BYDisplay.dp2px((25));
//            int h = BYDisplay.dp2px((12));
//            ivAdMarkLogo.setMaxWidth(maxW);
//            ivAdMarkLogo.setAdjustViewBounds(true);
//            LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
//            logoLayout.addView(ivAdMarkLogo, imgLp);
//
//            logoLayout.addView(tvAdMarkText);
//            logoView.addView(logoLayout);

//            if (usePro){
//                adDataPro.bindLogoView(logoView);
//            }
        } catch (Exception e) {

        }
    }

    private void loadAd() {
        if (sdkSupplier.versionTag == 1) {
            usePro = false;
        }
        LogUtil.simple(TAG + "usePro = " + usePro);


        if (usePro) {
            loadProAd();
            return;
        }
        String adID = sdkSupplier.adspotid;
        NativeAdParams.Builder builder = new NativeAdParams.Builder(adID);
        builder.setWxAppId(AdvanceSetting.getInstance().wxAppId);   //此非必须
        NativeAdParams adParams = builder.build();

        nativeAd = new VivoNativeAd(getActivity(), adParams, new NativeAdListener() {
            @Override
            public void onADLoaded(List<NativeResponse> list) {
                LogUtil.simple(TAG + "onADLoaded...");

                if (list == null || list.isEmpty() || list.get(0) == null) {
                    String nMsg = TAG + "ad list isEmpty";
                    handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                    return;
                }
                adData = list.get(0);

                dataConverter = new VivoRenderDataConverter(usePro, VivoRenderFeedAdapter.this, adData, null);

                updateBidding(VivoUtil.getPrice(adData));
                handleSucceed();
            }

            @Override
            public void onNoAD(AdError adError) {
                LogUtil.simple(TAG + "onNoAD... adError = " + adError);

                String errCode = AdvanceError.ERROR_LOAD_SDK;
                String errMsg = "onNoAD ";
                if (adError != null) {
                    int eCode = adError.getErrorCode();
                    if (eCode > 0) {
                        errCode = eCode + "";
                    }
                    errMsg = adError.toString();
                }

                handleFailed(errCode, errMsg);
            }

            @Override
            public void onClick(NativeResponse nativeResponse) {
                LogUtil.simple(TAG + "onClick...");

                handleClick();
            }

            @Override
            public void onAdShow(NativeResponse nativeResponse) {
                LogUtil.simple(TAG + "onAdShow...");

                handleShow();
            }

            @Override
            public void onAdClose() {
                LogUtil.simple(TAG + "onAdClose...");

                handleClose();
            }
        });
        nativeAd.loadAd();
    }

    private void loadProAd() {
        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);
        if (builder != null) {
            adParams = builder.build();
        }

        nativeAdPro = new ProVivoNativeAd(getRealContext(), adParams, new ProVivoNativeAdListener() {
            @Override
            public void onAdLoadSuccess(List<VNativeAd> list) {
                LogUtil.simple(TAG + "onAdLoadSuccess...");

                if (list == null || list.isEmpty() || list.get(0) == null) {
                    String nMsg = TAG + "ad list isEmpty";
                    handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                    return;
                }
                adDataPro = list.get(0);
                dataConverter = new VivoRenderDataConverter(usePro, VivoRenderFeedAdapter.this, null, adDataPro);

                updateBidding(VivoUtil.getPrice(adDataPro));
                handleSucceed();
            }

            @Override
            public void onAdFailed(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onAdFailed... , vivoAdError = " + vivoAdError);

                VivoUtil.handleErr(VivoRenderFeedAdapter.this, vivoAdError, AdvanceError.ERROR_LOAD_SDK, "onAdFailed");
            }
        });
        nativeAdPro.loadAd();
    }


    private Activity getActivity() {
        if (container == null && mAdvanceRFBridge != null && mAdvanceRFBridge.getMaterialProvider() != null) {
            container = mAdvanceRFBridge.getMaterialProvider().rootView;
        }

        return getRealActivity(container);
    }
}
