package com.advance.utils;

import android.content.Context;

import com.advance.AdvanceDrawSetting;
import com.advance.BannerSetting;
import com.advance.BaseParallelAdapter;
import com.advance.FullScreenVideoSetting;
import com.advance.InterstitialSetting;
import com.advance.NativeExpressSetting;
import com.advance.RewardVideoSetting;
import com.advance.SplashSetting;
import com.advance.core.srender.AdvanceRFBridge;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvanceSupplierBridge;

import java.lang.reflect.Constructor;

/**
 * 通过反射的方式获取对应渠道的各个广告位初始化方法，
 * todo 优化参数，不再传递上下文信息，改为通过 xxxSetting接口来获取上下文信息
 */
public class AdvanceLoader {


    public static String BASE_ADAPTER_PKG_PATH = "com.advance.supplier.";


    public static BaseParallelAdapter getDrawAdapter(String clzName, Context context, AdvanceDrawSetting setting) {
        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initDrawerSetting(setting);
        adapter.initContext(context);
        return adapter;
    }

    public static BaseParallelAdapter getBannerAdapter(String clzName, Context context, BannerSetting setting) {
        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initBannerSetting(setting);
        adapter.initContext(context);
        return adapter;
    }

    public static BaseParallelAdapter getSplashAdapter(String clzName, Context context, SplashSetting setting) {
        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initSplashSetting(setting);
        adapter.initContext(context);
        return adapter;
    }

    public static BaseParallelAdapter getRewardAdapter(String clzName, Context context, RewardVideoSetting setting) {
        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initRewardSetting(setting);
        adapter.initContext(context);
        return adapter;
    }

    public static BaseParallelAdapter getFullVideoAdapter(String clzName, Context context, FullScreenVideoSetting setting) {

        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initFullScreenVideoSetting(setting);
        adapter.initContext(context);
        return adapter;
    }

    public static BaseParallelAdapter getNativeAdapter(String clzName, Context context, NativeExpressSetting setting) {

        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initNativeExpressSetting(setting);
        adapter.initContext(context);
        return adapter;
    }

    public static BaseParallelAdapter getInterstitialAdapter(String clzName, Context context, InterstitialSetting setting) {

        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initInterstitialSetting(setting);
        adapter.initContext(context);
        return adapter;
    }

    //自渲染信息流
    public static BaseParallelAdapter getRenderFeedAdapter(String clzName, Context context, AdvanceRFBridge setting) {

        BaseParallelAdapter adapter = getAdapter(clzName);
        adapter.initNativeSetting(setting);
        adapter.initContext(context);
        return adapter;
    }


    public static AdvanceCustomInit getCustomInit(String clzName) {
        AdvanceCustomInit result = null;
        try {
            Class clz = Class.forName(clzName);
            Constructor cons1 = clz.getConstructor();
            result = (AdvanceCustomInit) cons1.newInstance();
            if (result != null)
                LogUtil.devDebug("getCustomInit result = " + result.toString());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    private static BaseParallelAdapter getAdapter(String clzName){
        BaseParallelAdapter result = null;
        try {
            Class clz = Class.forName(clzName);
            Constructor cons1 = clz.getConstructor();
            result = (BaseParallelAdapter) cons1.newInstance();
            if (result != null)
                LogUtil.devDebug("getAdapter result = " + result.toString());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }


    public static AdvanceSupplierBridge getSupConfig(String clzName) {
        AdvanceSupplierBridge result = null;
        try {
            Class clz = Class.forName(clzName);

            Constructor cons1 = clz.getConstructor();
            result = (AdvanceSupplierBridge) cons1.newInstance();
            if (result != null)
                LogUtil.devDebug("getSupConfig result = " + result);

        } catch (Throwable ignored) {
            LogUtil.high("getSupConfig failed :" + clzName);
        }
        return result;
    }

//    private static BaseParallelAdapter getSDKAdapter(String clzName, Activity activity, Class parClz, Object... parameterTypes) {
//        BaseParallelAdapter result = null;
//        try {
//            Class clz = Class.forName(clzName);
//            Object par1;
//            if (parameterTypes.length > 0) {
//                par1 = parameterTypes[0];
//                Constructor cons1 = clz.getConstructor(Activity.class, parClz);
//                result = (BaseParallelAdapter) cons1.newInstance(activity, par1);
//                if (result != null)
//                    LogUtil.devDebug("getSDKAdapter result = " + result.toString());
//            }
//        } catch (Throwable e) {
//            LogUtil.high("未找到adapter：" + clzName);
//            e.printStackTrace();
//        }
//        return result;
//    }
//
//    /**
//     * 弱引用初始化广告的反射获取方法
//     *
//     * @param clzName
//     * @param activity
//     * @param parClz
//     * @param parameterTypes
//     * @return
//     */
//    private static BaseParallelAdapter getSDKAdapter2(String clzName, SoftReference<Activity> activity, Class parClz, Object... parameterTypes) {
//        BaseParallelAdapter result = null;
//        try {
//            Class clz = Class.forName(clzName);
//            Object par1;
//            if (parameterTypes.length > 0) {
//                par1 = parameterTypes[0];
//                Constructor cons1 = clz.getConstructor(SoftReference.class, parClz);
//                result = (BaseParallelAdapter) cons1.newInstance(activity, par1);
//                if (result != null)
//                    LogUtil.devDebug("getSDKAdapter2 result = " + result.toString());
//            }
//
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }
//
//    /**
//     * context初始化广告的反射获取方法
//     *
//     * @param clzName
//     * @param context
//     * @param parClz
//     * @param parameterTypes
//     * @return
//     */
//    private static BaseParallelAdapter getSDKCtxAdapter(String clzName, Context context, Class parClz, Object... parameterTypes) {
//        BaseParallelAdapter result = null;
//        try {
//            Class clz = Class.forName(clzName);
//            Object par1;
//            if (parameterTypes.length > 0) {
//                par1 = parameterTypes[0];
//                Constructor cons1 = clz.getConstructor(Context.class, parClz);
//                result = (BaseParallelAdapter) cons1.newInstance(context, par1);
//                if (result != null)
//                    LogUtil.devDebug("getSDKCtxAdapter result = " + result.toString());
//            }
//
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }



}
