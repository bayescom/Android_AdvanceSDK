package com.advance.supplier.sigmob;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.sigmob.windad.WindAds;

public class SigmobGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {
        String v = "";
        try {
            v = WindAds.getVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {
        /*
         * 是否开启个性化推荐接口
         * true 开启, false 关闭,默认值为true
         */
        WindAds.sharedAds().setPersonalizedAdvertisingOn(allow);
    }
}
