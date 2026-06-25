package com.advance.supplier.sigmob;

import android.app.Activity;
import android.content.Context;

import com.advance.RewardServerCallBackInf;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.sigmob.windad.WindAdBiddingLossReason;
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

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);


        Map<String, Object> map = new HashMap<>();
        map.put(WindAds.AUCTION_PRICE, winPrice);//获胜价格，建议 Sigmob 渠道胜出后回传 Sigmob 原始出价。其他价格可能会影响实际的结算价格。
        map.put(WindAds.CURRENCY, WindAds.CNY);//汇率
        if (isWin){
            windRewardVideoAd.sendWinNotificationWithInfo(map);
        }else {
            map.put(WindAds.LOSS_REASON, WindAdBiddingLossReason.LOSS_REASON_LOW_PRICE.getCode()); // 竞败原因
            map.put(WindAds.ADN_ID, SigmobUtil.getLossPlatform(referBidInfo)); // 竞败平台
            windRewardVideoAd.sendLossNotificationWithInfo(map);
        }
    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        windRewardVideoAd.destroy();
    }



    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
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

                    handleSucceed(windRewardVideoAd == null ? 0 : SigmobUtil.getEcpmNumber(windRewardVideoAd.getEcpm()));
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


                    RewardServerCallBackInf inf = new RewardServerCallBackInf();

                    if (null != rewardInfo) {
                        inf.rewardVerify = rewardInfo.isReward();
                        if (inf.rewardVerify) {
                            handleReward();
                        }
                    }
                    handleRewardInf(inf);

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
        return true;
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            windRewardVideoAd.show(null);
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
