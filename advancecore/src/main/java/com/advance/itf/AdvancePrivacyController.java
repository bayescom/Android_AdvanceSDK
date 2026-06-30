package com.advance.itf;

import android.location.Location;

import com.bayes.sdk.basic.device.BYPrivacyController;

import java.util.List;

public abstract class AdvancePrivacyController extends BYPrivacyController {
    /**
     * 是否允许SDK主动使用地理位置信息
     *
     * @return true可以获取，false禁止获取。默认为true
     */
    public boolean isCanUseLocation() {
        return true;
    }

    /**
     * 当isCanUseLocation=false时，可传入地理位置信息，sdk使用您传入的地理位置信息
     *
     * @return 地理位置参数
     */
    public Location getLocation() {
        return null;
    }


    /**
     * 快手SDK需要
     * 当isCanUsePhoneState=false时，可传入imei信息集合，sdk使用您传入的imei信息
     *
     * @return imei信息
     */
    public String[] getImeis() {
        return null;
    }


    /**
     * 是否允许SDK主动使用WRITE_EXTERNAL_STORAGE权限
     *
     * @return true可以使用，false禁止使用。默认为true
     */
    public boolean isCanUseWriteExternal() {
        return true;
    }


    /**
     * 是否允许SDK主动获取oaid，应用于快手SDK配置项
     *
     * @return true可以使用，false禁止使用。默认为true
     */
    public boolean canUseOaid() {
        return true;
    }

    /**
     * 是否允许SDK主动获取Mac，应用于快手SDK、mercury配置项
     *
     * @return true可以使用，false禁止使用。默认为true
     */
    public boolean canUseMacAddress() {
        return true;
    }

    /**
     * 是否允许SDK使用NetworkState，应用于快手SDK配置项
     *
     * @return true可以使用，false禁止使用。默认为true
     */
    public boolean canUseNetworkState() {
        return true;
    }


    /**
     * 是否允许SDK主动获取设备上应用安装列表的采集权限
     *
     * @return true可以使用，false禁止使用。默认为true
     */
    public boolean alist() {
        return true;
    }

    /**
     * 开发者可以传入已安装app包列表，应用于快手、sigmobSDK配置项
     *
     * @return 包名列表
     */
    public List<String> getInstalledPackages() {
        return null;
    }
}


//接口适用描述备份：

// public abstract class AdvancePrivacyController {
// /**
// * 是否允许SDK主动使用地理位置信息，适用于百度、快手、穿山甲、mercury、oppo、vivo、sigmob、小米、荣耀、taptap
// *
// * @return true可以获取，false禁止获取。默认为true
// */
//public boolean isCanUseLocation() {
//    return true;
//}
//
///**
// * 当isCanUseLocation=false时，可传入地理位置信息，sdk使用您传入的地理位置信息。适用于快手、穿山甲、mercury、oppo、vivo、sigmob、小米、荣耀、taptap
// *
// * @return 地理位置参数
// */
//public Location getLocation() {
//    return null;
//}
//
///**
// * 是否允许SDK主动使用手机硬件参数，如：IMEI、IMSI、MEID、androidid。适用于百度、快手、穿山甲、优量汇、mercury、oppo、vivo、sigmob、taptap
// *
// * @return true可以使用，false禁止使用。默认为true
// */
//public boolean isCanUsePhoneState() {
//    return true;
//}
//
///**
// * 当isCanUsePhoneState=false时，可传入imei信息，sdk使用您传入的imei信息，适用于快手、穿山甲、mercury、oppo、vivo、sigmob、taptap
// *
// * @return imei信息
// */
//public String getDevImei() {
//    return null;
//}
//
///**
// *
// * 当isCanUsePhoneState=false时，可传入imei信息集合，sdk使用您传入的imei信息，仅快手SDK需要
// *
// * @return imei信息
// */
//public String[] getImeis() {
//    return null;
//}
//
///**
// * 当isCanUsePhoneState=false时，可传入AndroidID信息，sdk使用您传入的AndroidID信息，仅适用于快手、sigmob、oppo、Mercury、taptap
// *
// * @return AndroidID信息
// */
//public String getDevAndroidID() {
//    return null;
//}
//
///**
// * 当isCanUseWifiState=false时，可传入mac信息，sdk使用您传入的mac信息，适用于快手、穿山甲、mercury、oppo
// *
// * @return mac信息
// */
//public String getDevMac() {
//    return null;
//}
//
///**
// * 是否允许SDK主动使用WRITE_EXTERNAL_STORAGE权限来进行本地化存储，仅用于穿山甲、快手、百度、oppo、taptap
// *
// * @return true可以使用，false禁止使用。默认为true
// */
//public boolean isCanUseWriteExternal() {
//    return true;
//}
//
///**
// * 是否允许SDK主动使用ACCESS_WIFI_STATE权限获取设备mac信息，仅适用于穿山甲、优量汇、oppo、小米、Mercury、taptap
// *
// * @return true可以使用，false禁止使用。默认为true
// */
//public boolean isCanUseWifiState() {
//    return true;
//}
//
///**
// * 是否允许SDK主动获取oaid，仅应用于快手SDK配置项
// *
// * @return true可以使用，false禁止使用。默认为true
// */
//public boolean canUseOaid() {
//    return true;
//}
//
///**
// * 是否允许SDK主动获取Mac，仅应用于快手、Mercury SDK配置项
// *
// * @return true可以使用，false禁止使用。默认为true
// */
//public boolean canUseMacAddress() {
//    return true;
//}
//
///**
// * 是否允许SDK使用NetworkState，仅应用于快手SDK配置项
// *
// * @return true可以使用，false禁止使用。默认为true
// */
//public boolean canUseNetworkState() {
//    return true;
//}
//
///**
// * 开发者可以传入oaid，SDK将使用传入的oaid值，仅应用于穿山甲、快手、Mercury、vivo、sigmob、taptap SDK配置项
// *
// * @return oaid
// */
//public String getDevOaid() {
//    return null;
//}
//
//
///**
// * 是否允许SDK主动获取设备上应用安装列表的采集权限，百度、快手、穿山甲、优量汇、Mercury、oppo、vivo、sigmob、小米、荣耀、taptap
// *
// * @return true可以使用，false禁止使用。默认为true
// */
//public boolean alist() {
//    return true;
//}
//
///**
// * 开发者可以传入已安装app包列表，应用于快手、穿山甲 SDK配置项
// *
// * @return 包名列表
// */
//public List<String> getInstalledPackages() {
//    return null;
//}
//
//
///**
// * 开发者可以传入运营商信息，仅用于MercurySDK配置项
// *
// * @return the MCC+MNC (mobile country code + mobile network code) of the provider of the SIM. 5 or 6 decimal digits.
// */
//public String getSimOperator() {
//    return null;
//}
//}
