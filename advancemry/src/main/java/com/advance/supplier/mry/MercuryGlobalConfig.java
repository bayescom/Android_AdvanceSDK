package com.advance.supplier.mry;

import android.location.Location;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.mercury.sdk.core.config.AdConfigManager;
import com.mercury.sdk.core.config.MercuryAD;
import com.mercury.sdk.core.config.MercuryPrivacyController;

public class MercuryGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

        initMercuryPrivacy(advancePrivacyController);
    }

    @Override
    public String getSDKVersion() {
        String merV = "";
        try {
            merV = MercuryAD.getVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return merV;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {
        try {
            MercuryAD.enableTrackAD(allow);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static void initMercuryPrivacy(final AdvancePrivacyController controller) {
        try {
            if (controller != null) {
                MercuryAD.setMercuryPrivacyCustomController(new MercuryPrivacyController() {
                    @Override
                    public boolean isCanUseLocation() {
                        return controller.isCanUseLocation();
                    }

                    @Override
                    public Location getMLocation() {
                        return controller.getLocation();
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        return controller.isCanUsePhoneState();
                    }

                    @Override
                    public String getDevImei() {
                        return controller.getDevImei();
                    }

                    @Override
                    public String getDevAndroidID() {
                        return controller.getDevAndroidID();
                    }

                    @Override
                    public String getDevMac() {
                        return controller.getDevMac();
                    }

                    @Override
                    public boolean isCanUseWifiState() {
                        return controller.isCanUseWifiState() || controller.canUseMacAddress();
                    }

                    @Override
                    public String getDevOaid() {
                        return controller.getDevOaid();
                    }

                    @Override
                    public String getDevGaid() {
                        return controller.getDevGaid();
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
