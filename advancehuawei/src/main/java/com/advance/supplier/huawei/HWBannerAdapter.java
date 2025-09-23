package com.advance.supplier.huawei;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.widget.BYViewUtil;
import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.banner.BannerView;

import java.util.Locale;

public class HWBannerAdapter extends AdvanceBannerCustomAdapter {

    BannerView bannerView;

    //记录点击触发的时间，用来辅助判断是否点击了关闭按钮才回调Ad Leave
    long clickTime = 0;
    long openTime = 0;
    boolean isLandingPage = false;

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

            //监听布局可见性，因为华为未回调曝光事件，所以需要在此检查view展示有效性。todo 测试自动刷新，以及不刷新时执行情况
            new BYViewUtil().onVisibilityChange(bannerView, new BYViewUtil.VisChangeListener() {
                @Override
                public void onChange(View view, boolean isVisible) {
                    if (isVisible && !hasShown) {
                        handleShow();
                        hasShown = true;
                    }
                }
            });
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

                openTime = System.currentTimeMillis();
//                handleShow();
            }

            @Override
            public void onAdClicked() {
                // Called when a user taps an ad.
                LogUtil.simple(TAG + "Ad clicked");

                handleClick();
                clickTime = System.currentTimeMillis();
            }

            @Override
            public void onAdLeave() {
                // Called when a user has left the app.
                LogUtil.simple(TAG + "Ad Leave");

                isLandingPage = true;
            }

            @Override
            public void onAdClosed() {
                // Called when an ad is closed.
                LogUtil.simple(TAG + "Ad closed");

                if (isLandingPage && clickTime > 0) {
                    isLandingPage = false;
                    clickTime = 0;
                    LogUtil.simple(TAG + " 判定为点击后导致的广告关闭事件，不对外回调处理");
                    return;
                }
                handleClose();
            }
        });
        AdParam.Builder adParam = AdvanceHWManager.getInstance().globalAdParamBuilder;
        if (adParam == null) {
            adParam = new AdParam.Builder();
        }
        bannerView.loadAd(adParam.build());
    }
}
