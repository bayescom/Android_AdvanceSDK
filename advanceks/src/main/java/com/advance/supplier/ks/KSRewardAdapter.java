package com.advance.supplier.ks;

import android.app.Activity;
import androidx.annotation.Nullable;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.model.KsExtraRewardType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;
import static com.advance.model.AdvanceError.ERROR_EXCEPTION_SHOW;

public class KSRewardAdapter extends AdvanceRewardCustomAdapter implements KsRewardVideoAd.RewardAdInteractionListener {
    public RewardVideoSetting setting;
    private String TAG = "[KSRewardAdapter] ";
    KsRewardVideoAd ad;

    public KSRewardAdapter(Activity activity, RewardVideoSetting baseSetting) {
        super(activity, baseSetting);
        setting = baseSetting;
    }

    @Override
    protected void paraLoadAd() {
        KSUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
                startLoad();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });

    }

    private void startLoad() {
        KsScene scene = new KsScene.Builder(KSUtil.getADID(sdkSupplier)).build(); // 此为测试posId，请联系快手平台申请正式posId
        initS2SInf();
        KsAdSDK.getLoadManager().loadRewardVideoAd(scene, new KsLoadManager.RewardVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogUtil.simple(TAG + " onError ");

                handleFailed(code, msg);

            }

            @Override
            public void onRewardVideoResult(@Nullable List<KsRewardVideoAd> list) {
                LogUtil.simple(TAG + "onRewardVideoResult  ");

            }

//                @Override
//                public void onRequestResult(int adNumber) {
//                    LogUtil.simple(TAG + "onRequestResult，广告填充数量：" + adNumber);
//                }

            @Override
            public void onRewardVideoAdLoad(@Nullable List<KsRewardVideoAd> list) {
                LogUtil.simple(TAG + " onRewardVideoAdLoad");
                try {
                    if (list == null || list.size() == 0 || list.get(0) == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                    } else {
                        ad = list.get(0);
                        rewardVideoItem = new KSRewardItem(null, KSRewardAdapter.this, ad);
                        ad.setRewardAdInteractionListener(KSRewardAdapter.this);
                        updateBidding(ad.getECPM());
                        handleSucceed();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                }
            }
        });
    }


    //服务端回调参数设置
    private void initS2SInf() {
        try {
            // 此为测试posId，请联系快手平台申请正式posId
            KsScene.Builder builder = new KsScene.Builder(KSUtil.getADID(sdkSupplier));
            // 激励视频服务端回调的参数设置
            Map<String, String> rewardCallbackExtraData = new HashMap<>();
            // 开发者系统中的用户id，会在请求客户的回调url中带上
            rewardCallbackExtraData.put("thirdUserId", setting.getUserId());
            // 开发者自定义的附加参数，会在请求客户的回调url中带上
            rewardCallbackExtraData.put("extraData", setting.getExtraInfo());
            builder.rewardCallbackExtraData(rewardCallbackExtraData);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void adReady() {


    }

    @Override
    public void doDestroy() {
    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
        }
    }


    //--------广告回调--------

    @Override
    public void onAdClicked() {
        LogUtil.simple(TAG + " onAdClicked");

        handleClick();
    }

    @Override
    public void onPageDismiss() {
        LogUtil.simple(TAG + " onPageDismiss");
        if (setting != null) {
            setting.adapterAdClose();
        }
    }

    @Override
    public void onVideoPlayError(int code, int extra) {
        String msg = " onVideoPlayError,code = " + code + ",extra = " + extra;
        LogUtil.simple(TAG + msg);

        handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, msg);
    }

    @Override
    public void onVideoPlayEnd() {
        LogUtil.simple(TAG + " onVideoPlayEnd");
        if (setting != null) {
            setting.adapterVideoComplete();
        }
    }

    @Override
    public void onVideoSkipToEnd(long l) {
        LogUtil.simple(TAG + " onVideoSkipToEnd，l=" + l);
        if (null != setting) {
            setting.adapterVideoSkipped();
        }
    }

    @Override
    public void onVideoPlayStart() {
        LogUtil.simple(TAG + " onVideoPlayStart");

        handleShow();
    }

    @Override
    public void onRewardVerify() {
        LogUtil.simple(TAG + " onRewardVerify");
        try {
            if (setting != null) {
                setting.adapterAdReward();

                RewardServerCallBackInf inf = new RewardServerCallBackInf();
                inf.rewardVerify = true;
                if (sdkSupplier != null) {
                    inf.supId = sdkSupplier.id;
                }
                setting.postRewardServerInf(inf);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRewardVerify(Map<String, Object> map) {
        LogUtil.simple(TAG + " onRewardVerify -- Map");

    }

    /**
     * 视频激励分阶段回调（激励广告新玩法，相关政策请联系商务或技术支持）
     *
     * @param taskType          当前激励视频所属任务类型
     *                          RewardTaskType.LOOK_VIDEO 观看视频类型             属于浅度奖励类型
     *                          RewardTaskType.LOOK_LANDING_PAGE 浏览落地⻚N秒类型  属于深度奖励类型
     *                          RewardTaskType.USE_APP 下载使用App N秒类型          属于深度奖励类型
     * @param currentTaskStatus 当前所完成任务类型，@RewardTaskType中之一
     */
    @Override
    public void onRewardStepVerify(int taskType, int currentTaskStatus) {
        LogUtil.simple(TAG + " onRewardStepVerify , taskType :" + taskType + "，currentTaskStatus = " + currentTaskStatus);

    }

    /**
     * 额外奖励的回调，在触发激励视频的额外奖励的时候进行通知
     * AD_3.3.25 新增
     *
     * @param extraRewardType 额外奖励的类型，定义在 KsExtraRewardType 中
     */
    @Override
    public void onExtraRewardVerify(@KsExtraRewardType int extraRewardType) {
        LogUtil.simple(TAG + " onExtraRewardVerify , extraRewardType :" + extraRewardType);
    }
    //--------广告回调 结束--------

    @Override
    public void show() {
        try {
            if (isValid()) {
                ad.showRewardVideoAd(setting.getShowActivity(), AdvanceKSManager.getInstance().rewardVideoConfig);
            } else {
                runParaFailed(AdvanceError.parseErr(ERROR_EXCEPTION_SHOW, "RewardNotVis"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        try {
            if (ad != null) {
                return ad.isAdEnable();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return super.isValid();
    }
}
