package com.advance;

import android.content.Context;
import android.text.TextUtils;

import com.advance.advancelib.BuildConfig;
import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.advance.model.AdvanceLogLevel;
import com.advance.utils.ActivityTracker;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.advance.utils.SupplierBridgeUtil;
import com.bayes.sdk.basic.BYBasicSDK;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.mercury.sdk.core.config.AdConfigManager;
import com.mercury.sdk.core.config.MercuryAD;

import java.util.HashMap;

//3.3.4新增
public class AdvanceSDK {

    //3.3.4新增,v3.6.3 整合,是否允许进行个性化推荐广告，默认true
    //v4.3.2 解耦重构
    public static void enableTrackAD(final boolean doTrack) {
        try {
            AdvanceSetting.getInstance().isADTrack = doTrack;
            SupplierBridgeUtil.recycleCheckSup(new BYAbsCallBack<AdvanceSupplierBridge>() {
                @Override
                public void invoke(AdvanceSupplierBridge advanceSupplierBridge) {
                    if (advanceSupplierBridge != null) {
                        LogUtil.devDebug("setPersonalRecommend :" + doTrack + ", advanceSupplierBridge = " + advanceSupplierBridge);
                        advanceSupplierBridge.setPersonalRecommend(doTrack);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //3.3.4新增,设置请求补充信息，比如用户性别、年龄等
    public static void initCustom(HashMap<String, String> customData) {
        AdvanceSetting.getInstance().customData = customData;
    }


    //3.4.3 新增,获取聚合SDK版本号
    public static String getVersion() {
        return BuildConfig.advanceVersion;
    }

    //3.5.0 新增 初始化SDK
    public static void initSDK(Context context, String appId, boolean isDebug) {
        setDebug(isDebug);
        initSDK(context, appId);
    }

    //3.5.0 新增 设置穿山甲后台填写的应用名称
    public static void setAppNameInCsj(String appName) {
        AdvanceConfig.getInstance().setAppName(appName);
    }

    //3.5.1新增，设置是否使用源代码方式获取oaid，默认true
    @Deprecated
    public static void useSourceOAID(boolean isSource) {
//        AdvanceSetting.getInstance().useOAIDFromSource = isSource;
    }

    //3.5.2新增，设置开屏v+ 小窗口自动关闭时间，不设置使用默认各个渠道默认展示逻辑
    public static void setSplashPlusAutoClose(int time) {
        AdvanceSetting.getInstance().splashPlusAutoClose = time;
    }

    //3.5.2新增，不包含debug属性的初始化方法
    public static void initSDK(Context context, String appId) {
        try {
            if (TextUtils.isEmpty(appId)) {
                LogUtil.w("appId 不可为空，请检查SDK初始化方法中的appId值设置");
            } else {
                AdvanceConfig.getInstance().setMercuryMediaId(appId);
            }
            //初始化引入的渠道配置
            SupplierBridgeUtil.initSup();

            AdvanceConfig.getInstance().initSDKs(context);

            //跟踪activity信息
            ActivityTracker.getInstance().initialize(context);
        } catch (Throwable e) {

        }
    }

    //3.5.2新增，设置debug状态
    public static void setDebug(boolean isDebug) {
        try {
            BYBasicSDK.enableDebug(isDebug);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    //3.5.2新增，设置debug状态，同时可设置log打印等级
    @Deprecated
    public static void setDebug(boolean isDebug, AdvanceLogLevel logLevel) {
        setDebug(isDebug);
        setLogLevel(logLevel);
    }


    //3.5.2新增，设置本地日志打印级别。默认DEFAULT，只打印核心日志
    @Deprecated
    public static void setLogLevel(AdvanceLogLevel logLevel) {
//        try {
//            AdvanceSetting.getInstance().logLevel = logLevel;
//
//            long mv = AdvanceUtil.getNumberVersion(AdConfigManager.getInstance().getSDKVersion());
//
//
//            if (mv >= 346) {//3.4.6版本以后才可以设置log等级
//                MercuryLogLevel mll = MercuryLogLevel.CUSTOM;
//                mll.level = logLevel.level;
//                LogUtil.devDebug("mll.level = " + mll.level + ", mll = " + mll + ", mv = " + mv);
//                MercuryAD.setLogLevel(mll);
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

    }

    //最新版本穿山甲SDK已移除对应设置方法。3.5.3新增，穿山甲特殊设置，用于控制开屏广告的点击区域，具体设置值及含义可参考：  https://www.pangle.cn/support/doc/611f0f0c1b039f004611e4da
    @Deprecated
    public static void setCSJSplashButtonType(int splashButtonType) {
        AdvanceSetting.getInstance().csj_splashButtonType = splashButtonType;
    }

    //最新版本穿山甲SDK已移除对应设置方法。3.5.3新增，穿山甲特殊设置，用于控制下载APP前是否弹出二次确认弹窗(适用所有广告类型)。具体设置值及含义可参考：  https://www.pangle.cn/support/doc/611f0f0c1b039f004611e4da
    @Deprecated
    public static void setCSJDownloadType(int downloadType) {
        AdvanceSetting.getInstance().csj_downloadType = downloadType;
    }

    //3.5.4新增，设置是否使用https域名请求advanceSDK
    public static void setUseHttps(boolean useHttps) {
        try {
            AdvanceSetting.getInstance().useHttps = useHttps;

            long mv = AdvanceUtil.getNumberVersion(AdConfigManager.getInstance().getSDKVersion());

            if (mv >= 352) {//3.5.2版本以后才可以设置是否使用https
                MercuryAD.setUseHttps(useHttps);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //3.5.4新增， 设置是否对传输的个人信息进行md5加密处理
    @Deprecated
    public static void enableMD5UserInf(boolean md5) {
        try {
            AdvanceSetting.getInstance().userMD5 = md5;

            long mv = AdvanceUtil.getNumberVersion(AdConfigManager.getInstance().getSDKVersion());

            if (mv >= 352) {//3.5.2版本以后才可以设置是否使用https
                MercuryAD.enableMD5UserInf(md5);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //    3.5.6新增 设置自定义得图片加载器
//    public static void setTanxImgLoader(ILoader loader) {
//        AdvanceSetting.getInstance().iLoader = loader;
//    }

    //    3.6.3新增 设置隐私控制开关
    public static void setPrivacyController(final AdvancePrivacyController controller) {
        try {
            AdvanceSetting.getInstance().advPrivacyController = controller;

            SupplierBridgeUtil.recycleCheckSup(new BYAbsCallBack<AdvanceSupplierBridge>() {
                @Override
                public void invoke(AdvanceSupplierBridge advanceSupplierBridge) {
                    if (advanceSupplierBridge != null) {
                        LogUtil.devDebug("setCustomPrivacy , advanceSupplierBridge = " + advanceSupplierBridge);
                        advanceSupplierBridge.setCustomPrivacy(controller);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    //微信应用id，用于微信小程序唤起
    public static void setWxOpenAppId(String wxAppId) {
        AdvanceSetting.getInstance().wxAppId = wxAppId;
    }
}
