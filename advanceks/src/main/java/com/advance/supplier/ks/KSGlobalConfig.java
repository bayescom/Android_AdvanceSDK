package com.advance.supplier.ks;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.advance.AdvanceConfig;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsCustomController;
import com.kwad.sdk.api.KsInitCallback;
import com.kwad.sdk.api.SdkConfig;

import java.util.List;
import java.util.Map;

public class KSGlobalConfig extends AdvanceCustomInit {


    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        final String tag = "[KSGlobalConfig.initADN] ";

        KsCustomController customController = null;
        final AdvancePrivacyController advancePrivacyController = getPrivacyController();
        if (advancePrivacyController != null) {
            customController = new KsCustomController() {
                @Override
                public boolean canReadLocation() {
                    return advancePrivacyController.isCanUseLocation();
                }

                @Override
                public Location getLocation() {
                    if (advancePrivacyController.getLocation() != null) {
                        Location la = new Location("ADVCusLocation");
                        la.setLatitude(advancePrivacyController.getLocation().getLatitude());
                        la.setLongitude(advancePrivacyController.getLocation().getLongitude());
                        return la;
                    } else {
                        return super.getLocation();
                    }
                }

                @Override
                public boolean canUsePhoneState() {
                    return advancePrivacyController.isCanUsePhoneState();
                }

                @Override
                public String getImei() {
                    return advancePrivacyController.getDevImei();
                }

                @Override
                public String[] getImeis() {
                    return advancePrivacyController.getImeis();
                }

                @Override
                public String getAndroidId() {
                    return advancePrivacyController.getDevAndroidID();
                }

                @Override
                public boolean canUseOaid() {
                    return advancePrivacyController.canUseOaid();
                }

                @Override
                public String getOaid() {
                    return advancePrivacyController.getDevOaid();
                }

                @Override
                public boolean canUseMacAddress() {
                    return advancePrivacyController.canUseMacAddress();
                }

                @Override
                public String getMacAddress() {
                    return advancePrivacyController.getDevMac();
                }

                @Override
                public boolean canUseNetworkState() {
                    return advancePrivacyController.canUseNetworkState();
                }

                @Override
                public boolean canUseStoragePermission() {
                    return advancePrivacyController.isCanUseWriteExternal();
                }

                @Override
                public boolean canReadInstalledPackages() {
                    return advancePrivacyController.alist();
                }

                @Override
                public List<String> getInstalledPackages() {
                    return advancePrivacyController.getInstalledPackages();
                }
            };
        }
        SdkConfig.Builder builder = new SdkConfig.Builder();
        builder.appId(getAppID())// aapId，请联系快手平台申请正式AppId，必填
                .showNotification(true) // 是否展示下载通知栏
                .customController(customController)
                .debug(BYUtil.isDebug());
        String appName = AdvanceConfig.getInstance().getKsAppName();
        if (!TextUtils.isEmpty(appName)) {
            builder.appName(appName);// appName，请填写您应用的名称，非必填
        }

        String appKey = AdvanceConfig.getInstance().getKsAppKey();
        if (!TextUtils.isEmpty(appKey)) {
            builder.appKey(appKey);// 直播sdk安全验证，接入直播模块必填
        }
        String appWebKey = AdvanceConfig.getInstance().getKsAppWebKey();
        if (!TextUtils.isEmpty(appWebKey)) {
            builder.appWebKey(appWebKey);// 直播sdk安全验证，接入直播模块必填
        }
        builder.setInitCallback(new KsInitCallback() {
            @Override
            public void onSuccess() {
                LogUtil.simple("[KSUtil.initAD] InitCallback onSuccess");

            }

            @Override
            public void onFail(int i, String s) {
                String eMsg = "[KSUtil.initAD] InitCallback failed ; onFail ： code = "+ i + ",msg = " + s;

                LogUtil.e(eMsg);

            }
        });
        builder.setStartCallback(new KsInitCallback() {
            @Override
            public void onSuccess() {
                LogUtil.simple("[KSUtil.initAD] StartCallback onSuccess");

                callInitSuccess();
            }

            @Override
            public void onFail(int i, String s) {
                String eMsg = "[KSUtil.initAD] StartCallback failed ; onFail： code = "+ i + ",msg = " + s;

                LogUtil.e(eMsg);
                callInitFail(i + "", eMsg);
            }
        });

        // 建议只在需要的进程初始化SDK即可，如主进程
        KsAdSDK.init(context, builder.build());
        KsAdSDK.start();
        LogUtil.high(tag + "init call end  ");
    }

    @Override
    public String getSDKVersion() {
        String ksV = "";
        try {
            ksV = KsAdSDK.getSDKVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ksV;
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        personalRecommendChangeKS(isPersonalRecommend);
    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }


    public static void personalRecommendChangeKS(boolean allow) {
        try {
            KsAdSDK.setPersonalRecommend(allow);
            KsAdSDK.setProgrammaticRecommend(allow);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}

