package com.advance.supplier.mi;


import com.advance.AdvanceSetting;
import com.advance.itf.AdvancePrivacyController;
import com.miui.zeus.mimo.sdk.MimoCustomController;
import com.miui.zeus.mimo.sdk.MimoLocation;
import com.miui.zeus.mimo.sdk.base.BaseAd;

import java.util.HashMap;
import java.util.Map;

public class XMUtil {

    public static void bid(BaseAd baseAd, boolean isWin, double winPrice){

        if (isWin){
            //竞价成功时候上报win
            Map<String, Long> auctionBidInfo = new HashMap<>();
            auctionBidInfo.put(BaseAd.IBidding.EXPECT_COST_PRICE, (long)winPrice);
            baseAd.win(auctionBidInfo);
        } else {
            //竞价失败的时候上报loss
            Map<String, Object> lossReasonInfo = new HashMap<>();
            lossReasonInfo.put(BaseAd.IBidding.WIN_PRICE, (long)winPrice);
            lossReasonInfo.put(BaseAd.IBidding.LOSS_REASON, BaseAd.LossReason.TYPE_LOWER_OTHER_BIDDER_PRICE);
            lossReasonInfo.put(BaseAd.IBidding.ADN_ID, 1);
            baseAd.loss(lossReasonInfo);
        }
    }

//    public static void initAD(BaseParallelAdapter adapter, final AdvanceADNInitResult initResult) {
//
//        try {
//            final String tag = "[XMUtil.initAD] ";
//            String eMsg;
//            if (adapter == null) {
//                eMsg = tag + "initAD failed BaseParallelAdapter null";
//                LogUtil.e(eMsg);
//                if (initResult != null) {
//                    initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
//                }
//                return;
//            }
//            boolean hasInit = AdvanceXMManager.getInstance().hasInit;
//            if (hasInit) {
//                LogUtil.simple(tag + " already init");
//                if (initResult != null) {
//                    initResult.success();
//                }
//                return;
//            }
//
//
//            //确保线程安全
//            AdvanceInitManger.getInstance().initialize(new BYBaseCallBack() {
//                @Override
//                public void call() {
//                    //获取自定义参数
//                    MimoCustomController customController = generateMimoCustomController();
//                    //执行初始化
//                    MimoSdk.init(adapter.getRealContext(), customController, new MimoSdk.InitCallback() {
//
//                        @Override
//                        public void success() {
//                            AdvanceXMManager.getInstance().hasInit = true;
//                            if (initResult != null) {
//                                initResult.success();
//                            }
//                        }
//
//                        @Override
//                        public void fail(int code, String msg) {
//                            AdvanceXMManager.getInstance().hasInit = false;
//
//                            String eMsg = "小米初始化失败，code：" + code + " , msg: " + msg;
//                            if (initResult != null) {
//                                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
//                            }
//                        }
//                    });
//                }
//            });
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//
//    }

    public static MimoCustomController generateMimoCustomController() {
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
