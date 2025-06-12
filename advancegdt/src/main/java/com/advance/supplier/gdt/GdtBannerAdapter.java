package com.advance.supplier.gdt;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

public class GdtBannerAdapter extends AdvanceBannerCustomAdapter implements UnifiedBannerADListener {
    private BannerSetting advanceBanner;
    private UnifiedBannerView bv;
    String TAG = "[GdtBannerAdapter] ";

    public GdtBannerAdapter(Activity activity, BannerSetting advanceBanner) {
        super(activity, advanceBanner);
        this.advanceBanner = advanceBanner;
    }

    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void doDestroy() {
        try {
            if (null != bv) {
                bv.destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        try {
            int code = -1;
            String msg = "default onNoAD";
            if (adError != null) {
                code = adError.getErrorCode();
                msg = adError.getErrorMsg();
            }
            LogUtil.e(TAG + " onError: code = " + code + " msg = " + msg);
            AdvanceError advanceError = AdvanceError.parseErr(code, msg);

            doBannerFailed(advanceError);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onADReceive() {
        try {
            LogUtil.simple(TAG + "onADReceive");

            if (advanceBanner != null) {
                int refreshValue = advanceBanner.getRefreshInterval();
                LogUtil.high("refreshValue == " + refreshValue);

                if (refreshValue > 0) {
                    //当收到广告后，且有设置刷新间隔，代表目前正在刷新中
                    refreshing = true;
                }
            }
            if (bv != null) {
                updateBidding(bv.getECPM());
            }
            handleSucceed();
        } catch (Throwable e) {
            e.printStackTrace();
//            if (advanceBanner != null)
//                advanceBanner.adapterDidFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onADExposure() {
        LogUtil.simple(TAG + "onADExposure");

        handleShow();
    }

    @Override
    public void onADClosed() {
        LogUtil.simple(TAG + "onADClosed");

        if (null != advanceBanner) {
            advanceBanner.adapterDidDislike();
        }
    }

    @Override
    public void onADClicked() {
        LogUtil.simple(TAG + "onADClicked");

        handleClick();

    }

    @Override
    public void onADLeftApplication() {
        LogUtil.simple(TAG + "onADLeftApplication");

    }


    @Override
    protected void paraLoadAd() {
        GdtUtil.initAD(this);
        bv = new UnifiedBannerView(activity, sdkSupplier.adspotid, this);
        if (advanceBanner != null) {
            int refreshValue = advanceBanner.getRefreshInterval();
            bv.setRefresh(refreshValue);
        }
        /* 发起广告请求，收到广告数据后会展示数据   */
        bv.loadAD();
    }

    @Override
    protected void adReady() {

//        if (null != advanceBanner) {
//            ViewGroup adContainer = advanceBanner.getContainer();
//            if (adContainer != null) {
//                adContainer.removeAllViews();
//                adContainer.addView(bv, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            }
//        }
    }

    @Override
    public void show() {
        try {
            ViewGroup adContainer = advanceBanner.getContainer();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            boolean add = AdvanceUtil.addADView(adContainer, bv, lp);
            if (!add) {
                doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        if (bv != null) {
            return bv.isValid();
        }
        return super.isValid();
    }


}
