package com.advance.supplier.baidu;


import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.RewardVideoAd;


public class AdvanceBDManager {
    private static AdvanceBDManager instance;

    private AdvanceBDManager() {
    }

    public static synchronized AdvanceBDManager getInstance() {
        if (instance == null) {
            instance = new AdvanceBDManager();
        }
        return instance;
    }

    public RequestParameters splashParameters = null;
    public RequestParameters nativeExpressParameters = null;
    public RequestParameters nativeCustomParameters = null;

    public boolean bDSupportHttps = false;
    //全屏视频是否使用SurfaceView来渲染
    public boolean fullScreenUseSurfaceView = false;
    //激励视频是否使用SurfaceView来渲染
    public boolean rewardUseSurfaceView = false;
    //激励视频下载确认弹框设置，默认永不弹框
    public int rewardDownloadAppConfirmPolicy = RewardVideoAd.DOWNLOAD_APP_CONFIRM_NEVER;

    //------以下为v4.2.3新增设置------
    // 设置底价，<=0 代表不进行设置
    public int splashBidFloor = 0;
    public int interstitialBidFloor = 0;
    public int rewardBidFloor = 0;
    public int nativeExpressBidFloor = 0;
    public int nativeCusBidFloor = 0;
    public int fullScreenBidFloor = 0;
    //    下载广告是否出下载弹窗
    public boolean interstitialUseDialogFrame = true;

}
