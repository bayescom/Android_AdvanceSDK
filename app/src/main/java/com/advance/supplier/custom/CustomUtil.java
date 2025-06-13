package com.advance.supplier.custom;

import android.text.TextUtils;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.LogUtil;
import com.qq.e.comm.managers.GDTAdSdk;
import com.qq.e.comm.managers.setting.GlobalSetting;

public class CustomUtil {
    public boolean isYLHInited = false; //记录是否完成了初始化
    private static CustomUtil instance;

    private CustomUtil() {
    }

    public static synchronized CustomUtil getInstance() {
        if (instance == null) {
            instance = new CustomUtil();
        }
        return instance;
    }

    /**
     * SDK得初始化方法，每次策略开始执行，均会调用此处的初始化方法
     *
     * tips:开发者在选择初始化逻辑时，也可以自己在应用启动后，直接自行执行SDK的初始化逻辑，而不依赖策略下发的mediaid信息
     *
     * @param adapter adn适配器基类
     */
    public static synchronized void initAD(BaseParallelAdapter adapter) {

        try {
            boolean hasInited = CustomUtil.getInstance().isYLHInited;
            if (hasInited){
                LogUtil.d("[CustomUtil] 已完成过初始操作");
                return;
            }
            if (adapter == null || adapter.sdkSupplier == null || TextUtils.isEmpty(adapter.sdkSupplier.mediaid)) {
                LogUtil.e("[CustomUtil] 缺少初始化的媒体id信息");
                return;
            }


//            隐私相关设置
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
            if (advancePrivacyController != null) {
                // 建议在初始化 SDK 前进行此设置
                GlobalSetting.setEnableCollectAppInstallStatus(advancePrivacyController.alist());
            }

            //使用新初始化方法
            String appID = adapter.getAppID();
            GDTAdSdk.initWithoutStart(adapter.getRealContext(), appID); // 该接口不会采集用户信息
// 调用initWithoutStart后请尽快调用start，否则可能影响广告填充，造成收入下降
            GDTAdSdk.start(new GDTAdSdk.OnStartListener() {
                @Override
                public void onStartSuccess() {
                    LogUtil.simple("[GdtUtil] onStartSuccess");

                    // 推荐开发者在onStartSuccess回调后开始拉广告
                    CustomUtil.getInstance().isYLHInited = true;
                }

                @Override
                public void onStartFailed(Exception e) {
                    LogUtil.e("[GdtUtil]  onStartFailed:" + e.toString());
                    CustomUtil.getInstance().isYLHInited = false;
                }
            });
            CustomUtil.getInstance().isYLHInited = true;

        } catch (Throwable e) {
            e.printStackTrace();
        }


    }
}
