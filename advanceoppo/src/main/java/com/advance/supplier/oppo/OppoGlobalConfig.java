package com.advance.supplier.oppo;

import android.content.Context;

import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.heytap.msp.mobad.api.InitParams;
import com.heytap.msp.mobad.api.MobAdManager;
import com.heytap.msp.mobad.api.MobCustomController;
import com.heytap.msp.mobad.api.listener.IInitListener;

import java.util.Map;

public class OppoGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {

//            初始化配置参数
        InitParams.Builder builder = new InitParams.Builder()
                .setDebug(BYUtil.isDebug());

//            个人信息控制器
        final AdvancePrivacyController advancePrivacyController = getPrivacyController();
        if (advancePrivacyController != null) {
            builder.setMobCustomController(new MobCustomController() {
                @Override
                public String getDevImei() {
                    return advancePrivacyController.getDevImei();
                }

                @Override
                public boolean isCanUseLocation() {
                    return advancePrivacyController.isCanUseLocation();
                }

                @Override
                public LocationProvider getLocation() {
                    if (advancePrivacyController.getLocation() != null) {
                        return new LocationProvider() {
                            @Override
                            public double getLatitude() {
                                return advancePrivacyController.getLocation().getLatitude();
                            }

                            @Override
                            public double getLongitude() {
                                return advancePrivacyController.getLocation().getLongitude();
                            }
                        };
                    } else {
                        return super.getLocation();
                    }
                }

                @Override
                public boolean isCanUsePhoneState() {
                    return advancePrivacyController.isCanUsePhoneState();
                }

                @Override
                public boolean isCanUseAndroidId() {
                    String setAid = advancePrivacyController.getDevAndroidID();
                    //未赋值用默认设置
                    if (BYStringUtil.isEmpty(setAid)) {
                        return super.isCanUseAndroidId();
                    } else { //已赋值不再允许SDK获取
                        return false;
                    }
                }

                @Override
                public String getAndroidId() {
                    return advancePrivacyController.getDevAndroidID();
                }

                @Override
                public boolean isCanUseWifiState() {
                    return advancePrivacyController.isCanUseWifiState();
                }

                @Override
                public String getMacAddress() {
                    return advancePrivacyController.getDevMac();
                }

                @Override
                public boolean isCanUseWriteExternal() {
                    return advancePrivacyController.isCanUseWriteExternal();
                }

                @Override
                public boolean alist() {
                    return advancePrivacyController.alist();
                }
            });
        }

        //个性化开关；true-开启个性化开关，false-关闭个性化开关
        builder.setAppOUIDStatus(isPersonalRecommend());

        MobAdManager.getInstance().init(context, getAppID(), builder.build(), new IInitListener() {
            @Override
            public void onSuccess() {
                LogUtil.simple("[OppoUtil] init onSuccess");

                callInitSuccess();
                //  2025/2/19  测试不在成功回调后调用广告展示，是否会有什么问题。
                //  测试结果：无问题，基本上为同步返回结果，即便是初始化失败了，调用请求时也会返回广告失败，目前流程上无影响
            }

            @Override
            public void onFailed(String s) {
                LogUtil.e("[OppoUtil] init onFailed , " + s);

                callInitFail(AdvanceError.ERROR_INIT_DEFAULT+"", s);
            }
        });
        LogUtil.simple("[OppoUtil] init end");
    }

    @Override
    public String getSDKVersion() {
        String v = "";
        try {
            v = MobAdManager.getInstance().getSdkVerName();
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
