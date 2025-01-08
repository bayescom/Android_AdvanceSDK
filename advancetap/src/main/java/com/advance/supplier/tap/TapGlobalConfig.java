package com.advance.supplier.tap;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;

public class TapGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {
        String v = "";
        try {
            v = com.tapsdk.tapad.BuildConfig.VERSION_NAME;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {

    }
}

