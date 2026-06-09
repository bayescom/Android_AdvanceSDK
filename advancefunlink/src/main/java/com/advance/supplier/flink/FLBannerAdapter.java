package com.advance.supplier.flink;

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
import com.fl.saas.adx.api.FLBanner;
import com.fl.saas.adx.base.exception.FLError;
import com.fl.saas.adx.base.interfaces.AdViewBannerListener;

import java.util.Map;

public class FLBannerAdapter extends AdvanceBannerCustomAdapter {
    FLBanner flAd;
    View adView;


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        flAd = new FLBanner.Builder(getRealContext())
                .setKey(getPosID())
// 设置banner的宽度，单位dp，默认值为屏幕宽度，采用默认值时可不用设置
// .setExpressWidth(int)
// 设置banner的高度，单位dp，默认值为宽度1/6.4，采用默认值时可不用设置
// .setExpressHeight(int)
                .setBannerListener(new AdViewBannerListener() {
                    @Override
                    public void onReceived(View view) {
                        LogUtil.simple(TAG + "onReceived  ");

// 请求广告成功，返回banner
                        if (view == null) {
                            String nMsg = TAG + " view null";
                            handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                            return;
                        }
                        adView = view;


                        handleSucceed(flAd.getEcpm());
                    }

                    @Override
                    public void onAdExposure() {
                        LogUtil.simple(TAG + "onAdExposure  ");

                        handleShow();
                    }

                    @Override
                    public void onAdFailed(FLError error) {
// 广告异常、失败，回调该方法
                        LogUtil.simple(TAG + "onAdFailed , error = " + error);

                        FLUtil.handleErr(FLBannerAdapter.this, error);
                    }

                    @Override
                    public void onAdClick(String s) {
// 广告被点击时回调该方法
                        LogUtil.simple(TAG + "onAdClick ,s = " + s);

                        handleClick();
                    }

                    @Override
                    public void onClosed() {
// 广告关闭时回调该方法
                        LogUtil.simple(TAG + "onClosed  ");

                        handleClose();
                    }
                })
                .build();
        flAd.requestBanner();


    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (flAd != null) {
            flAd.destroy();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            ViewGroup adContainer = getAdContainer();
            RelativeLayout.LayoutParams rbl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rbl.addRule(RelativeLayout.CENTER_HORIZONTAL);
            boolean add = AdvanceUtil.addADView(adContainer, adView, rbl);
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
        if (flAd != null) {
            return flAd.isReady();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double price, Map<String, Object> referBidInfo) {

    }
}
