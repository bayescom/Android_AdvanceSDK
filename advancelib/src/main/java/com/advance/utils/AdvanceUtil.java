package com.advance.utils;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;
import com.advance.model.AdvanceReqModel;
import com.advance.model.CacheMode;
import com.advance.model.ElevenModel;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYCache;
import com.bayes.sdk.basic.util.BYCacheUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.mercury.sdk.core.config.AdConfigManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.ref.SoftReference;
import java.math.BigDecimal;

public class AdvanceUtil {
    private Context app;
    //广告请求数


    public AdvanceUtil(Activity activity) {
//        this.activity = activity;
        try {
            this.app = activity.getApplicationContext();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    public static String generateKey(String mediaId, String adspotId) {
        if (isCacheAllowExpired()) {
            //生成新的带版本号的key
            return generateACacheKey(mediaId, adspotId);
        } else {
            //使用旧的不带版本号的key
            return generateOldACacheKey(mediaId, adspotId);
        }
    }

    //生成新的ACacheKey，新增v1版本号
    private static String generateACacheKey(String mediaId, String adspotId) {
        return "supplier_list_" + mediaId + "_" + adspotId + "_v1";
    }

    private static String generateOldACacheKey(String mediaId, String adspotId) {
        return "supplier_list_" + mediaId + "_" + adspotId;
    }

    public static void saveElevenData(ElevenModel elevenModel, AdvanceReqModel reqModel) {
        try {
            BYCache aCache = BYCacheUtil.byCache();

            //  这里要先去拿一下策略返回的失效时间，如果有返回，使用策略返回的失效时间，否则使用默认的
            CacheMode cacheMode = AdvanceConfig.getInstance().getDefaultStrategyCacheTime();
            if (cacheMode == null) {
                cacheMode = CacheMode.DEFAULT;
            }
            int saveTime = cacheMode.savedTime;
            boolean needCache = true;
            String key = generateKey(reqModel.mediaId, reqModel.adspotId);

            if (elevenModel != null) {
                if (elevenModel.setting != null) {
                    int receivedSaveTime = elevenModel.setting.strategyCacheDuration;
                    // TODO: 2020-08-14 是否需要最大时长限制？
                    int largestTime = 60 * 24 * 60 * 60;//最大缓存时长，最小无限制。如果后台返回负值使用默认的三天
                    if (receivedSaveTime >= largestTime) {
                        saveTime = largestTime;
                    } else if (receivedSaveTime >= 0) {
                        saveTime = receivedSaveTime;
                    } else if (receivedSaveTime == -100) { //如果是-100，代表不限制缓存时长
                        saveTime = CacheMode.UNLIMIT.savedTime;
                    }

                    //赋值下发的缓存开关
                    int cache = elevenModel.setting.enableStrategyCache;
                    //后台下发字段 如果cache == 1，或者来自于默认的情况-1，都需要缓存。如果是0 2345等代表不缓存，并清理数据

                    if (reqModel.forceCache) {
                        //只要策略未返回 0（代表实时请求），才进行强制缓存
                        needCache = cache != 0;
                        LogUtil.high("强制缓存模式");
                    } else {

                        if (cache >= 0) {
                            needCache = cache == 1;
                        }
                    }
                    //                    else {
                    //                        needCache = false;
                    //                    }
                }
                if (needCache) {
                    //只要保留时间为正即进行定时缓存
                    if (saveTime >= 0) {
                        //                        if (AdvanceSetting.getInstance().isDev){
                        //                            saveTime = 20;
                        //                        }
                        LogUtil.high("缓存当前策略，缓存时长：" + saveTime + "s");
                        aCache.put(key, elevenModel.httpResult, saveTime);
                    } else {
                        LogUtil.high("缓存当前策略，缓存时长不限制");
                        aCache.put(key, elevenModel.httpResult);
                    }

                } else {
                    boolean rv = aCache.remove(key);
                    LogUtil.high("不再缓存当前策略，并移除缓存 " + rv);
                }

            } else {
                boolean isEmptyErr = AdvanceConfig.getInstance().isSupplierEmptyAsErr();

                if (isEmptyErr) {
                    boolean rv = aCache.remove(key);
                    LogUtil.high("失败返回视为空策略，移除当前缓存 " + rv);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.e("[saveElevenData] error");
        }
    }

    //缓存是否允许过期
    private static boolean isCacheAllowExpired() {
        CacheMode cacheMode = AdvanceConfig.getInstance().getDefaultStrategyCacheTime();
        if (cacheMode != null) {
            return !cacheMode.equals(CacheMode.UNLIMIT);
        }
        return true;
    }

//    public static void checkSDKVersion() {
//        try {
//
//            String minMV = AdvanceConfig.SDK_MIN_VERSION_MERCURY;
//            String minCV = AdvanceConfig.SDK_MIN_VERSION_CSJ;
//            String minGV = AdvanceConfig.SDK_MIN_VERSION_GDT;
//            String minBV = AdvanceConfig.SDK_MIN_VERSION_BD;
//            String minKV = AdvanceConfig.SDK_MIN_VERSION_KS;
//
//            LogUtil.simple("【SDK版本号检测】：" + "Advance聚合SDK 版本号" + AdvanceConfig.AdvanceSdkVersion);
//            try {
//                String merV = AdConfigManager.getInstance().getSDKVersion();
//                checkSingleVersion("Mercury", merV, minMV);
//                long mcv = getNumberVersion(merV);
//                // TODO: 2023/6/25 检查log打印效果
//                if (mcv >= 322) {//3.2.2 开始支持自定义设置debug_tag
//                    MercuryAD.setDebugTag("advance_mercury");
//                }
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//
//            try {
//                String csjV = TTAdSdk.getAdManager().getSDKVersion();
////                String csjV = TTVfSdk.getVfManager().getSDKVersion();
//                checkSingleVersion("穿山甲", csjV, minCV);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//
//            try {
//                String gdtV = SDKStatus.getSDKVersion();
//                checkSingleVersion("广点通", gdtV, minGV);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//
//            try {
//                String bdV = AdSettings.getSDKVersion();
//                checkSingleVersion("百度", bdV, minBV);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//
//            try {
//                String ksV = KsAdSDK.getSDKVersion();
//                checkSingleVersion("快手", ksV, minKV);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }


    public static Long getNumberVersion(String ver) {
        long v = 0;
        try {
            //限定取3位
            String[] vers = ver.split("\\.", 6);

            if (vers.length > 0) {
                v = v + (Long.parseLong(vers[0])) * 10000;
            }
            if (vers.length > 1) {
                v = v + (Long.parseLong(vers[1])) * 10;
            }
            if (vers.length > 2) {
                v = v + Long.parseLong(vers[2]);
            }

            LogUtil.devDebug("[getNumberVersion] result = " + v);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return v;
    }

    public static void checkSingleVersion(String tag, String nowV, String minMV) {
        try {
            String versionTips = "[checkSingleSDKVersion] " + tag + "SDK 当前版本号： " + nowV + " 推荐使用的版本号： " + minMV + "\n";

            String versionErrTips = "[checkSingleSDKVersion] 当前集成的 " + tag + " SDK版本过低，可能会影响广告展示，请升级至推荐的版本号！";

            LogUtil.simple(versionTips);
            long nv = getNumberVersion(nowV);
//            Long.parseLong(nowV.replace(".", ""));
            long mv = getNumberVersion(minMV);
//            Long.parseLong(minMV.replace(".", ""));
            LogUtil.devDebug("[checkSingleSDKVersion] nv = " + nv + ", mv = " + mv);

            //如果最先版本比当前版本大，需要提示升级版本
            if (mv > nv) {
                LogUtil.e(versionErrTips);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 检测和设置Mercury SDK的账号配置信息
     * 如果forceUseLocalAppID = true，则优先使用本地配置的媒体id。
     *
     * @param mediaId  策略服务下发字段
     * @param mediaKey 策略服务下发字段
     */
    public static void initMercuryAccount(String mediaId, String mediaKey) {
        String resultAppID = mediaId;
        String resultAppKey = mediaKey;
        String configMediaId = AdvanceConfig.getInstance().getMercuryMediaId();
        String configMediaKey = AdvanceConfig.getInstance().getMercuryMediaKey();

        if (AdvanceConfig.getInstance().forceUseLocalAppID && !TextUtils.isEmpty(configMediaId)) {
            LogUtil.simple("强制使用本地配置的Mercury AppID");
            resultAppID = configMediaId;
        }
        if (AdvanceConfig.getInstance().forceUseLocalAppID && !TextUtils.isEmpty(configMediaKey)) {
            LogUtil.simple("强制使用本地配置的Mercury AppKey");
            resultAppKey = configMediaKey;
        }
        LogUtil.high("[initMercuryAccount] Mercury AppID：" + resultAppID + "， Mercury AppKey：" + resultAppKey);

        AdConfigManager.getInstance().setMediaId(resultAppID);
        AdConfigManager.getInstance().setMediaKey(resultAppKey);
    }


    /**
     * 广点通账号信息确认，优先使用服务端下发的广点通媒体id。
     * 如果forceUseLocalAppID = true，则优先使用本地配置的媒体id。
     *
     * @param mediaId 策略服务下发字段
     * @return 实际使用的媒体id
     */
    public static String getGdtAccount(String mediaId) {
        String result = mediaId;
        String configMediaId = AdvanceConfig.getInstance().getGdtMediaId();

        if (AdvanceConfig.getInstance().forceUseLocalAppID && !TextUtils.isEmpty(configMediaId)) {
            LogUtil.simple("强制使用本地配置的广点通appID");
            result = configMediaId;
        }

        LogUtil.high("[getGdtAccount] 广点通appID：" + result);

//        if (configMediaId == null || "".equals(configMediaId)) {
//            LogUtil.AdvanceLog("Advance SDK初始化未配置广点通 mediaId，使用策略服务下发的 mediaId。");
//            configMediaId = mediaId;
//        }
        return result;
    }


    public static boolean isActivityDestroyed(Activity activity) {
        try {
            if (activity == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return activity.isDestroyed() || activity.isFinishing();
            } else {
                return activity.isFinishing();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isActivityDestroyed(SoftReference<Activity> activity) {
        try {
            if (activity == null || activity.get() == null) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return activity.get().isDestroyed() || activity.get().isFinishing();
            } else {
                return activity.get().isFinishing();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void switchMainThread(final BYBaseCallBack ensureListener) {
        try {
            boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
            if (isMainThread) {
                ensureListener.call();
            } else {
                //如果是非主线程，需要强制切换到主线程来进行初始化
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LogUtil.high("[switchMainThread] force to main thread");
                            ensureListener.call();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void removeFromParent(View view) {
        try {
            if (view != null) {
                ViewParent vp = view.getParent();
                if (vp instanceof ViewGroup) {
                    ((ViewGroup) vp).removeView(view);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void autoClose(final View view) {
        try {
            //不执行自动关闭
            int closeDur = AdvanceSetting.getInstance().getSplashPlusAutoClose();
            if (closeDur < 0) {
                return;
            }
            //延迟给定时间，执行移除布局操作
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    AdvanceUtil.removeFromParent(view);
                }
            }, closeDur);
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public static boolean isMainProcess(Context context) {
        boolean result;
        try {
            result = context.getPackageName().equals(getCurrentProcessName(context));
        } catch (Throwable e) {
            e.printStackTrace();
            result = false;
        }
        return result;

    }

    public static String getCurrentProcessName(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return Application.getProcessName();
            } else {
                return getProcessNameByPid(Process.myPid());
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getProcessNameByPid(int pid) {
        try {
            BufferedReader br = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            return br.readLine().replace('\u0000', ' ').trim();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean defaultCheck = true;

    public static boolean addADView(ViewGroup adContainer, View adView) {
        return addADView(adContainer, adView, null, defaultCheck);
    }


    public static boolean addADView(ViewGroup adContainer, View adView, final ViewGroup.LayoutParams params) {
        return addADView(adContainer, adView, params, defaultCheck);
    }

    public static boolean addADView(final ViewGroup adContainer, final View adView, final ViewGroup.LayoutParams params, boolean checkAdViewEmpty) {
        final String tag = "[base addADView] ";
        try {

            if (adContainer == null) {
                String msg = tag + "adContainer 不存在，请先调用setAdContainer() 方法设置adContainer";
                LogUtil.high(msg);
                return false;
            }
            if (adContainer.getChildCount() > 0
                    && adContainer.getChildAt(0) == adView) {
                LogUtil.high(tag + "已添加的布局");
                return true;
            }
            if (adView == null) {
                LogUtil.high(tag + "广告布局为空");
                return false;
            }

            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (adContainer.getChildCount() > 0) {
                        adContainer.removeAllViews();
                    }

                    if (adView.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                    adContainer.setVisibility(View.VISIBLE);
                    LogUtil.max(tag + "add adContainer = " + adContainer.toString());
                    // 广告可见才会产生曝光，否则将无法产生收益。
                    if (params == null) {
                        adContainer.addView(adView);
                    } else {
                        adContainer.addView(adView, params);
                    }
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

    //通过view找到activity
    public static Activity getActivityFromView(View view) {
        try {
            return getActivityFromCtx(view.getContext());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    //通过Context找到activity
    public static Activity getActivityFromCtx(@NonNull Context context) {
        try {
            if (context instanceof Activity) {
                return (Activity) context;
            } else if (context instanceof ContextWrapper) {
                return getActivityFromCtx(((ContextWrapper) context).getBaseContext());
            } else {
                return null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static double caseObjectToDouble(Object obj) {
        double result = 0;
        try {
            if (obj != null) {
                String sResult = "";
                //判断类型，进行转换
                if (obj instanceof Integer) {
                    sResult = (int) obj + "";
                } else if (obj instanceof Double) {
                    sResult = (double) obj + "";
                } else if (obj instanceof Float) {
                    sResult = (float) obj + "";
                } else if (obj instanceof String) {
                    sResult = (String) obj;
                } else if (obj instanceof Long) {
                    sResult = (long) obj + "";
                } else {
                    sResult = obj.toString();
                }
                BigDecimal decimal = new BigDecimal(sResult);
                result = decimal.doubleValue();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;

    }
}
