package com.advance.supplier.baidu;

import android.app.Activity;
import android.text.TextUtils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.utils.AdvanceSplashPlusManager;
import com.baidu.mobads.sdk.api.BiddingListener;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class BDUtil implements AdvanceSplashPlusManager.ZoomCall {

    public static LinkedHashMap<String, Object> getFailedObj(double winPrice, Map<String, Object> referBidInfo){
        int bitT = 2;
        try {
            int winType = (int) referBidInfo.get(AdvanceConstant.BID_RESULT_KEY_WIN_SDK_TYPE);
            if (winType == AdvanceConstant.BID_WIN_SDK_TYPE_BIDDING){
                bitT = 3;
            }
        } catch (Exception e) {
        }

        // 竞胜方信息
        LinkedHashMap<String, Object> winInfo = new LinkedHashMap<>();
        try {
            // 竞胜方出价
            winInfo.put("ecpm", winPrice);
            // 竞胜方的DSP id，参考文档获取
            winInfo.put("adn", 1);
            // 竞胜方的物料类型，参考文档获取
//        winInfo.put("ad_t", 3);
            // 竞价时间，秒级时间戳
//        winInfo.put("ad_time", System.currentTimeMillis() / 1000);
            // 竞价类型，（百度竞价结果参数：1：分层保价；2：广告价格（单位：分）；3：bidding；4：其他)
            winInfo.put("bid_t", bitT);
            // 竞价失败原因，203：输给其他竞价方，其他见联盟文档附录
            String biddingFailResult = "203";
            winInfo.put("reason", biddingFailResult);
//        winInfo.put("is_s", 是否曝光);
//        winInfo.put("is_c", 是否点击);
        } catch (Exception e) {
        }

        return  winInfo;

    }
    //根据百度返回的string类型的ecpm信息，解析成double类型，（单位：分）
    public static double getEcpmValue(String oriEcpm) {
        double result = 0;
        try {
            if (!TextUtils.isEmpty(oriEcpm)) {
                result = Double.parseDouble(oriEcpm);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public static final String BD_TAG = AdvanceConfig.SDK_TAG_BAIDU;

//    public static synchronized void initBDAccount(BaseParallelAdapter adapter) {
//        try {
//            if (adapter == null) {
//                LogUtil.e("[BDUtil] initAD failed BaseParallelAdapter null");
//                return;
//            }
//
//            String sid = "";
//            if (adapter.sdkSupplier != null) {
//                sid = adapter.sdkSupplier.mediaid;
//            }
//
//            boolean hasInit = AdvanceSetting.getInstance().hasBDInit;
//            String lastAppId = AdvanceSetting.getInstance().lastBDAID;
//
//            String resultAppID = sid;
//            String configAppId = AdvanceConfig.getInstance().getBdAppId();
//            //检测appid 账号配置
//            if (AdvanceConfig.getInstance().forceUseLocalAppID && !TextUtils.isEmpty(configAppId)) {
//                LogUtil.simple("强制使用本地配置的穿山甲 AppID");
//                resultAppID = configAppId;
//            }
//            LogUtil.high("[BDUtil.initBDAccount] 百度 appID：" + resultAppID);
//
//            boolean isSame = lastAppId.equals(resultAppID);
//            //只有当允许初始化优化时，且快手已经初始化成功过，并行初始化的id和当前id一致，才可以不再重复初始化。
//            if (hasInit && adapter.canOptInit() && isSame) {
//                LogUtil.simple("[BDUtil.initBDAccount] already init");
//                return;
//            }
//
//            Context context = adapter.getRealContext();
//
////            if (AdvanceUtil.getCurrentProcessName(activity).startsWith(activity.getPackageName())) {
//            // 初始化信息，初始化一次即可，（此处用startsWith()，可包括激励/全屏视频的进程）
//            // https、视频缓存空间有特殊需求可动态配置，一般取默认值即可，无需设置
//            BDAdConfig bdAdConfig = new BDAdConfig.Builder()
//                    // 1、设置app名称，可选
////                        .setAppName("网盟demo")
//                    // 2、应用在mssp平台申请到的appsid，和包名一一对应，此处设置等同于在AndroidManifest.xml里面设置
//                    .setAppsid(resultAppID)
//                    // 3、设置下载弹窗的类型和按钮动效样式，可选
//                    .setDialogParams(new BDDialogParams.Builder()
//                            .setDlDialogType(BDDialogParams.TYPE_BOTTOM_POPUP)
//                            .setDlDialogAnimStyle(BDDialogParams.ANIM_STYLE_NONE)
//                            .build())
////                    .setHttps(AdvanceBDManager.getInstance().bDSupportHttps)//如果设置为true，那么banner广告将会无法展示，
//                    .setDebug(BYUtil.isDebug())
//                    .build(context);
//            bdAdConfig.init();
//            // 设置SDK可以使用的权限，包含：设备信息、定位、存储、APP LIST
//            // 注意：建议授权SDK读取设备信息，SDK会在应用获得系统权限后自行获取IMEI等设备信息
//            // 授权SDK获取设备信息会有助于提升ECPM
//            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
//            boolean deviceOn = true;
//            boolean locationOn = true;
//            boolean storageOn = true;
//            boolean appListOn = true;
//            if (advancePrivacyController != null) {
//                deviceOn = advancePrivacyController.isCanUsePhoneState();
//                locationOn = advancePrivacyController.isCanUseLocation();
//                storageOn = advancePrivacyController.isCanUseWriteExternal();
//                appListOn = advancePrivacyController.alist();
//            }
//            MobadsPermissionSettings.setPermissionReadDeviceID(deviceOn);
//            MobadsPermissionSettings.setPermissionLocation(locationOn);
//            MobadsPermissionSettings.setPermissionStorage(storageOn);
//            MobadsPermissionSettings.setPermissionAppList(appListOn);
////            }
//
//            AdvanceSetting.getInstance().hasBDInit = true;
//            AdvanceSetting.getInstance().lastBDAID = resultAppID;
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//    }


    @Override
    public void zoomOut(Activity activity) {

    }
}
