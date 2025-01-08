package com.advance.supplier.ks;

import com.kwad.sdk.api.KsAdVideoPlayConfig;
import com.kwad.sdk.api.KsVideoPlayConfig;

public class AdvanceKSManager {
    private static AdvanceKSManager instance;

    private AdvanceKSManager() {
    }

    public static synchronized AdvanceKSManager getInstance() {
        if (instance == null) {
            instance = new AdvanceKSManager();
        }
        return instance;
    }


    private final KsVideoPlayConfig defaultConfig = new KsVideoPlayConfig.Builder()
            .videoSoundEnable(false)
            .build();


    public KsVideoPlayConfig rewardVideoConfig = null;
    public KsVideoPlayConfig fullScreenVideoConfig = null;
    public KsAdVideoPlayConfig nativeExpressConfig = null;
    public KsVideoPlayConfig interstitialVideoConfig = defaultConfig;


    //默认值，会执行advance原有初始化逻辑
    public static final int INIT_STATUS_DEFAULT = 0;
    //以下三个状态，会影响初始化执行逻辑，由调用时机改变
    public static final int INIT_STATUS_CALLING = 1;
    public static final int INIT_STATUS_SUCCESS = 2;
    public static final int INIT_STATUS_FAILED = 3;

    //内部初始化状态标记
    public int innerInitStatus = INIT_STATUS_DEFAULT;
    public int innerInitErrCode;
    public String innerInitErrMsg;
}
