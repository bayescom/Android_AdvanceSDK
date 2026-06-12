package com.advance.utils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.model.AdvanceCustomADNModel;
import com.advance.model.SdkSupplier;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.util.BYCache;
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

    //从服务端获取SDK自定义adn配置信息
    public synchronized static void initCustomInf(String appID) {
        try {
            BYReqModel reqModel = new BYReqModel();
            String reqOption = "?appid=" + appID; //如果不为空，添加请求版本号信息
            final String cachedADNVersion = BYCacheUtil.getCacheStringValue(AdvanceConstant.CACHED_CUSTOM_ADN_VERSION);
            if (BYStringUtil.isNotEmpty(cachedADNVersion)) {
                reqOption = reqOption + "&version=" + cachedADNVersion;
            }
            reqModel.reqUrl = getCustomSettingUrl() + reqOption;
            BYNetRequest.get(reqModel, new BYReqCallBack() {
                @Override
                public void onSuccess(String s) {
                    try {
                        JSONObject json = new JSONObject(s);

                        String version = json.optString("version");
                        if (BYStringUtil.isNotEmpty(version)) {
                            boolean isDiffVersion = BYStringUtil.isNotEqual(version, cachedADNVersion);
                            BYCache byCache = BYCacheUtil.byCache();
                            //版本不一致，需更新缓存version以及返回string
                            if (isDiffVersion && byCache != null) {
                                LogUtil.simple("initCustomInf  cache adn  v");

                                byCache.put(AdvanceConstant.CACHED_CUSTOM_ADN_VERSION, version);
                                //缓存整个返回串
                                JSONArray adnList = json.optJSONArray("custom_adn_list");
                                if (adnList != null && adnList.length() > 0) {
                                    LogUtil.simple("initCustomInf  cache adn  list");
                                    byCache.put(AdvanceConstant.CACHED_CUSTOM_ADN_SETTING, s);
                                }
//                                availableAdapterConfigMap \availableInitClassMap 加入
                            }
                        }
                    } catch (Throwable e) {
                    }
                }

                @Override
                public void onFailed(int i, String s) {
                    LogUtil.simple("initCustomInf onFailed ");

                }
            });
        } catch (Exception e) {
        }
    }

    private static String getCustomSettingUrl() {
        String url = AdvanceConfig.ADN_REQ_URL_HTTP;
        if (AdvanceSetting.getInstance().useHttps) {
            url = AdvanceConfig.ADN_REQ_URL_HTTPS;
        }
        return url;

    }

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
            if (BYStringUtil.isEmpty(savedAdnSetting)){
                LogUtil.simple("savedAdnSetting empty");
                return customADNModels;
            }
            JSONObject jsonObject = new JSONObject(savedAdnSetting);
            JSONArray jsonArray = jsonObject.optJSONArray("custom_adn_list");
            if (jsonArray != null) {
                int length = jsonArray.length();
                for (int i = 0; i < length; i++) {
                    JSONObject custom = jsonArray.getJSONObject(i);
                    if (custom != null) {
                        AdvanceCustomADNModel adn = new AdvanceCustomADNModel();
                        adn.sdkID = custom.optString("id");
                        adn.sdkName = custom.optString("name");

                        adn.initClzName = custom.optString("custom_config_adapter");
                        adn.splashClzName = custom.optString("custom_splash_adapter");
                        adn.bannerClzName = custom.optString("custom_banner_adapter");
                        adn.interstitialClzName = custom.optString("custom_interstitial_adapter");
                        adn.rewardClzName = custom.optString("custom_rewardvideo_adapter");
                        adn.nativeExpressClzName = custom.optString("custom_nativeexpress_adapter");
                        adn.nativeCustomClzName = custom.optString("custom_renderfeed_adapter");

                        customADNModels.add(adn);
                        LogUtil.d("【获取自定义ADN】：" + adn.sdkID);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return customADNModels;


    }

    //将服务端返回的ext数据（string json）转换成map
    public static Map<String, Object> getServerCustomExtData(String json) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (BYStringUtil.isNotEmpty(json)) {
                JSONObject jsonObject = new JSONObject(json);
                result = toMap(jsonObject);
            }

        } catch (JSONException e) {
        }

        return result;
    }

    public static Map<String, Object> toMap(JSONObject object) {
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

    public static List<Object> toList(JSONArray array) {
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
