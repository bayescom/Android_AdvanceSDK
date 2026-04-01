package com.advance.supplier.gdt;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.qq.e.comm.managers.setting.GlobalSetting;
import com.qq.e.comm.managers.status.SDKStatus;

import java.util.HashMap;
import java.util.Map;

public class GdtGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController controller) {
        try {
            if (controller != null) {
                GlobalSetting.setAgreeReadAndroidId(controller.isCanUsePhoneState());
                GlobalSetting.setAgreeReadDeviceId(controller.isCanUsePhoneState());

                Map<String, Boolean> params = new HashMap<>();
                params.put("mipaddr", controller.isCanUseWifiState()); //false为关闭移动网络状态下获取IP地址，不设置或者设置为true为获取
                params.put("wipaddr", controller.isCanUseWifiState()); //false为关闭WIFI状态下获取IP地址，不设置或者设置为true为获取
                params.put("android_id", controller.isCanUsePhoneState());//false为关闭android_id获取，不设置或者设置为true为获取
                GlobalSetting.setAgreeReadPrivacyInfo(params);

//                GlobalSetting.setExtraUserData(controller.isCanUsePhoneState());
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSDKVersion() {
        String gdtV = "";
        try {
            gdtV = SDKStatus.getSDKVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return gdtV;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {
        personalRecommendChangeYLH(allow);
    }


    public static void personalRecommendChangeYLH(boolean allow) {
        try {
            int state;
            if (allow) {
                state = 0;
            } else {
                state = 1;
            }
            GlobalSetting.setPersonalizedState(state);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //    不支持设置
    @Override
    public void disableShake(boolean disableShake) {
        try {
            String status = "1";
            if (disableShake) {
                status = "0";
            }
            Map<String, String> extraUserData = new HashMap<>();
            extraUserData.put("shakable", status); // 屏蔽开屏摇一摇广告
            GlobalSetting.setExtraUserData(extraUserData);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}

