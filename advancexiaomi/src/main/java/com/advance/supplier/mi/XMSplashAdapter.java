package com.advance.supplier.mi;

import android.app.Activity;

import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.SplashAd;

import java.lang.ref.SoftReference;

public class XMSplashAdapter extends AdvanceSplashCustomAdapter {
    SplashAd splashAd;

    public XMSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting splashSetting) {
        super(softReferenceActivity, splashSetting);
    }

    @Override
    protected void paraLoadAd() {
        XMUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                loadAd();
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
        if (splashAd != null)
            splashAd.destroy();
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            splashAd.showAd(splashSetting.getAdContainer(), new SplashAd.SplashAdInteractionListener() {

                @Override
                public void onAdShow() {
                    // 广告展示
                    LogUtil.d(TAG+"onAdShow");
                    handleShow();
                }

                @Override
                public void onAdClick() {
                    // 广告被点击
                    LogUtil.d(TAG+"onAdClick");
                    handleClick();
                }

                @Override
                public void onAdDismissed() {
                    // 点击关闭按钮广告消失回调

                    LogUtil.d(TAG+"onAdDismissed");
                    if (splashSetting != null) {
                        if (isCountingEnd) {
                            splashSetting.adapterDidTimeOver();
                        } else {
                            splashSetting.adapterDidSkip();
                        }
                    }
                }

                @Override
                public void onAdRenderFailed(int errorCode, String errorMsg) {
                    //广告渲染失败
//                container.setVisibility(View.GONE)
                    LogUtil.d(TAG+"onAdRenderFailed");
                    handleFailed(errorCode, errorMsg);

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        splashAd = new SplashAd();
        ADParams params = new ADParams.Builder().setUpId(sdkSupplier.adspotid).build();
        splashAd.loadAd(params, new SplashAd.SplashAdLoadListener() {

            @Override
            public void onAdRequestSuccess() {
                // 广告请求成功
                LogUtil.d(TAG+"onAdRequestSuccess");

                updateBidding(XMUtil.getPrice(splashAd.getMediaExtraInfo()));

                handleSucceed();
            }

            @Override
            public void onAdLoaded() {
                LogUtil.d(TAG+"onAdLoaded");
                // 广告加载成功，在需要的时候在此处展示广告
            }

            @Override
            public void onAdLoadFailed(int errorCode, String errorMsg) {
                // 广告加载失败
                LogUtil.d(TAG+"onAdLoadFailed");
                handleFailed(errorCode, errorMsg);
            }
        });
    }
}
