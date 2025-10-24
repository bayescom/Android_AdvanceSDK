package com.advance.supplier.vv;

import com.advance.BaseParallelAdapter;
import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.vivo.ad.nativead.NativeResponse;
import com.vivo.mobilead.unified.vnative.VNativeAd;

import java.util.Collections;
import java.util.List;

public class VivoRenderDataConverter implements AdvanceRFADData {
    boolean isPro;
    BaseParallelAdapter adapter;
    NativeResponse adData;
    VNativeAd adDataPro;

    public VivoRenderDataConverter(boolean isPro, BaseParallelAdapter adapter, NativeResponse adData, VNativeAd adDataPro) {
        this.adData = adData;
        this.isPro = isPro;
        this.adapter = adapter;
        this.adDataPro = adDataPro;
    }

    @Override
    public AdvanceSdkSupplier getSdkSupplier() {
        AdvanceSdkSupplier advanceSdkSupplier = new AdvanceSdkSupplier();
        try {
            if (adapter != null) {
                SdkSupplier mSdkSupplier = adapter.sdkSupplier;
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
            if (isPro) {
                return adDataPro.getTitle();
            } else {
                return adData.getTitle();
            }
        } catch (Throwable e) {

        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            if (isPro) {
                return adDataPro.getDesc();
            } else {
                return adData.getDesc();
            }
        } catch (Throwable e) {

        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (isPro) {
                return adDataPro.getIconUrl();
            } else {
                return adData.getIconUrl();
            }
        } catch (Throwable e) {

        }
        return "";
    }

    @Override
    public String getSourceText() {
        try {
            if (isPro) {
                return adDataPro.getAdMarkText();
            } else {
                return adData.getAdMarkText();
            }
        } catch (Throwable e) {

        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
//        try {
//            if (isPro) {
//                return adDataPro.getad();
//            } else {
//                adData.();
//            }
//        } catch (Throwable e) {
//
//        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        try {
            if (isPro) {
                return adDataPro.getImgUrl();
            } else {
                return adData.getImgUrl();
            }
        } catch (Throwable e) {

        }
        return Collections.emptyList();
    }

    @Override
    public boolean isDownloadAD() {
        try {
            if (isPro) {
                return adDataPro.getAdType() == 2;
            } else {
                return adData.getAdType() == 2;
            }
        } catch (Throwable e) {

        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (isPro) {
                return adDataPro.getMaterialMode() == VNativeAd.MODE_VIDEO || adDataPro.getMaterialMode() == VNativeAd.MODE_VIDEO_VERTICAL;
            } else {
                return adData.getMaterialMode() == VNativeAd.MODE_VIDEO || adData.getMaterialMode() == VNativeAd.MODE_VIDEO_VERTICAL;
            }
        } catch (Throwable e) {

        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (isPro) {
                return (int) VivoUtil.getPrice(adDataPro);
            } else {
                return (int) VivoUtil.getPrice(adData);
            }
        } catch (Throwable e) {

        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        return null;
    }
}
