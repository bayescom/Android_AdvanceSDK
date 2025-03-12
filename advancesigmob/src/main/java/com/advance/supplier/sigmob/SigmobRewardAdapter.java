package com.advance.supplier.sigmob;

import android.app.Activity;

import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardInfo;
import com.sigmob.windad.rewardVideo.WindRewardVideoAd;
import com.sigmob.windad.rewardVideo.WindRewardVideoAdListener;

import java.util.HashMap;
import java.util.Map;

public class SigmobRewardAdapter extends AdvanceRewardCustomAdapter {
    WindRewardVideoAd windRewardVideoAd;
    boolean isValid = false;

    public SigmobRewardAdapter(Activity activity, RewardVideoSetting setting) {
        super(activity, setting);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        SigmobUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法
                startLoad();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        windRewardVideoAd.destroy();
    }

    private void startLoad() {
        try {
            String userId = SigmobSetting.getInstance().userId;
            Map<String, Object> options = new HashMap<>();
            options.put("user_id", userId);

            WindRewardAdRequest rewardAdRequest = new WindRewardAdRequest(sdkSupplier.adspotid, userId, options);

            windRewardVideoAd = new WindRewardVideoAd(rewardAdRequest);
            windRewardVideoAd.setWindRewardVideoAdListener(new WindRewardVideoAdListener() {
                @Override
                public void onRewardAdLoadSuccess(String placementId) {
                    LogUtil.simple(TAG + "onRewardAdLoadSuccess");

                    isValid = true;
                    if (windRewardVideoAd != null)
                        updateBidding(SigmobUtil.getEcpmNumber(windRewardVideoAd.getEcpm()));

                    handleSucceed();
                }

                @Override
                public void onRewardAdPreLoadSuccess(String placementId) {
                    LogUtil.simple(TAG + "onRewardAdPreLoadFail");

                }

                @Override
                public void onRewardAdPreLoadFail(String placementId) {
                    LogUtil.simple(TAG + "onRewardAdPreLoadFail");

                }

                @Override
                public void onRewardAdPlayStart(String placementId) {
                    LogUtil.simple(TAG + "onRewardAdPlayStart");

                    handleShow();
                }

                @Override
                public void onRewardAdPlayEnd(String placementId) {
                    LogUtil.simple(TAG + "onRewardAdPlayEnd");

                    handleComplete();
                }

                @Override
                public void onRewardAdClicked(String placementId) {
                    LogUtil.simple(TAG + "onRewardAdClicked");

                    handleClick();
                }

                @Override
                public void onRewardAdClosed(String placementId) {
                    LogUtil.simple(TAG + "onRewardAdClosed");

                    handleClose();
                }

                @Override
                public void onRewardAdRewarded(WindRewardInfo rewardInfo, String placementId) {
                    LogUtil.simple(TAG + "onRewardAdRewarded");


                    if (null != setting) {
                        RewardServerCallBackInf inf = new RewardServerCallBackInf();

                        if (null != rewardInfo) {
                            inf.rewardVerify = rewardInfo.isReward();
                            if (inf.rewardVerify) {
                                setting.adapterAdReward();
                            }
                        }
                        setting.postRewardServerInf(inf);
                    }
                }

                @Override
                public void onRewardAdLoadError(WindAdError error, String placementId) {
                    LogUtil.simple(TAG + "onRewardAdLoadError" + error);

                    SigmobUtil.handlerErr(SigmobRewardAdapter.this, error, "");
                }

                @Override
                public void onRewardAdPlayError(WindAdError error, String placementId) {
                    LogUtil.simple(TAG + "onRewardAdPlayError" + error);

                    SigmobUtil.handlerErr(SigmobRewardAdapter.this, error, AdvanceError.ERROR_RENDER_FAILED);

                }
            });
            windRewardVideoAd.setCurrency(WindAds.CNY);//设置币种
            windRewardVideoAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        try {
            return windRewardVideoAd.isReady();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isValid;
    }

    @Override
    public void show() {
        try {
            windRewardVideoAd.show(null);
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
