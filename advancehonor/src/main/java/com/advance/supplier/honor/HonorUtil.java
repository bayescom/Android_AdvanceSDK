package com.advance.supplier.honor;

import android.content.Context;
import android.location.Location;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceInitManger;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYUtil;
import com.hihonor.adsdk.base.HnAds;
import com.hihonor.adsdk.base.api.BaseExpressAd;
import com.hihonor.adsdk.base.init.HnAdConfig;
import com.hihonor.adsdk.base.init.HnCustomController;

public class HonorUtil {
    public static void initAD(BaseParallelAdapter adapter) {

        try {
            final String tag = "[HonorUtil.initAD] ";
            String eMsg;
            if (adapter == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";
                LogUtil.e(eMsg);
//                if (initResult != null) {
//                    initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
//                }
                return;
            }

            SdkSupplier supplier = adapter.sdkSupplier;
            if (supplier == null) {
                eMsg = tag + "initAD failed supplier null";
                LogUtil.e(eMsg);
                return;
            }

            boolean hasInit = AdvanceHonorManager.getInstance().hasInit;
            if (hasInit) {
                LogUtil.simple(tag + " already init");
//                if (initResult != null) {
//                    initResult.success();
//                }
                return;
            }

// TODO: 2025/10/22 可能会出现未初始化情况？视情况，改为回调初始化结果方式
            //确保线程安全
            AdvanceInitManger.getInstance().initialize(new BYBaseCallBack() {
                @Override
                public void call() {
                    //获取自定义参数
                    HnCustomController customController = generateCustomController();
                    //执行初始化

                    // 构造广告配置
                    HnAdConfig config = new HnAdConfig.Builder()
                            // 设置您的媒体id，媒体id是您在荣耀广告平台注册的媒体id
                            .setAppId(supplier.mediaid)
                            // 设置您的appKey，appKey是您在荣耀广告平台注册的媒体id对应的密钥:
                            .setAppKey(supplier.mediakey)
                            // 媒体多进程场景需要设置该参数为true，非多进程场景不需要设置。
                            .setSupportMultiProcess(false)
                            // 如果您的推广目标有小程序推广的话，此处需要设置。
                            .setWxOpenAppId(AdvanceSetting.getInstance().wxAppId)
                            .setCustomController(customController)
                            // 设置激励监听
//                            .setRewardListener(new HnRewardListener() {
//                                @Override
//                                public void onReward(Bundle bundle) {
//                                    // 激励回调
//                                }
//                            })
                            .build();
                    // 调用初始化接口 context 与 config 不能为null，否则将会抛出异常
                    Context context;
                    if (adapter != null) {
                        context = adapter.getRealContext();
                    } else {
                        context = BYUtil.getCtx();
                    }
                    LogUtil.simple(tag+" 开始初始化");
                    HnAds.get().init(context, config);
                    AdvanceHonorManager.getInstance().hasInit = true;
                    LogUtil.simple(tag+" 初始化完成");

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    private static HnCustomController generateCustomController() {
        HnCustomController result = null;

        try {
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;

            if (advancePrivacyController != null) {
                result = new HnCustomController() {

                    @Override
                    public boolean isCanUseLocation() {
                        return advancePrivacyController.isCanUseLocation();
                    }

                    @Override
                    public Location getLocation() {
                        if (advancePrivacyController.getLocation() != null) {
                            Location la = new Location("ADVCusLocation");
                            la.setLatitude(advancePrivacyController.getLocation().getLatitude());
                            la.setLongitude(advancePrivacyController.getLocation().getLongitude());
                            return la;
                        } else {
                            return super.getLocation();
                        }
                    }


                    @Override
                    public boolean isCanGetAllPackages() {
                        return advancePrivacyController.alist();
                    }
                };
            }
        } catch (Throwable e) {

        }

        return result;

    }

    public static double getECPM(BaseExpressAd expressAd) {
        double result = 0;
        try {
            if (expressAd != null) {
                int mode = expressAd.getMediaBidMode();
                if (mode != 0) {
                    long ecpm = expressAd.getEcpm();
                    if (ecpm > 0) {
                        result = ecpm;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean isAdExpire(BaseExpressAd baseExpressAd) {
        try {
            //过期的时间戳。单位秒
            long expireSec = baseExpressAd.getExpirationTime();

            long currentSec = System.currentTimeMillis() / 1000;
            return expireSec < currentSec;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;


    }
}
