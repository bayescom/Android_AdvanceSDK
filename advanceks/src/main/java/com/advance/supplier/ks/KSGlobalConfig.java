package com.advance.supplier.ks;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.kwad.sdk.api.KsAdSDK;

public class KSGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {
        String ksV = "";
        try {
            ksV = KsAdSDK.getSDKVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ksV;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {
        personalRecommendChangeKS(allow);
    }


    public static void personalRecommendChangeKS(boolean allow) {
        try {
            KsAdSDK.setPersonalRecommend(allow);
            KsAdSDK.setProgrammaticRecommend(allow);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}

