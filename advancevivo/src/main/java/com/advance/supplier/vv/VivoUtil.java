package com.advance.supplier.vv;

import android.app.Application;
import android.location.Location;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceInitManger;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.vivo.mobilead.manager.VInitCallback;
import com.vivo.mobilead.manager.VivoAdManager;
import com.vivo.mobilead.model.VAdConfig;
import com.vivo.mobilead.model.VCustomController;
import com.vivo.mobilead.model.VLocation;
import com.vivo.mobilead.unified.IBidding;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.annotation.NonNull;

public class VivoUtil {
    public static final String TAG = "[VivoUtil] ";

    public static void initAD(BaseParallelAdapter adapter, AdvanceADNInitResult initResult) {
        String eMsg;
        if (adapter == null) {
            eMsg = TAG + "initAD failed BaseParallelAdapter null";
            LogUtil.e(eMsg);
            if (initResult != null) {
                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
            }
            return;
        }

        SdkSupplier supplier = adapter.sdkSupplier;
        if (supplier == null) {
            eMsg = TAG + "initAD failed supplier null";
            LogUtil.e(eMsg);
            if (initResult != null) {
                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
            }
            return;
        }

        boolean hasInit = AdvanceVivoManager.getInstance().hasInit;
        if (hasInit) {
            LogUtil.simple(TAG + " already init");
            if (initResult != null) {
                initResult.success();
            }
            return;
        }

        //确保线程安全
        AdvanceInitManger.getInstance().initialize(new BYBaseCallBack() {
            @Override
            public void call() {
                initVivo(adapter, initResult);
            }
        });
    }

    private static void initVivo(BaseParallelAdapter adapter, AdvanceADNInitResult initResult) {
        try {
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;

            VAdConfig adConfig = new VAdConfig.Builder()
                    .setMediaId(adapter.getAppID())
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
                            return AdvanceVivoManager.getInstance().allowPersonalRecommend;
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
            Application application = (Application) adapter.getRealContext().getApplicationContext();

            VivoAdManager.getInstance().init(application, adConfig, new VInitCallback() {
                @Override
                public void suceess() {
                    AdvanceVivoManager.getInstance().hasInit = true;
                    LogUtil.simple(TAG + " init suceess");
                    if (initResult != null) {
                        initResult.success();
                    }
                }

                @Override
                public void failed(@NonNull VivoAdError adError) {
                    AdvanceVivoManager.getInstance().hasInit = false;

                    if (initResult != null) {
                        initResult.fail(adError.getCode() + "", adError.getMsg());
                    }
                    //若是报超时错误，则检查是否正常出现广告，如无异常，则是正常的
                    LogUtil.simple(TAG + "failed: " + adError.toString());
                }
            });
        } catch (Throwable e) {
            if (!AdvanceVivoManager.getInstance().hasInit) {
                if (initResult != null) {
                    initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", "initVivo exception");
                }
            }
        }

    }

    public static AdParams.Builder getAdParamsBuilder(BaseParallelAdapter adapter) {
        try {
            SdkSupplier sdkSupplier = adapter.sdkSupplier;
            String adID = sdkSupplier.adspotid;
            AdParams.Builder builder = new AdParams.Builder(adID);
            builder.setWxAppid(AdvanceSetting.getInstance().wxAppId);   //此非必须
            builder.setFetchTimeout(sdkSupplier.timeout);// 允许拉取广告的超时时长：取值范围[3000, 5000]
            return builder;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void handleErr(BaseParallelAdapter adapter, VivoAdError vivoAdError, String defaultErrCode, String defaultErrMsg) {
        try {
            if (vivoAdError != null) {
                int vErrCode = vivoAdError.getCode();
                if (vErrCode > 0) {
                    defaultErrCode = vErrCode + "";
                }
                String vErrMsg = vivoAdError.getMsg();
                if (BYStringUtil.isNotEmpty(vErrMsg)) {
                    defaultErrMsg = vErrMsg;
                }
            }

            if (adapter != null) {
                adapter.handleFailed(defaultErrCode, defaultErrMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getPrice(IBidding bidding) {
        double price = 0;
        try {
            if (bidding != null) {
                price = bidding.getPrice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return price;
    }
}
