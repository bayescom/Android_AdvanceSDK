package com.advance.supplier.mi;

import android.app.Activity;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.TemplateAd;

public class XMNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    TemplateAd templateAd;

    public XMNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
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

        if (templateAd != null) {
            templateAd.destroy();
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            templateAd.showAd(mSetting.getAdContainer(), new TemplateAd.TemplateAdInteractionListener() {
                @Override
                public void onAdShow() {
                    LogUtil.d(TAG+"onAdShow");

                    handleShow();
                }

                @Override
                public void onAdClick() {
                    LogUtil.d(TAG+"onAdClick");

                    handleClick();
                }

                @Override
                public void onAdDismissed() {
                    LogUtil.d(TAG+"onAdDismissed");

                    handleClose();
                }

                @Override
                public void onAdRenderFailed(int errorCode, String errorMsg) {
                    LogUtil.d(TAG+"onAdRenderFailed");

                    handleFailed(errorCode, errorMsg);

                }
                //5.3.4添加showAd方法 使用同show（）
            });
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        templateAd = new TemplateAd();

        int width = mSetting.getExpressViewWidth();
        int height = mSetting.getExpressViewHeight();
        if (mSetting.getGdtAutoHeight()) {
            height = 0;
        }


        ADParams params = new ADParams.Builder().setUpId(getPosID()).setAdSize(width, height).build();
        templateAd.loadAd(params, new TemplateAd.TemplateAdLoadListener() {

            @Override
            public void onAdLoaded() {
                // 加载成功, 在需要的时候在此处展示广告
                LogUtil.d(TAG+"onAdLoaded");

                updateBidding(XMUtil.getPrice(templateAd.getMediaExtraInfo()));

                handleSucceed();
            }

            @Override
            public void onAdLoadFailed(int errorCode, String errorMsg) {
                // 加载失败
                LogUtil.d(TAG+"onAdLoadFailed");

                handleFailed(errorCode, errorMsg);

            }

        });

    }

}
