package com.advance.supplier.sigmob;

import android.app.Activity;
import android.content.Context;

import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.sigmob.windad.WindAdBiddingLossReason;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAd;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdListener;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdRequest;

import java.util.HashMap;
import java.util.Map;

public class SigmobInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    WindNewInterstitialAd windNewInterstitialAd;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        Map<String, Object> map = new HashMap<>();
        map.put(WindAds.AUCTION_PRICE, winPrice);//获胜价格，建议 Sigmob 渠道胜出后回传 Sigmob 原始出价。其他价格可能会影响实际的结算价格。
        map.put(WindAds.CURRENCY, WindAds.CNY);//汇率
        if (isWin){
            windNewInterstitialAd.sendWinNotificationWithInfo(map);
        }else {
            map.put(WindAds.LOSS_REASON, WindAdBiddingLossReason.LOSS_REASON_LOW_PRICE.getCode()); // 竞败原因
            map.put(WindAds.ADN_ID, SigmobUtil.getLossPlatform(referBidInfo)); // 竞败平台
            windNewInterstitialAd.sendLossNotificationWithInfo(map);
        }
    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        windNewInterstitialAd.destroy();
    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {

            String userId = SigmobSetting.getInstance().userId;
            Map<String, Object> options = new HashMap<>();
            options.put("user_id", userId);

            //placementId 必填,USER_ID,OPTIONS可不填，
            WindNewInterstitialAdRequest request = new WindNewInterstitialAdRequest(sdkSupplier.adspotid, userId, options);

            windNewInterstitialAd = new WindNewInterstitialAd(request);
            windNewInterstitialAd.setWindNewInterstitialAdListener(new WindNewInterstitialAdListener() {
                @Override
                public void onInterstitialAdLoadSuccess(String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdLoadSuccess");


                    handleSucceed(windNewInterstitialAd == null ? 0 : SigmobUtil.getEcpmNumber(windNewInterstitialAd.getEcpm()));
                }

                @Override
                public void onInterstitialAdPreLoadSuccess(String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdPreLoadSuccess");

                }

                @Override
                public void onInterstitialAdPreLoadFail(String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdPreLoadFail");

                }

                @Override
                public void onInterstitialAdShow(String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdShow");

                    handleShow();
                }

                @Override
                public void onInterstitialAdClicked(String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdClicked");

                    handleClick();
                }

                @Override
                public void onInterstitialAdClosed(String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdClosed");

                    handleClose();
                }

                @Override
                public void onInterstitialAdLoadError(WindAdError error, String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdLoadError" + error);

                    SigmobUtil.handlerErr(SigmobInterstitialAdapter.this, error, "");

                }

                @Override
                public void onInterstitialAdShowError(WindAdError error, String placementId) {
                    LogUtil.simple(TAG + "onInterstitialAdShowError" + error);

                    SigmobUtil.handlerErr(SigmobInterstitialAdapter.this, error, AdvanceError.ERROR_RENDER_FAILED);

                }
            });
            windNewInterstitialAd.setCurrency(WindAds.CNY);//设置币种
            windNewInterstitialAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (windNewInterstitialAd != null) {
                windNewInterstitialAd.show(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
