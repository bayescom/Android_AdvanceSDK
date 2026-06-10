package com.advance.supplier.sigmob;

import android.app.Activity;
import android.content.Context;


import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.sigmob.windad.Splash.WindSplashAD;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class SigmobSplashAdapter extends AdvanceSplashCustomAdapter {
    private WindSplashAD splashAd;

    private boolean isSkip = false;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {

    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {

            String userId = SigmobSetting.getInstance().userId;
            Map<String, Object> options = new HashMap<>();
            options.put("user_id", userId);

            WindSplashAdRequest splashAdRequest = new WindSplashAdRequest(sdkSupplier.adspotid, userId, options);

            splashAd = new WindSplashAD(splashAdRequest, new WindSplashADListener() {
                @Override
                public void onSplashAdShow(String placementId) {
                    LogUtil.simple(TAG + "onSplashAdShow");

                    handleShow();

                }

                @Override
                public void onSplashAdLoadSuccess(String placementId) {
                    LogUtil.simple(TAG + "onSplashAdLoadSuccess");

                    handleSucceed(splashAd == null ? 0 : SigmobUtil.getEcpmNumber(splashAd.getEcpm()));
                }

                @Override
                public void onSplashAdLoadFail(WindAdError error, String placementId) {
                    LogUtil.simple(TAG + "onSplashAdLoadFail" + error.toString());

                    SigmobUtil.handlerErr(SigmobSplashAdapter.this, error, "");

                }

                @Override
                public void onSplashAdShowError(WindAdError error, String placementId) {
                    LogUtil.simple(TAG + "onSplashAdShowError : " + error);

                    SigmobUtil.handlerErr(SigmobSplashAdapter.this, error, AdvanceError.ERROR_RENDER_FAILED);

                }

                @Override
                public void onSplashAdClick(String placementId) {
                    LogUtil.simple(TAG + "onSplashAdClick");

                    handleClick();
                }

                @Override
                public void onSplashAdClose(String placementId) {
                    LogUtil.simple(TAG + "onSplashAdClose");
                    if (isSkip) {
                        handleSkip();
                    } else {
                        handleTimeOver();
                    }
                }

                @Override
                public void onSplashAdSkip(String s) {
                    LogUtil.simple(TAG + "onSplashAdSkip");

                    isSkip = true;
                }
            });

            splashAd.setCurrency(WindAds.CNY);//设置币种
            splashAd.loadAd();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        try {
            if (splashAd != null) {
                splashAd.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (splashAd != null) {
                splashAd.show(getAdContainer());
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
