package com.advance.supplier.honor;

import static com.advance.supplier.honor.HonorUtil.generateCustomController;

import android.content.Context;

import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.hihonor.adsdk.base.HnAds;
import com.hihonor.adsdk.base.init.HnAdConfig;
import com.hihonor.adsdk.base.init.HnCustomController;

import java.util.Map;

public class HonorGlobalConfig extends AdvanceCustomInit {
    String tag = "HonorGlobalConfig--";

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {

        //获取自定义参数
        HnCustomController customController = generateCustomController();
        //执行初始化

        // 构造广告配置
        HnAdConfig config = new HnAdConfig.Builder()
                // 设置您的媒体id，媒体id是您在荣耀广告平台注册的媒体id
                .setAppId(getAppID())
                // 设置您的appKey，appKey是您在荣耀广告平台注册的媒体id对应的密钥:
                .setAppKey(getAppKey())
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
        LogUtil.simple(tag + " 开始初始化");
        HnAds.get().init(context, config);
        callInitSuccess();
        LogUtil.simple(tag + " 初始化完成");

    }

    @Override
    public String getSDKVersion() {
        return HnAds.get().getAdManager().getSDKVersion();
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {

    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }

}
