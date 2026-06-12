package com.advance.supplier.custom;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class CustomADNYLHBannerAdapter extends AdvanceBannerCustomAdapter implements UnifiedBannerADListener {
    private UnifiedBannerView bv;
    String TAG = "[CustomADNYLHBannerAdapter] ";

    @Override
    public void destroyAd() {
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

            if (bannerSetting != null) {
                int refreshValue = bannerSetting.getRefreshInterval();
                LogUtil.high("refreshValue == " + refreshValue);

                if (refreshValue > 0) {
                    //当收到广告后，且有设置刷新间隔，代表目前正在刷新中
                    refreshing = true;
                }
            }
            double ecpm = 0;
            if (bv != null) {
                ecpm = (bv.getECPM());
            }
            handleSucceed(ecpm);
        } catch (Throwable e) {
            e.printStackTrace();
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

        handleClose();
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


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        GdtUtil.initAD(this, new BYBaseCallBack() {
//            @Override
//            public void call() {
//                loadAd();
//            }
//        });
//    }
//    public void loadAd() {
//
//        //检查是否命中使用缓存逻辑
//        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, CustomADNYLHBannerAdapter.class, new BYAbsCallBack<CustomADNYLHBannerAdapter>() {
//            @Override
//            public void invoke(CustomADNYLHBannerAdapter cacheAdapter) {
//                //更新缓存广告得价格
//                updateBidding(cacheAdapter.bv.getECPM());
//            }
//        });
//        if (hitCache) {
//            return;
//        }

        bv = new UnifiedBannerView(activity, sdkSupplier.adspotid, this);
        if (bannerSetting != null) {
            int refreshValue = bannerSetting.getRefreshInterval();
            bv.setRefresh(refreshValue);
        }
        /* 发起广告请求，收到广告数据后会展示数据   */
        bv.loadAD();
    }

    @Override
    protected void adPrepared() {

//        if (null != advanceBanner) {
//            ViewGroup adContainer = advanceBanner.getContainer();
//            if (adContainer != null) {
//                adContainer.removeAllViews();
//                adContainer.addView(bv, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            }
//        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            ViewGroup adContainer = getAdContainer();
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
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);
        

    }


}
