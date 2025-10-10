package com.advance.supplier.honor;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.hihonor.adsdk.base.HnAds;

public class HonorGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {
        return HnAds.get().getAdManager().getSDKVersion();
    }

    @Override
    public void setPersonalRecommend(boolean allow) {

    }
}
