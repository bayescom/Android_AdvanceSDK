package com.advance.net;

import static com.advance.net.AdvanceJson.convertJsonArrayToArrayList;
import static com.advance.net.AdvanceJson.convertJsonArrayToList;
import static com.advance.net.AdvanceJson.convertJsonToGroup;
import static com.advance.utils.AdvanceUtil.generateKey;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.AdvanceSetting;
import com.advance.model.AdvanceReqModel;
import com.advance.model.ElevenModel;
import com.advance.model.GMParams;
import com.advance.model.SdkSupplier;
import com.advance.model.ServerRewardModel;
import com.advance.model.SupplierSettingModel;
import com.advance.utils.AdvanceSecurityCore;
import com.advance.utils.LogUtil;
import com.advance.utils.SupplierBridgeUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.util.BYCacheUtil;
import com.bayes.sdk.basic.util.BYSPUtil;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

//网络请求相关
public class AdvanceNetManger {


    /**
     * 请求策略分发cruiser服务
     *
     * @param reqModel    请求信息
     * @param reqCallBack 回调信息
     */
    public static void requestSupplierList(AdvanceReqModel reqModel, final BYAbsCallBack<ElevenModel> reqCallBack) {
        try {
            if (reqModel == null) {
                if (reqCallBack != null) {
                    reqCallBack.invoke(null);
                }
                return;
            }
            LogUtil.high("准备发起策略请求");
            long startDevice = System.currentTimeMillis();
            JSONObject deviceInfoObj = getDeviceInfo(reqModel);
            long cost = System.currentTimeMillis() - startDevice;
            LogUtil.devDebug("获取请求参数耗时：" + cost);

            //
            BYReqModel byReqModel = new BYReqModel();
            byReqModel.reqUrl = generateRequestUrl();
            byReqModel.reqBody = deviceInfoObj;
            byReqModel.timeoutMs = AdvanceConstant.strategyTimeOutDur;
            byReqModel.forceTimeOut = reqModel.requestForceTimeout;
            LogUtil.devDebug("请求url:" + byReqModel.reqUrl);
            BYNetRequest.post(byReqModel, new BYReqCallBack() {
                        @Override
                        public void onSuccess(String result) {
                            ElevenModel elevenModel = null;
                            try {
                                JSONObject resultObject = new JSONObject(result);
                                elevenModel = constructSdkSupplierFromJsonObject(resultObject);
                                if (elevenModel != null) {
                                    elevenModel.httpResult = result;
                                }
                                LogUtil.high("策略数据打包完成");
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            if (reqCallBack != null) {
                                reqCallBack.invoke(elevenModel);
                            }
                        }

                        @Override
                        public void onFailed(int code, String reason) {
                            LogUtil.e("策略请求失败，code：" + code + "， reason ：" + reason);
                            if (reqCallBack != null) {
                                reqCallBack.invoke(null);
                            }
                        }
                    }
            );
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //检查缓存中是否有策略信息
    public static ElevenModel requestSupplierInfFromCache(String adspotId) {
        try {
            JSONObject jsonObject = BYCacheUtil.byCache().getAsJSONObject(generateKey("", adspotId));
            if (jsonObject != null) {
                return constructSdkSupplierFromJsonObject(jsonObject);
            } else {
                return null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }


    private static String generateRequestUrl() {
        //dev模式、mock模式、且mock地址不为空，才会使用 mockUrl 进行请求
        if (BYUtil.isDev() && AdvanceSetting.getInstance().canMock && !BYStringUtil.isEmpty(AdvanceSetting.getInstance().mockUrl)) {
            return AdvanceSetting.getInstance().mockUrl;
        }
        if (AdvanceSetting.getInstance().useHttps) {
            return AdvanceConfig.AdvanceSdkRequestUrlHttps;
        }
        return AdvanceConfig.AdvanceSdkRequestUrl;
    }


    private static JSONObject getDeviceInfo(@NonNull AdvanceReqModel reqModel) {
        JSONObject jsonObject = new JSONObject();
        try {
            //set interface version
            jsonObject.put("version", "3.1.0");
            jsonObject.put("sdk_version", AdvanceConfig.AdvanceSdkVersion);
            //set timestamp
            String timestamp = System.currentTimeMillis() + "";
            jsonObject.put("time", timestamp);
            //set appid
            jsonObject.put("appid", reqModel.mediaId);
            //set adspotid
            jsonObject.put("adspotid", reqModel.adspotId);
            //set appver
            jsonObject.put("appver", BYDevice.getAppVersionValue());
            String reqid = reqModel.reqId;
            if (!TextUtils.isEmpty(reqid)) {
                jsonObject.put("reqid", reqid);
            }


            try {
                JSONObject secretObject = new JSONObject();
                secretObject.putOpt("carrier", BYDevice.getCarrierValue());
                secretObject.putOpt("network", BYDevice.getNetworkValue());

                secretObject.putOpt("os", 2);
                secretObject.putOpt("osv", BYDevice.getOSVValue());

                secretObject.putOpt("devicetype", 1);

                secretObject.putOpt("make", BYDevice.getDeviceMakeValue());
                secretObject.putOpt("model", BYDevice.getDeviceModelValue());
                secretObject.putOpt("brand", BYDevice.getDeviceBrandValue());


                secretObject.putOpt("imei", BYDevice.getImeiValue());
                secretObject.putOpt("oaid", BYDevice.getOaidValue());
                secretObject.putOpt("androidid", BYDevice.getAndroidIdValue());


                LogUtil.devDebug("secretObject.toString() = " + secretObject.toString());
                String device_encinfo = AdvanceSecurityCore.getInstance().encrypt(secretObject.toString());

                jsonObject.putOpt("device_encinfo", device_encinfo);

            } catch (Throwable e) {
                e.printStackTrace();
            }


            if (!AdvanceSetting.getInstance().isADTrack) {
                //3.3.4新增 是否允许个性化广告开关 0: (默认)允许1: 禁止。。
                jsonObject.put("donottrack", 1);
            }
            //3.3.4新增 包名 字段传递，预传后台未校验， 4.1.0中移除
//            jsonObject.put("bundle", getPackageName());
            //3.3.4新增 支持自定义额外请求参数
            HashMap<String, String> customData = AdvanceSetting.getInstance().customData;
            JSONObject extJsonObj = new JSONObject();
            boolean haCustom = customData != null && customData.size() > 0;
            boolean needReportVersions = needReportVersions();
            if (BYUtil.isDev()) {
                needReportVersions = true;
            }
            boolean hasExt = reqModel.isFromImm || reqModel.isCacheEffect || haCustom || needReportVersions;

            if (reqModel.isFromImm) {
                //3.5.0新增 单次广告二次请求渠道标识，一般是缓存模式下当缓存失效时 isFromImm = true
                extJsonObj.putOpt("repeat_pv", 1);
            }
            if (reqModel.isCacheEffect) {
                //3.5.1新增 当前请求是来自于策略缓存正常执行时，标记当前reqid的广告策略为缓存策略
                extJsonObj.putOpt("cache_effect", 1);
            }
            if (needReportVersions) {
                try {
                    extJsonObj.putOpt("mry_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_MERCURY));
                    extJsonObj.putOpt("csj_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_CSJ));
                    extJsonObj.putOpt("gdt_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_GDT));
                    extJsonObj.putOpt("bd_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_BAIDU));
                    extJsonObj.putOpt("ks_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_KS));
                    extJsonObj.putOpt("tanx_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_TANX));
                    extJsonObj.putOpt("tap_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_TAP));
                    extJsonObj.putOpt("oppo_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_OPPO));
                    extJsonObj.putOpt("sig_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_SIG));
                    extJsonObj.putOpt("hw_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_HW));
                    extJsonObj.putOpt("xm_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_XIAOMI));
                    extJsonObj.putOpt("vivo_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_VIVO));
                    extJsonObj.putOpt("honor_v", SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_HONOR));

                    //todo 如何增加自定义渠道方式得版本号采集
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (haCustom) {
                for (String key : customData.keySet()) {
                    extJsonObj.putOpt(key, customData.get(key));
                }
            }
            if (hasExt) {
                jsonObject.put("ext", extJsonObj);
            }

            LogUtil.high("request data:" + jsonObject.toString());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    public static boolean needReportVersions() {
        boolean result = false;
        try {
            long time = System.currentTimeMillis();
            int report = AdvanceSetting.getInstance().getReportVersionInf();
            LogUtil.high("instance report setting : " + report);
            if (report < 0) {
                report = BYSPUtil.getSavedInt(AdvanceConstant.SP_SETTING_REPORT);
                LogUtil.high("SP report setting : " + report);
            }
            result = report == 1;
            long cost = (System.currentTimeMillis() - time);
            LogUtil.devDebug("getReportVersionInf cost " + cost + " ms");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }


    private static ElevenModel constructSdkSupplierFromJsonObject(JSONObject jsonObject) {
        if (jsonObject == null) {
            return null;
        }
        try {
            ElevenModel elevenModel = new ElevenModel();
            ArrayList<SdkSupplier> al = new ArrayList<>();
            int code = jsonObject.optInt("code");
            if (code != 200) {
                return null;
            }
            elevenModel.code = code;
            elevenModel.msg = jsonObject.optString("msg");
            elevenModel.reqid = jsonObject.optString("reqid");
            try {
                //gromore相关配置项
                GMParams gmParams = new GMParams();
                JSONObject gmJson = jsonObject.optJSONObject("gro_more");
                if (gmJson != null) {
                    JSONObject gmInfs = gmJson.optJSONObject("gromore_params");
                    if (gmInfs != null) {
                        gmParams.appid = gmInfs.optString("appid");
                        gmParams.adspotid = gmInfs.optString("adspotid");
                        gmParams.timeout = gmInfs.optInt("timeout");
                    }
                    JSONObject gmTks = gmJson.optJSONObject("gmtk");
                    if (gmTks != null) {
                        JSONArray imptkJsonArray = gmTks.optJSONArray("imptk");
                        JSONArray clicktkJsonArray = gmTks.optJSONArray("clicktk");
                        JSONArray succeedJsonArray = gmTks.optJSONArray("succeedtk");
                        JSONArray failedJsonArray = gmTks.optJSONArray("failedtk");
                        JSONArray loadedJsonArray = gmTks.optJSONArray("loadedtk");
                        gmParams.loadedtk = convertJsonArrayToArrayList(loadedJsonArray);
                        gmParams.succeedtk = convertJsonArrayToArrayList(succeedJsonArray);
                        gmParams.imptk = convertJsonArrayToArrayList(imptkJsonArray);
                        gmParams.clicktk = convertJsonArrayToArrayList(clicktkJsonArray);
                        gmParams.failedtk = convertJsonArrayToArrayList(failedJsonArray);
                    }

                    elevenModel.gmParams = gmParams;
                }


            } catch (Throwable e) {
                e.printStackTrace();
            }

            try {
                JSONObject reward = jsonObject.optJSONObject("server_reward");
                if (reward != null) {
                    ServerRewardModel serverReward = new ServerRewardModel();

                    serverReward.url = reward.optString("url");
                    serverReward.rewardCount = reward.optInt("count");
                    serverReward.rewardName = reward.optString("name");

                    elevenModel.serverReward = serverReward;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }


            SupplierSettingModel settingModel = new SupplierSettingModel();
            JSONObject setting = jsonObject.optJSONObject("setting");
            try {
                if (setting != null) {

                    //缓存配置类参数
                    settingModel.enableStrategyCache = setting.optInt("use_cache", -1);
                    settingModel.strategyCacheDuration = setting.optInt("cache_dur", -1);
                    settingModel.delayReport = setting.optInt("report_delay", -1);

                    settingModel.reportVersionInf = setting.optInt("version_report", -1);
                    settingModel.biddingType = setting.optInt("bidding_type", AdvanceConstant.DEFAULT_BIDDING_TYPE);
                    settingModel.parallelTimeout = setting.optInt("parallel_timeout", AdvanceConstant.DEFAULT_PARA_TIMEOUT);

                    AdvanceSetting.getInstance().reportVersionInf(settingModel.reportVersionInf);
                    //长久存储report状态？
                    BYSPUtil.saveInt(AdvanceConstant.SP_SETTING_REPORT, settingModel.reportVersionInf);


                    JSONArray paraGroup = setting.optJSONArray("parallel_group");
                    if (paraGroup != null) {
                        settingModel.paraGroup = convertJsonToGroup(paraGroup);
                    }
                    JSONArray bidGroup = setting.optJSONArray("head_bidding_group");
                    if (bidGroup != null) {
                        settingModel.biddingGroup = convertJsonArrayToList(bidGroup);
                    }

                    ArrayList<SupplierSettingModel.ParaGroupSetting> pgs = new ArrayList<>();
                    JSONArray pgsSetting = setting.optJSONArray("para_group_setting");
                    if (pgsSetting != null) {
                        for (int i = 0; i < pgsSetting.length(); i++) {
                            JSONObject set = pgsSetting.getJSONObject(i);
                            SupplierSettingModel.ParaGroupSetting setting1 = new SupplierSettingModel.ParaGroupSetting();
                            setting1.type = set.optInt("type");
//                            setting1.cache = set.optInt("type");
                            pgs.add(setting1);
                        }

                    }
                    settingModel.paraGroupSetting = pgs;
                }
            } catch (Throwable e) {
                LogUtil.high("策略全局设置类数据转换异常");
                e.printStackTrace();
            }
            elevenModel.setting = settingModel;

            JSONArray supplierJsonArray = jsonObject.optJSONArray("suppliers");
            if (supplierJsonArray != null) {
                for (int i = 0; i < supplierJsonArray.length(); i++) {
                    JSONObject job = supplierJsonArray.getJSONObject(i);
                    String id = job.optString("id");
                    String name = job.optString("name");
                    int priority = job.optInt("priority");
                    int timeout = job.optInt("timeout");
                    int adCount = job.optInt("adcount");
                    int versionTag = job.optInt("versionTag", -1);
                    int initOpt = job.optInt("initOpt", 1);
                    String mediaid = job.optString("mediaid");
                    String adspotid = job.optString("adspotid");
                    String mediakey = job.optString("mediakey");
                    String sdktag = job.optString("sdktag");
                    JSONArray imptkJsonArray = job.optJSONArray("imptk");
                    JSONArray clicktkJsonArray = job.optJSONArray("clicktk");
                    JSONArray succeedJsonArray = job.optJSONArray("succeedtk");
                    JSONArray failedJsonArray = job.optJSONArray("failedtk");
                    JSONArray loadedJsonArray = job.optJSONArray("loadedtk");
                    JSONArray ext = job.optJSONArray("ext");
                    int biddingOpen = job.optInt("is_head_bidding", 0);
                    boolean enableBidding = biddingOpen == 1;
                    SdkSupplier ss = new SdkSupplier();
                    ss.mediaid = mediaid;
                    ss.adspotid = adspotid;
                    ss.mediakey = mediakey;
                    ss.sdkTag = sdktag;
                    ss.id = id;
                    ss.name = name;
                    ss.mediaSecret = job.optString("mediasecret");
                    ss.price = job.optDouble("sdk_price", 0);
                    ss.bidRatio = job.optDouble("bid_ratio", 1);
                    ss.priority = priority;
//                    ss.sortIndex = priority;
                    ss.timeout = timeout;
                    ss.adCount = adCount;
                    ss.versionTag = versionTag;
                    ss.initOpt = initOpt;
                    if (ss.adCount <= 0) {
                        ss.adCount = AdvanceConfig.DEFAULT_AD_COUNT;
                    }
                    ss.enableBidding = enableBidding;
                    ss.imptk = convertJsonArrayToArrayList(imptkJsonArray);
                    ss.clicktk = convertJsonArrayToArrayList(clicktkJsonArray);
                    ss.succeedtk = convertJsonArrayToArrayList(succeedJsonArray);
                    ss.failedtk = convertJsonArrayToArrayList(failedJsonArray);
                    ss.loadedtk = convertJsonArrayToArrayList(loadedJsonArray);
                    ss.wintk = convertJsonArrayToArrayList(job.optJSONArray("wintk"));
                    ss.ext = ext;
                    al.add(ss);
                }
            } else {
                LogUtil.simple("策略为空");
            }
            //如果supplierJsonArray 是null，al会是一个空的SdkSupplier数组，等于是无填充的策略，没有策略且不走打底。
            elevenModel.suppliers = al;
            return elevenModel;

        } catch (Throwable e) {
            LogUtil.high("策略数据转换异常");
            e.printStackTrace();
            return null;
        }

    }

}
