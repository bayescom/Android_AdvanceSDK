package com.advance.supplier.csj;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bykv.vk.openvk.TTImage;
import com.bykv.vk.openvk.TTVfConstant;
import com.bykv.vk.openvk.TTVfObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//数据转换器，用来将穿山甲自渲染信息转换成通用data回调给开发者
public class CsjRenderDataConverter implements AdvanceRFADData {
    TTVfObject mCsjData;
    SdkSupplier mSdkSupplier;
    public static final String TAG = "[CsjRenderDataConverter] ";

    public CsjRenderDataConverter(TTVfObject csjData, SdkSupplier sdkSupplier) {
        try {
            this.mCsjData = csjData;
            this.mSdkSupplier = sdkSupplier;

            LogUtil.devDebug(TAG + "mCsjData  inf  print start ");
            LogUtil.devDebug(TAG + getTitle());
            LogUtil.devDebug(TAG + getDesc());
            LogUtil.devDebug(TAG + csjData.getImageList().size());
            LogUtil.devDebug(TAG + csjData.getSource());
            LogUtil.devDebug(TAG + getVideoImageUrl());
            LogUtil.devDebug(TAG + "isVideo = " + isVideo());
            LogUtil.devDebug(TAG + "isDownloadAD = " + isDownloadAD());
            LogUtil.devDebug(TAG + "getECPM = " + getECPM());
            LogUtil.devDebug(TAG + "mCsjData  inf  print end");
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
            if (mCsjData != null) {
                return mCsjData.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            if (mCsjData != null) {
                return mCsjData.getDescription();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {
        try {
            if (mCsjData != null && !BYStringUtil.isEmpty(mCsjData.getSource())) {
                return mCsjData.getSource();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mCsjData != null && mCsjData.getIcon() != null && mCsjData.getIcon().isValid()) {
                return mCsjData.getIcon().getImageUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        try {
            if (mCsjData != null && mCsjData.getVideoCoverImage() != null) {
                return mCsjData.getVideoCoverImage().getImageUrl();
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
            //添加素材url
            if (mCsjData != null && mCsjData.getImageList() != null) {
                List<TTImage> cList = mCsjData.getImageList();
                if (cList.size() > 0) {
                    for (TTImage image : cList) {
                        if (image != null && !BYStringUtil.isEmpty(image.getImageUrl())) {
                            result.add(image.getImageUrl());
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
            int adType = mCsjData.getInteractionType();
            return adType == TTVfConstant.INTERACTION_TYPE_DOWNLOAD;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mCsjData != null) {
                int mode = mCsjData.getImageMode();
                return mode == TTVfConstant.IMAGE_MODE_VIDEO || mode == TTVfConstant.IMAGE_MODE_VIDEO_VERTICAL;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mCsjData != null) {
                return (int) CsjUtil.getEcpmValue("", mCsjData.getMediaExtraInfo());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        if (mCsjData == null || mCsjData.getComplianceInfo() == null) {
            return null;
        }
        return new CsjDownloadElement();
    }

    //下载六要素
    public class CsjDownloadElement implements AdvanceRFDownloadElement {

        @Override
        public String getAppName() {
            try {
                return mCsjData.getComplianceInfo().getAppName();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                return mCsjData.getComplianceInfo().getAppVersion();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                return mCsjData.getComplianceInfo().getDeveloperName();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                return mCsjData.getComplianceInfo().getPrivacyUrl();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
            try {
                return mCsjData.getComplianceInfo().getPermissionUrl();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

//
//        /**
//         * 获取产品功能url----v5.4.0.3版本新增
//         */
//        String getFunctionDescUrl();

        @Override
        public String getFunctionDescUrl() {
//            当前版本还未支持返回 todo 更新版本后需要加上该内容
//            try {
//                return mCsjData.getComplianceInfo().get();
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> pListResult) {

            try {
                ArrayList<AdvDownloadPermissionModel> result = new ArrayList<>();
                try {
                    Map<String, String> pMap = mCsjData.getComplianceInfo().getPermissionsMap();

                    for (Map.Entry<String, String> entry : pMap.entrySet()) {
                        LogUtil.devDebug(TAG + "getPermissionList : Key = " + entry.getKey() + ", Value = " + entry.getValue());
                        AdvDownloadPermissionModel permissionModel = new AdvDownloadPermissionModel();
                        permissionModel.permTitle = entry.getKey();
                        permissionModel.permDesc = entry.getValue();
                        result.add(permissionModel);
                    }

                } catch (Throwable e) {
                    e.printStackTrace();
                }
                if (pListResult != null) {
                    pListResult.invoke(result);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

        }

        @Override
        public long getPkgSize() {
//            不支持返回大小信息
            return 0;
        }


    }


}
