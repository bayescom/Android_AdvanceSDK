package com.advance.utils;

import android.app.Activity;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;

public class AdvanceSplashPlusManager {

    public static void startZoom(Activity activity) {
        try {
            //只有当支持展示v+形式时才执行对应缩放操作。
//            if (AdvanceSetting.getInstance().isSplashShowInSingleActivity){
//                return;
//            }
            if (AdvanceSetting.getInstance().isSplashSupportZoomOut) {
                String sdkId = AdvanceSetting.getInstance().currentSupId;
                ZoomCall call = null;
                switch (sdkId) {
                    case AdvanceConfig.SDK_ID_GDT:
                        call = reflectZoomMethod("gdt.GdtUtil");
                        break;
                    case AdvanceConfig.SDK_ID_CSJ:
                        call = reflectZoomMethod("csj.CsjUtil");

                        break;
                    case AdvanceConfig.SDK_ID_KS:
                        call = reflectZoomMethod("ks.KSUtil");
                        break;
                    case AdvanceConfig.SDK_ID_BAIDU:
                        call = reflectZoomMethod("baidu.BDUtil");
                        break;
                }

                if (call != null) {
                    call.zoomOut(activity);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static ZoomCall reflectZoomMethod(String supClzName) {
        ZoomCall zoomCall = null;
        try {
            Class clz = Class.forName(AdvanceLoader.BASE_ADAPTER_PKG_PATH + supClzName);
            zoomCall = (ZoomCall) clz.newInstance();
            if (zoomCall != null)
                LogUtil.devDebug("reflectZoomMethod result = " + zoomCall.toString());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return zoomCall;
    }


    public interface ZoomCall {
        void zoomOut(Activity activity);
    }

}
