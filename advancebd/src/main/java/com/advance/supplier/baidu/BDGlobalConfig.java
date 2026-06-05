package com.advance.supplier.baidu;

import android.content.Context;

import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.baidu.mobads.sdk.api.AdSettings;
import com.baidu.mobads.sdk.api.BDAdConfig;
import com.baidu.mobads.sdk.api.BDDialogParams;
import com.baidu.mobads.sdk.api.MobadsPermissionSettings;
import com.bayes.sdk.basic.util.BYUtil;

import java.util.Map;

public class BDGlobalConfig extends AdvanceCustomInit {
    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
// 初始化信息，初始化一次即可，（此处用startsWith()，可包括激励/全屏视频的进程）
        // https、视频缓存空间有特殊需求可动态配置，一般取默认值即可，无需设置
        BDAdConfig bdAdConfig = new BDAdConfig.Builder()
                // 1、设置app名称，可选
//                        .setAppName("网盟demo")
                // 2、应用在mssp平台申请到的appsid，和包名一一对应，此处设置等同于在AndroidManifest.xml里面设置
                .setAppsid(getAppID())
                // 3、设置下载弹窗的类型和按钮动效样式，可选
                .setDialogParams(new BDDialogParams.Builder()
                        .setDlDialogType(BDDialogParams.TYPE_BOTTOM_POPUP)
                        .setDlDialogAnimStyle(BDDialogParams.ANIM_STYLE_NONE)
                        .build())
//                    .setHttps(AdvanceBDManager.getInstance().bDSupportHttps)//如果设置为true，那么banner广告将会无法展示，
                .setDebug(BYUtil.isDebug())
                .build(context);
        bdAdConfig.init();
        // 设置SDK可以使用的权限，包含：设备信息、定位、存储、APP LIST
        // 注意：建议授权SDK读取设备信息，SDK会在应用获得系统权限后自行获取IMEI等设备信息
        // 授权SDK获取设备信息会有助于提升ECPM
        final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
        boolean deviceOn = false;
        boolean locationOn = false;
        boolean storageOn = false;
        boolean appListOn = false;
        if (advancePrivacyController != null) {
            deviceOn = advancePrivacyController.isCanUsePhoneState();
            locationOn = advancePrivacyController.isCanUseLocation();
            storageOn = advancePrivacyController.isCanUseWriteExternal();
            appListOn = advancePrivacyController.alist();
        }
        MobadsPermissionSettings.setPermissionReadDeviceID(deviceOn);
        MobadsPermissionSettings.setPermissionLocation(locationOn);
        MobadsPermissionSettings.setPermissionStorage(storageOn);
        MobadsPermissionSettings.setPermissionAppList(appListOn);
        // 初始化成功
        callInitSuccess();
    }

    @Override
    public String getSDKVersion() {
        try {
            return AdSettings.getSDKVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        try {
            MobadsPermissionSettings.setLimitPersonalAds(!isPersonalRecommend);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }

}
