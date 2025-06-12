package com.advance.supplier.ks;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.AdvanceSplashPlusManager;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYUtil;
import com.bayes.sdk.basic.widget.BYScheduleTimer;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsCustomController;
import com.kwad.sdk.api.KsInitCallback;
import com.kwad.sdk.api.SdkConfig;

import java.util.List;

public class KSUtil implements AdvanceSplashPlusManager.ZoomCall {

    public static synchronized void initAD(BaseParallelAdapter adapter, final AdvanceADNInitResult initResult) {
        try {
            final String tag = "[KSUtil.initAD] ";
            String eMsg;
            if (adapter == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";
                LogUtil.e(eMsg);
                if (initResult != null) {
                    initResult.fail(AdvanceError.ERROR_KS_INIT, eMsg);
                }
                return;
            }

            SdkSupplier supplier = adapter.sdkSupplier;
            if (supplier == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";

                LogUtil.e(eMsg);
                if (initResult != null) {
                    initResult.fail(AdvanceError.ERROR_KS_INIT, eMsg);
                }
                return;
            }
            String appId = supplier.mediaid;

            boolean hasInit = AdvanceSetting.getInstance().hasKSInit;

            String settingAppId = AdvanceConfig.getInstance().getKsAppId();
            LogUtil.high(tag + "策略服务下发的 快手appId = " + appId + "，本地初始化配置的 快手appId = " + settingAppId);
            if (!TextUtils.isEmpty(settingAppId) && AdvanceConfig.getInstance().forceUseLocalAppID) {
                LogUtil.simple(tag + "强制使用本地初始化配置 appId ： " + settingAppId);
                appId = settingAppId;
            }

            String lastAppId = AdvanceSetting.getInstance().lastKSAID;
            boolean isSame = lastAppId.equals(appId);
            //只有当允许初始化优化时，且快手已经初始化成功过，并行初始化的id和当前id一致，才可以不再重复初始化。
            if (hasInit && isSame && adapter.canOptInit()) {
                LogUtil.simple(tag + " already init");
                if (initResult != null) {
                    initResult.success();
                }
                return;
            }

            Context context = adapter.getRealContext();

            if (TextUtils.isEmpty(appId)) {
                eMsg = tag + " initAD failed ; appId isEmpty";

                LogUtil.e(eMsg);
                if (initResult != null) {
                    initResult.fail(AdvanceError.ERROR_KS_INIT, eMsg);
                }
                return;
            }
            LogUtil.high(tag + "init start ,appId = " + appId);

            //检查内部初始化状态，更新状态返回结果
            int innerInitStatus = AdvanceKSManager.getInstance().innerInitStatus;
            if (innerInitStatus == AdvanceKSManager.INIT_STATUS_SUCCESS) {
                LogUtil.d(tag + "inner init success");

                if (initResult != null) {
                    initResult.success();
                }
                AdvanceSetting.getInstance().hasKSInit = true;
                return;
            } else if (innerInitStatus == AdvanceKSManager.INIT_STATUS_FAILED) {
                LogUtil.d(tag + "inner init failed");

                if (initResult != null) {
                    initResult.fail(AdvanceKSManager.getInstance().innerInitErrMsg, AdvanceKSManager.getInstance().innerInitErrMsg);
                }

                return;
            } else if (innerInitStatus == AdvanceKSManager.INIT_STATUS_CALLING) {
                //如果外部已经调用过init方法，但是还没有结果时，需要等待结果状态返回
                LogUtil.d(tag + "inner init calling ，waiting result check");
                //每N毫秒轮询一次结果
                final boolean[] hasCallBack = {false};
                //轮询最大等待时间，超时后不再关注结果，按照succ处理
                final long scheduleTimeout = 2500L;
                final long startTime = System.currentTimeMillis();
                new BYScheduleTimer(50, new BYAbsCallBack<BYScheduleTimer>() {
                    @Override
                    public void invoke(BYScheduleTimer byScheduleTimer) {
                        try {
                            long cost = System.currentTimeMillis() - startTime;
                            LogUtil.d(tag + "result check running，cost：" + cost);

                            //检查是否超时，如果超时了，按照初始化成功处理，执行后面的广告load
                            if (cost > scheduleTimeout) {
                                LogUtil.d(tag + "result check timeout");

                                if (!hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    if (initResult != null)
                                        initResult.success();
                                }
                                AdvanceSetting.getInstance().hasKSInit = true;

                                if (byScheduleTimer != null) {
                                    byScheduleTimer.cancel();
                                }
                                return;
                            }

                            //检查结果状态
                            int innerInitStatus = AdvanceKSManager.getInstance().innerInitStatus;
                            if (innerInitStatus == AdvanceKSManager.INIT_STATUS_SUCCESS) {
                                LogUtil.d(tag + "result check: inner init success");

                                if (!hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    if (initResult != null)
                                        initResult.success();
                                }
                                AdvanceSetting.getInstance().hasKSInit = true;

                                if (byScheduleTimer != null) {
                                    byScheduleTimer.cancel();
                                }
                            } else if (innerInitStatus == AdvanceKSManager.INIT_STATUS_FAILED) {
                                LogUtil.d(tag + "result check: inner init failed");

                                if (!hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    if (initResult != null)
                                        initResult.fail(AdvanceKSManager.getInstance().innerInitErrCode + "", AdvanceKSManager.getInstance().innerInitErrMsg);
                                }
                                if (byScheduleTimer != null) {
                                    byScheduleTimer.cancel();
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                return;
            }


            KsCustomController customController = null;
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
            if (advancePrivacyController != null) {
                customController = new KsCustomController() {
                    @Override
                    public boolean canReadLocation() {
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
                    public boolean canUsePhoneState() {
                        return advancePrivacyController.isCanUsePhoneState();
                    }

                    @Override
                    public String getImei() {
                        return advancePrivacyController.getDevImei();
                    }

                    @Override
                    public String[] getImeis() {
                        return advancePrivacyController.getImeis();
                    }

                    @Override
                    public String getAndroidId() {
                        return advancePrivacyController.getDevAndroidID();
                    }

                    @Override
                    public boolean canUseOaid() {
                        return advancePrivacyController.canUseOaid();
                    }

                    @Override
                    public String getOaid() {
                        return advancePrivacyController.getDevOaid();
                    }

                    @Override
                    public boolean canUseMacAddress() {
                        return advancePrivacyController.canUseMacAddress();
                    }

                    @Override
                    public String getMacAddress() {
                        return advancePrivacyController.getDevMac();
                    }

                    @Override
                    public boolean canUseNetworkState() {
                        return advancePrivacyController.canUseNetworkState();
                    }

                    @Override
                    public boolean canUseStoragePermission() {
                        return advancePrivacyController.isCanUseWriteExternal();
                    }

                    @Override
                    public boolean canReadInstalledPackages() {
                        return advancePrivacyController.alist();
                    }

                    @Override
                    public List<String> getInstalledPackages() {
                        return advancePrivacyController.getInstalledPackages();
                    }
                };
            }
            SdkConfig.Builder builder = new SdkConfig.Builder();
            builder.appId(appId)// aapId，请联系快手平台申请正式AppId，必填
                    .showNotification(true) // 是否展示下载通知栏
                    .customController(customController)
                    .debug(BYUtil.isDebug());
            String appName = AdvanceConfig.getInstance().getKsAppName();
            if (!TextUtils.isEmpty(appName)) {
                builder.appName(appName);// appName，请填写您应用的名称，非必填
            }

            String appKey = AdvanceConfig.getInstance().getKsAppKey();
            if (!TextUtils.isEmpty(appKey)) {
                builder.appKey(appKey);// 直播sdk安全验证，接入直播模块必填
            }
            String appWebKey = AdvanceConfig.getInstance().getKsAppWebKey();
            if (!TextUtils.isEmpty(appWebKey)) {
                builder.appWebKey(appWebKey);// 直播sdk安全验证，接入直播模块必填
            }
            builder.setInitCallback(new KsInitCallback() {
                @Override
                public void onSuccess() {
                    LogUtil.simple("[KSUtil.initAD] InitCallback onSuccess");

                }

                @Override
                public void onFail(int i, String s) {
                    String eMsg = "[KSUtil.initAD] InitCallback failed ; onFail";
                    AdvanceSetting.getInstance().hasKSInit = false;
                    //内部失败状态标记
                    AdvanceKSManager.getInstance().innerInitStatus = AdvanceKSManager.INIT_STATUS_FAILED;
                    AdvanceKSManager.getInstance().innerInitErrCode = i;
                    AdvanceKSManager.getInstance().innerInitErrMsg = s;

                    LogUtil.e(eMsg);
                    if (initResult != null) {
                        initResult.fail(i + "", eMsg);
                    }
                }
            });
            final String finalAppId = appId;
            builder.setStartCallback(new KsInitCallback() {
                @Override
                public void onSuccess() {
                    LogUtil.simple("[KSUtil.initAD] StartCallback onSuccess");
                    AdvanceKSManager.getInstance().innerInitStatus = AdvanceKSManager.INIT_STATUS_SUCCESS;
                    LogUtil.devDebug(tag + "AdvanceKSManager.getInstance().innerInitStatus 3 = " + AdvanceKSManager.getInstance().innerInitStatus);

                    AdvanceSetting.getInstance().lastKSAID = finalAppId;
                    AdvanceSetting.getInstance().hasKSInit = true;

                    if (initResult != null) {
                        initResult.success();
                    }
                }

                @Override
                public void onFail(int i, String s) {
                    String eMsg = "[KSUtil.initAD] StartCallback failed ; onFail";
                    //内部失败状态标记
                    AdvanceKSManager.getInstance().innerInitStatus = AdvanceKSManager.INIT_STATUS_FAILED;
                    AdvanceKSManager.getInstance().innerInitErrCode = i;
                    AdvanceKSManager.getInstance().innerInitErrMsg = s;

                    AdvanceSetting.getInstance().hasKSInit = false;

                    LogUtil.e(eMsg);
                    if (initResult != null) {
                        initResult.fail(i + "", eMsg);
                    }
                }
            });

            // 建议只在需要的进程初始化SDK即可，如主进程
            KsAdSDK.init(context, builder.build());
            KsAdSDK.start();
            LogUtil.high(tag + "init call end  ");
            LogUtil.devDebug(tag + "AdvanceKSManager.getInstance().innerInitStatus = " + AdvanceKSManager.getInstance().innerInitStatus);
            if (AdvanceKSManager.getInstance().innerInitStatus != AdvanceKSManager.INIT_STATUS_FAILED && AdvanceKSManager.getInstance().innerInitStatus != AdvanceKSManager.INIT_STATUS_SUCCESS) {
                AdvanceKSManager.getInstance().innerInitStatus = AdvanceKSManager.INIT_STATUS_CALLING;
            }
            LogUtil.devDebug(tag + "AdvanceKSManager.getInstance().innerInitStatus 2 = " + AdvanceKSManager.getInstance().innerInitStatus);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static long getADID(SdkSupplier supplier) {
        long id = -1;
        try {
            id = Long.parseLong(supplier.adspotid);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return id;
    }


    @Override
    public void zoomOut(Activity activity) {

    }
}
