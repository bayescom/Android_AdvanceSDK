package com.advance.supplier.sigmob;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.location.Location;

import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.sigmob.windad.OnInitializationListener;
import com.sigmob.windad.OnStartListener;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindCustomController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SigmobGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        final String tag = "[SigmobGlobalConfig.initADN] ";

        final AdvancePrivacyController advancePrivacyController = getPrivacyController();
        final WindAds ads = WindAds.sharedAds();
        WindAdOptions windAdOptions = new WindAdOptions(getAppID(), getAppKey());

        if (advancePrivacyController != null) {
            //设置自定义设备信息，允许开发者自行传入设备及用户信息
            windAdOptions.setCustomController(new WindCustomController() {

                /**
                 * 是否允许SDK主动获取地理位置信息
                 *
                 * @return true可以获取，false禁止获取。默认为true
                 */
                @Override
                public boolean isCanUseLocation() {
                    return advancePrivacyController.isCanUseLocation();
                }

                /**
                 * 当isCanUseLocation=false时，可传入地理位置信息，sdk使用您传入的地理位置信息
                 *
                 * @return 地理位置参数 或者 null
                 */
                @Override
                public Location getLocation() {
                    if (advancePrivacyController.getLocation() != null) {
                        Location la = new Location("ADVCusLocation");
                        la.setLatitude(advancePrivacyController.getLocation().getLatitude());
                        la.setLongitude(advancePrivacyController.getLocation().getLongitude());
                        return la;
                    } else {
                        return super.getLocation();
                    }
                }

                /**
                 * 是否允许SDK主动获取手机设备信息，如：imei，运营商信息
                 *
                 * @return true允许获取，false禁止获取。默认为true
                 */
                @Override
                public boolean isCanUsePhoneState() {
                    return advancePrivacyController.isCanUsePhoneState();
                }

                /**
                 * 当isCanUsePhoneState=false时，可传入imei信息，sdk使用您传入的imei信息
                 *
                 * @return imei信息 或者 null
                 */

                @Override
                public String getDevImei() {
                    return advancePrivacyController.getDevImei();
                }

                /**
                 * 是否允许SDK主动获取OAID
                 *
                 * @return true允许获取，false禁止获取。默认为true
                 */
                @Override
                public boolean isCanUseOaid() {
                    return advancePrivacyController.canUseOaid();
//                        return SigmobSetting.getInstance().isCanUseOaid;
                }

                /**
                 * 当isCanUseOaid = false，开发者可以传入oaid
                 *
                 * @return oaid 或者 null
                 */
                @Override
                public String getDevOaid() {
                    String byOaid = "";
                    try {
                        String settingOaid = advancePrivacyController.getDevOaid();
                        if (BYStringUtil.isNotEmpty(settingOaid)) {
                            return settingOaid;
                        }
                        byOaid = BYDevice.getOaidValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return byOaid;
                }

                /**
                 * 是否允许SDK主动获取Android id
                 *
                 * @return true允许获取，false禁止获取。默认为true
                 */
                @Override
                public boolean isCanUseAndroidId() {
                    if (!advancePrivacyController.isCanUsePhoneState()) {
                        return false;
                    }
                    String cusAID = advancePrivacyController.getDevAndroidID();
                    //如果用户传递为空，或默认未配置，true，允许SDK获取； 若用户配置了id，则不再允许SDK获取
                    return BYStringUtil.isEmpty(cusAID);
                }

                /**
                 * isCanUseAndroidId=false时，可传入android id信息，SDK使用您传入的android id信息
                 *
                 * @return android id信息 或者 null
                 */
                @Override
                public String getAndroidId() {
                    return advancePrivacyController.getDevAndroidID();
                }

                /**
                 * 是否允许SDK主动收集上传应用列表
                 *
                 * @return true 允许SDK收集，false 开发者传入
                 */
                @Override
                public boolean isCanUseAppList() {
                    return advancePrivacyController.alist();
                }

                /**
                 *  当isCanUseAppList = false，那么调用开发者传入的应用列表
                 *
                 * @return 开发者收集应用列表信息或者null
                 */
                @Override
                public List<PackageInfo> getInstallPackageInfoList() {
                    List<String> pkgList = advancePrivacyController.getInstalledPackages();
                    if (pkgList != null && pkgList.size() > 0) {
                        List<PackageInfo> result = new ArrayList<>();
                        for (String name : pkgList) {
                            PackageInfo pkg = new PackageInfo();
                            pkg.packageName = name;
                            result.add(pkg);
                        }
                        return result;
                    }
                    return null;
                }


                /**
                 * 是否允许 SDK 查询运营商编码（4.22.0 版本新增）
                 *
                 * @return true 可以使用，false 禁止使用
                 */
                public boolean isCanUseSimOperator() {
                    return SigmobSetting.getInstance().isCanUseSimOperator;
                }

                /**
                 * isCanUseSimOperator=false 时，Sigmob 使用开发者传入的运营商编码，例如：46000（4.22.0 版本新增）
                 */
                public String getDevSimOperatorCode() {
                    return SigmobSetting.getInstance().devSimOperatorCode;
                }

                /**
                 * isCanUseSimOperator=false 时，Sigmob 使用开发者传入的运营商名称，例如：中国移动（4.22.0 版本新增）
                 */
                public String getDevSimOperatorName() {
                    return SigmobSetting.getInstance().devSimOperatorName;
                }
            });
        }

        //SDK初始化
        ads.init(context, windAdOptions, new OnInitializationListener() {
            @Override
            public void onInitializationSuccess() {
                LogUtil.devDebug(tag + "onInitializationSuccess SDK 初始化成功 ");

                ads.start(new OnStartListener() {
                    @Override
                    public void onStartSuccess() {
                        LogUtil.devDebug(tag + "onStartSuccess SDK 启动成功 ");

                        callInitSuccess();
                    }

                    @Override
                    public void onStartFail(String error) {
                        LogUtil.e(tag + "onStartFail SDK 启动失败 " + error);


                        callInitFail(AdvanceError.ERROR_INIT_DEFAULT + "", error);
                    }
                });
            }

            @Override
            public void onInitializationFail(String error) {
                LogUtil.e(tag + "onInitializationFail SDK 初始化失败 " + error);

                callInitFail(AdvanceError.ERROR_INIT_DEFAULT + "", error);
            }
        });
    }

    @Override
    public String getSDKVersion() {
        String v = "";
        try {
            v = WindAds.getVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {
        /*
         * 是否开启个性化推荐接口
         * true 开启, false 关闭,默认值为true
         */
        WindAds.sharedAds().setPersonalizedAdvertisingOn(isPersonalRecommend);
    }

    @Override
    public void switchDisableShake(boolean disableShake) {
        /*
         * 是否允许使用传感器
         * true 开启，false 关闭，默认值为 true
         */
        WindAds.sharedAds().setSensorStatus(!disableShake);
    }

}
