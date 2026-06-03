package com.advance.supplier.gdt;

import android.content.Context;

import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.comm.managers.GDTAdSdk;
import com.qq.e.comm.managers.setting.GlobalSetting;
import com.qq.e.comm.managers.status.SDKStatus;

import java.util.HashMap;
import java.util.Map;

public class GdtGlobalConfig extends AdvanceCustomInit {


    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        AdvancePrivacyController controller = getPrivacyController();

        if (controller != null) {
            GlobalSetting.setAgreeReadAndroidId(controller.isCanUsePhoneState());
            GlobalSetting.setAgreeReadDeviceId(controller.isCanUsePhoneState());

            Map<String, Boolean> params = new HashMap<>();
            params.put("mipaddr", controller.isCanUseWifiState()); //false为关闭移动网络状态下获取IP地址，不设置或者设置为true为获取
            params.put("wipaddr", controller.isCanUseWifiState()); //false为关闭WIFI状态下获取IP地址，不设置或者设置为true为获取
            params.put("android_id", controller.isCanUsePhoneState());//false为关闭android_id获取，不设置或者设置为true为获取
            GlobalSetting.setAgreeReadPrivacyInfo(params);

            GlobalSetting.setEnableCollectAppInstallStatus(controller.alist());


//                GlobalSetting.setExtraUserData(controller.isCanUsePhoneState());
        }

        //使用新初始化方法
        GDTAdSdk.initWithoutStart(context, getAppID()); // 该接口不会采集用户信息
// 调用initWithoutStart后请尽快调用start，否则可能影响广告填充，造成收入下降
        GDTAdSdk.start(new GDTAdSdk.OnStartListener() {
            @Override
            public void onStartSuccess() {
                LogUtil.simple("[GdtUtil] onStartSuccess");


                callInitSuccess();
            }

            @Override
            public void onStartFailed(Exception e) {
                LogUtil.e("[GdtUtil]  onStartFailed:" + e.toString());

                callInitFail(AdvanceError.ERROR_INIT_DEFAULT + "", e.toString());
            }
        });
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
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        personalRecommendChangeYLH(isPersonalRecommend);
    }

    @Override
    public void switchDisableShake(boolean disableShake) {
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

}

