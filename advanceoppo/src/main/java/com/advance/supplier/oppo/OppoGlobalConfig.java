package com.advance.supplier.oppo;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.heytap.msp.mobad.api.MobAdManager;

public class OppoGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {
    }

    @Override
    public String getSDKVersion() {
        String v = "";
        try {
            v = MobAdManager.getInstance().getSdkVerName();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {


    }


}
