package com.advance.supplier.oppo;

import android.content.Context;
import android.view.View;

import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYLog;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.heytap.msp.mobad.api.params.INativeAdFile;
import com.heytap.msp.mobad.api.params.INativeAdvanceComplianceInfo;
import com.heytap.msp.mobad.api.params.INativeAdvanceData;
import com.heytap.msp.mobad.api.params.INativeComplianceListener;

import java.util.ArrayList;
import java.util.List;

public class OppoRenderDataConverter implements AdvanceRFADDataOppo {
    INativeAdvanceData mRenderAD;
    SdkSupplier mSdkSupplier;
    Context mContext;
    public static final String TAG = "[OppoRenderDataConverter] ";

    public OppoRenderDataConverter(Context context, INativeAdvanceData mRenderAD, SdkSupplier sdkSupplier) {
        this.mContext = context;
        this.mRenderAD = mRenderAD;
        this.mSdkSupplier = sdkSupplier;

        LogUtil.devDebug(TAG + "mData  inf  print start ");
        LogUtil.devDebug(TAG + getTitle());
        LogUtil.devDebug(TAG + getDesc());
        LogUtil.devDebug(TAG + getVideoImageUrl());
        LogUtil.devDebug(TAG + "isVideo = " + isVideo());
        LogUtil.devDebug(TAG + "getECPM = " + getECPM());
        LogUtil.devDebug(TAG + "mData  inf  print end");

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
            if (mRenderAD != null) {
                return mRenderAD.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {

        try {
            if (mRenderAD != null) {
                return mRenderAD.getDesc();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mRenderAD != null) {
                return mRenderAD.getIconFiles().get(0).getUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {

        return "广告";
    }

    @Override
    public String getVideoImageUrl() {
        return "";
    }

    @Override
    public List<String> getImgList() {
        List<String> result = new ArrayList<>();

        try {
//        获取广告图片,注意：除512x512规格，其他图片类通过此接口获取
            List<INativeAdFile> images = mRenderAD.getImgFiles();
//        获取推广应用的Icon图标,注意：512x512规格通过此接口获取
            List<INativeAdFile> icons = mRenderAD.getIconFiles();

//                获取创意类型，取值说明： 0：无 3：512x512 6：640x320 7：320x210 8：组图320x210 13：视频  15:竖版图片   16:竖版视频
            int type = mRenderAD.getCreativeType();

            if (type == 3) {
                for (int i = 0; i < icons.size(); i++) {
                    INativeAdFile file = icons.get(i);
                    String url = file.getUrl();
                    if (BYStringUtil.isNotEmpty(url)) {
                        result.add(url);
                    }
                }
            } else if (!isVideo()) {
                for (int i = 0; i < images.size(); i++) {
                    INativeAdFile file = images.get(i);
                    String url = file.getUrl();
                    if (BYStringUtil.isNotEmpty(url)) {
                        result.add(url);
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
//        try {
//            if (mRenderAD != null) {
//                return mRenderAD.;
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mRenderAD != null) {
//                获取创意类型，取值说明： 0：无 3：512x512 6：640x320 7：320x210 8：组图320x210 13：视频  15:竖版图片   16:竖版视频
                int type = mRenderAD.getCreativeType();
                return (type == 13 || type == 16);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mRenderAD != null) {
                return mRenderAD.getECPM();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        try {
            return new OppoDownloadElement();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void bindToComplianceView(List<View> privacyClickViews, INativeComplianceListener privacyListener, List<View> permissionClickViews, INativeComplianceListener permissionListener, List<View> appDescClickViews, INativeComplianceListener appDescListener) {
        try {
            if (null == mRenderAD) {
                return;
            }
            if (!mRenderAD.canIUse(INativeAdvanceData.KEY_NATIVE_PRIVACY_COMPONENT)) {
                BYLog.d(TAG + "INativeAdvanceData not allow to use privacy component");
                return;
            }
            mRenderAD.bindToComplianceView(mContext, privacyClickViews, privacyListener, permissionClickViews, permissionListener, appDescClickViews, appDescListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    //下载六要素
    public class OppoDownloadElement implements AdvanceRFDownloadElement {
        INativeAdvanceComplianceInfo oppoComplianceInfo;

        OppoDownloadElement() {
            oppoComplianceInfo = mRenderAD.getComplianceInfo();
        }

        @Override
        public String getAppName() {
            if (oppoComplianceInfo != null) {
                return oppoComplianceInfo.getAppName();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            if (oppoComplianceInfo != null) {
                return oppoComplianceInfo.getAppVersion();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            if (oppoComplianceInfo != null) {
                return oppoComplianceInfo.getDeveloperName();
            }
            return "";
        }

        //        oppo 模板信息流2.0中 没有直接获取url的方式，要通过
        @Override
        public String getPrivacyUrl() {
            return "";
        }

        @Override
        public String getPermissionUrl() {
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> callBack) {

        }

        @Override
        public String getFunctionDescUrl() {
            return "";
        }

        @Override
        public String getFunctionDescText() {
            return "";
        }

        @Override
        public long getPkgSize() {
            return 0;
        }
    }
}


