package com.advance.supplier.honor;

import com.advance.BaseParallelAdapter;
import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.hihonor.adsdk.base.api.feed.PictureTextExpressAd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HonorRenderDataConverter implements AdvanceRFADData {
    public static   PictureTextExpressAd mExpressAd;
    BaseParallelAdapter adapter;
    SdkSupplier mSdkSupplier;

    public static final String TAG = "[HonorRenderDataConverter] ";

    public HonorRenderDataConverter(PictureTextExpressAd ad, BaseParallelAdapter adapter) {
        try {
            this.adapter = adapter;

            mExpressAd = ad;
            if (adapter != null) {
                mSdkSupplier = adapter.sdkSupplier;
            }

            printAdInf();
        } catch (Throwable e) {
        }
    }

    private void printAdInf() {

        try {
            /**********DevelopCode Start***********/
            LogUtil.devDebug(TAG + " this is: " + this);

            LogUtil.d(TAG + "getTitle() =" + mExpressAd.getTitle());
            LogUtil.d(TAG + "getImages() =" + mExpressAd.getImages());
            LogUtil.d(TAG + "getAppPackage() =" + mExpressAd.getAppPackage());
            LogUtil.d(TAG + "getRenderMode() =" + mExpressAd.getRenderMode());
            LogUtil.d(TAG + "getExpressAdView() =" + mExpressAd.getExpressAdView());
            LogUtil.d(TAG + "getAdId() =" + mExpressAd.getAdId());
            LogUtil.d(TAG + "getAppVersion() =" + mExpressAd.getAppVersion());
            LogUtil.d(TAG + "getTrackUrl() =" + mExpressAd.getTrackUrl());
            LogUtil.d(TAG + "getDeveloperName() =" + mExpressAd.getDeveloperName());
            LogUtil.d(TAG + "getPkgSizeBytes() =" + mExpressAd.getPkgSizeBytes());
            LogUtil.d(TAG + "getInstallPkgType() =" + mExpressAd.getInstallPkgType());
            LogUtil.d(TAG + "getAdType() =" + mExpressAd.getAdType());
            LogUtil.d(TAG + "getRequestId() =" + mExpressAd.getRequestId());
            LogUtil.d(TAG + "getInteractType() =" + mExpressAd.getInteractType());
            LogUtil.d(TAG + "getLandingPageUrl() =" + mExpressAd.getLandingPageUrl());
            LogUtil.d(TAG + "getAdSpecTemplateType() =" + mExpressAd.getAdSpecTemplateType());
            LogUtil.d(TAG + "getImgHeight() =" + mExpressAd.getImgHeight());
            LogUtil.d(TAG + "getImgWidth() =" + mExpressAd.getImgWidth());
            LogUtil.d(TAG + "getSequence() =" + mExpressAd.getSequence());
            LogUtil.d(TAG + "getSubType() =" + mExpressAd.getSubType());
            LogUtil.d(TAG + "getAdFlag() =" + mExpressAd.getAdFlag());
            LogUtil.d(TAG + "getCloseFlag() =" + mExpressAd.getCloseFlag());
            LogUtil.d(TAG + "getStyle() =" + mExpressAd.getStyle());

            LogUtil.d(TAG + "getPromotionPurpose() =" + mExpressAd.getPromotionPurpose());
            LogUtil.d(TAG + "getPkgType() = " + mExpressAd.getPkgType());
            LogUtil.d(TAG + "getProportion() = " + mExpressAd.getProportion());
            LogUtil.d(TAG + "getLogo() = " + mExpressAd.getLogo());
            LogUtil.d(TAG + "getBrand() = " + mExpressAd.getBrand());
            LogUtil.d(TAG + "getPkgSign() = " + mExpressAd.getPkgSign());
            LogUtil.d(TAG + "getEcpm() = " + mExpressAd.getEcpm());
            LogUtil.d(TAG + "getLevel() = " + mExpressAd.getLevel());
            LogUtil.d(TAG + "getAdnId() = " + mExpressAd.getAdnId());
            LogUtil.d(TAG + "getAdnType() = " + mExpressAd.getAdnType());
            LogUtil.d(TAG + "getUseTime() = " + mExpressAd.getUseTime());
            LogUtil.d(TAG + "isDownload() = " + mExpressAd.isDownload());
            LogUtil.d(TAG + "getPermissionsUrl() = " + mExpressAd.getPermissionsUrl());
            LogUtil.d(TAG + "getPrivacyAgreementUrl() = " + mExpressAd.getPrivacyAgreementUrl());
            LogUtil.d(TAG + "getHomePage() = " + mExpressAd.getHomePage());
            LogUtil.d(TAG + "getAppIntro() = " + mExpressAd.getAppIntro());
            LogUtil.d(TAG + "getIntroUrl() = " + mExpressAd.getIntroUrl());
            LogUtil.d(TAG + "getCreativeTemplateId() = " + mExpressAd.getCreativeTemplateId());
            LogUtil.d(TAG + "getIncentivePoints() = " + mExpressAd.getIncentivePoints());
            LogUtil.d(TAG + "getDetailPageOpenMode() = " + mExpressAd.getDetailPageOpenMode());
            LogUtil.d(TAG + "getStoreChannel() = " + mExpressAd.getStoreChannel());
            LogUtil.d(TAG + "getChannelInfo() = " + mExpressAd.getChannelInfo());
            LogUtil.d(TAG + "getExtraJson() = " + mExpressAd.getExtraJson());
            LogUtil.d(TAG + "getSubChannel() = " + mExpressAd.getSubChannel());
            LogUtil.d(TAG + "getItemPosition() = " + mExpressAd.getItemPosition());
            LogUtil.d(TAG + "getDataType() = " + mExpressAd.getDataType());
            LogUtil.d(TAG + "getButtonText() = " + mExpressAd.getButtonText());
            LogUtil.d(TAG + "getUnInstallFilter() = " + mExpressAd.getUnInstallFilter());
            LogUtil.d(TAG + "getDpPackageName() = " + mExpressAd.getDpPackageName());
            LogUtil.d(TAG + "getTemplateId() = " + mExpressAd.getTemplateId());
            LogUtil.d(TAG + "getDnComponentStyle() = " + mExpressAd.getDnComponentStyle());
            LogUtil.d(TAG + "getMediaBidMode() = " + mExpressAd.getMediaBidMode());
            LogUtil.d(TAG + "getAppName() = " + mExpressAd.getAppName());
            LogUtil.d(TAG + "getExpirationTime() = " + mExpressAd.getExpirationTime());


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
            if (mExpressAd != null) {
                return mExpressAd.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.getLogo();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {

        return "";
    }

    @Override
    public String getVideoImageUrl() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.getCoverUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.getImages();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isDownloadAD() {
        try {
            if (mExpressAd != null) {
//                获取推广目标。 返回推广目标 0：应用推广（下载） 1：网页推广 2：应用直达 3:小程序推广 4:预约广告 103：快应用推广。
                return mExpressAd.getPromotionPurpose() == 0;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.hasVideo();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mExpressAd != null) {
                return (int) HonorUtil.getECPM(mExpressAd);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        if (mExpressAd == null) {
            return null;
        }
        return new HonorDownloadElement();
    }

    private static class HonorDownloadElement implements AdvanceRFDownloadElement {

        @Override
        public String getAppName() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getAppName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getAppVersion();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getDeveloperName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getPrivacyAgreementUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getPermissionsUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> callBack) {

        }

        @Override
        public String getFunctionDescUrl() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getIntroUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getAppIntro();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public long getPkgSize() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getPkgSizeBytes();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return 0;
        }
    }
}
