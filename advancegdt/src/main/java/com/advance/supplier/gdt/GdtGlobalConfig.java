package com.advance.supplier.gdt;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.qq.e.comm.managers.setting.GlobalSetting;
import com.qq.e.comm.managers.status.SDKStatus;

public class GdtGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController controller) {
        try {
            if (controller != null) {
                GlobalSetting.setAgreeReadAndroidId(controller.isCanUsePhoneState());
                GlobalSetting.setAgreeReadDeviceId(controller.isCanUsePhoneState());
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
}

