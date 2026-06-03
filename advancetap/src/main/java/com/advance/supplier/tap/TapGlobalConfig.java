package com.advance.supplier.tap;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.tapsdk.tapad.CustomUser;
import com.tapsdk.tapad.TapAdConfig;
import com.tapsdk.tapad.TapAdCustomController;
import com.tapsdk.tapad.TapAdLocation;
import com.tapsdk.tapad.TapAdSdk;

import java.util.Map;

public class TapGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {

        TapAdCustomController customController = null;
        final AdvancePrivacyController advancePrivacyController = getPrivacyController();

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


        long mediaId = Long.parseLong(getAppID());

        TapAdConfig.Builder configBuilder = new TapAdConfig.Builder()
                .withMediaId(mediaId)
                .withMediaKey(getAppKey())
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

        //todo 注意！！！！因为oaid为空时，若立即加载广告，会导致报9999错误码。
        // SDK实现问题，目前可以通过传递倍业获取的oaid给tap使用，获取不到oaid得再延迟一定时间请求广告，可以尽可能得避免此问题。。。。
        // 待后续版本验证是否有初始化完成得异步回调结果
        if (needDelayInitResult) {
            LogUtil.d(TAG + " needDelayInitResult ");
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                   callInitSuccess();
                }
            }, 300);
        } else {
            callInitSuccess();
        }
    }

    @Override
    public String getSDKVersion() {
        String v = "";
        try {
            v = com.tapsdk.tapad.BuildConfig.VERSION_NAME;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {

    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }
}

