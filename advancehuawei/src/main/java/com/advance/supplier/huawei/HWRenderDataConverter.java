package com.advance.supplier.huawei;

import com.advance.BaseParallelAdapter;
import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.huawei.hms.ads.AppPermision;
import com.huawei.hms.ads.Image;
import com.huawei.hms.ads.nativead.NativeAd;

import java.util.ArrayList;
import java.util.List;

public class HWRenderDataConverter implements AdvanceRFADData {
    public static NativeAd mData;
    SdkSupplier mSdkSupplier;
    BaseParallelAdapter baseParallelAdapter;
    public static final String TAG = "[HWRenderDataConverter] ";

    public HWRenderDataConverter(NativeAd nativeAd, BaseParallelAdapter baseParallelAdapter) {
        mData = nativeAd;
        mSdkSupplier = baseParallelAdapter.sdkSupplier;
        this.baseParallelAdapter = baseParallelAdapter;
        printAdInf();
    }

    private void printAdInf() {
        try {
            /**********DevelopCode Start***********/
            LogUtil.devDebug(TAG + " this is: " + this);
            LogUtil.d(TAG + "getAdSource() =" + mData.getAdSource());

            LogUtil.d(TAG + "getTitle() =" + mData.getTitle());
            LogUtil.d(TAG + "getImages() =" + mData.getImages());
            LogUtil.d(TAG + "getDescription() =" + mData.getDescription());
            LogUtil.d(TAG + "getIcon() =" + mData.getIcon());
            LogUtil.d(TAG + "getCallToAction() =" + mData.getCallToAction());
            LogUtil.d(TAG + "getRating() =" + mData.getRating());
            LogUtil.d(TAG + "getMarket() =" + mData.getMarket());
            LogUtil.d(TAG + "getPrice() =" + mData.getPrice());
            LogUtil.d(TAG + "getVideoOperator() =" + mData.getVideoOperator());
            LogUtil.d(TAG + "getChoicesInfo() =" + mData.getChoicesInfo());
            LogUtil.d(TAG + "isCustomDislikeThisAdEnabled() =" + mData.isCustomDislikeThisAdEnabled());
            LogUtil.d(TAG + "getDislikeAdReasons() =" + mData.getDislikeAdReasons());
            LogUtil.d(TAG + "getExtraBundle() =" + mData.getExtraBundle());
            LogUtil.d(TAG + "isCustomClickAllowed() =" + mData.isCustomClickAllowed());
            LogUtil.d(TAG + "getMediaContent() =" + mData.getMediaContent());
            LogUtil.d(TAG + "getCreativeType() =" + mData.getCreativeType());
            LogUtil.d(TAG + "getUniqueId() =" + mData.getUniqueId());
            LogUtil.d(TAG + "getAdSign() =" + mData.getAdSign());
            LogUtil.d(TAG + "getWhyThisAd() =" + mData.getWhyThisAd());
            LogUtil.d(TAG + "isAutoDownloadApp() =" + mData.isAutoDownloadApp());
            LogUtil.d(TAG + "getExt() =" + mData.getExt());
            LogUtil.d(TAG + "getDspName() =" + mData.getDspName());
            LogUtil.d(TAG + "getDspLogo() =" + mData.getDspLogo());

            LogUtil.d(TAG + "getAbilityDetailInfo() =" + mData.getAbilityDetailInfo());
            LogUtil.d(TAG + "getHwChannelId() = " + mData.getHwChannelId());
            LogUtil.d(TAG + "hasAdvertiserInfo() = " + mData.hasAdvertiserInfo());
            LogUtil.d(TAG + "getAdvertiserInfo() = " + mData.getAdvertiserInfo());
            LogUtil.d(TAG + "getAppInfo() = " + mData.getAppInfo());
            LogUtil.d(TAG + "isShowAppElement() = " + mData.isShowAppElement());
            LogUtil.d(TAG + "isTransparencyOpen() = " + mData.isTransparencyOpen());
            LogUtil.d(TAG + "getTransparencyTplUrl() = " + mData.getTransparencyTplUrl());
            LogUtil.d(TAG + "getInteractionType() = " + mData.getInteractionType());
            LogUtil.d(TAG + "getPromoteInfo() = " + mData.getPromoteInfo());
            LogUtil.d(TAG + "getBiddingInfo() = " + mData.getBiddingInfo());


            // 其他数据

            LogUtil.devDebug(TAG + "isVideo = " + isVideo());
            LogUtil.devDebug(TAG + "isDownloadAD = " + isDownloadAD());
            LogUtil.devDebug(TAG + "getECPM = " + getECPM());

            /**********DevelopCode End***********/
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public AdvanceSdkSupplier getSdkSupplier() {
        AdvanceSdkSupplier advanceSdkSupplier = new AdvanceSdkSupplier();
        try {
            if (mSdkSupplier != null) {
                advanceSdkSupplier.adnId = mSdkSupplier.id;
                advanceSdkSupplier.adspotId = mSdkSupplier.adspotid;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return advanceSdkSupplier;
    }

    @Override
    public String getTitle() {
        try {
            if (mData != null) {
                return mData.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            if (mData != null) {
                return mData.getDescription();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mData != null) {
                return HWUtil.getMaterialPath(baseParallelAdapter.getRealContext(), mData.getIcon().getUri());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {
        try {
            if (mData != null) {
                return mData.getAdSource();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        try {
            if (mData != null) {
//                return mData.getVideo();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        List<String> result = new ArrayList<>();
        try {
            if (mData != null) {
                List<Image> ksImages = mData.getImages();
                if (ksImages != null) {
                    for (Image ksImage : ksImages) {
                        String imageUrl = HWUtil.getMaterialPath(baseParallelAdapter.getRealContext(), ksImage.getUri());
                        if (!BYStringUtil.isEmpty(imageUrl)) {
                            result.add(imageUrl);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean isDownloadAD() {
//        根据NativeAd.getCreativeType()判断广告的创意类型是否带有下载按钮，如果是，则为应用下载类广告
        int createType = mData.getCreativeType();
        return createType == 103 || createType == 106;
//        return false;
    }

    @Override
    public boolean isVideo() {
        boolean hasVideo = false;
        try {
            hasVideo = mData.getVideoOperator().hasVideo();
        } catch (Exception e) {
        }
        return hasVideo;
    }

    @Override
    public int getECPM() {
        double price = HWUtil.getPrice(mData.getBiddingInfo());
        return (int) price;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        if (mData == null) {
            return null;
        }
        return new HwDownloadElement();

    }

    private static class HwDownloadElement implements AdvanceRFDownloadElement {
        @Override
        public String getAppName() {
            try {
                if (mData != null) {
                    return mData.getAppInfo().getAppName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                if (mData != null) {
                    return mData.getAppInfo().getVersionName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                if (mData != null) {
                    return mData.getAppInfo().getDeveloperName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                if (mData != null) {
                    return mData.getAppInfo().getPrivacyLink();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
            try {
                if (mData != null) {
                    return mData.getAppInfo().getPermissionUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> callBack) {
            try {
                ArrayList<AdvDownloadPermissionModel> result = new ArrayList<>();
                if (mData != null) {
                    List<AppPermision> permissionInfo = mData.getAppInfo().getAppPermissions();
                    if (permissionInfo != null) {
                        //  查看列表信息，转换方式
                        for (AppPermision permission : permissionInfo) {
                            AdvDownloadPermissionModel permissionModel = new AdvDownloadPermissionModel();
                            permissionModel.permTitle = permission.getName();
                            result.add(permissionModel);
                        }
                    }

                }

                if (callBack != null) {
                    callBack.invoke(result);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getFunctionDescUrl() {
            try {
                if (mData != null) {
                    return mData.getAppInfo().getAppDetailUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            try {
//                if (mData != null) {
//                    return mData.getAppInfo().get();
//                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public long getPkgSize() {
            try {
                if (mData != null) {
                    return mData.getAppInfo().getFileSize();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

}

