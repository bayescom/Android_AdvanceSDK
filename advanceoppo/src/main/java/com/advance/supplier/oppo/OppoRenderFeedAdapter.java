package com.advance.supplier.oppo;

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
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.heytap.msp.mobad.api.ad.NativeAdvanceAd;
import com.heytap.msp.mobad.api.listener.INativeAdvanceInteractListener;
import com.heytap.msp.mobad.api.listener.INativeAdvanceLoadListener;
import com.heytap.msp.mobad.api.listener.INativeAdvanceMediaListener;
import com.heytap.msp.mobad.api.params.INativeAdvanceData;
import com.heytap.msp.mobad.api.params.MediaView;
import com.heytap.msp.mobad.api.params.NativeAdvanceContainer;
import com.mercury.sdk.util.MercuryTool;

import java.util.List;

public class OppoRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    NativeAdvanceAd mNativeAdvanceAd;
    INativeAdvanceData mRenderAD;

    public OppoRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        OppoUtil.initAD(this);
        startLoad();
    }

    private void startLoad() {
        try {
            /**
             * 通过构造NativeAdSize对象，在NativeTempletAd初始化时传入、可以指定原生模板广告的大小，单位为dp
             * 也可以传入null，展示默认的大小
             */
            mNativeAdvanceAd = new NativeAdvanceAd(getRealContext(), sdkSupplier.adspotid, new INativeAdvanceLoadListener() {
                @Override
                public void onAdSuccess(List<INativeAdvanceData> list) {
                    LogUtil.simple(TAG + " onAdSuccess ");

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
                        dataConverter = new OppoRenderDataConverter(getRealContext(), mRenderAD, sdkSupplier);

                        //标记广告成功
                        handleSucceed();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);
                }
            });
            /**
             * 调用loadAd方法请求原生模板广告
             */
            mNativeAdvanceAd.loadAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            LogUtil.devDebug("doDestroy");
            if (mNativeAdvanceAd != null) {
                mNativeAdvanceAd.destroyAd();
            }

            if (mRenderAD != null) {
                mRenderAD.release();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
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
            Activity activity = getRealActivity(rootView);
            //新建oppo自己的根布局
            final NativeAdvanceContainer adContainer = new NativeAdvanceContainer(activity);


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

            //核心绑定事件
            mRenderAD.bindToView(activity, adContainer, rfMaterialProvider.clickViews, rfMaterialProvider.creativeViews);


            //关闭广告事件绑定
            bindClosView(rfMaterialProvider, adContainer);

            //广告行为事件监听
            bindListener();

            //添加广告logo标识
            bindSourceLogo(rfMaterialProvider);

            //视频广告处理
            bindVideo(rfMaterialProvider);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bindClosView(AdvanceRFMaterialProvider rfMaterialProvider, NativeAdvanceContainer adContainer) {
        try {
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

                        destroy();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void bindListener() {
        mRenderAD.setInteractListener(new INativeAdvanceInteractListener() {
            @Override
            public void onClick() {
//                    Toast.makeText(NativeAdvance320X210Activity.this, "原生广告点击", Toast.LENGTH_SHORT).show();
                LogUtil.simple(TAG + " onClick");

                handleClick();
            }

            @Override
            public void onShow() {
//                    Toast.makeText(NativeAdvance320X210Activity.this, "原生广告展示", Toast.LENGTH_SHORT).show();
                LogUtil.simple(TAG + " onShow");

                handleShow();
            }

            @Override
            public void onError(int code, String msg) {
//                    Toast.makeText(NativeAdvance320X210Activity.this, "原生广告出错，ret:" + code + ",msg:" + msg, Toast.LENGTH_SHORT).show();
                try {
                    LogUtil.simple(TAG + "onError, render err");
                    handleFailed(code, msg);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }


        });
    }

    private void bindSourceLogo(AdvanceRFMaterialProvider rfMaterialProvider) {
//添加广告logo标识
        try {
            if (rfMaterialProvider.logoView != null) {

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


//                    if (BYUtil.isDev()) {
//                        sourceLogoUrl = "https://img0.baidu.com/it/u=4252001042,1570788180&fm=253&fmt=auto&app=138&f=JPEG?w=1540&h=500";
//                        sourceText = "广告";
//                    }
                boolean needLogoText = false;
                try {
                    String sourceLogoUrl = mRenderAD.getLogoFile().getUrl();
                    //logo 图标
                    if (!BYStringUtil.isEmpty(sourceLogoUrl)) {
                        ImageView recLogo = new ImageView(getRealContext());
                        int maxW = BYDisplay.dp2px((55));
                        int h = BYDisplay.dp2px((16));
                        recLogo.setMaxWidth(maxW);
                        recLogo.setAdjustViewBounds(true);
                        //调用渲染图片方法
                        MercuryTool.renderNetImg(sourceLogoUrl, recLogo);
                        LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
                        imgLp.setMargins(0, 0, BYDisplay.dp2px(3), 0);
                        logoLayout.addView(recLogo, imgLp);
                    } else {
                        needLogoText = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    needLogoText = true;
                }
                //oppo的图片上自带了广告两个字，而且有可能不返回，所以需要判断自行添加以下标识
                if (needLogoText) {
                    try {
                        String sourceText = "oppo广告";
                        //文字一般是"广告"二字
                        TextView tv = new TextView(getRealContext());
                        tv.setText(sourceText);
                        tv.setTextColor(Color.WHITE);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

                        LinearLayout.LayoutParams txtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        logoLayout.addView(tv, txtLp);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                rfMaterialProvider.logoView.addView(logoLayout);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void bindVideo(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            boolean isVideo = dataConverter.isVideo();
            if (isVideo) {
                //添加自定义播放view
                AdvRFVideoView videoView = rfMaterialProvider.videoView;
                MediaView mediaView = new MediaView(getRealContext());
                videoView.addView(mediaView);

                final AdvanceRFVideoEventListener videoEventListener = rfMaterialProvider.videoEventListener;
                mRenderAD.bindMediaView(getRealContext(), mediaView, new INativeAdvanceMediaListener() {
                    @Override
                    public void onVideoPlayStart() {
                        LogUtil.simple(TAG + "onVideoPlayStart");
                        if (videoEventListener != null)

                            videoEventListener.onPlayStart(dataConverter);
                    }

                    @Override
                    public void onVideoPlayComplete() {
                        LogUtil.simple(TAG + "onVideoPlayComplete");

                        if (videoEventListener != null)

                            videoEventListener.onComplete(dataConverter);
                    }

                    @Override
                    public void onVideoPlayError(int errCode, String msg) {
                        LogUtil.simple(TAG + "onVideoPlayError , errCode = " + errCode + " , msg = " + msg);

                        handleFailed(errCode, msg);

                        if (videoEventListener != null)
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(errCode, msg));

                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
