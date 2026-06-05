package com.advance.utils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.custom.AdvanceCustomInit;
import com.advance.model.AdvanceCustomADNModel;
import com.advance.model.SdkSupplier;
import com.bayes.sdk.basic.util.BYCacheUtil;
import com.bayes.sdk.basic.util.BYStringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CustomADNUtil {

    public synchronized static AdvanceCustomInit getCustomInitClass(SdkSupplier supplier) {
        AdvanceCustomInit result = null;
        try {
            String tag = "【getCustomInitClass】";
            String initKey = getInitClassKey(supplier);
            //尝试从已存在的初始化类中获取
            result = AdvanceInitManger.getInstance().runningInitMap.get(initKey);

            if (result != null) {
                LogUtil.simple(tag + "使用已缓存初始化类");
                return result;
            }
            //如果不存在，尝试新建
            String sdkID = supplier.id;
            String clzName = AdvanceConfig.getInstance().availableInitClassMap.get(sdkID);
            //初始化
            result = AdvanceLoader.getCustomInit(clzName);
            AdvanceInitManger.getInstance().runningInitMap.put(initKey, result);
            LogUtil.simple(tag + "使用新建初始化类");
        } catch (Exception e) {
        }

        return result;
    }

    //获取初始化class得唯一性key，SDK唯一id+ 媒体id
    private static String getInitClassKey(SdkSupplier supplier) {
        if (supplier != null) {
            return supplier.id + supplier.mediaid;
        }
        return "";
    }

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
            if (!customADNModels.isEmpty()) {
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

    //将服务端返回的ext数据（string json）转换成map
    public static Map<String, Object> getServerCustomExtData(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (BYStringUtil.isNotEmpty(json)){
                JSONObject jsonObject = new JSONObject(json);
                result = toMap(jsonObject);
            }

        } catch (JSONException e) {
        }

        return  result;
    }

    public static Map<String, Object> toMap(JSONObject object)  {
        Map<String, Object> map = new HashMap<>();
        try {
            Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = object.get(key);
                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                map.put(key, value);
            }
        } catch (JSONException e) {

        }
        return map;
    }

    public static List<Object> toList(JSONArray array)  {
        List<Object> list = new ArrayList<>();
        try {
            for (int i = 0; i < array.length(); i++) {
                Object value = array.get(i);
                if (value instanceof JSONArray) {
                    value = toList((JSONArray) value);
                } else if (value instanceof JSONObject) {
                    value = toMap((JSONObject) value);
                }
                list.add(value);
            }
        } catch (JSONException e) {

        }
        return list;
    }
}
