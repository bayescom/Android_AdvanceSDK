package com.advance.supplier.baidu;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.NativeResponse;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;

import java.util.ArrayList;
import java.util.List;

public class BDRenderDataConverter implements AdvanceRFADData {
    NativeResponse mNativeAD;
    SdkSupplier mSdkSupplier;
    public static final String TAG = "[BDRenderDataConverter] ";


    public BDRenderDataConverter(NativeResponse nativeAd, SdkSupplier sdkSupplier) {
        mNativeAD = nativeAd;
        mSdkSupplier = sdkSupplier;

        printAdInf();
    }

    private void printAdInf() {
        try {
            /**********DevelopCode Start***********/
            LogUtil.devDebug(TAG + " this is: " + this);
            LogUtil.d(TAG + "getTitle() =" + mNativeAD.getTitle());
            LogUtil.d(TAG + "getDesc() =" + mNativeAD.getDesc());
            LogUtil.d(TAG + "getIconUrl() =" + mNativeAD.getIconUrl());
            LogUtil.d(TAG + "getImageUrl() =" + mNativeAD.getImageUrl());
            LogUtil.d(TAG + "getMainPicWidth() =" + mNativeAD.getMainPicWidth());
            LogUtil.d(TAG + "getMainPicHeight() =" + mNativeAD.getMainPicHeight());
            LogUtil.d(TAG + "getBrandName() =" + mNativeAD.getBrandName());
            LogUtil.d(TAG + "getAdLogoUrl() =" + mNativeAD.getAdLogoUrl());
            LogUtil.d(TAG + "百香果 getBaiduLogoUrl() =" + mNativeAD.getBaiduLogoUrl());
            LogUtil.d(TAG + "isNeedDownloadApp() =" + mNativeAD.isNeedDownloadApp());
            LogUtil.d(TAG + "getAdActionType() =" + mNativeAD.getAdActionType());
            LogUtil.d(TAG + "isAdAvailable() =" + mNativeAD.isAdAvailable(BYUtil.getCtx()));
            LogUtil.d(TAG + "getAppSize() =" + mNativeAD.getAppSize());
            LogUtil.d(TAG + "isAutoPlay() =" + mNativeAD.isAutoPlay());
            LogUtil.d(TAG + "getAppPackage() =" + mNativeAD.getAppPackage());
            LogUtil.d(TAG + "getMultiPicUrls() =" + mNativeAD.getMultiPicUrls());
            LogUtil.d(TAG + "getVideoUrl() =" + mNativeAD.getVideoUrl());
            LogUtil.d(TAG + "getDuration() =" + mNativeAD.getDuration());
            LogUtil.d(TAG + "getMaterialType() =" + mNativeAD.getMaterialType());
            LogUtil.d(TAG + "getHtmlSnippet() =" + mNativeAD.getHtmlSnippet());
            LogUtil.d(TAG + "getWebView() =" + mNativeAD.getWebView());
            LogUtil.d(TAG + "getECPMLevel() =" + mNativeAD.getECPMLevel());

            LogUtil.d(TAG + "isNonWifiAutoPlay() =" + mNativeAD.isNonWifiAutoPlay());
            LogUtil.d(TAG + "getDownloadStatus() =" + mNativeAD.getDownloadStatus());
            LogUtil.d(TAG + "getMarketingPendant() =" + mNativeAD.getMarketingPendant());
            LogUtil.d(TAG + "getActButtonString() =" + mNativeAD.getActButtonString());
            LogUtil.d(TAG + "getDislikeList() =" + mNativeAD.getDislikeList());


            // 其他数据
            LogUtil.d(TAG + "应用包名 = " + mNativeAD.getAppPackage());
            LogUtil.d(TAG + "应用版本 = " + mNativeAD.getAppVersion());
            LogUtil.d(TAG + "开发者 = " + mNativeAD.getPublisher());
            LogUtil.d(TAG + "包大小 = " + mNativeAD.getAppSize());
            LogUtil.d(TAG + "隐私条款链接 = " + mNativeAD.getAppPrivacyLink());
            LogUtil.d(TAG + "功能链接 = " + mNativeAD.getAppFunctionLink());
            LogUtil.d(TAG + "权限信息链接 = " + mNativeAD.getAppPermissionLink());


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
            if (mNativeAD != null) {
                return mNativeAD.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            if (mNativeAD != null) {
                return mNativeAD.getDesc();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mNativeAD != null) {
                return mNativeAD.getIconUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {
        try {
            if (mNativeAD != null) {
                return mNativeAD.getActButtonString();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        if (isVideo()) {
            if (mNativeAD != null) {
                return mNativeAD.getImageUrl();
            }
        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        List<String> result = new ArrayList<>();

        try {
            if (mNativeAD != null) {
//                如果list的length大于0则为三图广告。
                if (mNativeAD.getMultiPicUrls() != null && mNativeAD.getMultiPicUrls().size() > 0) {
                    return mNativeAD.getMultiPicUrls();
                } else {
                    String imgUrl = mNativeAD.getImageUrl();
                    if (!BYStringUtil.isEmpty(imgUrl)) {
                        result.add(imgUrl);
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
            if (mNativeAD != null) {
                return mNativeAD.getAdActionType() == NativeResponse.ACTION_TYPE_APP_DOWNLOAD;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mNativeAD != null) {
                return mNativeAD.getMaterialType() == NativeResponse.MaterialType.VIDEO;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    public int getECPM() {
        try {
            if (mNativeAD != null) {
                String ecpm = mNativeAD.getECPMLevel();
                if (!BYStringUtil.isEmpty(ecpm)) {
                    int price = Integer.parseInt(ecpm);
                    if (price > 0) {
                        return price;
                    }
                }

            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        return null;
    }
}
