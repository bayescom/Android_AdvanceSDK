package com.advance.supplier.gdt;

import com.advance.utils.LogUtil;
import com.qq.e.comm.constants.BiddingLossReason;
import com.qq.e.comm.pi.IBidding;

import java.util.HashMap;
import java.util.Map;

//
//import android.app.Activity;
//
//import com.advance.utils.AdvanceSplashPlusManager;
//
public class GdtUtil {

    public static void notifyBid(IBidding bidding, boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        try {
            LogUtil.simple("GdtUtil notifyBiddingResult , isWin = " + isWin + " , winPrice = " + winPrice + ", referBidInfo = " + referBidInfo);
            if (isWin) {
                Map<String, Object> winMap = new HashMap<>();
                winMap.put(IBidding.EXPECT_COST_PRICE,winPrice);
                bidding.sendWinNotification(winMap);
            } else {
//                 竞败之后或未参竞调用
//*  @param map - 必填，其中
//*  键 IBidding.WIN_PRICE 值为本次竞胜方出价（单位：分），类型为Integer。选填
//*  键 IBidding.LOSS_REASON 值为平台广告竞败原因，类型为Integer。必填
//*  键 IBidding.ADN_ID 值为本次竞胜方渠道ID，类型为Sring。必填。
//                 对于【IBidding.ADN_ID】字段回传支持4个枚举值，分别代表：
//*         1 - 输给平台其它非bidding广告位，当平台目标价报价为本次竞价的最高报价时，可上报此值，仅对混合比价类型的开发者适用
//*         2 - 输给第三方ADN，当其它ADN报价为本次竞价的最高报价时，可上报此值，您无需回传具体竞胜方渠道；
//*         3 - 输给自售客户，当自售广告源报价为本次竞价的最高报价时，可上报此值，仅对有自售广告源的开发者使用；
//*         4 - 输给平台其他bidding广告位，当平台其他bidding广告位报价为本次竞价的最高报价时，可上报此值，仅对有预加载或缓存池逻辑的开发者适用
                Map<String, Object> lossMap = new HashMap<>();
                lossMap.put(IBidding.WIN_PRICE,(int)winPrice);
                lossMap.put(IBidding.LOSS_REASON, BiddingLossReason.LOW_PRICE);
                lossMap.put(IBidding.ADN_ID,"2");
                bidding.sendLossNotification(lossMap);
            }
        } catch (Exception e) {

        }
    }
//
////    public static synchronized void initAD(BaseParallelAdapter adapter) {
////        initAD(adapter, null);
////    }
//
////    public static synchronized void initAD(final BaseParallelAdapter adapter, final BYBaseCallBack callBack) {
////        try {
////            if (adapter == null) {
////                String eMsg = "[GdtUtil] initAD failed BaseParallelAdapter null";
////                LogUtil.e(eMsg);
////                return;
////            }
////            boolean hasInit = AdvanceSetting.getInstance().hasGDTInit;
////
////            if (adapter.sdkSupplier == null) {
////                String eMsg = "[GdtUtil] initAD failed adapter.sdkSupplier null";
////                LogUtil.e(eMsg);
////                adapter.handleFailed(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
////                return;
////            }
////            String mid = adapter.sdkSupplier.mediaid;
////            String lastAppId = AdvanceSetting.getInstance().lastGDTAID;
////            String gdtMID = AdvanceUtil.getGdtAccount(mid);
////            boolean isSame = lastAppId.equals(gdtMID);
////            //只有当允许初始化优化时，且快手已经初始化成功过，并行初始化的id和当前id一致，才可以不再重复初始化。
////            if (hasInit && adapter.canOptInit() && isSame) {
////                LogUtil.simple("[GdtUtil] initAD already init");
////                if (callBack != null) {
////                    callBack.call();
////                }
////                return;
////            }
////
//////            GDTAdSdk.init(adapter.getRealContext(), gdtMID);
////            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
////            if (advancePrivacyController != null) {
////                // 建议在初始化 SDK 前进行此设置
////                GlobalSetting.setEnableCollectAppInstallStatus(advancePrivacyController.alist());
////            }
////
////            //使用新初始化方法
////            GDTAdSdk.initWithoutStart(adapter.getRealContext(), gdtMID); // 该接口不会采集用户信息
////// 调用initWithoutStart后请尽快调用start，否则可能影响广告填充，造成收入下降
////            GDTAdSdk.start(new GDTAdSdk.OnStartListener() {
////                @Override
////                public void onStartSuccess() {
////                    LogUtil.simple("[GdtUtil] onStartSuccess");
////
////
////                    // 推荐开发者在onStartSuccess回调后开始拉广告
////                    AdvanceSetting.getInstance().hasGDTInit = true;
////                }
////
////                @Override
////                public void onStartFailed(Exception e) {
////                    LogUtil.e("[GdtUtil]  onStartFailed:" + e.toString());
////                    AdvanceSetting.getInstance().hasGDTInit = false;
////                    adapter.handleFailed(AdvanceError.ERROR_INIT_DEFAULT + "", e.toString());
////                }
////            });
////            AdvanceSetting.getInstance().hasGDTInit = true;
////            AdvanceSetting.getInstance().lastGDTAID = gdtMID;
////            if (callBack != null) {
////                callBack.call();
////            }
////        } catch (Throwable e) {
////            e.printStackTrace();
////        }
////    }
//
//    @Override
//    public void zoomOut(Activity activity) {
////        try {
////            LogUtil.simple("GdtUtil start zoomOut");
////            final SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance();
////            final SplashAD zoomAd = zoomOutManager.getSplashAD();
////            final ViewGroup zoomOutView = zoomOutManager.startZoomOut((ViewGroup) activity.getWindow().getDecorView(),
////                    (ViewGroup) activity.findViewById(android.R.id.content), new SplashZoomOutManager.AnimationCallBack() {
////
////                        @Override
////                        public void animationStart(int animationTime) {
////
////                        }
////
////                        @Override
////                        public void animationEnd() {
////                            zoomAd.zoomOutAnimationFinish();
////                        }
////                    });
////
////            if (zoomOutView != null) {
////                activity.overridePendingTransition(0, 0);
////            }
////            AdvanceUtil.autoClose(zoomOutView);
////        } catch (Throwable e) {
////            e.printStackTrace();
////        }
//    }
//
//
}
