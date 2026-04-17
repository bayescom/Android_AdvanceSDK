package com.advance.supplier.flink;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.fl.saas.adx.api.FLConfig;

public class FLGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {
        if (advancePrivacyController != null){
            FLConfig.getInstance().setEnableCollectAppInstallStatus(advancePrivacyController.alist());
        }
    }

    @Override
    public String getSDKVersion() {
        return FLConfig.getInstance().getSdkVersion();
    }

    @Override
    public void setPersonalRecommend(boolean allow) {
        FLConfig.getInstance().setPersonalizedState(allow);
    }

    @Override
    public void disableShake(boolean disableShake) {
    }
}
