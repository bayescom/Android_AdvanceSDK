package com.advance.supplier.flink;

import static com.fl.saas.adx.api.mixNative.NativeAdConst.AD_TYPE_VIDEO;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.fl.saas.adx.api.mixNative.NativeAd;
import com.fl.saas.adx.api.mixNative.NativeMaterial;

import java.util.Collections;
import java.util.List;

public class FLRenderDataConverter implements AdvanceRFADData {
    NativeAd ad;
    NativeMaterial adMaterial;
    SdkSupplier sdkSupplier;

    public FLRenderDataConverter(NativeAd ad, SdkSupplier sdkSupplier) {
        try {
            this.ad = ad;
            this.sdkSupplier = sdkSupplier;
            adMaterial = adMaterial;
        } catch (Exception e) {

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
        if (adMaterial != null) {
            return adMaterial.getTitle();
        }
        return "";
    }

    @Override
    public String getDesc() {
        if (adMaterial != null) {
            return adMaterial.getDescription();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        if (adMaterial != null) {
            return adMaterial.getIconUrl();
        }
        return "";
    }

    @Override
    public String getSourceText() {
//        if (adMaterial != null) {
//            return adMaterial.get();
//        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
//        if (adMaterial != null) {
//            return adMaterial.();
//        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        if (adMaterial != null) {
            return adMaterial.getImageUrlList();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isDownloadAD() {
        if (adMaterial != null) {
            return adMaterial.isNativeAppAd();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        if (adMaterial != null) {
            return adMaterial.getAdType() == AD_TYPE_VIDEO;
        }
        return false;
    }

    @Override
    public int getECPM() {
        if (ad != null) {
            return ad.getECPM();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        return null;
    }
}
