package com.advance.supplier.oppo;

import android.app.Activity;
import android.os.Handler;


import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.heytap.msp.mobad.api.ad.HotSplashAd;
import com.heytap.msp.mobad.api.listener.IHotSplashListener;
import com.heytap.msp.mobad.api.params.SplashAdParams;

import java.lang.ref.SoftReference;

public class OppoSplashAdapter extends AdvanceSplashCustomAdapter {
    private final String TAG = "[OppoSplashAdapter] ";
    private HotSplashAd splashAd;

    public OppoSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting baseSetting) {
        super(softReferenceActivity, baseSetting);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        OppoUtil.initAD(this);
        startLoad();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (splashAd != null) {
                splashAd.destroyAd();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            if (splashAd == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "splashAd null"));
                return;
            }


            splashAd.showAd(getRealActivity(splashSetting.getAdContainer()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void startLoad() {
        try {
            //可以自定义跳过按钮样式结束
            SplashAdParams.Builder builder = new SplashAdParams.Builder()
                    .setFetchTimeout(sdkSupplier.timeout);

            //如果请求时未赋值，将无法展示自定义跳过view
            if (splashSetting.getSkipView() != null) {
                builder.setBottomArea(splashSetting.getSkipView());
            }

            splashAd = new HotSplashAd(getRealContext(), sdkSupplier.adspotid, "", new IHotSplashListener() {
                @Override
                public void onAdReady() {
                    LogUtil.simple(TAG + " onAdReady ");

                    updateBidding(splashAd.getECPM());

                    handleSucceed();
                }

                @Override
                public void onAdDismissed() {
                    LogUtil.simple(TAG + " onAdDismissed ");

                    if (splashSetting != null) {
                        if (isCountingEnd) {
                            splashSetting.adapterDidTimeOver();
                        } else {
                            splashSetting.adapterDidSkip();
                        }
                    }
                }

                @Override
                public void onAdShow(String transportData) {//请求时透传的信息
                    LogUtil.simple(TAG + " onAdShow ");


                    handleShow();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isCountingEnd = true;
                        }
                    }, 4800);
                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);

                }

                @Override
                public void onAdClick() {
                    LogUtil.simple(TAG + " onAdClick ");
                    handleClick();


                }
            }, builder.build());
        } catch (Throwable e) {
            e.printStackTrace();
        }


    }


}
