package com.advance.supplier.honor;

import com.advance.BaseParallelAdapter;
import com.advance.core.model.AdvanceSdkSupplier;
import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.model.SdkSupplier;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.hihonor.adsdk.base.api.feed.PictureTextExpressAd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HonorRenderDataConverter implements AdvanceRFADData {
    public static   PictureTextExpressAd mExpressAd;
    BaseParallelAdapter adapter;
    SdkSupplier mSdkSupplier;

    public static final String TAG = "[HonorRenderDataConverter] ";

    public HonorRenderDataConverter(PictureTextExpressAd ad, BaseParallelAdapter adapter) {
        this.adapter = adapter;

        mExpressAd = ad;
        if (adapter != null) {
            mSdkSupplier = adapter.sdkSupplier;
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
            if (mExpressAd != null) {
                return mExpressAd.getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getDesc() {
        return "";
    }

    @Override
    public String getIconUrl() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.getLogo();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String getSourceText() {

        return "";
    }

    @Override
    public String getVideoImageUrl() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.getCoverUrl();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public List<String> getImgList() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.getImages();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isDownloadAD() {
        try {
            if (mExpressAd != null) {
//                获取推广目标。 返回推广目标 0：应用推广（下载） 1：网页推广 2：应用直达 3:小程序推广 4:预约广告 103：快应用推广。
                return mExpressAd.getPromotionPurpose() == 0;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isVideo() {
        try {
            if (mExpressAd != null) {
                return mExpressAd.hasVideo();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getECPM() {
        try {
            if (mExpressAd != null) {
                return (int) HonorUtil.getECPM(mExpressAd);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdvanceRFDownloadElement getDownloadElement() {
        if (mExpressAd == null) {
            return null;
        }
        return new HonorDownloadElement();
    }

    private static class HonorDownloadElement implements AdvanceRFDownloadElement {

        @Override
        public String getAppName() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getAppName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppVersion() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getAppVersion();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getAppDeveloper() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getDeveloperName();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPrivacyUrl() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getPrivacyAgreementUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getPermissionUrl() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getPermissionsUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> callBack) {

        }

        @Override
        public String getFunctionDescUrl() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getIntroUrl();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public String getFunctionDescText() {
            try {
                if (mExpressAd != null) {
                    return mExpressAd.getAppIntro();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public long getPkgSize() {
            return 0;
        }
    }
}
