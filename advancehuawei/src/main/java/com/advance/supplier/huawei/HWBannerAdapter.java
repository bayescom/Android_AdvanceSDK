package com.advance.supplier.huawei;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.banner.BannerView;

import java.util.Locale;

public class HWBannerAdapter extends AdvanceBannerCustomAdapter {

    BannerView bannerView;

    public HWBannerAdapter(Activity activity, BannerSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        loadAd();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (bannerView != null) {
                bannerView.destroy();
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void orderLoadAd() {
        loadAd();
    }

    @Override
    public void show() {
        try {
            ViewGroup adContainer = bannerSetting.getContainer();
            RelativeLayout.LayoutParams rbl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rbl.addRule(RelativeLayout.CENTER_HORIZONTAL);
            boolean add = AdvanceUtil.addADView(adContainer, bannerView, rbl);
            if (!add) {
                doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
                return;
            }
//            if (bannerView != null) {
//                bannerView.showAD();
//            }
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        //先执行SDK初始化
        HWUtil.initAD(this);

        // Call new BannerView(Context context) to create a BannerView class.
        bannerView = new BannerView(getRealContext());
        // Set an ad slot ID.
        bannerView.setAdId(sdkSupplier.adspotid);
        // Set the background color and size based on user selection.
        BannerAdSize adSize = AdvanceHWManager.getInstance().bannerAdSize;
        if (adSize != null) {
            bannerView.setBannerAdSize(adSize);
        }

        int color = AdvanceHWManager.getInstance().bannerBGColor;
        if (color > 0) {
            bannerView.setBackgroundColor(color);
        }

        if (bannerSetting != null) {
            int refreshSec = bannerSetting.getRefreshInterval();
            if (refreshSec > 0) {
                // 设置轮播时间间隔为XX秒
                bannerView.setBannerRefresh(refreshSec);
            }
        }

        bannerView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Called when an ad is loaded successfully.
                LogUtil.simple(TAG + "Ad loaded.");

                if (bannerView != null) {
                    updateBidding(HWUtil.getPrice(bannerView.getBiddingInfo()));
                }

                handleSucceed();
            }

            @Override
            public void onAdFailed(int errorCode) {
                // Called when an ad fails to be loaded.
                LogUtil.simple(TAG + String.format(Locale.ROOT, "Ad failed to load with error code %d.", errorCode));
            }

            @Override
            public void onAdOpened() {
                // Called when an ad is opened.
                LogUtil.simple(TAG + String.format("Ad opened "));

                handleShow();
            }

            @Override
            public void onAdClicked() {
                // Called when a user taps an ad.
                LogUtil.simple(TAG + "Ad clicked");
                handleClick();
            }

            @Override
            public void onAdLeave() {
                // Called when a user has left the app.
                LogUtil.simple(TAG + "Ad Leave");
            }

            @Override
            public void onAdClosed() {
                // Called when an ad is closed.
                LogUtil.simple(TAG + "Ad closed");

                handleClose();
            }
        });
        AdParam adParam = AdvanceHWManager.getInstance().globalAdParam;
        if (adParam == null) {
            adParam = new AdParam.Builder().build();
        }
        bannerView.loadAd(adParam);
    }
}
