package com.advance.supplier.sigmob;

import android.app.Activity;


import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
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

    public SigmobSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting setting) {
        super(softReferenceActivity, setting);
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

    private void startLoad() {
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

                    if (splashAd != null)
                        updateBidding(SigmobUtil.getEcpmNumber(splashAd.getEcpm()));

                    handleSucceed();
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
                    if (splashSetting != null) {
                        if (isSkip) {
                            splashSetting.adapterDidSkip();
                        } else {
                            splashSetting.adapterDidTimeOver();
                        }
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
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (splashAd != null) {
                splashAd.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            if (splashAd != null) {
                splashAd.show(splashSetting.getAdContainer());
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
