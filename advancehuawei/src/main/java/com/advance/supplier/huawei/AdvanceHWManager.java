package com.advance.supplier.huawei;

import androidx.annotation.ColorInt;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.VideoConfiguration;

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
    AdParam globalAdParam = null;
    VideoConfiguration globalVideoConfig = null;
    //横幅尺寸设置
    BannerAdSize bannerAdSize = null;
    //banner背景色
    @ColorInt int bannerBGColor = 0;
}
