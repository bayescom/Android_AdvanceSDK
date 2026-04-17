package com.advance.supplier.flink;

import android.location.Location;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.LogUtil;
import com.fl.saas.adx.api.FLConfig;
import com.fl.saas.adx.api.FLParamConfig;
import com.fl.saas.adx.base.bean.CustomLocation;
import com.fl.saas.adx.base.exception.FLError;

public class FLUtil {
    public static void initAD(BaseParallelAdapter adapter) {
        try {
            final String tag = "[FLUtil.initAD] ";
            String eMsg;
            if (adapter == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";
                LogUtil.e(eMsg);
                //            if (initResult != null) {
                //                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
                //            }
                return;
            }
            boolean hasInit = AdvanceFLManager.getInstance().hasInit;
            if (hasInit) {
                LogUtil.simple(tag + " already init");
                //            if (initResult != null) {
                //                initResult.success();
                //            }
                return;
            }
            FLParamConfig.Builder builder = new FLParamConfig.Builder();
            //传感器监听开关
            boolean disableShake = AdvanceSetting.getInstance().disableShake;
            builder.setCanUseSensor(!disableShake);

//            隐私配置
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
            if (advancePrivacyController != null) {
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

            FLConfig.getInstance().init(adapter.getRealContext(), adapter.getAppID(), config);
        } catch (Exception e) {

        }
    }

    public static void handleErr(BaseParallelAdapter adapter, FLError error) {
        try {
            if (adapter != null && error != null) {
                adapter.handleFailed(error.getCode(), error.getMsg());
            }
        } catch (Exception e) {

        }
    }
}
