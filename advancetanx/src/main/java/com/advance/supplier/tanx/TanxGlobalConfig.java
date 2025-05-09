package com.advance.supplier.tanx;

import androidx.annotation.Keep;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.alimm.tanx.core.SdkConstant;
import com.alimm.tanx.core.ad.ad.table.screen.model.TableScreenParam;
import com.alimm.tanx.core.ad.bean.RewardParam;
import com.alimm.tanx.core.image.ILoader;

@Keep
public class TanxGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

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
    public void setPersonalRecommend(boolean allow) {

    }

    public static void setImgLoader(ILoader loader) {
        try {
            AdvanceTanxSetting.getInstance().iLoader = loader;
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
