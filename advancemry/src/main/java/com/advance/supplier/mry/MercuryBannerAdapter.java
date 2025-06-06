package com.advance.supplier.mry;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.mercury.sdk.core.banner.BannerAD;
import com.mercury.sdk.core.banner.BannerADListener;
import com.mercury.sdk.util.ADError;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

public class MercuryBannerAdapter extends AdvanceBannerCustomAdapter implements BannerADListener {
    private BannerSetting advanceBanner;
    private BannerAD mercuryBanner;
    String TAG = "[MercuryBannerAdapter] ";

    public MercuryBannerAdapter(Activity activity, BannerSetting advanceBanner) {
        super(activity, advanceBanner);
        this.advanceBanner = advanceBanner;
    }

    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD, " orderLoadAd Throwable"));
        }
    }

    @Override
    public void onADReceived() {
        try {
            LogUtil.simple(TAG + "onADReceived");
            if (advanceBanner != null) {
                int refreshValue = advanceBanner.getRefreshInterval();
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
            handleSucceed();
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
//            if (advanceBanner != null)
//                advanceBanner.adapterDidFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onADClosed() {
        LogUtil.simple(TAG + "onADClosed");

        if (null != advanceBanner) {
            advanceBanner.adapterDidDislike();
        }
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
    public void doDestroy() {
        try {
            if (mercuryBanner != null)
                mercuryBanner.destroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paraLoadAd() {
        AdvanceUtil.initMercuryAccount(sdkSupplier.mediaid, sdkSupplier.mediakey);
        if (mercuryBanner != null) {
            mercuryBanner.destroy();
        }
        mercuryBanner = new BannerAD(activity, sdkSupplier.adspotid, this);
        try {
            if (null != advanceBanner) {
                if (advanceBanner.getRefreshInterval() > 0) {
                    mercuryBanner.setRefresh(advanceBanner.getRefreshInterval());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.e("当前版本Mercury SDK不支持banner自动刷新，请更新Mercury版本至3.2.1以上");
        }

        mercuryBanner.loadOnly();
    }

    @Override
    protected void adReady() {
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

    @Override
    public void show() {
        try {
            ViewGroup adContainer = advanceBanner.getContainer();
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
        return super.isValid();
    }
}
