package com.advance.supplier.sigmob;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.NativeADEventListener;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigmobRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    WindNativeUnifiedAd windNativeUnifiedAd;
    WindNativeAdData mRenderAD;
    AdvanceRFMaterialProvider rfMaterialProvider;

    public SigmobRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        SigmobUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法
                startLoad();
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

        try {
            if (windNativeUnifiedAd!=null){
                windNativeUnifiedAd.destroy();
            }
            if (mRenderAD!=null){
                mRenderAD.destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void startLoad() {
        try {
            String userId = SigmobSetting.getInstance().userId;
            Map<String, Object> options = new HashMap<>();
            options.put("user_id", userId);

            WindNativeAdRequest nativeAdRequest = new WindNativeAdRequest(sdkSupplier.adspotid, userId, options);

            windNativeUnifiedAd = new WindNativeUnifiedAd(nativeAdRequest);
            windNativeUnifiedAd.setNativeAdLoadListener(new WindNativeUnifiedAd.WindNativeAdLoadListener() {
                @Override
                public void onAdError(WindAdError error, String placementId) {
                    SigmobUtil.handlerErr(SigmobRenderFeedAdapter.this, error, "");

                }

                @Override
                public void onAdLoad(List<WindNativeAdData> list, String placementId) {
                    if (list == null || list.size() == 0) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "ads empty");
                        return;
                    }
                    mRenderAD = list.get(0);
                    if (mRenderAD == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "mRenderAD null");
                        return;
                    }

                    if (windNativeUnifiedAd != null)
                        //更新ecpm价格信息
                        updateBidding(SigmobUtil.getEcpmNumber(windNativeUnifiedAd.getEcpm()));

                    //转换返回广告model为聚合通用model
                    dataConverter = new SigmobRenderDataConverter(mRenderAD, sdkSupplier);

                    //标记广告成功
                    handleSucceed();
                }
            });
            windNativeUnifiedAd.loadAd(1);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {

        try {
            if (AdvanceRFUtil.skipRender(this)) {
                LogUtil.d(TAG + " skipRender");
                return;
            }


            if (windNativeUnifiedAd == null || mRenderAD == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW, "广告对象为空"));
                return;
            }
            rfMaterialProvider = mAdvanceRFBridge.getMaterialProvider();
            //必要事件
            bindCoreView();
            //视频内容
            bindMedia();
            //图片内容
            bindImage();

            //关闭及dislike
            bindDislike();
            //广告标识
            bindAdSource();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW, "代码异常"));
        }


    }


    private void bindCoreView() {
        try {
            List<View> clickableViews = rfMaterialProvider.clickViews;
            List<View> creativeViewList = rfMaterialProvider.creativeViews;
            View disLikeView = rfMaterialProvider.disLikeView;
            mRenderAD.bindViewForInteraction(rfMaterialProvider.rootView, clickableViews, creativeViewList, disLikeView, new NativeADEventListener() {
                @Override
                public void onAdExposed() {

                    LogUtil.simple(TAG + "onAdExposed");

                    handleShow();
                }

                @Override
                public void onAdClicked() {
                    LogUtil.simple(TAG + "onAdClicked");

                    handleClick();
                }

                @Override
                public void onAdDetailShow() {
                    LogUtil.simple(TAG + "onAdDetailShow");

                }

                @Override
                public void onAdDetailDismiss() {
                    LogUtil.simple(TAG + "onAdDetailDismiss");

                }

                @Override
                public void onAdError(WindAdError error) {
                    LogUtil.simple(TAG + "onAdError .error = " + error);

                    SigmobUtil.handlerErr(SigmobRenderFeedAdapter.this, error, AdvanceError.ERROR_RENDER_FAILED);

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void bindImage(){
        try {
            mRenderAD.bindImageViews(rfMaterialProvider.imageViews,0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    private void bindMedia() {
        try {
            final AdvanceRFVideoEventListener vListener =  rfMaterialProvider.videoEventListener;
            //默认调用不带六要素的视频布局，不然聚合维度上，app实现起来会变得更臃肿
            mRenderAD.bindMediaViewWithoutAppInfo(rfMaterialProvider.videoView, new WindNativeAdData.NativeADMediaListener() {
                @Override
                public void onVideoLoad() {
                    LogUtil.simple(TAG + "onVideoLoad");

                    if (vListener!=null){
                        vListener.onReady(dataConverter);
                    }
                }

                @Override
                public void onVideoError(WindAdError error) {
                    LogUtil.simple(TAG + "onVideoError");

                    AdvanceError advanceError =AdvanceError.parseErr(AdvanceError.ERROR_VIDEO_RENDER_ERR);
                    if (error!=null){
                        advanceError.code = error.getErrorCode()+"";
                        advanceError.msg = error.getMessage();
                    }

                    if (vListener!=null){
                        vListener.onError(dataConverter,advanceError);
                    }
                }

                @Override
                public void onVideoStart() {
                    LogUtil.simple(TAG + "onVideoStart");
                    if (vListener!=null){
                        vListener.onPlayStart(dataConverter);
                    }
                }

                @Override
                public void onVideoPause() {
                    LogUtil.simple(TAG + "onVideoPause");

                    if (vListener!=null){
                        vListener.onPause(dataConverter);
                    }
                }

                @Override
                public void onVideoResume() {
                    LogUtil.simple(TAG + "onVideoResume");

                    if (vListener!=null){
                        vListener.onResume(dataConverter);
                    }
                }

                @Override
                public void onVideoCompleted() {
                    LogUtil.simple(TAG + "onVideoCompleted");

                    if (vListener!=null){
                        vListener.onComplete(dataConverter);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void bindDislike() {
        try {
            Activity activity = getRealActivity(rfMaterialProvider.rootView);


            mRenderAD.setDislikeInteractionCallback(activity, new WindNativeAdData.DislikeInteractionCallback() {
                @Override
                public void onShow() {
                    LogUtil.simple(TAG + " bindDislike -- onShow");

                }

                @Override
                public void onSelected(int position, String value, boolean enforce) {
                    LogUtil.simple(TAG + " bindDislike -- onSelected , value = " + value + " , position= " + position + " , enforce= " + enforce);
                    handleClose();
                }

                @Override
                public void onCancel() {
                    LogUtil.simple(TAG + " bindDislike -- onCancel");

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void bindAdSource() {
        try {
            if (rfMaterialProvider.logoView != null) {
                Bitmap logoRes = mRenderAD.getAdLogo();
                if (logoRes != null) {
                    ImageView logo = new ImageView(getRealContext());
                    logo.setImageBitmap(logoRes);
                    rfMaterialProvider.logoView.addView(logo);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
