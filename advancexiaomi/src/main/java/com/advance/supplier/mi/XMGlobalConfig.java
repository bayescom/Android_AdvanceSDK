package com.advance.supplier.mi;

import static com.advance.supplier.mi.XMUtil.generateMimoCustomController;

import android.content.Context;

import com.advance.custom.AdvanceCustomInit;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.BuildConfig;
import com.miui.zeus.mimo.sdk.MimoCustomController;
import com.miui.zeus.mimo.sdk.MimoSdk;

import java.util.Map;

public class XMGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        //获取自定义参数
        MimoCustomController customController = generateMimoCustomController();
        //执行初始化
        MimoSdk.init(context, customController, new MimoSdk.InitCallback() {

            @Override
            public void success() {
                LogUtil.simple(" MimoSdk.init success");

                callInitSuccess();
            }

            @Override
            public void fail(int code, String msg) {
                AdvanceXMManager.getInstance().hasInit = false;
                String eMsg = "小米初始化失败，code：" + code + " , msg: " + msg;
                LogUtil.simple(" MimoSdk.init fail :" + eMsg);

                callInitFail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);

            }
        });
    }

    @Override
    public String getSDKVersion() {

        return BuildConfig.VERSION_NAME;
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        MimoSdk.setPersonalizedAdEnabled(isPersonalRecommend);//true 开启，false 关闭
    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }

}
