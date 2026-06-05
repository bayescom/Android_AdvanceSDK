package com.advance.supplier.tanx;

import android.app.Application;
import android.content.Context;

import androidx.annotation.Keep;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;
import com.advance.custom.AdvanceCustomInit;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.SdkConstant;
import com.alimm.tanx.core.TanxInitListener;
import com.alimm.tanx.core.ad.ad.table.screen.model.TableScreenParam;
import com.alimm.tanx.core.ad.bean.RewardParam;
import com.alimm.tanx.core.config.TanxConfig;
import com.alimm.tanx.core.image.ILoader;
import com.alimm.tanx.ui.TanxSdk;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.util.BYUtil;

import java.util.Map;

@Keep
public class TanxGlobalConfig extends AdvanceCustomInit {

    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {

//            AdvanceUtil advanceUtil = new AdvanceUtil(adapter.getADActivity());
        String oaid = BYDevice.getOaidValue();
//            String imei = BYDevice.getImeiValue();
//设置图片加载自定义loader

        TanxConfig config = new TanxConfig.Builder()
                .appName(AdvanceConfig.getInstance().getAppName())
                .appId(getAppID())
                .appKey(getAppKey())
//                    .appSecret(adapter.sdkSupplier.mediaSecret)
                .oaid(oaid)
                //是不是开启自动获取oaid开关
                .oaidSwitch(true)
//                    .imei(imei)
//                    .imageLoader(iLoader)
                .debug(BYUtil.isDebug())
                .setEnableSensor(!AdvanceSetting.getInstance().disableShake)
                //                .dark(new SettingConfig().setNightConfig())
                .build();

        ILoader iLoader = AdvanceTanxSetting.getInstance().iLoader;
        if (iLoader != null) {
            config.setImageLoader(iLoader);
        }
        Application application = (Application) context.getApplicationContext();
        LogUtil.simple("[TanxUtil.initTanx] tanx init start");

        TanxSdk.init(application, config, new TanxInitListener() {
            @Override
            public void succ() {
                LogUtil.high("[TanxUtil.initTanx] TanxInitListener succ");

                callInitSuccess();
            }


            @Override
            public void error(int code, String msg) {
                LogUtil.e("[TanxUtil.initTanx] TanxInitListener err code = " + code + " ,msg = " + msg);

                callInitFail(code + "", msg);
            }
        });
        LogUtil.simple("[TanxUtil.initTanx] tanx init end");
    }

    @Override
    public String getSDKVersion() {
        String tanxV = "";
        try {
            tanxV = SdkConstant.getSdkVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return tanxV;
    }

    @Override
    public void switchPersonalRecommend(boolean isPersonalRecommend) {

    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }


    @Deprecated
    public static void setImgLoader(ILoader loader) {
    }

    //激励视频-必须！！！！：   媒体的用户id，激励广告任务完成后用于用户关联，当前媒体内需保证id的唯一性，防止奖励发放偏差。（必传）
    public static void setMediaUID(String uid) {
        try {
            AdvanceTanxSetting.getInstance().mediaUID = uid;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //激励视频-可选：用于查询历史的遗漏发奖，媒体侧可选择性使用，如不使用可通过调用主动查询历史奖励接口来补发历史奖励。
    //当前查询依赖激励广告的展示，当激励广告展示且媒体传入的RewardParam不为空则启动一次查询
    public static void setRewardParam(RewardParam rewardParam) {
        try {
            AdvanceTanxSetting.getInstance().rewardParam = rewardParam;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void isInterClickClose(boolean close) {
        try {
            AdvanceTanxSetting.getInstance().interClickAdClose = close;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static void setInterParam(TableScreenParam tableScreenParam) {
        try {
            AdvanceTanxSetting.getInstance().tableScreenParam = tableScreenParam;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
