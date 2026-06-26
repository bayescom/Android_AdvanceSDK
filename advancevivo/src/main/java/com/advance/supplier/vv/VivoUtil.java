package com.advance.supplier.vv;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.model.SdkSupplier;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.vivo.mobilead.unified.IBidding;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.bidding.AdnId;
import com.vivo.mobilead.unified.bidding.LossReason;

import java.util.Map;

public class VivoUtil {
    public static final String TAG = "[VivoUtil] ";

    public static final void bid(IBidding bidding, boolean isWin, double winPrice, Map<String, Object> referBidInfo) {

        if (bidding != null) {
            if (isWin) {
                /**
                 * 竞胜
                 * 若本轮 vivo 广告胜出，请务必调此接口通知 SDK 竞胜结果，参数为二价计费的结算价（一价计费传 0
                 即可）。
                 * 注：二价计费广告一定要调此接口传入计费价格，否则广告无法计费，曝光无效！！！
                 * 注：要在广告曝光前先调用此接口。
                 */
                bidding.sendWinNotification(0);
            } else {
                /**
                 * 竞败
                 * 如本轮 vivo 广告竞败，需调此接口通知 SDK 竞败的结果；
                 * 第一个参数传入竞败原因，枚举值可见 LossReason
                 * 10001-其他原因。
                 * 第二个参数传入竞胜方的出价。
                 */
                int adnID = getAdnID(referBidInfo);
                bidding.sendLossNotification(LossReason.LOW_PRICE, (int) winPrice, adnID, "");
            }
        }
    }

    public static  int getAdnID(Map<String, Object> referBidInfo) {
        int adnID = AdnId.OTHER;
        try {
            String winID = "";
            if (referBidInfo != null) {
                winID = (String) referBidInfo.get(AdvanceConstant.BID_RESULT_KEY_WIN_SDK_ID);
            }
            switch (winID) {
                case AdvanceConfig.SDK_ID_VIVO:
                    adnID = AdnId.VIVO;
                case AdvanceConfig.SDK_ID_GDT:
                    adnID = AdnId.GDT;

                    break;
                case AdvanceConfig.SDK_ID_CSJ:
                    adnID = AdnId.CSJ;

                    break;
                case AdvanceConfig.SDK_ID_KS:
                    adnID = AdnId.KS;

                    break;
                case AdvanceConfig.SDK_ID_BAIDU:
                    adnID = AdnId.BAIDU;

                    break;
                case AdvanceConfig.SDK_ID_HW:
                    adnID = AdnId.HUAWEI;

                    break;
                case AdvanceConfig.SDK_ID_XIAOMI:
                    adnID = AdnId.XIAOMI;

                    break;
                case AdvanceConfig.SDK_ID_OPPO:
                    adnID = AdnId.OPPO;
                    break;
            }
        } catch (Exception e) {
        }
        return adnID;
    }

//
//    public static void initAD(BaseParallelAdapter adapter, AdvanceADNInitResult initResult) {
//        String eMsg;
//        if (adapter == null) {
//            eMsg = TAG + "initAD failed BaseParallelAdapter null";
//            LogUtil.e(eMsg);
//            if (initResult != null) {
//                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
//            }
//            return;
//        }
//
//        SdkSupplier supplier = adapter.sdkSupplier;
//        if (supplier == null) {
//            eMsg = TAG + "initAD failed supplier null";
//            LogUtil.e(eMsg);
//            if (initResult != null) {
//                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
//            }
//            return;
//        }
//
//        boolean hasInit = AdvanceVivoManager.getInstance().hasInit;
//        if (hasInit) {
//            LogUtil.simple(TAG + " already init");
//            if (initResult != null) {
//                initResult.success();
//            }
//            return;
//        }
//
//        //确保线程安全
//        AdvanceInitManger.getInstance().initialize(new BYBaseCallBack() {
//            @Override
//            public void call() {
//                initVivo(adapter, initResult);
//            }
//        });
//    }
//
//    private static void initVivo(BaseParallelAdapter adapter, AdvanceADNInitResult initResult) {
//        try {
//            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
//
//            VAdConfig adConfig = new VAdConfig.Builder()
//                    .setMediaId(adapter.getAppID())
//                    .setDebug(BYUtil.isDebug())  //是否开启日志输出
//                    .setCustomController(new VCustomController() {
//                        @Override
//                        public boolean isCanUseLocation() {
//                            if (advancePrivacyController != null) {
//                                return advancePrivacyController.isCanUseLocation();
//                            }
//                            //是否允许获取位置信息，默认允许
//                            return true;
//                        }
//
//                        @Override
//                        public VLocation getLocation() {
//                            if (advancePrivacyController != null) {
//                                Location aLocation = advancePrivacyController.getLocation();
//                                if (aLocation != null) {
//                                    return new VLocation(aLocation.getLongitude(), aLocation.getLatitude());
//                                }
//                            }
//
//                            //若不允许获取位置信息，亦可主动传给SDK位置信息
//                            return null;
//                        }
//
//                        @Override
//                        public boolean isCanUsePhoneState() {
//                            if (advancePrivacyController != null) {
//                                return advancePrivacyController.isCanUsePhoneState();
//                            }
//                            //是否允许获取imei信息，默认允许
//                            return true;
//                        }
//
//                        @Override
//                        public String getImei() {
//                            if (advancePrivacyController != null) {
//                                return advancePrivacyController.getDevImei();
//                            }
//                            //若不允许获取imei信息，亦可主动传给SDK imei信息
//                            return null;
//                        }
//
//                        @Override
//                        public String getOaid() {
//                            if (advancePrivacyController != null) {
//                                return advancePrivacyController.getDevOaid();
//                            }
//                            //传入获取到的oaia
//                            return "";
//                        }
//
////                        @Override
////                        public boolean isCanUseWifiState() {
////                            //是否允许获取网络信息（mac、ip等），默认允许
////                            return true;
////                        }
//
//                        @Override
//                        public boolean isCanUseWriteExternal() {
////                            if (advancePrivacyController!=null){
////                                return  advancePrivacyController.();
////                            }
//                            //是否允许SDK使用公共存储空间
//                            return true;
//                        }
//
//                        @Override
//                        public boolean isCanPersonalRecommend() {
////                            if (advancePrivacyController!=null){
////                                return  advancePrivacyController.();
////                            }
//                            //是否允许推荐个性化广告
//                            return AdvanceVivoManager.getInstance().allowPersonalRecommend;
//                        }
//
//                        @Override
//                        public boolean isCanUseImsi() {
//                            if (advancePrivacyController != null) {
//                                return advancePrivacyController.isCanUsePhoneState();
//                            }
//                            //是否允许获取imsi
//                            return true;
//                        }
//
//                        @Override
//                        public boolean isCanUseApplist() {
//                            if (advancePrivacyController != null) {
//                                return advancePrivacyController.alist();
//                            }
//                            //是否允许获取手机安装应用列表
//                            return true;
//                        }
//                    }).build();
//
//
//            // 这里完成SDK的初始化
//            Application application = (Application) adapter.getRealContext().getApplicationContext();
//
//            VivoAdManager.getInstance().init(application, adConfig, new VInitCallback() {
//                @Override
//                public void suceess() {
//                    AdvanceVivoManager.getInstance().hasInit = true;
//                    LogUtil.simple(TAG + " init suceess");
//                    if (initResult != null) {
//                        initResult.success();
//                    }
//                }
//
//                @Override
//                public void failed(@NonNull VivoAdError adError) {
//                    AdvanceVivoManager.getInstance().hasInit = false;
//
//                    if (initResult != null) {
//                        initResult.fail(adError.getCode() + "", adError.getMsg());
//                    }
//                    //若是报超时错误，则检查是否正常出现广告，如无异常，则是正常的
//                    LogUtil.simple(TAG + "failed: " + adError.toString());
//                }
//            });
//        } catch (Throwable e) {
//            if (!AdvanceVivoManager.getInstance().hasInit) {
//                if (initResult != null) {
//                    initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", "initVivo exception");
//                }
//            }
//        }
//
//    }

    public static AdParams.Builder getAdParamsBuilder(BaseParallelAdapter adapter) {
        try {
            SdkSupplier sdkSupplier = adapter.sdkSupplier;
            String adID = sdkSupplier.adspotid;
            AdParams.Builder builder = new AdParams.Builder(adID);
            builder.setWxAppid(AdvanceSetting.getInstance().wxAppId);   //此非必须
            builder.setFetchTimeout(sdkSupplier.timeout);// 允许拉取广告的超时时长：取值范围[3000, 5000]
            return builder;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void handleErr(BaseParallelAdapter adapter, VivoAdError vivoAdError, String defaultErrCode, String defaultErrMsg) {
        try {
            if (vivoAdError != null) {
                int vErrCode = vivoAdError.getCode();
                if (vErrCode > 0) {
                    defaultErrCode = vErrCode + "";
                }
                String vErrMsg = vivoAdError.getMsg();
                if (BYStringUtil.isNotEmpty(vErrMsg)) {
                    defaultErrMsg = vErrMsg;
                }
            }

            if (adapter != null) {
                adapter.handleFailed(defaultErrCode, defaultErrMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double getPrice(IBidding bidding) {
        double price = 0;
        try {
            if (bidding != null) {
                price = bidding.getPrice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return price;
    }
}
