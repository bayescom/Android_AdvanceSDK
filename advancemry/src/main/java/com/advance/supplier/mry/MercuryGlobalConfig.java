package com.advance.supplier.mry;

import android.content.Context;
import android.location.Location;

import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.bayes.sdk.basic.core.BYConstants;
import com.mercury.sdk.core.config.AdConfigManager;
import com.mercury.sdk.core.config.MercuryAD;
import com.mercury.sdk.core.config.MercuryPrivacyController;

import java.util.Map;

public class MercuryGlobalConfig extends AdvanceCustomInit {


    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        initMercuryPrivacy(getPrivacyController());

        AdConfigManager.getInstance().setMediaId(getAppID());
        AdConfigManager.getInstance().setMediaKey(getAppKey());

        callInitSuccess();
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
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        try {
            MercuryAD.enableTrackAD(isPersonalRecommend);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void switchDisableShake(boolean disableShake) {
        try {
            MercuryAD.disableShake(disableShake);
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
                        if (!controller.canUseMacAddress()){
                            return false;
                        }
                        if (!controller.isCanUseWifiState()){
                            return false;
                        }
                        return true;
//                        return controller.isCanUseWifiState() || controller.canUseMacAddress();
                    }

                    @Override
                    public String getDevOaid() {
                        //不允许获取oaid时，传入约定枚举值
                        if (!controller.canUseOaid()){
                            return BYConstants.OAID_VALUE_TYPE_DENY;
                        }
                        return controller.getDevOaid();
                    }

                    @Override
                    public String getDevGaid() {
                        return controller.getDevGaid();
                    }

                    @Override
                    public boolean alist() {
                        return controller.alist();
                    }

                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



}
