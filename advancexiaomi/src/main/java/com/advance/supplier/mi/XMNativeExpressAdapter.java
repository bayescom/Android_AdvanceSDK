package com.advance.supplier.mi;

import android.app.Activity;
import android.content.Context;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.TemplateAd;

import java.util.Map;

public class XMNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    TemplateAd templateAd;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {

        if (templateAd != null) {
            templateAd.destroy();
        }
    }

    

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            templateAd.showAd(getAdContainer(), new TemplateAd.TemplateAdInteractionListener() {
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

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        XMUtil.initAD(this, new AdvanceADNInitResult() {
//            @Override
//            public void success() {
//                loadAd();
//            }
//
//            @Override
//            public void fail(String code, String msg) {
//                handleFailed(code, msg);
//            }
//        });
//    }
//    private void loadAd() {
//        //检查是否命中使用缓存逻辑
//        boolean hitCache = AdvanceCacheUtil.loadWithCacheData(this, TemplateAd.class, new BYAbsCallBack<TemplateAd>() {
//            @Override
//            public void invoke(TemplateAd cacheAD) {
//                templateAd = cacheAD;
//
//                updateBidding(XMUtil.getPrice(cacheAD.getMediaExtraInfo()));
//            }
//        });
//        if (hitCache) {
//            return;
//        }
        
        templateAd = new TemplateAd();

        int width = nativeExpressSetting.getExpressViewWidth();
        int height = nativeExpressSetting.getExpressViewHeight();
        if (nativeExpressSetting.getGdtAutoHeight()) {
            height = 0;
        }


        ADParams params = new ADParams.Builder().setUpId(getPosID()).setAdSize(width, height).build();
        templateAd.loadAd(params, new TemplateAd.TemplateAdLoadListener() {

            @Override
            public void onAdLoaded() {
                // 加载成功, 在需要的时候在此处展示广告
                LogUtil.d(TAG+"onAdLoaded");

                updateBidding(XMUtil.getPrice(templateAd.getMediaExtraInfo()));

                handleSucceed(templateAd);
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
