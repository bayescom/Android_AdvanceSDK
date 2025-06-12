package com.advance.supplier.csj;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.bykv.vk.openvk.TTVfConfig;
import com.bykv.vk.openvk.TTVfSdk;

import org.json.JSONArray;
import org.json.JSONObject;

public class CsjGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

    }

    @Override
    public String getSDKVersion() {
        String csjV = "";
        try {
            csjV = TTVfSdk.getVfManager().getSDKVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return csjV;
    }

    @Override
    public void setPersonalRecommend(boolean allow) {
        personalRecommendChangeCSJ(allow);
    }


    //个性化广告推荐配置
    private void personalRecommendChangeCSJ(boolean allow) {
        try {
            String personalTypeValue;
            if (allow) {
                personalTypeValue = "1";
            } else {
                personalTypeValue = "0";
            }

            TTVfConfig ttAdConfig = new TTVfConfig.Builder()
                    .data(getData(personalTypeValue))
                    .build();
            TTVfSdk.updateAdConfig(ttAdConfig);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private String getData(String personalTypeValue) {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject personalObject = new JSONObject();
            personalObject.putOpt("personal_ads_type", personalTypeValue);
            jsonArray.put(personalObject);
            return jsonArray.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }
}
