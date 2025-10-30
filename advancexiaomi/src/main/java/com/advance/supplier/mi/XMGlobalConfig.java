package com.advance.supplier.mi;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.miui.zeus.mimo.sdk.BuildConfig;

public class XMGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {

        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {

    }
}
