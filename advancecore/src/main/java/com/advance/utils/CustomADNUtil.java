package com.advance.utils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.model.AdvanceCustomADNModel;
import com.bayes.sdk.basic.util.BYCacheUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CustomADNUtil {

    public static ArrayList<AdvanceCustomADNModel> getCustomADNModels() {
        ArrayList<AdvanceCustomADNModel> customADNModels = new ArrayList<>();
        try {
            //优先取单例
            customADNModels = AdvanceConfig.getInstance().customADNList;
            if (customADNModels != null && !customADNModels.isEmpty()) {
                return customADNModels;
            }

            //尝试本地缓存中获取
            customADNModels = getCustomADNModelsFromCache();

            //不为空则进行单例存储
            if ( !customADNModels.isEmpty()) {
                AdvanceConfig.getInstance().customADNList = customADNModels;
            }
        } catch (Exception e) {
        }
        return customADNModels;
    }


    // TODO: 2026/6/2 尝试缓存中获取并解析
    private static ArrayList<AdvanceCustomADNModel> getCustomADNModelsFromCache() {
        ArrayList<AdvanceCustomADNModel> customADNModels = new ArrayList<>();
        try {
            String savedAdnSetting = BYCacheUtil.getCacheStringValue(AdvanceConstant.CACHED_CUSTOM_ADN_SETTING);
            try {
                JSONObject jsonObject = new JSONObject(savedAdnSetting);
                JSONArray jsonArray = jsonObject.optJSONArray("custom_adn");
                AdvanceCustomADNModel adn = new AdvanceCustomADNModel();
            } catch (JSONException e) {

            }
        } catch (Exception e) {
        }
        return customADNModels;


    }
}
