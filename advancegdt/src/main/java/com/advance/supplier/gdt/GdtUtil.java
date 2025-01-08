package com.advance.supplier.gdt;

import android.app.Activity;
import android.view.ViewGroup;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.AdvanceSplashPlusManager;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.comm.managers.GDTAdSdk;
import com.qq.e.comm.managers.setting.GlobalSetting;

public class GdtUtil implements AdvanceSplashPlusManager.ZoomCall {

    public static synchronized void initAD(BaseParallelAdapter adapter) {
        try {
            if (adapter == null) {
                LogUtil.e("[GdtUtil] initAD failed BaseParallelAdapter null");
                return;
            }
            boolean hasInit = AdvanceSetting.getInstance().hasGDTInit;

            if (adapter.sdkSupplier == null) {
                LogUtil.e("[GdtUtil] initAD failed adapter.sdkSupplier null");
                return;
            }
            String mid = adapter.sdkSupplier.mediaid;
            String lastAppId = AdvanceSetting.getInstance().lastGDTAID;
            String gdtMID = AdvanceUtil.getGdtAccount(mid);
            boolean isSame = lastAppId.equals(gdtMID);
            //只有当允许初始化优化时，且快手已经初始化成功过，并行初始化的id和当前id一致，才可以不再重复初始化。
            if (hasInit && adapter.canOptInit() && isSame) {
                LogUtil.simple("[GdtUtil] initAD already init");
                return;
            }

//            GDTAdSdk.init(adapter.getRealContext(), gdtMID);
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
            if (advancePrivacyController != null) {
                // 建议在初始化 SDK 前进行此设置
                GlobalSetting.setEnableCollectAppInstallStatus(advancePrivacyController.alist());
            }

            //使用新初始化方法
            GDTAdSdk.initWithoutStart(adapter.getRealContext(), gdtMID); // 该接口不会采集用户信息
// 调用initWithoutStart后请尽快调用start，否则可能影响广告填充，造成收入下降
            GDTAdSdk.start(new GDTAdSdk.OnStartListener() {
                @Override
                public void onStartSuccess() {
                    LogUtil.simple("[GdtUtil] onStartSuccess");

                    // 推荐开发者在onStartSuccess回调后开始拉广告
                    AdvanceSetting.getInstance().hasGDTInit = true;
                }

                @Override
                public void onStartFailed(Exception e) {
                    LogUtil.e("[GdtUtil]  onStartFailed:" + e.toString());
                    AdvanceSetting.getInstance().hasGDTInit = false;
                }
            });
            AdvanceSetting.getInstance().hasGDTInit = true;
            AdvanceSetting.getInstance().lastGDTAID = gdtMID;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void zoomOut(Activity activity) {
        try {
            LogUtil.simple("GdtUtil start zoomOut");
            final SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance();
            final SplashAD zoomAd = zoomOutManager.getSplashAD();
            final ViewGroup zoomOutView = zoomOutManager.startZoomOut((ViewGroup) activity.getWindow().getDecorView(),
                    (ViewGroup) activity.findViewById(android.R.id.content), new SplashZoomOutManager.AnimationCallBack() {

                        @Override
                        public void animationStart(int animationTime) {

                        }

                        @Override
                        public void animationEnd() {
                            zoomAd.zoomOutAnimationFinish();
                        }
                    });

            if (zoomOutView != null) {
                activity.overridePendingTransition(0, 0);
            }
            AdvanceUtil.autoClose(zoomOutView);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
