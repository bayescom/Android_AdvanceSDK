package com.advance.supplier.vv;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.vivo.mobad.BuildConfig;

public class VivoGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {
        return BuildConfig.VERSION_NAME ;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {

        AdvanceVivoManager.getInstance().allowPersonalRecommend = allow;
    }
}
