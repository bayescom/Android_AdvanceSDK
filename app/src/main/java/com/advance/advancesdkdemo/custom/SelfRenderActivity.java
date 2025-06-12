package com.advance.advancesdkdemo.custom;


import static com.advance.advancesdkdemo.util.DemoUtil.logAndToast;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.advance.AdvanceRenderFeed;
import com.advance.advancesdkdemo.AdvanceAD;
import com.advance.advancesdkdemo.Constants;
import com.advance.advancesdkdemo.R;
import com.advance.advancesdkdemo.util.DemoManger;
import com.advance.advancesdkdemo.util.DemoUtil;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFConstant;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.core.srender.AdvanceRFDownloadListener;
import com.advance.core.srender.AdvanceRFEventListener;
import com.advance.core.srender.AdvanceRFLoadListener;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.core.srender.AdvanceRFVideoEventListener;
import com.advance.core.srender.AdvanceRFVideoOption;
import com.advance.core.srender.widget.AdvRFLogoView;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.advance.model.AdvanceError;
import com.advance.supplier.oppo.AdvanceRFADDataOppo;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bumptech.glide.Glide;
import com.heytap.msp.mobad.api.params.INativeComplianceListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SelfRenderActivity extends Activity {
    String TAG = "[SelfRenderActivity] ";

    ImageView mIcon;
    ImageView mDislike;
    Button mCreativeButton;
    TextView mTitle;
    TextView mDescription;
    TextView mSource;

    ImageView mGroupImage1;
    ImageView mGroupImage2;
    ImageView mGroupImage3;
    LinearLayout mGroupContainer;

    //    下载6要素
    ConstraintLayout appRoot;
    TextView mAppName, mAppVersion, mAppDeveloper, mAppPermission, mAppPrivacy, mAppFunction;

    AdvRFRootView advRFRootView;
    AdvRFVideoView advRFVideoView;
    AdvRFLogoView advRFLogoView;

    FrameLayout adContainer;

    private ImageView mImagePoster;

    AdvanceRenderFeed advanceRenderFeed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_render);


        //advance必需要的view框架
        advRFRootView = findViewById(R.id.adv_root);
        advRFVideoView = findViewById(R.id.adv_video);
        advRFLogoView = findViewById(R.id.adv_logo);
//        adContainer = findViewById(R.id.ad_container);

        //广告自定义得布局view信息
        mIcon = findViewById(R.id.iv_nc_icon);
        mDislike = findViewById(R.id.iv_nc_dislike);
        mCreativeButton = findViewById(R.id.btn_nc_creative);
        mTitle = findViewById(R.id.tv_nc_ad_title);
        mDescription = findViewById(R.id.tv_nc_ad_desc);
        mSource = findViewById(R.id.tv_nc_ad_source);

        //图片素材展示相关view
        mGroupContainer = findViewById(R.id.native_3img);
        mGroupImage1 = findViewById(R.id.img_1);
        mGroupImage2 = findViewById(R.id.img_2);
        mGroupImage3 = findViewById(R.id.img_3);

        mImagePoster = findViewById(R.id.img_poster);

        appRoot = findViewById(R.id.cl_dl_root);
        mAppName = findViewById(R.id.tv_app_name);
        mAppVersion = findViewById(R.id.tv_app_version);
        mAppDeveloper = findViewById(R.id.tv_app_developer);
        mAppPermission = findViewById(R.id.tv_app_permission);
        mAppPrivacy = findViewById(R.id.tv_app_privacy);
        mAppFunction = findViewById(R.id.tv_app_introduce);
        DemoUtil.addTextLine(mAppPermission);
        DemoUtil.addTextLine(mAppPrivacy);
        DemoUtil.addTextLine(mAppFunction);

        loadAD();

    }

    private void loadAD() {

        //广告初始化，传入聚合广告位id
        advanceRenderFeed = new AdvanceRenderFeed(this, DemoManger.getInstance().currentDemoIds.nativeCustom);
        //设置期望图片大小，单位px，主要是设置给穿山甲使用，不设置将使用默认值 640*320
        advanceRenderFeed.setCsjImgSize(1080, 720);
        //设置广告请求回调
        advanceRenderFeed.setLoadListener(new AdvanceRFLoadListener() {
            @Override
            public void onADLoaded(AdvanceRFADData adData) {
                logAndToast(TAG + "onADLoaded  ");
                bindViewShow(adData);
            }

            @Override
            public void onAdFailed(AdvanceError advanceError) {
                logAndToast(TAG + "onAdFailed, advanceError = " + advanceError);
            }
        });
        //发起请求
        advanceRenderFeed.loadStrategy();
    }

    //绑定view信息给广告，并开始展示广告
    private void bindViewShow(AdvanceRFADData adData) {
        advRFRootView.setVisibility(View.VISIBLE);
        //进行广告图片、文字 及回调处理相关逻辑
        bindAdInf(adData);

        //必须，开始展现广告，一定要在 bindAdInf 之后调用！
        advanceRenderFeed.show();
    }


    //展示广告信息
    private void bindAdInf(AdvanceRFADData adData) {
        //建议，监听广告渲染事件回调
        advanceRenderFeed.setRenderEventListener(new AdvanceRFEventListener() {
            @Override
            public void onAdShow(AdvanceRFADData adData) {
                AdvanceAD.logAndToast(SelfRenderActivity.this, "onAdShow");

            }

            @Override
            public void onAdClicked(AdvanceRFADData adData) {
                AdvanceAD.logAndToast(SelfRenderActivity.this, "onAdClicked");
            }

            @Override
            public void onAdClose(AdvanceRFADData adData) {
                AdvanceAD.logAndToast(SelfRenderActivity.this, "onAdClose");

            }

            @Override
            public void onAdErr(AdvanceRFADData adData, AdvanceError advanceError) {
                AdvanceAD.logAndToast(SelfRenderActivity.this, "onAdErr" + " ,advanceError = " + advanceError);

            }
        });

        AdvanceRFMaterialProvider materialProvider = new AdvanceRFMaterialProvider();
        //必须，绑定视图给SDK渲染使用
        materialProvider.rootView = advRFRootView;
        materialProvider.videoView = advRFVideoView;
        materialProvider.logoView = advRFLogoView;
        //必须，添加可响应点击事件的view
        materialProvider.clickViews.add(advRFVideoView);
        materialProvider.clickViews.add(mDescription);
        materialProvider.clickViews.add(mCreativeButton);
        materialProvider.clickViews.add(mImagePoster);
        materialProvider.clickViews.add(mTitle);
        materialProvider.clickViews.add(mIcon);

        //必须，关闭按钮
        materialProvider.disLikeView = mDislike;
        //可选，创意按钮指定
        materialProvider.creativeViews.add(mCreativeButton);
        //可选，设置下载监听，仅穿山甲支持
        materialProvider.downloadListener = new AdvanceRFDownloadListener() {
            @Override
            public void onIdle(AdvanceRFADData data) {

            }

            @Override
            public void onDownloadStatusUpdate(AdvanceRFADData data, AdvanceRFDownloadInf downloadInf) {
                int status = downloadInf.downloadStatus;
                String showText = "";
                switch (status) {
                    case AdvanceRFConstant.AD_DOWNLOAD_STATUS_PAUSED:
                    case AdvanceRFConstant.AD_DOWNLOAD_STATUS_ACTIVE:
                        showText = downloadInf.getDownloadPercent() + "%";
                        break;
                    case AdvanceRFConstant.AD_DOWNLOAD_STATUS_FINISHED:
                        showText = "点击安装";
                        break;
                    case AdvanceRFConstant.AD_DOWNLOAD_STATUS_FAILED:
                        showText = "重新下载";
                        break;
                }
                mCreativeButton.setText(showText);
            }

            @Override
            public void onInstalled(AdvanceRFADData data, String appName) {
                mCreativeButton.setText("点击打开");

            }
        };

        if (adData.isVideo()) {
            //可选，设置视频播放选项，对优量汇、mercury、百度 生效
            AdvanceRFVideoOption videoOption = new AdvanceRFVideoOption();
            videoOption.isMute = true;
            videoOption.autoPlayNetStatus = AdvanceRFConstant.VIDEO_AUTO_PLAY_ALWAYS;
            materialProvider.videoOption = videoOption;

            //可选，设置视频播放监听，全部生效
            materialProvider.videoEventListener = new AdvanceRFVideoEventListener() {
                @Override
                public void onReady(AdvanceRFADData data) {
                    AdvanceAD.logAndToast(SelfRenderActivity.this, "onReady");


                }

                @Override
                public void onPlayStart(AdvanceRFADData data) {
                    AdvanceAD.logAndToast(SelfRenderActivity.this, "onPlayStart");

                }

                @Override
                public void onPlaying(AdvanceRFADData data, long current, long duration) {
                    AdvanceAD.logAndToast(SelfRenderActivity.this, "onPlaying");

                }

                @Override
                public void onPause(AdvanceRFADData data) {
                    AdvanceAD.logAndToast(SelfRenderActivity.this, "onPause");

                }

                @Override
                public void onResume(AdvanceRFADData data) {
                    AdvanceAD.logAndToast(SelfRenderActivity.this, "onResume");

                }

                @Override
                public void onComplete(AdvanceRFADData data) {
                    AdvanceAD.logAndToast(SelfRenderActivity.this, "onComplete");

                }

                @Override
                public void onError(AdvanceRFADData data, AdvanceError error) {
                    AdvanceAD.logAndToast(SelfRenderActivity.this, "onError ,err = " + error);

                }
            };
        }
        //必须，传递必要信息给SDK用于渲染
        advanceRenderFeed.setRfMaterialProvider(materialProvider);

// --------------- 以下为APP侧广告view渲染相关逻辑 -----------------------

        //通用展示部分，可能为空
        mTitle.setText(adData.getTitle());
        mDescription.setText(adData.getDesc());

        //设置广告icon图标，可能无返回
        if (!TextUtils.isEmpty(adData.getIconUrl())) {
            Glide.with(this).load(adData.getIconUrl()).into(mIcon);
        }
        //广告来源信息，仅穿山甲会返回
        if (!TextUtils.isEmpty(adData.getSourceText())) {
            mSource.setText(adData.getSourceText());
        }
        //视频类广告
        if (adData.isVideo()) {
            advRFVideoView.setVisibility(View.VISIBLE);
            mImagePoster.setVisibility(View.GONE);
            mGroupContainer.setVisibility(View.GONE);

        } else {//图片类广告
            //隐藏视频视图
            advRFVideoView.setVisibility(View.GONE);

            List<String> imgList = adData.getImgList();
            int picSize = imgList.size();
            LogUtil.devDebug("picSize = " + picSize);
            if (picSize > 1) {//多图
                mImagePoster.setVisibility(View.GONE);
                mGroupContainer.setVisibility(View.VISIBLE);

                String imgUrl1 = imgList.get(0);
                Glide.with(this).load(imgUrl1).into(mGroupImage1);

                String imgUrl2 = imgList.get(1);
                Glide.with(this).load(imgUrl2).into(mGroupImage2);

                if (picSize > 2) {
                    String imgUrl3 = imgList.get(2);
                    Glide.with(this).load(imgUrl3).into(mGroupImage3);
                }
            } else if (picSize > 0) {//单图
                mImagePoster.setVisibility(View.VISIBLE);
                mGroupContainer.setVisibility(View.GONE);

                String imgUrl = imgList.get(0);
                Glide.with(this).load(imgUrl).into(mImagePoster);
            }
        }

        if (adData.isDownloadAD()) {
            //oppo自渲染2.0 仅支持通过bind方式进行连接类处理

            mCreativeButton.setText("立即下载");
            // 六要素 相关内容
            AdvanceRFDownloadElement downloadElement = adData.getDownloadElement();
            if (downloadElement != null) {
                appRoot.setVisibility(View.VISIBLE);

                mAppName.setText("应用名：" + downloadElement.getAppName());
                mAppVersion.setText("版本号：" + downloadElement.getAppVersion());
                mAppDeveloper.setText("开发者：" + downloadElement.getAppDeveloper());

                boolean isOppo = adData instanceof AdvanceRFADDataOppo;
                if (isOppo) {
                    AdvanceRFADDataOppo oppoData = (AdvanceRFADDataOppo) adData;
                    oppoData.bindToComplianceView(new LinkedList<View>() {
                        {
                            /*
                             * 添加隐私声明交互view
                             * */
                            add(mAppPrivacy);
                        }
                    }, new INativeComplianceListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(Constants.DEMO_TAG, "privacy onclick = " + view);
                        }

                        @Override
                        public void onClose() {
                            Log.d(Constants.DEMO_TAG, "privacy onClose ");
                        }
                    }, new LinkedList<View>() {
                        {
                            /*
                             * 添加权限声明交互view
                             * */
                            add(mAppPermission);
                        }
                    }, new INativeComplianceListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(Constants.DEMO_TAG, "permission onclick = " + view);
                        }

                        @Override
                        public void onClose() {
                            Log.d(Constants.DEMO_TAG, "permission onClose ");
                        }
                    }, new LinkedList<View>() {
                        {
                            /*
                             * 添加应用介绍交互view
                             * */
                            add(mAppFunction);
                        }
                    }, new INativeComplianceListener() {
                        @Override
                        public void onClick(View view) {
                            Log.d(Constants.DEMO_TAG, "desc onclick = " + view);
                        }

                        @Override
                        public void onClose() {
                            Log.d(Constants.DEMO_TAG, "desc onClose ");
                        }
                    });
                } else {
                    String privacy = downloadElement.getPrivacyUrl();
                    if (TextUtils.isEmpty(privacy)) {
                        mAppPrivacy.setVisibility(View.GONE);
                    } else {
                        mAppPrivacy.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DemoUtil.openInWeb(privacy, "", null);
                            }
                        });
                    }


                    String pUrl = downloadElement.getPermissionUrl();
                    // 因为部分adn为异步返回信息，需要在回调里进行
                    downloadElement.getPermissionList(new BYAbsCallBack<ArrayList<AdvanceRFDownloadElement.AdvDownloadPermissionModel>>() {
                        @Override
                        public void invoke(ArrayList<AdvanceRFDownloadElement.AdvDownloadPermissionModel> pList) {
                            //都为空的话不展示，权限信息
                            if (TextUtils.isEmpty(pUrl) && pList.size() == 0) {
                                mAppPermission.setVisibility(View.GONE);
                            } else {
                                mAppPermission.setVisibility(View.VISIBLE);
                                mAppPermission.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        //优先看是否为url，然后再看是否有权限列表信息
                                        if (!TextUtils.isEmpty(pUrl)) {
                                            DemoUtil.openInWeb(pUrl, "", null);
                                        } else if (pList.size() > 0) {
                                            DemoUtil.openInWeb("", "", pList);
                                        }
                                    }
                                });
                            }
                        }
                    });


                    String fUrl = downloadElement.getFunctionDescUrl();
                    String fText = downloadElement.getFunctionDescText();
                    //都为空的话不展示，介绍说明
                    if (TextUtils.isEmpty(fUrl) && TextUtils.isEmpty(fText)) {
                        mAppFunction.setVisibility(View.GONE);
                    } else {
                        mAppFunction.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (!TextUtils.isEmpty(fUrl)) {
                                    DemoUtil.openInWeb(fUrl, "", null);
                                } else if (!TextUtils.isEmpty(fText)) {
                                    DemoUtil.openInWeb("", fText, null);
                                }
                            }
                        });
                    }

                }
            }
        } else {
            mCreativeButton.setText("查看详情");
        }
    }

}
