package com.advance.supplier.csj;

import android.content.Context;
import android.os.Looper;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.bytedance.sdk.openadsdk.LocationProvider;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Map;

public class CsjGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(final Context context, Map<String, Object> serverExtra) {
        boolean supportMP = AdvanceConfig.getInstance().getSupportMultiProcess();
        int[] directDownloadNetworkType = AdvanceConfig.getInstance().getCsjDirectDownloadNetworkType();

        //如果未设置下载状态集合，默认4g和wifi下可以下载。
        if (directDownloadNetworkType == null || directDownloadNetworkType.length == 0) {
            directDownloadNetworkType = new int[]{TTAdConstant.NETWORK_STATE_4G, TTAdConstant.NETWORK_STATE_5G, TTAdConstant.NETWORK_STATE_WIFI};
        }

        boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
        if (!isMainThread) {
            LogUtil.high("[CsjUtil.initCsj]需要在主线程中调用穿山甲sdk 初始化方法");
        } else {
            LogUtil.high("[CsjUtil.initCsj]当前在主线程中调用穿山甲sdk 初始化方法");
        }
        LogUtil.high("[CsjUtil.initCsj] supportMultiProcess = " + supportMP + " directDownloadNetworkType = " + Arrays.toString(directDownloadNetworkType));

        TTCustomController ttCustomController = null;
        final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
        if (advancePrivacyController != null) {
            ttCustomController = new TTCustomController() {
                @Override
                public boolean isCanUseLocation() {
                    return advancePrivacyController.isCanUseLocation();
                }

                @Override
                public LocationProvider getTTLocation() {
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
                        return super.getTTLocation();
                    }
                }

                @Override
                public boolean alist() {
                    return advancePrivacyController.alist();
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
                public String getMacAddress() {
                    return advancePrivacyController.getDevMac();
                }

                @Override
                public boolean isCanUseWriteExternal() {
                    return advancePrivacyController.isCanUseWriteExternal();
                }

                @Override
                public String getDevOaid() {
                    return advancePrivacyController.getDevOaid();
                }
            };
        }

        TTAdConfig.Builder ttBuilder = new TTAdConfig.Builder().appId(getAppID())
                .debug(BYUtil.isDebug()) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                .appName(AdvanceConfig.getInstance().getAppName());
        try { //避免部分配置被突然移除，导致初始化异常
            ttBuilder
//                        .useTextureView(true) //使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                    .titleBarTheme(TTAdConstant.TITLE_BAR_THEME_LIGHT)
                    .allowShowNotify(true) //是否允许sdk展示通知栏提示
                    // .allowShowPageWhenScreenLock(true) //是否在锁屏场景支持展示广告落地页
                    .directDownloadNetworkType(directDownloadNetworkType) //允许直接下载的网络状态集合
                    .supportMultiProcess(supportMP) //是否支持多进程，true支持
                    .customController(ttCustomController);
            //                    .asyncInit(true) //如果是主线程使用异步
        } catch (Throwable e) {
            e.printStackTrace();
        }
        final TTAdConfig config = ttBuilder.build();

        //主线程和非主线程逻辑分开
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {

                TTAdSdk.init(context.getApplicationContext(), config);
                TTAdSdk.start(new TTAdSdk.Callback() {
                    @Override
                    public void success() {
                        LogUtil.simple("csj init success");


                        callInitSuccess();
                    }

                    @Override
                    public void fail(int code, String msg) {
                        LogUtil.e("csj init fail : code = " + code + " msg = " + msg);

                        callInitFail(code + "", msg);

                    }
                });
            }
        });

    }

    @Override
    public String getSDKVersion() {
        String csjV = "";
        try {
            csjV = TTAdSdk.getAdManager().getSDKVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return csjV;
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        personalRecommendChangeCSJ(isPersonalRecommend);
    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }


    //个性化广告推荐配置
    private void personalRecommendChangeCSJ(boolean allow) {
        try {
            String personalTypeValue;
            if (allow) {
                personalTypeValue = "1";
            } else {
                personalTypeValue = "0";
            }

//            TTVfConfig ttAdConfig = new TTVfConfig.Builder()
//                    .data(getData(personalTypeValue))
//                    .build();
//            TTVfSdk.updateAdConfig(ttAdConfig);

            TTAdConfig ttAdConfig = new TTAdConfig.Builder()
                    .data(getData(personalTypeValue))
                    .build();
            TTAdSdk.updateAdConfig(ttAdConfig);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private String getData(String personalTypeValue) {
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject personalObject = new JSONObject();
            personalObject.putOpt("personal_ads_type", personalTypeValue);
            jsonArray.put(personalObject);
            return jsonArray.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }
}
