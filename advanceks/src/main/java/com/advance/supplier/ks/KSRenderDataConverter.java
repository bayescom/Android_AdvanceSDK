package com.advance.supplier.ks;


import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.kwad.sdk.api.KsImage;
import com.kwad.sdk.api.KsNativeAd;
import com.kwad.sdk.api.model.AdSourceLogoType;
import com.kwad.sdk.api.model.InteractionType;
import com.kwad.sdk.api.model.MaterialType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KSRenderDataConverter implements AdvanceRFADData {
    public static KsNativeAd mKsData;
    SdkSupplier mSdkSupplier;
    public static final String TAG = "[KSRenderDataConverter] ";


    public KSRenderDataConverter(KsNativeAd nativeAd, SdkSupplier sdkSupplier) {
        mKsData = nativeAd;
        mSdkSupplier = sdkSupplier;

        printAdInf();
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

    //没有标题信息
    @Override
    public String getTitle() {
//        try {
//            if (mKsData != null) {
//                return mKsData.getAdDescription();
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            if (mKsData != null) {
                return mKsData.getAdDescription();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mKsData != null) {
                return mKsData.getAppIconUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";

    }

    @Override
    public String getSourceText() {
        try {
            if (mKsData != null) {
                return mKsData.getAdSource();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        try {
            if (mKsData != null && mKsData.getVideoCoverImage() != null) {
                return mKsData.getVideoCoverImage().getImageUrl();
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
            if (mKsData != null) {
                List<KsImage> ksImages = mKsData.getImageList();
                if (ksImages != null) {
                    for (KsImage ksImage : ksImages) {
                        String imageUrl = ksImage.getImageUrl();
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
        try {
            if (mKsData != null) {
                return mKsData.getInteractionType() == InteractionType.DOWNLOAD;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mKsData != null) {
                return mKsData.getMaterialType() == MaterialType.VIDEO || mKsData.getMaterialType() == MaterialType.ORIGIN_LIVE;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mKsData != null) {
                return mKsData.getECPM();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        if (mKsData == null) {
            return null;
        }
        return new KsDownloadElement();

    }


    public void printAdInf() {
        try {
            /**********DevelopCode Start***********/
            LogUtil.devDebug(TAG + " this is: " + this);
            LogUtil.d(TAG + "getAdDescription() =" + mKsData.getAdDescription());
            LogUtil.d(TAG + "getAdSource() =" + mKsData.getAdSource());
            LogUtil.d(TAG + "getImageList() =" + mKsData.getImageList());
            LogUtil.d(TAG + "getAdSourceLogoUrl() =" + mKsData.getAdSourceLogoUrl(AdSourceLogoType.NORMAL));
            LogUtil.d(TAG + "getAppIconUrl() =" + mKsData.getAppIconUrl());
            LogUtil.d(TAG + "getAppName() =" + mKsData.getAppName());
            LogUtil.d(TAG + "getAppDownloadCountDes() =" + mKsData.getAppDownloadCountDes());
            LogUtil.d(TAG + "getAppScore() =" + mKsData.getAppScore());
            LogUtil.d(TAG + "getCorporationName() =" + mKsData.getCorporationName());
            LogUtil.d(TAG + "getPermissionInfo() =" + mKsData.getPermissionInfo());
            LogUtil.d(TAG + "getPermissionInfoUrl() =" + mKsData.getPermissionInfoUrl());
            LogUtil.d(TAG + "getIntroductionInfo() =" + mKsData.getIntroductionInfo());
            LogUtil.d(TAG + "getIntroductionInfoUrl() =" + mKsData.getIntroductionInfoUrl());
            LogUtil.d(TAG + "getAppPrivacyUrl() =" + mKsData.getAppPrivacyUrl());
            LogUtil.d(TAG + "getAppVersion() =" + mKsData.getAppVersion());
            LogUtil.d(TAG + "getAppPackageName() =" + mKsData.getAppPackageName());
            LogUtil.d(TAG + "getAppPackageSize() =" + mKsData.getAppPackageSize());
            LogUtil.d(TAG + "getVideoUrl() =" + mKsData.getVideoUrl());
            LogUtil.d(TAG + "getVideoCoverImage() =" + mKsData.getVideoCoverImage());
            LogUtil.d(TAG + "getVideoWidth() =" + mKsData.getVideoWidth());
            LogUtil.d(TAG + "getVideoHeight() =" + mKsData.getVideoHeight());
            LogUtil.d(TAG + "getVideoDuration() =" + mKsData.getVideoDuration());
            LogUtil.d(TAG + "getActionDescription() =" + mKsData.getActionDescription());
            LogUtil.d(TAG + "getProductName() =" + mKsData.getProductName());


            // 其他数据
            LogUtil.d(TAG + "应用名字 = " + mKsData.getAppName());
            LogUtil.d(TAG + "应用包名 = " + mKsData.getAppPackageName());
            LogUtil.d(TAG + "应用版本 = " + mKsData.getAppVersion());
            LogUtil.d(TAG + "开发者 = " + mKsData.getCorporationName());
            LogUtil.d(TAG + "包大小 = " + mKsData.getAppPackageSize());
            LogUtil.d(TAG + "隐私条款链接 = " + mKsData.getAppPrivacyUrl());
            LogUtil.d(TAG + "权限信息 = " + mKsData.getPermissionInfo());
            LogUtil.d(TAG + "权限信息链接 = " + mKsData.getPermissionInfoUrl());


            LogUtil.devDebug(TAG + "isVideo = " + isVideo());
            LogUtil.devDebug(TAG + "isDownloadAD = " + isDownloadAD());
            LogUtil.devDebug(TAG + "getECPM = " + getECPM());

            /**********DevelopCode End***********/
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static class KsDownloadElement implements AdvanceRFDownloadElement {
        @Override
        public String getAppName() {
            try {
                if (mKsData != null) {
                    return mKsData.getAppName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                if (mKsData != null) {
                    return mKsData.getAppVersion();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                if (mKsData != null) {
                    return mKsData.getCorporationName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                if (mKsData != null) {
                    return mKsData.getAppPrivacyUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
            try {
                if (mKsData != null) {
                    return mKsData.getPermissionInfoUrl();
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
                if (mKsData != null) {
                    String permissionInfo = mKsData.getPermissionInfo();
                    //  查看列表信息，转换方式

                    if (!BYStringUtil.isEmpty(permissionInfo)) {
                        String[] perList = permissionInfo.split("\n");
                        for (String per : perList) {
                            AdvDownloadPermissionModel permissionModel = new AdvDownloadPermissionModel();
                            permissionModel.permTitle = per;
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
                if (mKsData != null) {
                    return mKsData.getIntroductionInfoUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            try {
                if (mKsData != null) {
                    return mKsData.getIntroductionInfo();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public long getPkgSize() {
            try {
                if (mKsData != null) {
                    return mKsData.getAppPackageSize();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
}
