package com.advance.supplier.mi;


import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceInitManger;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.miui.zeus.mimo.sdk.MimoCustomController;
import com.miui.zeus.mimo.sdk.MimoLocation;
import com.miui.zeus.mimo.sdk.MimoSdk;

import java.util.Map;

public class XMUtil {

    public static void initAD(BaseParallelAdapter adapter, final AdvanceADNInitResult initResult) {

        try {
            final String tag = "[XMUtil.initAD] ";
            String eMsg;
            if (adapter == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";
                LogUtil.e(eMsg);
                if (initResult != null) {
                    initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
                }
                return;
            }
            boolean hasInit = AdvanceXMManager.getInstance().hasInit;
            if (hasInit) {
                LogUtil.simple(tag + " already init");
                if (initResult != null) {
                    initResult.success();
                }
                return;
            }


            //确保线程安全
            AdvanceInitManger.getInstance().initialize(new BYBaseCallBack() {
                @Override
                public void call() {
                    //获取自定义参数
                    MimoCustomController customController = generateMimoCustomController();
                    //执行初始化
                    MimoSdk.init(adapter.getRealContext(), customController, new MimoSdk.InitCallback() {

                        @Override
                        public void success() {
                            AdvanceXMManager.getInstance().hasInit = true;
                            if (initResult != null) {
                                initResult.success();
                            }
                        }

                        @Override
                        public void fail(int code, String msg) {
                            AdvanceXMManager.getInstance().hasInit = false;

                            String eMsg = "小米初始化失败，code：" + code + " , msg: " + msg;
                            if (initResult != null) {
                                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
                            }
                        }
                    });
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }

    private static MimoCustomController generateMimoCustomController() {
        MimoCustomController result = null;
        try {
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;

            if (advancePrivacyController != null) {
                result = new MimoCustomController() {

                    @Override
                    public boolean isCanUseLocation() {
                        return advancePrivacyController.isCanUseLocation();
                    }

                    @Override
                    public MimoLocation getMimoLocation() {
                        if (advancePrivacyController.getLocation() != null) {
                            MimoLocation la = new MimoLocation();
                            la.setLatitude(advancePrivacyController.getLocation().getLatitude());
                            la.setLongitude(advancePrivacyController.getLocation().getLongitude());
                            return la;
                        } else {
                            return super.getMimoLocation();
                        }
                    }

                    @Override
                    public boolean isCanUseWifiState() {
                        return advancePrivacyController.isCanUseWifiState();
                    }

                    @Override
                    public boolean alist() {
                        return advancePrivacyController.alist();
                    }
                };
            }
        } catch (Throwable e) {

        }

        return result;
    }

    public static double getPrice(Map<String, Object> mediaMap) {
        try {
            if (mediaMap == null || mediaMap.isEmpty()) {
                return 0;
            }
            return (long) mediaMap.get("price");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;

    }
}
