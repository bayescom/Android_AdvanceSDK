package com.advance.custom;

import android.content.Context;

import com.advance.AdvanceSetting;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYStringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//每个初始化类对应一个appid，单例中持有此实现类，避免多次初始化
public abstract class AdvanceCustomInit implements AdvanceAdnInitItf {
    public static final String TAG = "AdvanceCustomInit";

    // 初始化状态管理
    private enum State {UNINITIALIZED, INITIALIZING, INITIALIZED_SUCCESS, INITIALIZED_FAILED}

    // Map结构存储不同sdk初始化状态
    private State initState = State.UNINITIALIZED;

    private ArrayList<SdkSupplier> sdkSupplierList = new ArrayList<>();
    private ArrayList<AdvanceADNInitResult> initCallbackList = new ArrayList<>();

    private String appID = "";
    private String appKey = "";
    private Map<String, Object> serverExtra = new HashMap<>();


    private long startInitTime = 0;
    String errCode = "", errMsg = "";

    //调用方需在回调结果中处理是否再调用innerInitSDK方法，如果广告初始化成功，相同的SDK则不再调用innerInitSDK方法。在BaseParallelAdapter中处理此逻辑？
    @Override
    public synchronized void innerInitSDK(Context context) {
        try {
            // TODO: 2026/6/1 loadedtk上报
            startInitTime = System.currentTimeMillis();
            reportLoaded();

            if (initState == State.INITIALIZED_SUCCESS) {
                LogUtil.simple(TAG + "(innerInitSDK) init success , no need to init again");
                callInitSuccess();
                return;
            }


            if (initState == State.INITIALIZED_FAILED) {
                LogUtil.simple(TAG + "(innerInitSDK) init failed , no need to init again");
                callInitFail(errCode, errMsg);
                return;
            }

            if (initState == State.INITIALIZING) {
                LogUtil.simple(TAG + "(innerInitSDK) initing , no need to init again");
                return;
            }

            initState = State.INITIALIZING;

            initADN(context, serverExtra);
        } catch (Exception e) {

        }
    }

    private void reportLoaded() {

    }

    @Override
    public synchronized void addSdkSupplier(SdkSupplier sdkSupplier) {
        try {
            //赋值媒体id参数
            if (sdkSupplier != null) {
                if (BYStringUtil.isEmpty(appID)) {
                    appID = sdkSupplier.mediaid;
                }
                if (BYStringUtil.isEmpty(appKey)) {
                    appKey = sdkSupplier.mediakey;
                }
            }
            //添加至列表
            sdkSupplierList.add(sdkSupplier);
        } catch (Exception e) {

        }

    }

    // TODO: 2026/6/2 测试多个并发相同appid自定义SDK策略，执行情况
    @Override
    public synchronized void addInitListener(AdvanceADNInitResult initListener) {
        try {
            initCallbackList.add(initListener);
        } catch (Exception e) {
        }
    }


    //回调初始化成功事件
    @Override
    public void callInitSuccess() {
        try {
            synchronized (initCallbackList) {
                initState = State.INITIALIZED_SUCCESS;

                LogUtil.simple(TAG + "(callInitSuccess) start");
                LogUtil.devDebug(TAG + "init success cost :" + (System.currentTimeMillis() - startInitTime));
                for (AdvanceADNInitResult initListener : initCallbackList) {
                    if (initListener != null) {
                        initListener.success();
                    }
                }

                initCallbackList.clear();
            }
        } catch (Exception e) {
        }

    }

    //回调初始化失败事件
    @Override
    public void callInitFail(String errCode, String errMsg) {
        try {
            synchronized (initCallbackList) {
                initState = State.INITIALIZED_FAILED;
                this.errCode = errCode;
                this.errMsg = errMsg;

                LogUtil.simple(TAG + "(callInitFail) start , errCode = " + errCode + " , errMsg = " + errMsg);
                LogUtil.devDebug(TAG + "init fail cost :" + (System.currentTimeMillis() - startInitTime));

                for (AdvanceADNInitResult initListener : initCallbackList) {
                    if (initListener != null) {
                        initListener.fail(errCode, errMsg);
                    }
                }

                initCallbackList.clear();
            }
        } catch (Exception e) {

        }
    }


    //获取隐私开关控制器
    @Override
    public AdvancePrivacyController getPrivacyController() {
        return AdvanceSetting.getInstance().advPrivacyController;
    }

    //是否允许个性化广告推送，true允许
    @Override
    public boolean isPersonalRecommend() {
        return AdvanceSetting.getInstance().isADTrack;
    }

    //是否关闭摇一摇互动方式，true关闭
    @Override
    public boolean disableShake() {
        return AdvanceSetting.getInstance().disableShake;
    }


    @Override
    public String getAppID() {
        return appID;
    }

    @Override
    public String getAppKey() {
        return appKey;
    }
}
