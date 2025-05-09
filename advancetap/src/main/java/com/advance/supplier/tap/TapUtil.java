package com.advance.supplier.tap;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.tapsdk.tapad.CustomUser;
import com.tapsdk.tapad.TapAdConfig;
import com.tapsdk.tapad.TapAdCustomController;
import com.tapsdk.tapad.TapAdLocation;
import com.tapsdk.tapad.TapAdManager;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapAdSdk;

import java.util.Map;

public class TapUtil {
    public static final String TAG = "[TapUtil] ";

    public synchronized static void initAD(BaseParallelAdapter adapter, final BYBaseCallBack successCall) {
        try {
            if (adapter == null) {
                LogUtil.e(TAG + "initAD failed BaseParallelAdapter null");
                return;
            }

            SdkSupplier supplier = adapter.sdkSupplier;
            if (supplier == null) {

                LogUtil.e(TAG + "initAD failed BaseParallelAdapter null");
                adapter.handleFailed(AdvanceError.ERROR_TAP_INIT, "initAD failed BaseParallelAdapter null");
                return;
            }
            String appId = supplier.mediaid;
            boolean hasInit = AdvanceSetting.getInstance().hasTAPInit;
            String lastAppId = AdvanceSetting.getInstance().lastTAPAID;

            boolean isSame = lastAppId.equals(appId);
            //只有当允许初始化优化时，且快手已经初始化成功过，并行初始化的id和当前id一致，才可以不再重复初始化。
            if (hasInit && isSame && adapter.canOptInit()) {
                LogUtil.simple(TAG + "[initAD] already init");
                if (successCall != null) {
                    successCall.call();
                }
                return;
            }
            Context context = adapter.getRealContext();


            TapAdCustomController customController = null;
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;

            boolean needDelayInitResult = false;
            if (advancePrivacyController != null) {

                //获取用来传递给tap的坐标信息
                double longitude = 0;
                double latitude = 0;
                final double accuracy = 0;
                try {
                    Location cusLocation = advancePrivacyController.getLocation();
                    if (cusLocation != null) {
                        longitude = cusLocation.getLongitude();
                        latitude = cusLocation.getLatitude();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                final TapAdLocation tapAdLocation = new TapAdLocation(latitude, longitude, accuracy);

                //获取用来传递给oaid信息，注意，如果为空，影响第一次的广告填充
                String configOaid = advancePrivacyController.getDevOaid();
                String byOaid = BYDevice.getOaidValue();
                if (BYStringUtil.isEmpty(configOaid)) {
                    LogUtil.devDebug(TAG + "configOaid empty");
                    configOaid = byOaid;
                }
                if (BYStringUtil.isEmpty(configOaid)) {
                    needDelayInitResult = true;
                    LogUtil.high(TAG + "configOaid empty double");
                }
                final String finalConfigOaid = configOaid;

                customController = new TapAdCustomController() {
                    @Override
                    public boolean isCanUseLocation() {
                        return advancePrivacyController.isCanUseLocation();
                    }

                    @Override
                    public TapAdLocation getTapAdLocation() {
                        return tapAdLocation;
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        return advancePrivacyController.isCanUsePhoneState();
                    }

                    @Override
                    public String getDevImei() {
                        return advancePrivacyController.getDevImei();
                    }

                    @Override
                    public boolean isCanUseWifiState() {
                        return advancePrivacyController.isCanUseWifiState();
                    }

                    @Override
                    public boolean isCanUseWriteExternal() {
                        return advancePrivacyController.isCanUseWriteExternal();
                    }

                    @Override
                    public String getDevOaid() {
                        return finalConfigOaid;
                    }

                    @Override
                    public boolean alist() {
                        return advancePrivacyController.alist();
                    }

                    @Override
                    public boolean isCanUseAndroidId() {
                        String cusAID = advancePrivacyController.getDevAndroidID();
                        //如果用户传递为空，或默认未配置，true，允许SDK获取； 若用户配置了id，则不再允许SDK获取
                        return BYStringUtil.isEmpty(cusAID);
                    }

                    @Override
                    public CustomUser provideCustomUser() {
                        return AdvanceTapManger.getInstance().customUser;
                    }
                };
            }


            long mediaId = Long.parseLong(appId);

            TapAdConfig.Builder configBuilder = new TapAdConfig.Builder()
                    .withMediaId(mediaId)
                    .withMediaKey(adapter.getAppKey())
                    .enableDebug(BYUtil.isDebug());
            //通过AdvanceTapManger来自定义配置信息
            String mediaName = "默认Name-Android";
            if (!BYStringUtil.isEmpty(AdvanceTapManger.getInstance().customMediaName)) {
                mediaName = AdvanceTapManger.getInstance().customMediaName;
            }
//            貌似可以任意修改，用户可配置设置
            configBuilder.withMediaName(mediaName);

            String mediaVersion = "1";
            if (!BYStringUtil.isEmpty(AdvanceTapManger.getInstance().customMediaVersion)) {
                mediaVersion = AdvanceTapManger.getInstance().customMediaVersion;
            }
            configBuilder.withMediaVersion(mediaVersion);

            String gameChannel = "taptap2";
            if (!BYStringUtil.isEmpty(AdvanceTapManger.getInstance().customGameChannel)) {
                gameChannel = AdvanceTapManger.getInstance().customGameChannel;
            }
            configBuilder.withGameChannel(gameChannel);

            //可选配置，仅当有数据时才会填入
            if (!BYStringUtil.isEmpty(AdvanceTapManger.getInstance().customTapClientId)) {
                configBuilder.withTapClientId(AdvanceTapManger.getInstance().customTapClientId);
            }


            //自定义授权信息
            if (customController != null) {
                configBuilder.withCustomController(customController);
            }

            TapAdConfig config = configBuilder.build();
            TapAdSdk.init(context, config);
            LogUtil.d(TAG + "[initAD]  init ok");
            AdvanceSetting.getInstance().hasTAPInit = true;
            AdvanceSetting.getInstance().lastTAPAID = appId;

            //todo 注意！！！！因为oaid为空时，若立即加载广告，会导致报9999错误码。
            // SDK实现问题，目前可以通过传递倍业获取的oaid给tap使用，获取不到oaid得再延迟一定时间请求广告，可以尽可能得避免此问题。。。。
            // 待后续版本验证是否有初始化完成得异步回调结果
            if (needDelayInitResult) {
                LogUtil.d(TAG + " needDelayInitResult ");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (successCall != null) {
                            successCall.call();
                        }
                    }
                }, 300);
            } else {
                if (successCall != null) {
                    successCall.call();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    public static int getPlaceId(String idString) {
        int result = 0;
        try {
            if (!BYStringUtil.isEmpty(idString)) {
                result = Integer.parseInt(idString);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public static double getBiddingPrice(Map<String, Object> extraInfo) {
        double result = 0;
        try {
            if (extraInfo != null) {
                Object price = extraInfo.get("bid_price");
                result = AdvanceUtil.caseObjectToDouble(price);
            }
            LogUtil.high(TAG + "--getBiddingPrice-- cpm = " + result + " , extraInfo = " + extraInfo);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public static TapAdNative getTapADManger(Context context) {
        TapAdNative result = null;
        try {
            Activity activity = AdvanceUtil.getActivityFromCtx(context);
            //非activity信息初始化得，不缓存
            if (activity == null) {
                LogUtil.high(TAG + "getTapADManger no activity , create New");
                result = TapAdManager.get().createAdNative(context);
            } else {
                String key = activity.toString();
                //从单例中取值，
                result = AdvanceTapManger.getInstance().tapADMap.get(key);
                if (result == null) {
                    LogUtil.high(TAG + "getTapADManger no save value , create New");
                    //无缓存值，立即初始化，并存储
                    result = TapAdManager.get().createAdNative(context);
                    AdvanceTapManger.getInstance().tapADMap.put(key, result);
                } else {
                    LogUtil.high(TAG + "getTapADManger use saved value ");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return result;
    }


    //广告生命周期结束，需要调用remove 移除TapAdNative实例，避免长期占用
    public static void removeTapMap(Context context) {
        try {
            Activity activity = AdvanceUtil.getActivityFromCtx(context);
            if (activity != null) {
                String key = activity.toString();

                AdvanceTapManger.getInstance().tapADMap.remove(key);
                LogUtil.high(TAG + "remove TapAdNative");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
