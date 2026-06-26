package com.advance.supplier.mi;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.BannerAd;

import java.util.Map;

public class XMBannerAdapter extends AdvanceBannerCustomAdapter {
    BannerAd bannerAd;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        XMUtil.bid(bannerAd,isWin,winPrice);
    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (bannerAd != null)
            bannerAd.destroy();
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            ViewGroup adContainer = getAdContainer();

            bannerAd.showAd(activity, adContainer, new BannerAd.BannerInteractionListener() {

                @Override
                public void onAdShow() {
                    LogUtil.d(TAG + "onAdShow");

                    // 广告展示
                    handleShow();
                }

                @Override
                public void onAdClick() {
                    LogUtil.d(TAG + "onAdClick");

                    // 广告被点击
                    handleClick();
                }


                @Override
                public void onAdDismiss() {
                    LogUtil.d(TAG + "onAdDismiss");

                    // 点击关闭按钮广告消失回调
                    handleClose();
                }

                @Override
                public void onRenderSuccess() {
                    // 广告渲染成功
                    LogUtil.d(TAG + "onRenderSuccess");

                }

                @Override
                public void onRenderFail(int errorCode, String errorMsg) {
                    LogUtil.d(TAG + "onRenderFail");

                    //广告渲染失败
//                container.setVisibility(View.GONE)
                    handleFailed(errorCode, errorMsg);

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        bannerAd = new BannerAd();
        //(5.3.4新增接口) 请使用最新接口集成
        ADParams params = new ADParams.Builder().setUpId(sdkSupplier.adspotid).build();
        bannerAd.loadAd(params, new BannerAd.BannerLoadListener() {
            //请求成功回调
            @Override
            public void onBannerAdLoadSuccess() {
                LogUtil.d(TAG + "onBannerAdLoadSuccess");

                handleSucceed(bannerAd == null ? 0 : XMUtil.getPrice(bannerAd.getMediaExtraInfo()));
            }

            //请求失败回调
            @Override
            public void onAdLoadFailed(int errorCode, String errorMsg) {
                LogUtil.d(TAG + "onAdLoadFailed");

                handleFailed(errorCode, errorMsg);
            }
        });
    }
}
