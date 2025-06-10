package com.advance.supplier.oppo;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.itf.AdvancePrivacyController;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.heytap.msp.mobad.api.InitParams;
import com.heytap.msp.mobad.api.MobAdManager;
import com.heytap.msp.mobad.api.MobCustomController;
import com.heytap.msp.mobad.api.listener.IInitListener;

public class OppoUtil {
    public static synchronized void initAD(BaseParallelAdapter adapter) {
        try {
            if (adapter == null) {
                LogUtil.e("[OppoUtil] initAD failed BaseParallelAdapter null");
                return;
            }
            boolean hasInit = AdvanceOppoManger.getInstance().hasOppoInit;

            if (adapter.sdkSupplier == null) {
                LogUtil.e("[OppoUtil] initAD failed adapter.sdkSupplier null");
                return;
            }
            String mid = adapter.sdkSupplier.mediaid;
            String lastAppId = AdvanceOppoManger.getInstance().lastOppoAID;

            boolean isSame = lastAppId.equals(mid);
            //只有当允许初始化优化时，且快手已经初始化成功过，并行初始化的id和当前id一致，才可以不再重复初始化。
            if (hasInit && adapter.canOptInit() && isSame) {
                LogUtil.simple("[OppoUtil] initAD already init");
                return;
            }

//            初始化配置参数
            InitParams.Builder builder = new InitParams.Builder()
                    .setDebug(BYUtil.isDebug());

//            个人信息控制器
            final AdvancePrivacyController advancePrivacyController = AdvanceSetting.getInstance().advPrivacyController;
            if (advancePrivacyController != null) {
                builder.setMobCustomController(new MobCustomController() {
                    @Override
                    public String getDevImei() {
                        return advancePrivacyController.getDevImei();
                    }

                    @Override
                    public boolean isCanUseLocation() {
                        return advancePrivacyController.isCanUseLocation();
                    }

                    @Override
                    public LocationProvider getLocation() {
                        if (advancePrivacyController.getLocation() != null) {
                            return new LocationProvider() {
                                @Override
                                public double getLatitude() {
                                    return advancePrivacyController.getLocation().getLatitude();
                                }

                                @Override
                                public double getLongitude() {
                                    return advancePrivacyController.getLocation().getLongitude();
                                }
                            };
                        } else {
                            return super.getLocation();
                        }
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        return advancePrivacyController.isCanUsePhoneState();
                    }

                    @Override
                    public boolean isCanUseAndroidId() {
                        String setAid = advancePrivacyController.getDevAndroidID();
                        //未赋值用默认设置
                        if (BYStringUtil.isEmpty(setAid)) {
                            return super.isCanUseAndroidId();
                        } else { //已赋值不再允许SDK获取
                            return false;
                        }
                    }

                    @Override
                    public String getAndroidId() {
                        return advancePrivacyController.getDevAndroidID();
                    }

                    @Override
                    public boolean isCanUseWifiState() {
                        return advancePrivacyController.isCanUseWifiState();
                    }

                    @Override
                    public String getMacAddress() {
                        return advancePrivacyController.getDevMac();
                    }

                    @Override
                    public boolean isCanUseWriteExternal() {
                        return advancePrivacyController.isCanUseWriteExternal();
                    }

                    @Override
                    public boolean alist() {
                        return advancePrivacyController.alist();
                    }
                });
            }

            //个性化开关；true-开启个性化开关，false-关闭个性化开关
            builder.setAppOUIDStatus(AdvanceSetting.getInstance().isADTrack);

            MobAdManager.getInstance().init(adapter.getRealContext(), mid, builder.build(), new IInitListener() {
                @Override
                public void onSuccess() {
                    LogUtil.simple("[OppoUtil] init onSuccess");
                    AdvanceOppoManger.getInstance().hasOppoInit = true;
                    //  2025/2/19  测试不在成功回调后调用广告展示，是否会有什么问题。
                    //  测试结果：无问题，基本上为同步返回结果，即便是初始化失败了，调用请求时也会返回广告失败，目前流程上无影响
                }

                @Override
                public void onFailed(String s) {
                    LogUtil.e("[OppoUtil] init onFailed , " + s);
                    AdvanceOppoManger.getInstance().hasOppoInit = false;
                    AdvanceOppoManger.getInstance().lastOppoAID = "";
                }
            });
            LogUtil.simple("[OppoUtil] init end");
            AdvanceOppoManger.getInstance().hasOppoInit = true;
            AdvanceOppoManger.getInstance().lastOppoAID = mid;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
