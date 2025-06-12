package com.advance.supplier.gdt;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFConstant;
import com.advance.core.srender.AdvanceRFDownloadListener;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.core.srender.AdvanceRFVideoOption;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.MediaView;
import com.qq.e.ads.nativ.NativeADEventListener;
import com.qq.e.ads.nativ.NativeADMediaListener;
import com.qq.e.ads.nativ.NativeADUnifiedListener;
import com.qq.e.ads.nativ.NativeUnifiedAD;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.ads.nativ.widget.NativeAdContainer;
import com.qq.e.comm.util.AdError;

import java.util.List;

public class GdtRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    NativeUnifiedADData mRenderAD;

    public GdtRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        doShow();
    }

    private void doStart() {
        try {
            LogUtil.simple(TAG + "call load start ");

            GdtUtil.initAD(this);
            NativeUnifiedAD mAdManager = new NativeUnifiedAD(getRealActivity(null), sdkSupplier.adspotid, new NativeADUnifiedListener() {
                @Override
                public void onADLoaded(List<NativeUnifiedADData> list) {
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
                        dataConverter = new GdtRenderDataConverter(mRenderAD, sdkSupplier);

                        //标记广告成功
                        handleSucceed();
                        //通知广告成功
//                        mAdvanceRFBridge.adapterDidLoaded(dataConverter);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onNoAD(AdError adError) {
                    try {
                        int code = -1;
                        String msg = "default onNoAD";
                        if (adError != null) {
                            code = adError.getErrorCode();
                            msg = adError.getErrorMsg();
                        }
                        LogUtil.simple(TAG + " onNoAD");
                        handleFailed(code, msg);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            mAdManager.loadData(1);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void doShow() {
        try {
            LogUtil.simple(TAG + "call show ");
            if (mAdvanceRFBridge == null || mRenderAD == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "advanceRFBridge or mRenderAD null");
                return;
            }
            //无效广告不展示
            if (!mRenderAD.isValid()) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "ad invalid");
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
            //添加root根布局到 广点通自定义根布局。
            final NativeAdContainer adContainer = new NativeAdContainer(getRealActivity(rfMaterialProvider.rootView));

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

//            -----------方案B copy全部子布局
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

            mRenderAD.bindAdToView(getRealActivity(rfMaterialProvider.rootView), adContainer, null, rfMaterialProvider.clickViews, rfMaterialProvider.creativeViews);

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
                public void onADError(AdError adError) {
                    try {
                        int code = -1;
                        String msg = "default render err";
                        if (adError != null) {
                            code = adError.getErrorCode();
                            msg = adError.getErrorMsg();
                        }
                        LogUtil.simple(TAG + "onADError, render err");
                        handleFailed(code, msg);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onADStatusChanged() {

                    try {
                        //主要是更新下载状态
                        final AdvanceRFDownloadListener downloadListener = rfMaterialProvider.downloadListener;
                        if (mRenderAD.isAppAd() && downloadListener != null) {
                            String appName = "";
                            if (mRenderAD.getAppMiitInfo() != null) {
                                appName = mRenderAD.getAppMiitInfo().getAppName();
                            }
                            AdvanceRFDownloadListener.AdvanceRFDownloadInf downloadInf = new AdvanceRFDownloadListener.AdvanceRFDownloadInf();
                            downloadInf.appName = appName;

//                            getAppStatus()	获取应用状态，0:未开始下载；1:已安装；2:需要更新；4:下载中；8:下载完成；16:下载失败；32:下载暂停；64:下载删除
                            switch (mRenderAD.getAppStatus()) {
                                case 0:
                                    downloadListener.onIdle(dataConverter);
                                    break;

                                case 1:
                                    downloadListener.onInstalled(dataConverter, appName);
                                    break;

                                case 4:
                                    downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_ACTIVE;
                                    downloadInf.downloadPercent = mRenderAD.getProgress();
                                    downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);
                                    break;
                                case 8:
                                    downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_FINISHED;
                                    downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);
                                    break;

                                case 16:
                                    downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_FAILED;
                                    downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);
                                    break;

                                case 32:
                                    downloadInf.downloadStatus = AdvanceRFConstant.AD_DOWNLOAD_STATUS_PAUSED;
                                    downloadListener.onDownloadStatusUpdate(dataConverter, downloadInf);
                                    break;

                            }
                        }
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

            //视频类广告额外处理
            if (dataConverter != null && dataConverter.isVideo()) {
                //设置播放相关配置
                AdvanceRFVideoOption advanceRFVideoOption = rfMaterialProvider.videoOption;
                VideoOption.Builder builder = new VideoOption.Builder();
                if (advanceRFVideoOption != null) {
                    builder.setAutoPlayPolicy(advanceRFVideoOption.autoPlayNetStatus);
                    builder.setAutoPlayMuted(advanceRFVideoOption.isMute);
                    builder.setDetailPageMuted(advanceRFVideoOption.isMute);
                }


                //添加自定义播放view
                AdvRFVideoView videoView = rfMaterialProvider.videoView;
                MediaView mediaView = new MediaView(getRealContext());
//                mediaView.addView(videoView);
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
                    public void onVideoResume() {
                        LogUtil.simple(TAG + "onVideoResume: ");
                        if (videoEventListener != null)

                            videoEventListener.onResume(dataConverter);

                    }

                    @Override
                    public void onVideoCompleted() {
                        LogUtil.simple(TAG + "onVideoCompleted: ");
                        if (videoEventListener != null)

                            videoEventListener.onComplete(dataConverter);

                    }

                    @Override
                    public void onVideoError(AdError error) {
                        LogUtil.simple(TAG + "onVideoError, error: " + error);
                        int code = -1;
                        String msg = "default video err";

                        if (error != null) {
                            code = error.getErrorCode();
                            msg = error.getErrorMsg();
                        }
                        handleFailed(code, msg);

                        if (videoEventListener != null)
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(code, msg));
                    }

                    @Override
                    public void onVideoStop() {
                        LogUtil.simple(TAG + "onVideoStop");
                    }

                    @Override
                    public void onVideoClicked() {
                        LogUtil.simple(TAG + "onVideoClicked");
                    }
                });
            }

        } catch (Throwable e) {
            e.printStackTrace();
            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "catch err，请检查log输出了解更多");
        }


    }

    @Override
    public boolean isValid() {
        if (mRenderAD != null) {
            return mRenderAD.isValid();
        }
        return super.isValid();
    }
}
