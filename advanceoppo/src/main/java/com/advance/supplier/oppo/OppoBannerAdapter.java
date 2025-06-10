package com.advance.supplier.oppo;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.heytap.msp.mobad.api.ad.BannerAd;
import com.heytap.msp.mobad.api.listener.IBannerAdListener;

public class OppoBannerAdapter extends AdvanceBannerCustomAdapter {
    BannerSetting setting;
    private BannerAd mBannerAd;

    public OppoBannerAdapter(Activity activity, BannerSetting setting) {
        super(activity, setting);
        this.setting = setting;
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

    private void startLoad() {
        try {
            mBannerAd = new BannerAd(getRealActivity(null), sdkSupplier.adspotid);
            /**
             * 设置Banner广告行为监听器
             */
            mBannerAd.setAdListener(new IBannerAdListener() {
                @Override
                public void onAdReady() {
                    LogUtil.simple(TAG + "onAdReady ");

                    updateBidding(mBannerAd.getECPM());

                    handleSucceed();
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + " onAdClose");

                    if (setting != null)
                        setting.adapterDidDislike();
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
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (mBannerAd != null)
                mBannerAd.destroyAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    @Override
    public void show() {
        try {
            ViewGroup adContainer = setting.getContainer();
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
