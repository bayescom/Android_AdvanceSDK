package com.advance;

import android.app.Activity;

import com.advance.itf.AdvancePrivacyController;

import java.util.HashMap;

public class AdvanceSetting {
    private static AdvanceSetting instance;

    private AdvanceSetting() {
    }

    public static synchronized AdvanceSetting getInstance() {
        if (instance == null) {
            instance = new AdvanceSetting();
        }
        return instance;
    }

    public String logTag = "AdvanceSDK";

//    todo 改为存储在sp中
public boolean isADTrack = true;
    public HashMap<String, String> customData = null;

    //标记各平台是否初始化过
    public boolean hasGDTInit = false;
    public boolean hasCSJInit = false;
    public boolean hasKSInit = false;
    public boolean hasBDInit = false;
    public boolean hasTANXInit = false;
    public boolean hasTAPInit = false;

    //各平台最近一次初始化的APP_ID
    public String lastGDTAID = "";
    public String lastCSJAID = "";
    public String lastKSAID = "";
    public String lastBDAID = "";
    public String lastTANXAID = "";
    public String lastTAPAID = "";

//    public String oaid = "";
//    public boolean useOAIDFromSource = true; //是否使用来自源代码方式获取oaid，默认true

    public String currentSupId = "";//当前执行的SDK渠道id
    public boolean isSplashSupportZoomOut = false; //是否需要执行v+形式的缩放操作
//    public boolean isSplashShowInSingleActivity = false; //是否需要执行v+形式的缩放操作

    public int splashPlusAutoClose = -1;//开屏v+自动关闭时间，单位毫秒，默认-1不执行强制关闭

//    public AdvanceLogLevel logLevel = AdvanceLogLevel.DEFAULT;

    private int reportVersionInf = -1;

    public int csj_splashButtonType = -1;
    public int csj_downloadType = -1;
    public boolean useHttps = false;
    public boolean userMD5 = false;
    public String wxAppId = "";
//    public ILoader iLoader;

    public AdvancePrivacyController advPrivacyController;

    //存放请求时传输的版本信息，非加密
//    public String osv = "";
//    public String appVer = "";

    public boolean canMock = false;
    public String mockUrl = "";

    //用来标记当前展示的activity，方便开发者未传递时，直接使用
    public Activity currentActivity;


    //    advance得广告实例，key 为广告位id
    public HashMap<String, AdvanceSplash> advanceSplashInf;
    public HashMap<String, AdvanceBanner> advanceBannerInf;
    public HashMap<String, AdvanceRewardVideo> advanceRewardInf;
    public HashMap<String, AdvanceFullScreenVideo> advanceFullScreenInf;
    public HashMap<String, AdvanceNativeExpress> advanceNativeExpressInf;
    public HashMap<String, AdvanceInterstitial> advanceInterstitialInf;
    public HashMap<String, AdvanceDraw> advanceDrawInf;

    public int getReportVersionInf(){
        return reportVersionInf;
    }

    public void reportVersionInf(int report) {
        reportVersionInf = report;
    }

    public int getSplashPlusAutoClose() {
        try {
            if (splashPlusAutoClose <= 0) {
                return -1;
            }
            //最短3秒
            if (splashPlusAutoClose < 3000) {
                return 3000;
            }
            //最长30秒
            if (splashPlusAutoClose > 30000) {
                return 30000;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return splashPlusAutoClose;
    }

}
