package com.advance.supplier.mry;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.mercury.sdk.core.banner.BannerAD;
import com.mercury.sdk.core.banner.BannerADListener;
import com.mercury.sdk.util.ADError;


import java.util.Map;

public class MercuryBannerAdapter extends AdvanceBannerCustomAdapter implements BannerADListener {
    private BannerAD mercuryBanner;
    String TAG = "[MercuryBannerAdapter] ";

    @Override
    public void onADReceived() {
        try {
            LogUtil.simple(TAG + "onADReceived");
            if (bannerSetting != null) {
                int refreshValue = bannerSetting.getRefreshInterval();
                LogUtil.high(TAG + "refreshValue == " + refreshValue);

                if (refreshValue > 0) {
                    //当收到广告后，且有设置刷新间隔，代表目前正在刷新中
                    refreshing = true;
                }
            }


            //旧版本SDK中不包含价格返回方法，catch住
            try {
                int cpm = mercuryBanner.getEcpm();
                updateBidding(cpm);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            handleSucceed(this);
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onADClosed() {
        LogUtil.simple(TAG + "onADClosed");

        handleClose();
    }

    @Override
    public void onADLeftApplication() {
        LogUtil.simple(TAG + "onADLeftApplication");

    }

    @Override
    public void onADExposure() {
        LogUtil.simple(TAG + "onADExposure");

        handleShow();
    }

    @Override
    public void onADClicked() {
        LogUtil.simple(TAG + "onADClicked");

        handleClick();
    }

    @Override
    public void onNoAD(ADError adError) {
        int code = -1;
        String msg = "default onNoAD";
        if (adError != null) {
            code = adError.code;
            msg = adError.msg;
        }

        LogUtil.simple(" onError: code = " + code + " msg = " + msg);
        AdvanceError advanceError = AdvanceError.parseErr(code, msg);

        doBannerFailed(advanceError);

    }


    @Override
    public void destroyAd() {
        try {
            if (mercuryBanner != null)
                mercuryBanner.destroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);
//
//
//        //检查是否命中使用缓存逻辑
//        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, MercuryBannerAdapter.class, new BYAbsCallBack<MercuryBannerAdapter>() {
//            @Override
//            public void invoke(MercuryBannerAdapter cacheAdapter) {
//
//                //更新缓存广告得价格
//                updateBidding(cacheAdapter.mercuryBanner.getEcpm());
//            }
//        });
//        if (hitCache) {
//            return;
//        }
        
        if (mercuryBanner != null) {
            mercuryBanner.destroy();
        }
        mercuryBanner = new BannerAD(activity, sdkSupplier.adspotid, this);

        mercuryBanner.loadOnly();
    }

    @Override
    protected void adPrepared() {
//        if (null != advanceBanner) {
//            ViewGroup adContainer = advanceBanner.getContainer();
//            if (adContainer != null) {
//                RelativeLayout.LayoutParams rbl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                rbl.addRule(RelativeLayout.CENTER_HORIZONTAL);
//                adContainer.removeAllViews();
//                adContainer.addView(mercuryBanner, rbl);
//            }
//        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            ViewGroup adContainer = getAdContainer();
            RelativeLayout.LayoutParams rbl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rbl.addRule(RelativeLayout.CENTER_HORIZONTAL);
            boolean add = AdvanceUtil.addADView(adContainer, mercuryBanner, rbl);
            if (!add) {
                doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
                return;
            }
            if (mercuryBanner != null) {
                mercuryBanner.showAD();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        if (mercuryBanner != null) {
            return mercuryBanner.isValid();
        }
           return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }
}
