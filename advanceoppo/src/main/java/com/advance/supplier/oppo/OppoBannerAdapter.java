package com.advance.supplier.oppo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.heytap.msp.mobad.api.ad.BannerAd;
import com.heytap.msp.mobad.api.listener.IBannerAdListener;

import java.util.Map;

public class OppoBannerAdapter extends AdvanceBannerCustomAdapter {
    private BannerAd mBannerAd;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);
        

    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            mBannerAd = new BannerAd(getRealActivity(null), sdkSupplier.adspotid);
            /**
             * 设置Banner广告行为监听器
             */
            mBannerAd.setAdListener(new IBannerAdListener() {
                @Override
                public void onAdReady() {
                    LogUtil.simple(TAG + "onAdReady ");

                    int ecpm = mBannerAd == null ? 0 : mBannerAd.getECPM();
                    handleSucceed(ecpm);
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + " onAdClose");

                    handleClose();
                }

                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + " onAdShow");

                    handleShow();
                }

                @Override
                public void onAdFailed(String s) {
                    //                已废弃，

                }

                @Override
                public void onAdFailed(int code, String errMsg) {
                    LogUtil.simple(TAG + " onAdFailed ");

                    handleFailed(code, errMsg);

                }

                @Override
                public void onAdClick() {
                    LogUtil.simple(TAG + "onAdClick  ，   ");

                    handleClick();
                }
            });
            mBannerAd.loadAd();
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
            if (mBannerAd != null)
                mBannerAd.destroyAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            ViewGroup adContainer = getAdContainer();
            RelativeLayout.LayoutParams rbl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rbl.addRule(RelativeLayout.CENTER_HORIZONTAL);
            View bannerView = mBannerAd.getAdView();
            boolean add = AdvanceUtil.addADView(adContainer, bannerView, rbl);
            if (!add) {
                doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
//                return;
            }

        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
