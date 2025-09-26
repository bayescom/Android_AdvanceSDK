package com.advance.supplier.mi;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;

public class XMGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {

        return "";
    }

    @Override
    public void setPersonalRecommend(boolean allow) {

    }
}
