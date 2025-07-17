package com.advance.supplier.csj;


import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvancePrivacyController;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceSplashPlusManager;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.bayes.sdk.basic.widget.BYScheduleTimer;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
//import com.bytedance.sdk.openadsdk.ISplashClickEyeListener;
import com.bytedance.sdk.openadsdk.LocationProvider;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;
//import com.bytedance.sdk.openadsdk.TTSplashAd;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Map;

public class CsjUtil implements AdvanceSplashPlusManager.ZoomCall {

    /**
     * 穿山甲3450以后版本初始化方法，支持异步初始化以及对应回调方法
     *
     * @param adapter  渠道基础适配器
     * @param listener 初始化回调
     */
    public static synchronized void initCsj(final BaseParallelAdapter adapter, final InitListener listener) {
        try {
            final String tag = "[CsjUtil.initCsj] ";
            //检查是否有外部APP自己得初始化状态，有任何状态返回
            int customInitStatus = AdvanceCsjManager.get().initStatus;
            if (customInitStatus == AdvanceCsjManager.INIT_STATUS_SUCCESS) {
                LogUtil.d(tag + "outer init success");

                if (listener != null) {
                    listener.success();
                }
                AdvanceSetting.getInstance().hasCSJInit = true;
                return;
            } else if (customInitStatus == AdvanceCsjManager.INIT_STATUS_FAILED) {
                LogUtil.d(tag + "outer init failed");

                if (listener != null) {
                    listener.fail(AdvanceCsjManager.get().initErrCode, AdvanceCsjManager.get().initErrMsg);
                }

                return;
            } else if (customInitStatus == AdvanceCsjManager.INIT_STATUS_CALLING) {
                //如果外部已经调用过init方法，但是还没有结果时，需要等待结果状态返回
                LogUtil.d(tag + "outer init calling ，waiting result check");
                //每N毫秒轮询一次结果
                final boolean[] hasCallBack = {false};
                //轮询最大等待时间，超时后不再关注结果，按照succ处理
                final long scheduleTimeout = 1500L;
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

                                if (listener != null && !hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    listener.success();
                                }
                                AdvanceSetting.getInstance().hasCSJInit = true;

                                if (byScheduleTimer != null) {
                                    byScheduleTimer.cancel();
                                }
                                return;
                            }

                            //检查结果状态
                            int outerInitStatus = AdvanceCsjManager.get().initStatus;
                            if (outerInitStatus == AdvanceCsjManager.INIT_STATUS_SUCCESS) {
                                LogUtil.d(tag + "result check: outer init success");

                                if (listener != null && !hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    listener.success();
                                }
                                AdvanceSetting.getInstance().hasCSJInit = true;

                                if (byScheduleTimer != null) {
                                    byScheduleTimer.cancel();
                                }
                            } else if (outerInitStatus == AdvanceCsjManager.INIT_STATUS_FAILED) {
                                LogUtil.d(tag + "result check: outer init failed");

                                if (listener != null && !hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    listener.fail(AdvanceCsjManager.get().initErrCode, AdvanceCsjManager.get().initErrMsg);
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

            boolean hasInit = AdvanceSetting.getInstance().hasCSJInit;
            if (hasInit) {
                LogUtil.simple("[CsjUtil.initCsj] already init");
                if (listener != null) {
                    listener.success();
                }
                return;
            }

            if (adapter == null) {
                String msg = "[initCsj] initAD failed BaseParallelAdapter null";
                if (listener != null) {
                    listener.fail(AdvanceError.ERROR_INIT_DEFAULT, msg);
                }
                return;
            }
            String appID = "";
            if (adapter.sdkSupplier != null) {
                appID = adapter.sdkSupplier.mediaid;
            }

            String resultAppID = appID;
            String configAppId = AdvanceConfig.getInstance().getCsjAppId();
            //检测appid 账号配置
            if (AdvanceConfig.getInstance().forceUseLocalAppID && !TextUtils.isEmpty(configAppId)) {
                LogUtil.simple("[CsjUtil.initCsj] 强制使用本地配置的穿山甲 AppID");
                resultAppID = configAppId;
            }
            //mock测试 appid
            if (BYUtil.isDev()) {
//           客户端 bidding 测试媒体
//                resultAppID = "5412264";

//                resultAppID = "5669453"; //维度为来
//                resultAppID = "5412551"; //ceshi
//                resultAppID = "5001121"; //demo id
            }

            LogUtil.high("[CsjUtil.initCsj] 穿山甲 appID：" + resultAppID);

            String lastAppId = AdvanceSetting.getInstance().lastCSJAID;
            boolean isSame = lastAppId.equals(resultAppID);


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

            TTAdConfig.Builder ttBuilder = new TTAdConfig.Builder().appId(resultAppID)
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
            final String finalResultAppID = resultAppID;
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    doInit(adapter.getRealContext(), config, listener, finalResultAppID);
                }
            });

        } catch (Throwable e) {
            String msg = "穿山甲sdk 初始化异常";
            LogUtil.e(msg);
            e.printStackTrace();
            if (listener != null) {
                listener.fail(AdvanceError.ERROR_INIT_DEFAULT, msg);
            }
        }
    }

    private static synchronized void doInit(Context context, TTAdConfig config, final InitListener listener, final String appID) {
        try {
            final String tag = "[CsjUtil.initCsj] doInit ";
            LogUtil.high(tag + " start ");

            //检查内部初始化状态，更新状态返回结果
            int innerInitStatus = AdvanceCsjManager.get().innerInitStatus;
            if (innerInitStatus == AdvanceCsjManager.INIT_STATUS_SUCCESS) {
                LogUtil.d(tag + "inner init success");

                if (listener != null) {
                    listener.success();
                }
                AdvanceSetting.getInstance().hasCSJInit = true;
                return;
            } else if (innerInitStatus == AdvanceCsjManager.INIT_STATUS_FAILED) {
                LogUtil.d(tag + "inner init failed");

                if (listener != null) {
                    listener.fail(AdvanceCsjManager.get().innerInitErrCode, AdvanceCsjManager.get().innerInitErrMsg);
                }

                return;
            } else if (innerInitStatus == AdvanceCsjManager.INIT_STATUS_CALLING) {
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
                                    if (listener != null)
                                        listener.success();
                                }
                                AdvanceSetting.getInstance().hasCSJInit = true;

                                if (byScheduleTimer != null) {
                                    byScheduleTimer.cancel();
                                }
                                return;
                            }

                            //检查结果状态
                            int innerInitStatus = AdvanceCsjManager.get().innerInitStatus;
                            if (innerInitStatus == AdvanceCsjManager.INIT_STATUS_SUCCESS) {
                                LogUtil.d(tag + "result check: inner init success");

                                if (!hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    if (listener != null)
                                        listener.success();
                                }
                                AdvanceSetting.getInstance().hasCSJInit = true;

                                if (byScheduleTimer != null) {
                                    byScheduleTimer.cancel();
                                }
                            } else if (innerInitStatus == AdvanceCsjManager.INIT_STATUS_FAILED) {
                                LogUtil.d(tag + "result check: inner init failed");

                                if (!hasCallBack[0]) {
                                    hasCallBack[0] = true;
                                    if (listener != null)
                                        listener.fail(AdvanceCsjManager.get().innerInitErrCode, AdvanceCsjManager.get().innerInitErrMsg);
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

            TTAdSdk.init(context.getApplicationContext(), config);
            TTAdSdk.start(new TTAdSdk.Callback() {
                @Override
                public void success() {
                    LogUtil.simple("csj init success");
                    AdvanceCsjManager.get().innerInitStatus = AdvanceCsjManager.INIT_STATUS_SUCCESS;

                    AdvanceSetting.getInstance().lastCSJAID = appID;
                    AdvanceSetting.getInstance().hasCSJInit = true;

                    if (listener != null) {
                        listener.success();
                    }

                }

                @Override
                public void fail(int code, String msg) {
                    LogUtil.e("csj init fail : code = " + code + " msg = " + msg);

                    //内部失败状态标记
                    AdvanceCsjManager.get().innerInitStatus = AdvanceCsjManager.INIT_STATUS_FAILED;
                    AdvanceCsjManager.get().innerInitErrCode = code;
                    AdvanceCsjManager.get().innerInitErrMsg = msg;

                    AdvanceSetting.getInstance().hasCSJInit = false;

                    if (listener != null) {
                        listener.fail(code, msg);
                    }
                }
            });

            AdvanceCsjManager.get().innerInitStatus = AdvanceCsjManager.INIT_STATUS_CALLING;
            LogUtil.devDebug(tag + " end ");
        } catch (Throwable e) {
            e.printStackTrace();
            if (listener != null) {
                listener.fail(AdvanceError.ERR_CODE_EXCEPTION_INT, "csj init exception");
            }
        }
    }

    //尝试从ext中取出价格信息
    public static double getEcpmValue(String TAG, Map<String, Object> extraInfo) {
        double result = 0;
        try {
            if (extraInfo != null) {
                Object price = extraInfo.get("price");
                result = AdvanceUtil.caseObjectToDouble(price);
            }
            LogUtil.devDebug(TAG + " cpm = " + result + " , extraInfo = " + extraInfo);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 以下处理点睛广告，到达首页后的展示逻辑
     */

    @Override
    public void zoomOut(Activity activity) {
    }




    public interface InitListener {
        void success();

        void fail(int code, String msg);
    }
}
