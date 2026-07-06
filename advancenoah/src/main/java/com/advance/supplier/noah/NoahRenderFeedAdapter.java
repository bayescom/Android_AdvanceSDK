package com.advance.supplier.noah;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFUtil;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.noah.api.AdError;
import com.noah.api.MediaView;
import com.noah.api.NativeAd;
import com.noah.api.NativeAdView;
import com.noah.api.RequestInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NoahRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    NativeAd noahAD;
    MediaView mediaView;

    @Override
    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        RequestInfo requestInfo = new RequestInfo();
        NativeAd.getAd(context, getPosID(), requestInfo, new NativeAd.AdListener() {
            @Override
            public void onAdError(final AdError adError) {
                LogUtil.simple(TAG + " onAdError , adError = " + adError);
                //广告返回失败

                NoahUtil.loadErr(NoahRenderFeedAdapter.this, adError);
            }

            @Override
            public void onAdLoaded(final List<NativeAd> ads) {

                //广告请求成功，展示广告
                LogUtil.simple(TAG + " onAdLoaded , ad = " + ads);
                //广告返回成功，展示广告
                if (ads == null || ads.isEmpty() || ads.get(0) == null) {
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "noahAD null");
                    return;
                }
                //广告返回成功，如果requestInfo中要求多条广告返回，会返回多条广告，默认返回1条
                NativeAd ad = ads.get(0);
                noahAD = ad;

                handleSucceed(new NoahRenderFeedData(ad), ad.getPrice());
            }

            @Override
            public void onAdShown(final NativeAd ad) {
                //广告曝光
                LogUtil.simple(TAG + " onAdShown ");

                handleShow();
            }

            @Override
            public void onAdClosed(final NativeAd ad) {
                //广告关闭，只有使用模板渲染方式才会有这个回调
                LogUtil.simple(TAG + " onAdClosed ");

                handleClose();
            }

            @Override
            public void onAdClicked(final NativeAd ad) {
                //广告点击
                LogUtil.simple(TAG + " onAdClicked ");

                handleClick();
            }

            @Override
            public void onDownloadStatusChanged(final NativeAd ad, final int apkDownloadStatus) {
                //下载类广告下载进度回调
                LogUtil.simple("onDownloadStatusChanged, return status: " + apkDownloadStatus);
            }

            @Override
            public void onAdEvent(final NativeAd ad, final int eventId, final Object extInfo) {
                //其他广告事件回调
                LogUtil.simple("onAdEvent,   eventId: " + eventId + " extInfo = " + extInfo);

            }
        });
    }

    @Override
    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        final AdvanceRFMaterialProvider rfMaterialProvider = getMaterialProvider();

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
        if (activity == null)
            activity = getRealActivity(rfMaterialProvider.rootView);

//        NativeAdView adContainer = new NativeAdView(activity);
//
//
//        AdvanceRFUtil.copyChild(rootView, adContainer);


//传入直接下载View列表并注册广告点击事件
//        noahAD.registerViewForInteraction(adContainer, rfMaterialProvider.clickViews, rfMaterialProvider.clickViews, rfMaterialProvider.creativeViews);
        noahAD.registerTargetForInteraction(rootView, rfMaterialProvider.clickViews, rfMaterialProvider.clickViews, rfMaterialProvider.creativeViews);

//关闭广告事件绑定
        View dislikeView = rfMaterialProvider.disLikeView;
        if (dislikeView != null) {
            dislikeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogUtil.simple(TAG + " dislikeView onClick");

                    handleClose();

                    try {
                        rootView.removeAllViews();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }

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
            //logo 图标
            try {
                if (noahAD.getAdAssets() != null && noahAD.getAdAssets().getAdLogo() != null) {

                    ImageView recLogo = new ImageView(getRealContext());
                    int maxW = BYDisplay.dp2px((25));
                    int h = BYDisplay.dp2px((12));
                    recLogo.setMaxWidth(maxW);
                    recLogo.setAdjustViewBounds(true);

                    Bitmap logoRes = noahAD.getAdAssets().getAdLogo();
                    recLogo.setImageBitmap(logoRes);
                    LinearLayout.LayoutParams imgLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, h);
                    imgLp.setMargins(0, 0, BYDisplay.dp2px(3), 0);
                    logoLayout.addView(recLogo, imgLp);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            //文字一般是"广告"二字
            String sourceText = "广告";
            if (dataConverter != null && BYStringUtil.isNotEmpty(dataConverter.getSourceText())) {
                sourceText = dataConverter.getSourceText();
            }

            TextView tv = new TextView(getRealContext());
            tv.setText(sourceText);
            tv.setTextColor(Color.WHITE);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);

            LinearLayout.LayoutParams txtLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            logoLayout.addView(tv, txtLp);

            rfMaterialProvider.logoView.addView(logoLayout);
        }


        //视频添加额外视图
        if (dataConverter != null && dataConverter.isVideo()) {
            AdvRFVideoView videoView = rfMaterialProvider.videoView;
            mediaView = new MediaView(activity);
            mediaView.setNativeAd(noahAD);
            videoView.addView(mediaView);
        }




    }

    @Override
    public boolean isValid() {
        if (noahAD != null)
            return noahAD.isValid();

        return false;
    }

    @Override
    public void destroyAd() {
        if (noahAD != null)
            noahAD.destroy();
        if (mediaView != null) {
            mediaView.destroy();
        }
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " + winPrice + ", referBidInfo = " + referBidInfo);

        NoahUtil.bid(noahAD, isWin, winPrice, referBidInfo);
    }
}
