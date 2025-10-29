package com.advance.supplier.vv;

import com.advance.BaseParallelAdapter;
import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.vivo.ad.model.AppElement;
import com.vivo.ad.model.Permission;
import com.vivo.ad.nativead.NativeResponse;
import com.vivo.mobilead.unified.vnative.VNativeAd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VivoRenderDataConverter implements AdvanceRFADData {
    public boolean isPro;
    public BaseParallelAdapter adapter;
    public NativeResponse adData;
    public VNativeAd adDataPro;

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
        try {
            AppElement appElement;
            if (isPro) {
                appElement = adDataPro.getAppMiitInfo();
            } else {
                appElement = adData.getAppMiitInfo();
            }
            if (appElement != null) {
                return new VivoDownloadElement(appElement);

            }
        } catch (Exception e) {

        }
        return null;
    }


    public class VivoDownloadElement implements AdvanceRFDownloadElement {
        AppElement appElement;

        public VivoDownloadElement(AppElement appElement) {
            this.appElement = appElement;
        }

        @Override
        public String getAppName() {
            try {
                return appElement.getName();
            } catch (Throwable e) {

            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                return appElement.getVersionName();
            } catch (Throwable e) {

            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                return appElement.getDeveloper();
            } catch (Throwable e) {

            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                return appElement.getPrivacyPolicyUrl();
            } catch (Throwable e) {

            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
            try {
                return appElement.getPermissionUrl();
            } catch (Throwable e) {

            }
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> callBack) {
            ArrayList<AdvDownloadPermissionModel> result = new ArrayList<>();

            try {
                List<Permission> permissionList = appElement.getPermissionList();
                if (permissionList != null && !permissionList.isEmpty()) {
                    for (Permission permission : permissionList) {
                        if (permission != null) {
                            AdvDownloadPermissionModel permissionModel = new AdvDownloadPermissionModel();
                            permissionModel.permTitle = permission.getTitle();
                            permissionModel.permDesc = permission.getDescribe();
                            result.add(permissionModel);
                        }

                    }
                }
            } catch (Throwable e) {
            }
            if (callBack != null) {
                callBack.invoke(result);
            }
        }

        @Override
        public String getFunctionDescUrl() {
            try {
                return appElement.getDescriptionUrl();
            } catch (Throwable e) {

            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            try {
                return appElement.getDescription();
            } catch (Throwable e) {

            }
            return "";
        }

        @Override
        public long getPkgSize() {
            try {
                return appElement.getSize();
            } catch (Throwable e) {

            }
            return 0;
        }
    }
}
