package com.advance.supplier.huawei;


import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.VideoConfiguration;
import com.huawei.hms.ads.nativead.NativeAdConfiguration;

public class AdvanceHWManager {
    private static AdvanceHWManager instance;

    private AdvanceHWManager() {
    }

    public static synchronized AdvanceHWManager getInstance() {
        if (instance == null) {
            instance = new AdvanceHWManager();
        }
        return instance;
    }


    //标记是否初始化执行过
    boolean hasInit = false;
    //全局广告请求参数，
    AdParam.Builder globalAdParamBuilder = null;
    //全局视频配置
    VideoConfiguration.Builder globalVideoConfigBuilder = null;
//    原生广告配置
    NativeAdConfiguration.Builder nativeConfigBuilder = null;
    //横幅尺寸设置
    BannerAdSize bannerAdSize = null;
    //banner背景色
     int bannerBGColor = 0;
}
