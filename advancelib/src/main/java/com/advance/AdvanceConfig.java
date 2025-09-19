package com.advance;


import static android.app.Application.getProcessName;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.WebView;

import com.advance.itf.AdvanceSupplierBridge;
import com.advance.model.AdvanceSupConfigModel;
import com.advance.model.CacheMode;
import com.advance.utils.AdvanceLoader;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.advance.utils.SupplierBridgeUtil;
import com.bayes.sdk.basic.BYBasicSDK;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.util.BYCacheUtil;
import com.bayes.sdk.basic.util.BYThreadPoolUtil;
import com.mercury.sdk.core.config.AdConfig;
import com.mercury.sdk.core.config.AdConfigManager;
import com.mercury.sdk.core.config.MercuryAD;

import java.util.ArrayList;
import java.util.HashMap;

public class AdvanceConfig {
    public static final String AdvanceSdkVersion = AdvanceSDK.getVersion();
    public static final String AdvanceSdkRequestUrl = "http://cruiser.bayescom.cn/eleven";
    public static final String AdvanceSdkRequestUrlHttps = "https://cruiser.bayescom.cn/eleven";
    public static final String SDK_ERR_REPORT_URL = "http://cruiser.bayescom.cn/sdkevent";
    public static final String SDK_ERR_REPORT_URL_HTTPS = "https://cruiser.bayescom.cn/sdkevent";



    private static AdvanceConfig instance;
    public static final int DEFAULT_AD_COUNT = 1;
    @Deprecated
    public static final String SDK_TAG_MERCURY = "mercury";
    @Deprecated
    public static final String SDK_TAG_GDT = "gdt";
    @Deprecated
    public static final String SDK_TAG_CSJ = "csj";
    @Deprecated
    public static final String SDK_TAG_BAIDU = "baidu";
    @Deprecated
    public static final String SDK_TAG_KS = "ksh";
    public static final String SDK_ID_MERCURY = "1";
    public static final String SDK_ID_GDT = "2";
    public static final String SDK_ID_CSJ = "3";
    public static final String SDK_ID_BAIDU = "4";
    public static final String SDK_ID_KS = "5";
    public static final String SDK_ID_TANX = "7";
    public static final String SDK_ID_TAP = "10";
    public static final String SDK_ID_OPPO = "9";
    public static final String SDK_ID_SIG = "11";
    public static final String SDK_ID_HW = "12";
    public static final String SDK_ID_XIAOMI = "13";
    public static final String SDK_ID_VIVO = "14";
    public static final String SDK_ID_HONOR = "15";


    static final String NOT_SUPPORT_SUPPLIER_TIPS = "不支持的SDK渠道，跳过该渠道加载。如需加载此渠道，请查看文档使用自定义渠道或者自定义广告来完成广告展示";

    private String appName = "";
    private boolean supportMultiProcess = true;
    private int[] csjDirectDownloadNetworkType;
    private boolean needPermissionCheck = false;
    private boolean isSupplierEmptyAsErr = false; //渠道为空时是否按照异常算，true 渠道信息空不算正常策略，会去尝试加载广告；false 空渠道信息依然算正常策略，不加载策略
    private int reportDelayTime = -1; //设置上报的延迟执行时间，默认-1不延迟，单位毫秒
    private String savedUserAgent;//单例记录保存 UserAgent

    private String csjAppId;
    private String bdAppId;
    private String ksAppId;
    private String ksAppName;
    private String ksAppKey;
    private String ksAppWebKey;
    private String gdtMediaId;
    private String mercuryMediaId;
    private String mercuryMediaKey;

    public boolean forceUseLocalAppID = false;

    private CacheMode defaultStrategyCacheTime; //如果后台未下发策略缓存时长，本地的默认的策略缓存时间

    //有效的渠道config配置信息，一般有此信息代表引入了对应的adapter库。 key为SDKid信息，value为config实例。
    public HashMap<String, AdvanceSupplierBridge> availableAdapterConfigMap = new HashMap<>();
    public boolean hasInitConfig = false;

    private AdvanceConfig() {
        LogUtil.simple(" advance config start");
    }

    public static synchronized AdvanceConfig getInstance() {
        if (instance == null) {
            instance = new AdvanceConfig();
        }
        return instance;
    }

    //todo 整理优化此处初始化调用，合并倍业SDK后，将不再多次初始化基础库SDK，也无需关心版本适配问题
    public AdvanceConfig initSDKs(final Context context) {
        try {
            //初始化基础库SDK
            BYBasicSDK.init(context);

            if (!AdvanceUtil.isMainProcess(context)) {
                LogUtil.simple("非主进程，不再重复初始化");
                return this;
            }
            BYThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        //23-09-06 倍业SDK设置为默认不进行webview初始化
                        MercuryAD.disableWebInit(true);

                        LogUtil.simple("子线程中开始初始化Advance");
                        long start = System.currentTimeMillis();

//                        AdvanceUtil.checkSDKVersion();
                        //mercury sdk 初始化
                        try {
                            new AdConfig.Builder(context).build();
                            //Android 9及以上必须设置、多进程WebView兼容
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                String processName = getProcessName();
                                if (!context.getPackageName().equals(processName)) {
                                    WebView.setDataDirectorySuffix(processName);
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        //尝试获取oaid
//                        BYDevice.getOaidValue();

                        long cost = System.currentTimeMillis() - start;
                        LogUtil.devDebug("advance 初始化耗时：" + cost);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return this;

    }


    public AdvanceConfig setDebug(boolean isDebug) {
        BYBasicSDK.enableDebug(isDebug);
        return this;
    }

    public AdvanceConfig setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getAppName() {
        return this.appName;
    }

//    public boolean getDebug() {
//        return this.debug;
//    }

    //仅穿山甲生效
    public AdvanceConfig setSupportMultiProcess(boolean supportMultiProcess) {
        this.supportMultiProcess = supportMultiProcess;
        return this;
    }

    public boolean getSupportMultiProcess() {
        return supportMultiProcess;
    }


    public int[] getCsjDirectDownloadNetworkType() {
        return csjDirectDownloadNetworkType;
    }

    public void setCsjDirectDownloadNetworkType(int[] csjDirectDownloadNetworkType) {
        this.csjDirectDownloadNetworkType = csjDirectDownloadNetworkType;
    }

    public boolean isNeedPermissionCheck() {
        return needPermissionCheck;
    }

    public AdvanceConfig setNeedPermissionCheck(boolean needPermissionCheck) {
        this.needPermissionCheck = needPermissionCheck;
        return this;
    }


    public String getCsjAppId() {
        return csjAppId;
    }

    public AdvanceConfig setCsjAppId(String csjAppId) {
        this.csjAppId = csjAppId;
        return this;
    }

    public String getGdtMediaId() {
        return gdtMediaId;
    }

    public AdvanceConfig setGdtMediaId(String gdtMediaId) {
        this.gdtMediaId = gdtMediaId;
        return this;
    }

    public String getMercuryMediaId() {
        return mercuryMediaId;
    }

    public AdvanceConfig setMercuryMediaId(String mercuryMediaId) {
        this.mercuryMediaId = mercuryMediaId;
        try {
            if (!TextUtils.isEmpty(mercuryMediaId)) {
                AdConfigManager.getInstance().setMediaId(mercuryMediaId);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    public String getMercuryMediaKey() {
        return mercuryMediaKey;
    }

    public AdvanceConfig setMercuryMediaKey(String mercuryMediaKey) {
        this.mercuryMediaKey = mercuryMediaKey;
        try {
            if (!TextUtils.isEmpty(mercuryMediaKey)) {
                AdConfigManager.getInstance().setMediaKey(mercuryMediaKey);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    public int getReportDelayTime() {
        return reportDelayTime;
    }

    @Deprecated  //本地进行固定时间的延迟上报设置不再生效，服务端下发配置来进行。且延迟上报时机变更为广告事件结束后自动上报
    public AdvanceConfig setReportDelayTime(int reportDelayTime) {
        //最多10s
        if (reportDelayTime > 10000) {
            reportDelayTime = 10000;
        }
        this.reportDelayTime = reportDelayTime;
        return this;
    }

    public String getSavedUserAgent() {
        return savedUserAgent;
    }

    public void setSavedUserAgent(String savedUserAgent) {
        this.savedUserAgent = savedUserAgent;
    }

    public CacheMode getDefaultStrategyCacheTime() {
        return defaultStrategyCacheTime;
    }

    public AdvanceConfig setDefaultStrategyCacheTime(CacheMode defaultStrategyCacheTime) {
        this.defaultStrategyCacheTime = defaultStrategyCacheTime;
        return this;
    }

    public void clearCache(Context ctx) {
        try {
            BYCacheUtil.byCache().clear();
            LogUtil.high("clearCache finish");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public boolean isSupplierEmptyAsErr() {
        return isSupplierEmptyAsErr;
    }

    public void setSupplierEmptyAsErr(boolean supplierEmptyAsErr) {
        isSupplierEmptyAsErr = supplierEmptyAsErr;
    }

    public String getBdAppId() {
        return bdAppId;
    }

    public AdvanceConfig setBdAppId(String bdAppId) {
        this.bdAppId = bdAppId;
        return this;
    }

    public String getKsAppId() {
        return ksAppId;
    }

    public AdvanceConfig setKsAppId(String ksAppId) {
        this.ksAppId = ksAppId;
        return this;
    }

    public String getKsAppName() {
        return ksAppName;
    }

    public AdvanceConfig setKsAppName(String ksAppName) {
        this.ksAppName = ksAppName;
        return this;

    }

    public String getKsAppKey() {
        return ksAppKey;
    }

    public AdvanceConfig setKsAppKey(String ksAppKey) {
        this.ksAppKey = ksAppKey;
        return this;

    }

    public String getKsAppWebKey() {
        return ksAppWebKey;
    }

    public AdvanceConfig setKsAppWebKey(String ksAppWebKey) {
        this.ksAppWebKey = ksAppWebKey;
        return this;

    }
}
