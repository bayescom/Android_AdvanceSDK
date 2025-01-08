package com.advance.supplier.baidu;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.baidu.mobads.sdk.api.AdSettings;
import com.baidu.mobads.sdk.api.MobadsPermissionSettings;

public class BDGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

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
    public void setPersonalRecommend(boolean allow) {
        try {
            MobadsPermissionSettings.setLimitPersonalAds(!allow);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
