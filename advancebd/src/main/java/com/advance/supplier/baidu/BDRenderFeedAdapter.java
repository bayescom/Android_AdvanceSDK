package com.advance.supplier.baidu;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_SHOW;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.core.srender.AdvanceRFVideoOption;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.BaiduNativeManager;
import com.baidu.mobads.sdk.api.ExpressResponse;
import com.baidu.mobads.sdk.api.INativeVideoListener;
import com.baidu.mobads.sdk.api.NativeResponse;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.XAdNativeResponse;
import com.baidu.mobads.sdk.api.XNativeView;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.mercury.sdk.util.MercuryTool;

import java.util.List;

public class BDRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    private BaiduNativeManager mBaiduNativeManager;
    private RequestParameters parameters;

    private NativeResponse nativeResponseAD;

    public BDRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
        parameters = AdvanceBDManager.getInstance().nativeCustomParameters;
    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    protected void paraLoadAd() {

        if (sdkSupplier != null) {
            BDUtil.initBDAccount(this);

            /**
             * Step 1. 创建BaiduNative对象，参数分别为： 上下文context，广告位ID
             * 注意：请将adPlaceId替换为自己的广告位ID
             * 注意：信息流广告对象，与广告位id一一对应，同一个对象可以多次发起请求
             */
            mBaiduNativeManager = new BaiduNativeManager(getRealContext(), sdkSupplier.adspotid);
            //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
            int bidFloor = AdvanceBDManager.getInstance().nativeCusBidFloor;
            if (bidFloor > 0) {
                mBaiduNativeManager.setBidFloor(bidFloor);
            }
            if (parameters == null) {
                parameters = new RequestParameters.Builder().build();
            }
            mBaiduNativeManager.loadFeedAd(parameters, new BaiduNativeManager.FeedAdListener() {
                @Override
                public void onNativeLoad(List<NativeResponse> list) {

                    try {
                        if (list == null || list.size() == 0) {
                            handleFailed(AdvanceError.ERROR_DATA_NULL, "NativeResponse empty");
                        } else {
                            LogUtil.simple(TAG + "onNativeLoad ， size:" + list.size());

                            nativeResponseAD = list.get(0);

//                            if (BYUtil.isDev()) {
//                                for (NativeResponse response : list) {
//                                    if (response != nativeResponseAD) {
//                                        LogUtil.devDebug("test print all response ad");
//                                        new BDRenderDataConverter(nativeResponseAD, sdkSupplier);
//                                    }
//                                }
//                            }

                            try { //避免方法有异常，catch一下，不影响success逻辑
                                if (nativeResponseAD != null) {
                                    updateBidding(BDUtil.getEcpmValue(nativeResponseAD.getECPMLevel()));
                                }

                                dataConverter = new BDRenderDataConverter(nativeResponseAD, sdkSupplier);

                            } catch (Throwable e) {
                                e.printStackTrace();
                            }

                            handleSucceed();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                    }
                }

                @Override
                public void onNativeFail(int errorCode, String message, NativeResponse expressResponse) {
                    LogUtil.simple(TAG + "onNativeFail");

                    handleFailed(errorCode + "", message);

                }

                @Override
                public void onNoAd(int code, String msg, NativeResponse expressResponse) {
                    LogUtil.simple(TAG + "onNoAd");

                    handleFailed(code + "", msg);

                }


                @Override
                public void onVideoDownloadSuccess() {
                    LogUtil.simple(TAG + "onVideoDownloadSuccess");

                }

                @Override
                public void onVideoDownloadFailed() {
                    LogUtil.simple(TAG + "onVideoDownloadFailed");

                }

                @Override
                public void onLpClosed() {
                    LogUtil.simple(TAG + "onLpClosed");

                }
            });
        }

    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
    }


    @Override
    public boolean isValid() {
        if (nativeResponseAD != null) {
            return nativeResponseAD.isReady(getRealContext());
        }
        return super.isValid();
    }

    @Override
    public void show() {
        try {
            if (AdvanceRFUtil.skipRender(this)) {
                return;
            }
            final AdvanceRFMaterialProvider rfMaterialProvider = mAdvanceRFBridge.getMaterialProvider();

            if (nativeResponseAD == null) {
                handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "未获取到广告信息");
                return;
            }

            final XAdNativeResponse nrAd = (XAdNativeResponse) nativeResponseAD;

            // clickViews: 可点击的View，默认展示下载整改弹框
            List<View> clickViews = rfMaterialProvider.clickViews;
            // creativeViews: 带有下载引导文案的View，默认不会触发下载整改弹框
            List<View> creativeViews = rfMaterialProvider.creativeViews;

            //核心，注册绑定重要view信息
            nrAd.registerViewForInteraction(rfMaterialProvider.rootView, clickViews, creativeViews, new NativeResponse.AdInteractionListener() {
                @Override
                public void onAdClick() {
                    LogUtil.simple(TAG + "onAdClick ");
                    handleClick();

                }

                @Override
                public void onADExposed() {
                    LogUtil.simple(TAG + "onADExposed ");
                    handleShow();
                }

                @Override
                public void onADExposureFailed(int reason) {
                    LogUtil.simple(TAG + "onADExposureFailed: " + reason);

                    handleFailed(reason, TAG + " onADExposureFailed , reason = " + reason);
                }

                @Override
                public void onADStatusChanged() {
                    LogUtil.simple(TAG + "onADStatusChanged ");

                }

                @Override
                public void onAdUnionClick() {
                    LogUtil.simple(TAG + "onAdUnionClick ");

                }
            });


            //核心：视频广告渲染
            bindVideo(rfMaterialProvider);

            //广告标识内容渲染
            bindSourceLogo(rfMaterialProvider);

            //绑定关闭按钮
            bindCloseView(rfMaterialProvider);

            //隐私相关监听
            nrAd.setAdPrivacyListener(new NativeResponse.AdPrivacyListener() {
                @Override
                public void onADPrivacyClick() {
                    LogUtil.simple(TAG + "onADPrivacyClick ");

                }

                @Override
                public void onADFunctionClick() {
                    LogUtil.simple(TAG + "onADFunctionClick ");

                }

                @Override
                public void onADPermissionShow() {
                    LogUtil.simple(TAG + "onADPermissionShow ");

                }

                @Override
                public void onADPermissionClose() {
                    LogUtil.simple(TAG + "onADPermissionClose ");

                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(ERROR_EXCEPTION_SHOW));
        }
    }


    private void bindVideo(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            if (dataConverter.isVideo()) {
                //设置播放相关配置
                AdvanceRFVideoOption advanceRFVideoOption = rfMaterialProvider.videoOption;
                Activity activity = getRealActivity(rfMaterialProvider.rootView);

                XNativeView videoView;
                if (activity != null) {
                    videoView = new XNativeView(activity);
                } else {
                    videoView = new XNativeView(getRealContext());
                }
                videoView.setNativeItem(nativeResponseAD);
                boolean isMute = true;
                if (advanceRFVideoOption != null) {
                    isMute = advanceRFVideoOption.isMute;
                }

                videoView.setVideoMute(isMute);

                LogUtil.simple(TAG + "videoPlayConfig,  isMute = " + isMute);

                videoView.setNativeViewClickListener(new XNativeView.INativeViewClickListener() {
                    @Override
                    public void onNativeViewClick(XNativeView xNativeView) {
                        LogUtil.simple(TAG + "videoView onNativeViewClick");

                        handleClick();
                    }
                });
                final AdvanceRFVideoEventListener videoEventListener = rfMaterialProvider.videoEventListener;
                videoView.setNativeVideoListener(new INativeVideoListener() {
                    @Override
                    public void onRenderingStart() {
                        LogUtil.simple(TAG + " onRenderingStart");

                        if (videoEventListener != null) {
                            videoEventListener.onReady(dataConverter);
                        }

                        if (videoEventListener != null) {
                            videoEventListener.onPlayStart(dataConverter);
                        }
                    }

                    @Override
                    public void onPause() {
                        LogUtil.simple(TAG + " onPause");


                        if (videoEventListener != null) {
                            videoEventListener.onPause(dataConverter);
                        }
                    }

                    @Override
                    public void onResume() {
                        LogUtil.simple(TAG + " onResume");

                        if (videoEventListener != null) {
                            videoEventListener.onResume(dataConverter);
                        }
                    }

                    @Override
                    public void onCompletion() {
                        LogUtil.simple(TAG + " onCompletion");

                        if (videoEventListener != null) {
                            videoEventListener.onComplete(dataConverter);
                        }
                    }

                    @Override
                    public void onError() {
                        LogUtil.simple(TAG + " onError");

                        String errCode = AdvanceError.ERROR_BD_FAILED;
                        String errMsg = "video play err";
                        handleFailed(errCode, errMsg);

                        if (videoEventListener != null) {
                            videoEventListener.onError(dataConverter, AdvanceError.parseErr(errCode, errMsg));
                        }
                    }
                });

                //添加视频视图view到APP使用的通用view
                if (videoView.getParent() == null) {
                    rfMaterialProvider.videoView.removeAllViews();
                    rfMaterialProvider.videoView.addView(videoView);
                }

                //必须，调用渲染以后才会正常加载出来广告内容
                videoView.render();
            }
        } catch (Throwable e) {
            e.printStackTrace();
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


    private void bindSourceLogo(AdvanceRFMaterialProvider rfMaterialProvider) {
        try {
            LinearLayout logoLayout = new LinearLayout(getRealContext());
            logoLayout.setOrientation(LinearLayout.HORIZONTAL);
            logoLayout.setGravity(Gravity.CENTER_VERTICAL);

            String sourceLogoUrl = nativeResponseAD.getBaiduLogoUrl();
            String sourceText = nativeResponseAD.getAdLogoUrl();

            int h = BYDisplay.dp2px((12));

            try {
                //百青藤 logo 图标
                if (!BYStringUtil.isEmpty(sourceLogoUrl)) {
                    ImageView recLogo = new ImageView(getRealContext());
                    int maxW = BYDisplay.dp2px((25));
                    recLogo.setMaxWidth(maxW);
                    recLogo.setAdjustViewBounds(true);
                    //调用渲染图片方法
                    MercuryTool.renderNetImg(sourceLogoUrl, recLogo);
                    LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
                    logoLayout.addView(recLogo, imgLp);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (!BYStringUtil.isEmpty(sourceText)) {
                //一般是"广告"二字得图片形式
                ImageView ivSource = new ImageView(getRealContext());
                int maxW = BYDisplay.dp2px((55));
                ivSource.setMaxWidth(maxW);
                ivSource.setAdjustViewBounds(true);

                //调用渲染图片方法
                MercuryTool.renderNetImg(sourceText, ivSource);
                LinearLayout.LayoutParams txtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
                logoLayout.addView(ivSource, txtLp);
            }
            logoLayout.setClickable(true);
            logoLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击联盟logo打开官网
                    if (nativeResponseAD != null) {
                        nativeResponseAD.unionLogoClick();
                    }
                }
            });


            LogUtil.d(TAG + "add source logoLayout ;  ");
            rfMaterialProvider.logoView.addView(logoLayout);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
