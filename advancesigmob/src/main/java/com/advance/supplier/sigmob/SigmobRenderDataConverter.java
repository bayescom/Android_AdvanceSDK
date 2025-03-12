package com.advance.supplier.sigmob;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.sigmob.sdk.base.models.SigImage;
import com.sigmob.windad.natives.WindNativeAdData;

import java.util.ArrayList;
import java.util.List;

public class SigmobRenderDataConverter implements AdvanceRFADData {
    WindNativeAdData mRenderAD;
    SdkSupplier sdkSupplier;
    public static final String TAG = "[SigmobRenderDataConverter] ";

    public SigmobRenderDataConverter(WindNativeAdData mRenderAD, SdkSupplier sdkSupplier) {
        try {
            this.mRenderAD = mRenderAD;
            this.sdkSupplier = sdkSupplier;


            LogUtil.devDebug(TAG + "adData  inf  print start ");
            LogUtil.devDebug(TAG + getTitle());
            LogUtil.devDebug(TAG + getDesc());
            LogUtil.devDebug(TAG + getVideoImageUrl());
            LogUtil.devDebug(TAG + "isVideo = " + isVideo());
            LogUtil.devDebug(TAG + "isDownloadAD = " + isDownloadAD());
            LogUtil.devDebug(TAG + "getECPM = " + getECPM());
            LogUtil.devDebug(TAG + "adData  inf  print end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AdvanceSdkSupplier getSdkSupplier() {
        AdvanceSdkSupplier advanceSdkSupplier = new AdvanceSdkSupplier();
        try {
            if (sdkSupplier != null) {
                advanceSdkSupplier.adnId = sdkSupplier.id;
                advanceSdkSupplier.adspotId = sdkSupplier.adspotid;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return advanceSdkSupplier;
    }

    @Override
    public String getTitle() {
        if (mRenderAD != null) {
            return mRenderAD.getTitle();
        }
        return "";
    }

    @Override
    public String getDesc() {
        if (mRenderAD != null) {
            return mRenderAD.getDesc();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        if (mRenderAD != null) {
            return mRenderAD.getIconUrl();
        }
        return "";
    }

    @Override
    public String getSourceText() {
//        if (mRenderAD!=null){
//            return  mRenderAD.g
//        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        if (mRenderAD != null) {
            return mRenderAD.getVideoCoverImageUrl();
        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        List<String> result = new ArrayList<>();
        if (mRenderAD != null) {
            List<SigImage> images = mRenderAD.getImageList();
            if (images != null && images.size() > 0) {
                for (SigImage img : images) {
                    result.add(img.getImageUrl());
                }
            }
        }
        return result;
    }

    @Override
    public boolean isDownloadAD() {
        if (mRenderAD != null) {
//            获取广告交互类型（0: 未知，1: 下载，2: 浏览器，小程序，快应用等）
            return mRenderAD.getInteractionType() == 1;
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            //根据图片素材来判断是否为视频类广告
            List<SigImage> images = mRenderAD.getImageList();
            return images == null || images.size() <= 0;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mRenderAD != null) {
                return (int) SigmobUtil.getEcpmNumber(mRenderAD.getEcpm());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        return null;
    }
}
