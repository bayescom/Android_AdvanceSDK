package com.advance.supplier.vv;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.vivo.mobad.BuildConfig;
import com.vivo.mobilead.manager.VInitCallback;
import com.vivo.mobilead.manager.VivoAdManager;
import com.vivo.mobilead.model.VAdConfig;
import com.vivo.mobilead.model.VCustomController;
import com.vivo.mobilead.model.VLocation;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.annotation.NonNull;

import java.util.Map;

public class VivoGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        final AdvancePrivacyController advancePrivacyController = getPrivacyController();

        VAdConfig adConfig = new VAdConfig.Builder()
                .setMediaId(getAppID())
                .setDebug(BYUtil.isDebug())  //是否开启日志输出
                .setCustomController(new VCustomController() {
                    @Override
                    public boolean isCanUseLocation() {
                        if (advancePrivacyController != null) {
                            return advancePrivacyController.isCanUseLocation();
                        }
                        //是否允许获取位置信息，默认允许
                        return true;
                    }

                    @Override
                    public VLocation getLocation() {
                        if (advancePrivacyController != null) {
                            Location aLocation = advancePrivacyController.getLocation();
                            if (aLocation != null) {
                                return new VLocation(aLocation.getLongitude(), aLocation.getLatitude());
                            }
                        }

                        //若不允许获取位置信息，亦可主动传给SDK位置信息
                        return null;
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        if (advancePrivacyController != null) {
                            return advancePrivacyController.isCanUsePhoneState();
                        }
                        //是否允许获取imei信息，默认允许
                        return true;
                    }

                    @Override
                    public String getImei() {
                        if (advancePrivacyController != null) {
                            return advancePrivacyController.getDevImei();
                        }
                        //若不允许获取imei信息，亦可主动传给SDK imei信息
                        return null;
                    }

                    @Override
                    public String getOaid() {
                        if (advancePrivacyController != null) {
                            return advancePrivacyController.getDevOaid();
                        }
                        //传入获取到的oaia
                        return "";
                    }

//                        @Override
//                        public boolean isCanUseWifiState() {
//                            //是否允许获取网络信息（mac、ip等），默认允许
//                            return true;
//                        }

                    @Override
                    public boolean isCanUseWriteExternal() {
//                            if (advancePrivacyController!=null){
//                                return  advancePrivacyController.();
//                            }
                        //是否允许SDK使用公共存储空间
                        return true;
                    }

                    @Override
                    public boolean isCanPersonalRecommend() {
//                            if (advancePrivacyController!=null){
//                                return  advancePrivacyController.();
//                            }
                        //是否允许推荐个性化广告
                        return isPersonalRecommend();
                    }

                    @Override
                    public boolean isCanUseImsi() {
                        if (advancePrivacyController != null) {
                            return advancePrivacyController.isCanUsePhoneState();
                        }
                        //是否允许获取imsi
                        return true;
                    }

                    @Override
                    public boolean isCanUseApplist() {
                        if (advancePrivacyController != null) {
                            return advancePrivacyController.alist();
                        }
                        //是否允许获取手机安装应用列表
                        return true;
                    }
                }).build();


        // 这里完成SDK的初始化
        Application application = (Application) context.getApplicationContext();

        VivoAdManager.getInstance().init(application, adConfig, new VInitCallback() {
            @Override
            public void suceess() {
                LogUtil.simple(TAG + " init suceess");

                callInitSuccess();
            }

            @Override
            public void failed(@NonNull VivoAdError adError) {

                callInitFail(adError.getCode() + "", adError.getMsg());
                //若是报超时错误，则检查是否正常出现广告，如无异常，则是正常的
                LogUtil.simple(TAG + "failed: " + adError.toString());
            }
        });
    }

    @Override
    public String getSDKVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {

    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }

}
