package com.advance.supplier.noah;

import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.noah.api.DownloadApkInfo;
import com.noah.api.NativeAd;
import com.noah.common.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NoahRenderFeedData implements AdvanceRFADData {
    NativeAd ad;
    NativeAd.NativeAssets material;
    DownloadApkInfo downloadApkInfo;

    public NoahRenderFeedData(NativeAd ad) {
        try {
            this.ad = ad;
            material = ad.getAdAssets();
            downloadApkInfo = ad.getDownloadApkInfo();
        } catch (Exception e) {

        }
    }

    @Override
    public String getTitle() {
        if (material != null) {
            return material.getTitle();
        }
        return "";
    }

    @Override
    public String getDesc() {
        if (material != null) {
            return material.getDescription();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        if (material != null && material.getIcon() != null) {
            return material.getIcon().getUrl();
        }
        return "";
    }

    @Override
    public String getSourceText() {
        if (material != null) {
            return material.getSource();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
//        if (material != null) {
//            return material.getCover();
//        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        if (material != null) {
            List<String> result = new ArrayList<>();
            if (material.getCover() != null) {
                String url = material.getCover().getUrl();
                if (BYStringUtil.isNotEmpty(url)) {
                    result.add(url);
                }
            }
            if (material.getCovers() != null && !material.getCovers().isEmpty()) {
                for (Image img : material.getCovers()) {
                    String url = img.getUrl();
                    if (BYStringUtil.isNotEmpty(url)) {
                        result.add(url);
                    }
                }
            }
            return result;
        } else {

        }
        return Collections.emptyList();
    }

    @Override
    public boolean isDownloadAD() {
        if (material != null) {
            return material.isAppAd();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        if (material != null) {
            return material.isVideo();
        }
        return false;
    }

    @Override
    public int getECPM() {
        if (material != null) {
            return (int) material.getPrice();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        return new NoahAppData();
    }

    public class NoahAppData implements AdvanceRFDownloadElement {

        @Override
        public String getAppName() {
            if (downloadApkInfo != null) {
                return downloadApkInfo.appName;
            }
            return "";
        }

        @Override
        public String getAppVersion() {

            if (downloadApkInfo != null) {
                return downloadApkInfo.versionName;
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {

            if (downloadApkInfo != null) {
                return downloadApkInfo.authorName;
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {

            if (downloadApkInfo != null) {
                return downloadApkInfo.privacyAgreementUrl;
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {

            if (downloadApkInfo != null) {
                return downloadApkInfo.permissionUrl;
            }
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> pListResult) {
            ArrayList<AdvDownloadPermissionModel> result = new ArrayList<>();

            if (downloadApkInfo != null && downloadApkInfo.permissions != null) {
                int pList = downloadApkInfo.permissions.size();
                for (int i = 0; i < pList; i++) {
                    try {
                        AdvDownloadPermissionModel downloadPermissionModel = new AdvDownloadPermissionModel();
                        String ps = downloadApkInfo.permissions.get(i);
                        if (BYStringUtil.isNotEmpty(ps)) {
                            downloadPermissionModel.permTitle = ps;
                            if (downloadApkInfo.permissionDescriptions != null && !downloadApkInfo.permissionDescriptions.isEmpty() && downloadApkInfo.permissionDescriptions.size() < (i + 1)) {
                                downloadPermissionModel.permDesc = downloadApkInfo.permissionDescriptions.get(0);
                            }
                        }
                        result.add(downloadPermissionModel);
                    } catch (Exception e) {
                    }
                }
            }
            if (pListResult != null) {
                pListResult.invoke(result);
            }
        }

        @Override
        public String getFunctionDescUrl() {

            if (downloadApkInfo != null) {
                return downloadApkInfo.functionDescUrl;
            }
            return "";
        }

        @Override
        public String getFunctionDescText() {

//            if (downloadApkInfo != null) {
//                return downloadApkInfo.;
//            }
            return "";
        }

        @Override
        public long getPkgSize() {

            if (downloadApkInfo != null) {
                return downloadApkInfo.fileSize;
            }
            return 0;
        }
    }

}
