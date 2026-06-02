package com.advance.custom;

import android.content.Context;

import com.advance.itf.AdvanceADNInitResult;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.SdkSupplier;

import java.util.Map;


public interface AdvanceAdnInitItf {

    //     ------必须由adn方实现内容 start -------
    //    执行SDK初始化代码
    void initADN(Context context, Map<String, Object> serverExtra);

    //    获取SDK版本号
    String getSDKVersion();

    //APP运行中改变个性化广告推送状态，若无相关设置方法则忽略
    void switchPersonalRecommend(boolean isPersonalRecommend);

    //APP运行中改变摇一摇状态，若无相关设置方法则忽略
    void switchDisableShake(boolean disableShake);
    //     ------必须由adn方实现内容 end -------


    //初始化SDK -- advance 内部调用
    void innerInitSDK(Context context);

    void addSdkSupplier(SdkSupplier sdkSupplier);
    void addInitListener(AdvanceADNInitResult initListener);

    //回调初始化成功事件
    void callInitSuccess();
    //回调初始化失败事件
    void callInitFail(String errCode,String errMsg);

    //获取隐私开关控制器
    AdvancePrivacyController getPrivacyController();

    //是否允许个性化广告推送，true允许
    boolean isPersonalRecommend();

    //是否关闭摇一摇互动方式，true关闭
    boolean disableShake();


    //获取应用id参数
    String getAppID();

    // 获取应用key参数
    String getAppKey();
}
