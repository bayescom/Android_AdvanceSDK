package com.advance.utils;

import com.advance.AdvanceConfig;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvanceSupplierBridge;
import com.advance.model.AdvanceCustomADNModel;
import com.advance.model.AdvanceSupConfigModel;
import com.bayes.sdk.basic.itf.BYAbsCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SupplierBridgeUtil {

    public static synchronized void initSup() {
        try {
            if (AdvanceConfig.getInstance().hasInitConfig) {
                LogUtil.simple("hasInitConfig ,skip initSup");
                return;
            }

            // TODO: 2025/2/20 自定义adn如何增加至该方法？ 是否必要？
//全部已有的渠道信息。    包含了获取config实例需要的所有参数信息。
            ArrayList<AdvanceSupConfigModel> supportSupList = new ArrayList<>();
//            后续如果添加SDK，只需要在这里添加并指定好对应的类名即可
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_MERCURY, "mry.MercuryGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_GDT, "gdt.GdtGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_CSJ, "csj.CsjGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_BAIDU, "baidu.BDGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_KS, "ks.KSGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_TANX, "tanx.TanxGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_TAP, "tap.TapGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_SIG, "sigmob.SigmobGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_OPPO, "oppo.OppoGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_HW, "huawei.HWGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_XIAOMI, "mi.XMGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_HONOR, "honor.HonorGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_VIVO, "vv.VivoGlobalConfig"));
            supportSupList.add(new AdvanceSupConfigModel(AdvanceConfig.SDK_ID_FLINK, "flink.FLGlobalConfig"));

            //添加自定义adn检查
            ArrayList<AdvanceCustomADNModel> adns = CustomADNUtil.getCustomADNModels();
            if (!adns.isEmpty()) {
                for (AdvanceCustomADNModel adn : adns) {
                    String adnID = adn.sdkID;
                    String initClassName = adn.initClzName;
                    AdvanceSupConfigModel configModel = new AdvanceSupConfigModel(adnID, initClassName);
                    configModel.isCustom = true;
                    supportSupList.add(configModel);
                }
            }

            AdvanceConfig.getInstance().availableAdapterConfigMap = new HashMap<>();
            for (AdvanceSupConfigModel supConfigModel : supportSupList) {
                if (supConfigModel != null) {
                    String clzName = supConfigModel.className;
                    if (!supConfigModel.isCustom) {
                        clzName = AdvanceLoader.BASE_ADAPTER_PKG_PATH + supConfigModel.className;
                    }

                    AdvanceCustomInit customInit = AdvanceLoader.getCustomInit(clzName);
                    if (customInit == null) {
                        LogUtil.e("检测到未引入得SDK id：" + supConfigModel.sdkID);
                        continue;
                    }

                    if (AdvanceConfig.getInstance().availableAdapterConfigMap != null) {
                        LogUtil.simple("检测到已引入得SDK id：" + supConfigModel.sdkID);
                        //放入已生效的map
                        AdvanceConfig.getInstance().availableAdapterConfigMap.put(supConfigModel.sdkID, customInit);
                    }
                }
            }

            int adapterSize = 0;
            if (AdvanceConfig.getInstance().availableAdapterConfigMap != null) {
                adapterSize = AdvanceConfig.getInstance().availableAdapterConfigMap.size();
            }
            AdvanceConfig.getInstance().hasInitConfig = true;
            LogUtil.devDebug("availableAdapterConfigMap adapterSize = " + adapterSize);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void recycleCheckSup(BYAbsCallBack<AdvanceCustomInit> singleCheck) {
        try {
            HashMap<String, AdvanceCustomInit> availableAdapterConfigMap = AdvanceConfig.getInstance().availableAdapterConfigMap;

            //先进行check，如果为空可能还没有进行过初始化，需要初始化以后重新赋值
            if (availableAdapterConfigMap == null || availableAdapterConfigMap.size() == 0) {
                LogUtil.d("recycleCheckSup initSup");
                initSup();
                availableAdapterConfigMap = AdvanceConfig.getInstance().availableAdapterConfigMap;
            }
            if (availableAdapterConfigMap != null && availableAdapterConfigMap.size() > 0) {
                for (Map.Entry<String, AdvanceCustomInit> entry : availableAdapterConfigMap.entrySet()) {
                    AdvanceCustomInit value = entry.getValue();
                    String sdkid = entry.getKey();
                    LogUtil.d("recycleCheckSup sdkid : " + sdkid);
                    if (singleCheck != null) {
                        singleCheck.invoke(value);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String getSupVersion(String sdkID) {
        String result = "";
        try {
            HashMap<String, AdvanceCustomInit> availableAdapterConfigMap = AdvanceConfig.getInstance().availableAdapterConfigMap;
            //先进行check，如果为空可能还没有进行过初始化，需要初始化以后重新赋值
            if (availableAdapterConfigMap == null || availableAdapterConfigMap.size() == 0) {
                LogUtil.d("getSupVersion initSup");
                initSup();
                availableAdapterConfigMap = AdvanceConfig.getInstance().availableAdapterConfigMap;
            }
            if (availableAdapterConfigMap != null && availableAdapterConfigMap.size() > 0) {
                AdvanceCustomInit supplierBridge = availableAdapterConfigMap.get(sdkID);
                if (supplierBridge != null) {
                    result = supplierBridge.getSDKVersion();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

}
