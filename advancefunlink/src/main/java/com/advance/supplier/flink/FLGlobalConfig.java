package com.advance.supplier.flink;

import android.content.Context;
import android.location.Location;

import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.fl.saas.adx.api.FLConfig;
import com.fl.saas.adx.api.FLParamConfig;
import com.fl.saas.adx.base.bean.CustomLocation;

import java.util.Map;

public class FLGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        FLParamConfig.Builder builder = new FLParamConfig.Builder();
        //传感器监听开关
        boolean disableShake = AdvanceSetting.getInstance().disableShake;
        builder.setCanUseSensor(!disableShake);

//            隐私配置
        final AdvancePrivacyController advancePrivacyController = getPrivacyController();
        if (advancePrivacyController != null) {
            FLConfig.getInstance().setEnableCollectAppInstallStatus(advancePrivacyController.alist());

            builder.setCanUseLocation(advancePrivacyController.isCanUseLocation());
            builder.setCanUseAndroid(advancePrivacyController.isCanUsePhoneState());
            builder.setCanUseIMEI(advancePrivacyController.isCanUsePhoneState());
            builder.setCanUseIMSI(advancePrivacyController.isCanUsePhoneState());
            builder.setCanUseMac(advancePrivacyController.canUseMacAddress());
            builder.setCanUseSSID(advancePrivacyController.canUseMacAddress());
            builder.setCanUseBootID(AdvanceFLManager.getInstance().canUseBootId);
            builder.setCanUseOaid(advancePrivacyController.canUseOaid());

            builder.setCustomIMEI(advancePrivacyController.getDevImei());
            builder.setCustomAndroidId(advancePrivacyController.getDevAndroidID());
            builder.setCustomOaid(advancePrivacyController.getDevOaid());
            builder.setCustomMac(advancePrivacyController.getDevMac());
            Location location = advancePrivacyController.getLocation();
            if (location != null) {
                builder.setCustomLocation(new CustomLocation(location.getLatitude(), location.getLongitude()));
            }
        }

        FLParamConfig config = builder.build();

        FLConfig.getInstance().init(context, getAppID(), config);

        callInitSuccess();
    }

    @Override
    public String getSDKVersion() {
        return FLConfig.getInstance().getSdkVersion();
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        FLConfig.getInstance().setPersonalizedState(isPersonalRecommend);
    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }

}
