package com.advance.supplier.sigmob;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAd;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdListener;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdRequest;

import java.util.HashMap;
import java.util.Map;

public class SigmobInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    WindNewInterstitialAd windNewInterstitialAd;

    public SigmobInterstitialAdapter(Activity activity, InterstitialSetting setting) {
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
        windNewInterstitialAd.destroy();
    }

    private void startLoad() {

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

                    if (windNewInterstitialAd != null)
                        updateBidding(SigmobUtil.getEcpmNumber(windNewInterstitialAd.getEcpm()));

                    handleSucceed();
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

    @Override
    public void show() {
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
