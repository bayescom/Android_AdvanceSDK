package com.advance.supplier.tanx;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.ad.ad.feed.ITanxFeedAd;
import com.alimm.tanx.core.ad.bean.CreativeItem;
import com.bayes.sdk.basic.util.BYStringUtil;

import java.util.ArrayList;
import java.util.List;

public class TanxRenderDataConverter implements AdvanceRFADData {

    ITanxFeedAd mNativeAD;
    SdkSupplier mSdkSupplier;
    public static final String TAG = "[TanxRenderDataConverter] ";

    public TanxRenderDataConverter(ITanxFeedAd feedAd, SdkSupplier sdkSupplier) {
        try {
            this.mNativeAD = feedAd;
            this.mSdkSupplier = sdkSupplier;

            printAdInf();

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    private void printAdInf() {
        try {
            /**********DevelopCode Start***********/
            LogUtil.devDebug(TAG + " this is: " + this);
            CreativeItem creativeItem = mNativeAD.getBidInfo().getCreativeItem();

            LogUtil.d(TAG + "getTitle() =" + creativeItem.getTitle());
            LogUtil.d(TAG + "getDescription() =" + creativeItem.getDescription());
            LogUtil.d(TAG + "getAdvName() =" + creativeItem.getAdvName());
            LogUtil.d(TAG + "getImgSm() =" + creativeItem.getImgSm());
            LogUtil.d(TAG + "getImageUrl() =" + creativeItem.getImageUrl());
            LogUtil.d(TAG + "getAdvLogo() =" + creativeItem.getAdvLogo());
            LogUtil.d(TAG + "getActionText() =" + creativeItem.getActionText());


            LogUtil.d(TAG + "iTanxFeedAd.getAdType() =" + mNativeAD.getAdType());
            LogUtil.d(TAG + "getBidInfo.getAdSource() =" + mNativeAD.getBidInfo().getAdSource());
            LogUtil.d(TAG + "getBidInfo.getRawJsonStr() =" + mNativeAD.getBidInfo().getRawJsonStr());
            LogUtil.d(TAG + "getBidInfo.getSubMaterials() =" + mNativeAD.getBidInfo().getSubMaterials());


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
            return mNativeAD.getBidInfo().getCreativeItem().getTitle();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            return mNativeAD.getBidInfo().getCreativeItem().getDescription();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            return mNativeAD.getBidInfo().getCreativeItem().getImgSm();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {
        try {
            return mNativeAD.getBidInfo().getCreativeItem().getAdvName();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        return "";
    }

    @Override
    public List<String> getImgList() {
        List<String> result = new ArrayList<>();
        try {
            String imgUrl = mNativeAD.getBidInfo().getCreativeItem().getImageUrl();
            if (!BYStringUtil.isEmpty(imgUrl)) {
                result.add(imgUrl);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public boolean isDownloadAD() {
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            return mNativeAD.getAdType() == 4 || mNativeAD.getAdType() == 6;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mNativeAD != null) {
                return (int) mNativeAD.getBidInfo().getBidPrice();
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
