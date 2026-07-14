package com.advance.supplier.noah;

import android.app.Application;
import android.content.Context;

import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.noah.api.GlobalConfig;
import com.noah.api.InitCallback;
import com.noah.api.NoahSdk;
import com.noah.api.NoahSdkConfig;

import java.util.Map;

public class NoahInit extends AdvanceCustomInit {
    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        AdvancePrivacyController controller = getPrivacyController();

        NoahSdkConfig.Builder builder = new NoahSdkConfig.Builder()
                //填平台上申请的AppId
                .setAppKey(getAppID())
                .setOuterSettings(new NoahSdkConfig.NoahOuterSettings() {
                    /**
                     * 必须返回oaid，缺失oaid会严重影响广告投放效果,
                     例如不填充广告
                     * 如果是荣耀手机同时有华为的oaid和荣耀的oaid的情
                     况，getOAID返回华为的oaid, 下面的getOAID2接口返回荣耀的oaid
                     */
                    @Override
                    public String getOAID() {
                        try {
                            if (controller != null && controller.canUseOaid()) {
                                String setOaid = controller.getDevOaid();
                                if (BYStringUtil.isNotEmpty(setOaid)) {
                                    return setOaid;
                                } else {
                                    return BYDevice.getOaidValue();
                                }
                            }
                        } catch (Exception e) {
                        }
                        return "";
                    }

                    /**
                     * 如果是荣耀手机同时有华为的oaid和荣耀的oaid的情
                     况，这个接口返回荣耀的oaid, 上面的getOAID接口返回华为的oai
                     * 只有一个oaid的设备，getOAID2接口可返回空
                     */
                    @Override
                    public String getOAID2() {
                        return "";
                    }
                });

        float lat = 0;
        float lng = 0;
        if (controller!=null){
            boolean canUseLat = controller.isCanUseLocation();
            builder.setUseLocation(canUseLat);
            if ( controller.getLocation()!=null){
                lat = (float) controller.getLocation().getLatitude();
                lng = (float) controller.getLocation().getLongitude();
            }
            if (lat >0 ){
                builder.setLantitude(lat);
            }
            if (lng>0){
                builder.setLongtitude(lng);
            }

        }


        builder.setUseHttps(AdvanceSetting.getInstance().useHttps);

        NoahSdkConfig config =builder.build();
        GlobalConfig globalConfig = GlobalConfig.newBuilder()
                .setDebug(BYUtil.isDebug()) //需要看SDK日志可 以打开这个开关，正式包务必设置为false
                .build();

        Application application = (Application) context.getApplicationContext();
        NoahSdk.initAsync(application, config, globalConfig, new InitCallback() {
            @Override
            public void success() {
                callInitSuccess();
            }

            @Override
            public void fail(int i, String s) {

                callInitFail(i+"",s);
            }
        });
    }

    @Override
    public String getSDKVersion() {
        return NoahSdk.getSdkVersionName();
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {

    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }
}
