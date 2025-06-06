package com.advance.supplier.csj;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.advance.AdvanceConfig;
import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFConstant;
import com.advance.core.srender.AdvanceRFDownloadListener;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.TTNativeAd;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class CsjRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    TTAdNative mTTAdNative;
    TTFeedAd mRenderAD;
//    CsjRenderDataConverter dataConverter;
    private final Map<CsjRenderFeedAdapter, TTAppDownloadListener> mTTAppDownloadListenerMap = new WeakHashMap<>();

    public CsjRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        startLoadOnly();
    }

    @Override
    protected void paraLoadAd() {
        startLoadOnly();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (mRenderAD != null) {
                mRenderAD.destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        startShow();
    }

    private void startLoadOnly() {
        CsjUtil.initCsj(this, new CsjUtil.InitListener() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
                startLoad();
            }

            @Override
            public void fail(int code, String msg) {
                handleFailed(code, msg);
            }
        });
    }

    private void startLoad() {
        try {
            //step1:初始化sdk
            final TTAdManager ttAdManager = TTAdSdk.getAdManager();
            //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
            if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
                ttAdManager.requestPermissionIfNecessary(getRealContext());
            }
            //step3:创建TTAdNative对象,用于调用广告请求接口
            mTTAdNative = ttAdManager.createAdNative(getRealContext());

            AdSlot adSlot = new AdSlot.Builder()
                    .setCodeId(sdkSupplier.adspotid)
                    .setSupportDeepLink(true)
                    .setImageAcceptedSize(mAdvanceRFBridge.getADSizeW(), mAdvanceRFBridge.getADSizeH())
                    .setAdCount(1) //请求广告数量为1到3条
                    .build();

            //请求自渲染广告
            mTTAdNative.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
                @Override
                public void onError(int code, String msg) {
                    handleFailed(code, msg);
                }

                @Override
                public void onFeedAdLoad(List<TTFeedAd> list) {
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
                        updateBidding(CsjUtil.getEcpmValue(TAG, mRenderAD.getMediaExtraInfo()));

                        //转换穿山甲返回广告model为聚合通用model
                        dataConverter = new CsjRenderDataConverter(mRenderAD, sdkSupplier);
                        //标记广告成功
                        handleSucceed();
                        //通知广告成功
//                        mAdvanceRFBridge.adapterDidLoaded(dataConverter);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void startShow() {
        try {
            if (mAdvanceRFBridge == null || mTTAdNative == null || mRenderAD == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "advanceRFBridge null");
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

            if (rfMaterialProvider.logoView != null) {
                ImageView logo = new ImageView(getRealContext());
                logo.setImageBitmap(mRenderAD.getAdLogo());
                rfMaterialProvider.logoView.addView(logo);
            }

            //绑定关闭按钮
            View dislikeView = rfMaterialProvider.disLikeView;
            if (dislikeView != null) {
                final TTAdDislike ttAdDislike = mRenderAD.getDislikeDialog(getRealActivity(rfMaterialProvider.rootView));
                dislikeView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ttAdDislike != null) {
                            ttAdDislike.showDislikeDialog();
                        }
                    }
                });
                ttAdDislike.setDislikeInteractionCallback(new TTAdDislike.DislikeInteractionCallback() {
                    @Override
                    public void onShow() {
                        LogUtil.devDebug(TAG + "ttAdDislike onShow ");

                    }

                    @Override
                    public void onSelected(int position, String value, boolean enforce) {
                        LogUtil.devDebug(TAG + "ttAdDislike onSelected ,value = " + value);
                        handleClose();
                    }

                    @Override
                    public void onCancel() {
                        LogUtil.devDebug(TAG + "ttAdDislike onCancel ");

                    }
                });
            }

//            绑定view
            mRenderAD.registerViewForInteraction((ViewGroup) rfMaterialProvider.rootView, rfMaterialProvider.clickViews, rfMaterialProvider.creativeViews, rfMaterialProvider.disLikeView, new TTNativeAd.AdInteractionListener() {
                @Override
                public void onAdClicked(View view, TTNativeAd ttNtObject) {
                    LogUtil.simple(TAG + "onClicked");

                    handleClick();
                }

                @Override
                public void onAdCreativeClick(View view, TTNativeAd ttNtObject) {
                    LogUtil.simple(TAG + "onCreativeClick");
                    handleClick();

                }

                @Override
                public void onAdShow(TTNativeAd ttNtObject) {
                    LogUtil.simple(TAG + "onShow");

                    handleShow();
                }
            });


            final AdvanceRFDownloadListener downloadListener = rfMaterialProvider.downloadListener;
            if (dataConverter.isDownloadAD() && downloadListener != null) {
                TTAppDownloadListener ttAppDownloadListener = new TTAppDownloadListener() {
                    @Override
                    public void onIdle() {
                        if (notValid()) {
                            return;
                        }
                        downloadListener.onIdle(dataConverter);
                    }

                    @Override
                    public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        if (notValid()) {
                            return;
                        }
                        downloadListener.onDownloadStatusUpdate(dataConverter, new AdvanceRFDownloadListener.AdvanceRFDownloadInf(AdvanceRFConstant.AD_DOWNLOAD_STATUS_ACTIVE, totalBytes, 0, fileName, appName));

                    }

                    @Override
                    public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        if (notValid()) {
                            return;
                        }
                        downloadListener.onDownloadStatusUpdate(dataConverter, new AdvanceRFDownloadListener.AdvanceRFDownloadInf(AdvanceRFConstant.AD_DOWNLOAD_STATUS_PAUSED, totalBytes, 0, fileName, appName));
                    }

                    @Override
                    public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        if (notValid()) {
                            return;
                        }
                        downloadListener.onDownloadStatusUpdate(dataConverter, new AdvanceRFDownloadListener.AdvanceRFDownloadInf(AdvanceRFConstant.AD_DOWNLOAD_STATUS_FAILED, totalBytes, 0, fileName, appName));
                    }

                    @Override
                    public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        if (notValid()) {
                            return;
                        }
                        downloadListener.onDownloadStatusUpdate(dataConverter, new AdvanceRFDownloadListener.AdvanceRFDownloadInf(AdvanceRFConstant.AD_DOWNLOAD_STATUS_FINISHED, totalBytes, 0, fileName, appName));
                    }

                    @Override
                    public void onInstalled(String fileName, String appName) {
                        if (notValid()) {
                            return;
                        }
                        downloadListener.onInstalled(dataConverter, appName);
                    }

                    private boolean notValid() {
                        return mTTAppDownloadListenerMap.get(CsjRenderFeedAdapter.this) != this;
                    }
                };
                mRenderAD.setDownloadListener(ttAppDownloadListener);
                mTTAppDownloadListenerMap.put(this, ttAppDownloadListener);
            }
            if (dataConverter.isVideo()) {
                View video = mRenderAD.getAdView();
                if (video != null) {
                    if (video.getParent() == null) {
                        rfMaterialProvider.videoView.removeAllViews();
                        rfMaterialProvider.videoView.addView(video);
                    }
                }

                final AdvanceRFVideoEventListener videoEventListener = rfMaterialProvider.videoEventListener;
                mRenderAD.setVideoAdListener(new TTFeedAd.VideoAdListener() {
                    @Override
                    public void onVideoLoad(TTFeedAd ttAdObject) {

                        if (videoEventListener != null) {
                            videoEventListener.onReady(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoError(int errCode, int i1) {
                        String eMsg = " extraCode = " + i1;
                        handleFailed(errCode, eMsg);

                        if (videoEventListener != null) {
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(errCode, eMsg));
                        }
                    }

                    @Override
                    public void onVideoAdStartPlay(TTFeedAd ttAdObject) {
                        if (videoEventListener != null) {
                            videoEventListener.onPlayStart(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoAdPaused(TTFeedAd ttAdObject) {
                        if (videoEventListener != null) {
                            videoEventListener.onPause(dataConverter);
                        }
                    }

                    @Override
                    public void onVideoAdContinuePlay(TTFeedAd ttAdObject) {
                        if (videoEventListener != null) {
                            videoEventListener.onResume(dataConverter);
                        }
                    }

                    @Override
                    public void onProgressUpdate(long l, long l1) {
                        if (videoEventListener != null) {
                            videoEventListener.onPlaying(dataConverter, l, l1);
                        }
                    }

                    @Override
                    public void onVideoAdComplete(TTFeedAd ttAdObject) {
                        if (videoEventListener != null) {
                            videoEventListener.onComplete(dataConverter);
                        }
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "code exception");
        }
    }

    @Override
    public boolean isValid() {
        if (mRenderAD != null && mRenderAD.getMediationManager() != null) {
            return mRenderAD.getMediationManager().isReady();
        }
        return super.isValid();
    }

}
