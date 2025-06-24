package com.advance.supplier.mry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
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
import com.advance.core.srender.AdvanceRFVideoOption;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.util.BYCacheUtil;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.bayes.sdk.basic.widget.BYViewUtil;
import com.mercury.sdk.core.config.VideoOption;
import com.mercury.sdk.core.nativ.NativeAD;
import com.mercury.sdk.core.nativ.NativeADData;
import com.mercury.sdk.core.nativ.NativeADEventListener;
import com.mercury.sdk.core.nativ.NativeADListener;
import com.mercury.sdk.core.nativ.NativeADMediaListener;
import com.mercury.sdk.core.widget.MediaView;
import com.mercury.sdk.core.widget.NativeAdContainer;
import com.mercury.sdk.util.ADError;
import com.mercury.sdk.util.MercuryTool;

import java.util.ArrayList;
import java.util.List;

public class MercuryRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    boolean hasPicExpose = false;

    NativeADData mRenderAD;
    NativeAD nativeAD;

    public MercuryRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        doStart();
    }

    @Override
    protected void paraLoadAd() {
        doStart();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            LogUtil.devDebug("doDestroy");
            if (mRenderAD != null) {
                mRenderAD.destroy();
            }
            if (nativeAD != null) {
                nativeAD.destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        doShow();
    }


    private void doStart() {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);

        nativeAD = new NativeAD(getRealActivity(null), sdkSupplier.adspotid, new NativeADListener() {
            @Override
            public void onADLoaded(List<NativeADData> list) {
                try {
                    if (list == null || list.size() == 0) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "ads empty");
                        return;
                    }
                    mRenderAD = list.get(0);
                    if (mRenderAD == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "mRenderAD null");
                        return;
                    }

                    //更新ecpm价格信息
                    updateBidding(mRenderAD.getECPM());

                    //转换返回广告model为聚合通用model
                    dataConverter = new MercuryRenderDataConverter(mRenderAD, sdkSupplier);

                    //标记广告成功
                    handleSucceed();
                    //通知广告成功
//                    mAdvanceRFBridge.adapterDidLoaded(mDataConverter);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNoAD(ADError adError) {
                int code = -1;
                String msg = "default onNoAD";
                if (adError != null) {
                    code = adError.code;
                    msg = adError.msg;
                }
                LogUtil.simple(TAG + "onAdFailed");
                handleFailed(code, msg);
            }
        });
        nativeAD.loadAD(1);
    }

    private void doShow() {
        try {
            LogUtil.simple(TAG + "call show ");
            if (mAdvanceRFBridge == null || mRenderAD == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "advanceRFBridge or mRenderAD null");
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
            Activity activity = getRealActivity(rfMaterialProvider.rootView);
            //添加root根布局到 广点通自定义根布局。
            final NativeAdContainer adContainer = new NativeAdContainer(activity);

//            -----------方案A 替换根布局 (坏处是，当广告view上下均有内容时，会导致布局位置错乱)
            //将根布局添加至广点通承载布局，并删除旧根布局
//            ViewGroup rootParent = (ViewGroup) rfMaterialProvider.rootView.getParent();
//            if (rootParent != null) {
//                rootParent.removeView(rfMaterialProvider.rootView);
//            }
//            adContainer.addView(rootView);
////            将广点通布局添加至旧layout布局上
//            if (rootParent != null)
//                rootParent.addView(adContainer);

//            -----------方案B copy全部子布局，复制子控件至新布局，并将新布局添加至旧父布局中
            AdvanceRFUtil.copyChild(rootView, adContainer);
//            int childSize = rootView.getChildCount();
//            LogUtil.devDebug(TAG + "  childSize = " + childSize);
//            if (childSize > 0) {
//                for (int i = 0; i < childSize; i++) {
//                    View child = rootView.getChildAt(0);
//                    rootView.removeView(child);
//                    if (child != null) {
//                        adContainer.addView(child, i);
//                    }
//                    LogUtil.devDebug(TAG + "  adContainer.addView  i= " + i + " child = " + child);
//                }
//            }
//            rootView.addView(adContainer);

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


            mRenderAD.bindAdToView(activity, adContainer, rfMaterialProvider.clickViews);

            //新增创意按钮绑定方法
            ArrayList<View> creativeViews = rfMaterialProvider.creativeViews;
            if (creativeViews != null && creativeViews.size() > 0) {
                mRenderAD.bindCreativeView(creativeViews);
            }


//添加广告logo标识
            if (rfMaterialProvider.logoView != null) {
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

                    String sourceLogoUrl = mRenderAD.getADSourceLogo();
                    String sourceText = mRenderAD.getADSource();

//                    if (BYUtil.isDev()) {
//                        sourceLogoUrl = "https://img0.baidu.com/it/u=4252001042,1570788180&fm=253&fmt=auto&app=138&f=JPEG?w=1540&h=500";
//                        sourceText = "广告";
//                    }
                    //logo 图标
                    if (!BYStringUtil.isEmpty(sourceLogoUrl)) {
                        ImageView recLogo = new ImageView(getRealContext());
                        int maxW = BYDisplay.dp2px((25));
                        int h = BYDisplay.dp2px((12));
                        recLogo.setMaxWidth(maxW);
                        recLogo.setAdjustViewBounds(true);
                        //调用渲染图片方法
                        MercuryTool.renderNetImg(sourceLogoUrl, recLogo);
                        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
                        imgLp.setMargins(0, 0, BYDisplay.dp2px(3), 0);
                        logoLayout.addView(recLogo, imgLp);
                    }
                    //文字一般是"广告"二字
                    TextView tv = new TextView(getRealContext());
                    tv.setText(sourceText);
                    tv.setTextColor(Color.WHITE);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                    LinearLayout.LayoutParams txtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    logoLayout.addView(tv, txtLp);

                    rfMaterialProvider.logoView.addView(logoLayout);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }


            mRenderAD.setNativeAdEventListener(new NativeADEventListener() {
                @Override
                public void onADExposed() {
                    LogUtil.simple(TAG + "onADExposed ");

                    handleShow();
                }

                @Override
                public void onADClicked() {
                    LogUtil.simple(TAG + " onADClicked");

                    handleClick();
                }


                @Override
                public void onADError(ADError error) {
                    try {
                        int code = -1;
                        String msg = "default render err";
                        if (error != null) {
                            code = error.code;
                            msg = error.msg;
                        }
                        LogUtil.simple(TAG + "onADError, render err");
                        handleFailed(code, msg);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });


            //关闭广告事件绑定
            View dislikeView = rfMaterialProvider.disLikeView;
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

            if (dataConverter != null && dataConverter.isVideo()) {
//设置播放相关配置
                AdvanceRFVideoOption advanceRFVideoOption = rfMaterialProvider.videoOption;
                VideoOption.Builder builder = new VideoOption.Builder();
                builder.setAutoPlayPolicy(advanceRFVideoOption.autoPlayNetStatus);
                builder.setAutoPlayMuted(advanceRFVideoOption.isMute);

                //添加自定义播放view
                AdvRFVideoView videoView = rfMaterialProvider.videoView;
                MediaView mediaView = new MediaView(getRealContext());
                videoView.addView(mediaView);
                final AdvanceRFVideoEventListener videoEventListener = rfMaterialProvider.videoEventListener;
                mRenderAD.bindMediaView(mediaView, builder.build(), new NativeADMediaListener() {
                    @Override
                    public void onVideoInit() {
                        LogUtil.simple(TAG + "onVideoInit: ");
                    }

                    @Override
                    public void onVideoLoading() {
                        LogUtil.simple(TAG + "onVideoLoading: ");
                    }

                    @Override
                    public void onVideoReady() {
                        LogUtil.simple(TAG + "onVideoReady");
                        if (videoEventListener != null)

                            videoEventListener.onReady(dataConverter);
                    }

                    @Override
                    public void onVideoLoaded(int videoDuration) {
                        LogUtil.simple(TAG + "onVideoLoaded: ");
                    }

                    @Override
                    public void onVideoStart() {
                        LogUtil.simple(TAG + "onVideoStart");
                        if (videoEventListener != null)

                            videoEventListener.onPlayStart(dataConverter);

                    }

                    @Override
                    public void onVideoPause() {
                        LogUtil.simple(TAG + "onVideoPause: ");
                        if (videoEventListener != null)

                            videoEventListener.onPause(dataConverter);

                    }


                    @Override
                    public void onVideoCompleted() {
                        LogUtil.simple(TAG + "onVideoCompleted: ");
                        if (videoEventListener != null)

                            videoEventListener.onComplete(dataConverter);

                    }

                    @Override
                    public void onVideoError(ADError error) {
                        LogUtil.simple(TAG + "onVideoError, error: " + error);
                        int code = -1;
                        String msg = "default video err";

                        if (error != null) {
                            code = error.code;
                            msg = error.msg;
                        }
                        handleFailed(code, msg);

                        if (videoEventListener != null)
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(code, msg));
                    }


                });
            } else {
                //图片需要在view可见时自动调用曝光
                checkExpose(adContainer);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    private void checkExpose(View view) {
        //监听布局可见性，并设置是否监听摇一摇
        new BYViewUtil().onVisibilityChange(view, new BYViewUtil.VisChangeListener() {
            @Override
            public void onChange(View view, boolean isVisible) {
                if (isVisible && !hasPicExpose && mRenderAD != null) {
                    mRenderAD.onPicADExposure();
                    hasPicExpose = true;
                }
            }
        });

    }

    @Override
    public boolean isValid() {
        if (nativeAD != null) {
            return nativeAD.isValid();
        }
        return super.isValid();
    }
}
