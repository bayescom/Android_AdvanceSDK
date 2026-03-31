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
