package com.advance.supplier.mry;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.mercury.sdk.core.config.AdPatternType;
import com.mercury.sdk.core.model.MercuryDownloadInfModel;
import com.mercury.sdk.core.nativ.NativeADData;

import java.util.ArrayList;
import java.util.List;

public class MercuryRenderDataConverter implements AdvanceRFADData {
    NativeADData mMryData;
    SdkSupplier mSdkSupplier;
    public static final String TAG = "[MercuryRenderDataConverter] ";

    public MercuryRenderDataConverter(NativeADData mryData, SdkSupplier sdkSupplier) {
        try {
            this.mMryData = mryData;
            this.mSdkSupplier = sdkSupplier;

            LogUtil.devDebug(TAG + "adData  inf  print start ");
            LogUtil.devDebug(TAG + getTitle());
            LogUtil.devDebug(TAG + getDesc());
            LogUtil.devDebug(TAG + getVideoImageUrl());
            LogUtil.devDebug(TAG + "isVideo = " + isVideo());
            LogUtil.devDebug(TAG + "isDownloadAD = " + isDownloadAD());
            LogUtil.devDebug(TAG + "getECPM = " + getECPM());
            LogUtil.devDebug(TAG + "adData  inf  print end");
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
            if (mMryData != null) {
                return mMryData.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        try {
            if (mMryData != null) {
                return mMryData.getDesc();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {
        try {
            if (mMryData != null) {
                return mMryData.getADSource();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mMryData != null) {
                return mMryData.getIconUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getVideoImageUrl() {
        if (mMryData != null) {
            return mMryData.getVideoImage();
        }
        return "";
    }


    @Override
    public List<String> getImgList() {
        List<String> result = new ArrayList<>();
        try {
            //添加素材url
            if (mMryData != null) {
                int patternType = mMryData.getAdPatternType();
                if (patternType == AdPatternType.NATIVE_1IMAGE_1ICON_2TEXT || patternType == AdPatternType.NATIVE_1IMAGE_2TEXT) {
                    result.add(mMryData.getImgUrl());
                } else if (patternType == AdPatternType.NATIVE_3IMAGE_1ICON_2TEXT || patternType == AdPatternType.NATIVE_3IMAGE_2TEXT) {
                    List<String> cList = mMryData.getImgList();
                    if (cList.size() > 0) {
                        for (String imageUrl : cList) {
                            if (!BYStringUtil.isEmpty(imageUrl)) {
                                result.add(imageUrl);
                            }
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
//            if (BYUtil.isDev()) {
//                return true;
//            }
            if (mMryData != null) {
                return mMryData.isAppAd();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mMryData != null) {
                int mode = mMryData.getAdPatternType();
                return mode == AdPatternType.NATIVE_VIDEO_2TEXT || mode == AdPatternType.NATIVE_1VIDEO_1ICON_2TEXT;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mMryData != null) {
                return mMryData.getECPM();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
//        if (BYUtil.isDev()){
//            return new MercuryDownloadElement();
//        }
        if (mMryData == null || mMryData.getDownloadAppInf() == null) {
            return null;
        }
        return new MercuryDownloadElement();
    }

    //下载六要素
    public class MercuryDownloadElement implements AdvanceRFDownloadElement {

        @Override
        public String getAppName() {
            try {
                return mMryData.getDownloadAppInf().name;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                return mMryData.getDownloadAppInf().appver;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                return mMryData.getDownloadAppInf().developer;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                return mMryData.getDownloadAppInf().privacy_url;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
            try {
                return mMryData.getDownloadAppInf().permission_url;
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
            try {
                return mMryData.getDownloadAppInf().desc_url;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            try {
                return mMryData.getDownloadAppInf().desc;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> pListResult) {
            try {
                ArrayList<AdvDownloadPermissionModel> result = new ArrayList<>();
                try {
                    ArrayList<MercuryDownloadInfModel.DownloadPermissionModel> permissionList = mMryData.getDownloadAppInf().permissionList;
                    if (permissionList.size() > 0) {
                        for (MercuryDownloadInfModel.DownloadPermissionModel model : permissionList) {
                            if (model != null) {
                                LogUtil.devDebug(TAG + "getPermissionList : title = " + model.title + ", desc = " + model.desc);
                                AdvDownloadPermissionModel downloadPermissionModel = new AdvDownloadPermissionModel();
                                downloadPermissionModel.permTitle = model.title;
                                downloadPermissionModel.permDesc = model.desc;
                                result.add(downloadPermissionModel);
                            }
                        }
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
//            返回大小信息
            try {
                return mMryData.getDownloadAppInf().size;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return 0;
        }


    }


}
