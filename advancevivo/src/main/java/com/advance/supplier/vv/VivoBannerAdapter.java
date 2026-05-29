package com.advance.supplier.vv;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.vivo.mobilead.unified.banner.UnifiedVivoBannerAd;
import com.vivo.mobilead.unified.banner.UnifiedVivoBannerAdListener;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;

import java.util.Map;


//注意：此广告类型不支持
public class VivoBannerAdapter extends AdvanceBannerCustomAdapter {
    UnifiedVivoBannerAd vivoBannerAd;
    View adView;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        VivoUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                loadAd();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });

    }

    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        if (vivoBannerAd != null) {
            vivoBannerAd.destroy();
        }
    }


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (adView == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "adView null"));
                return;
            }

            //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
            boolean add = AdvanceUtil.addADView(getAdContainer(), adView);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {


        //检查是否命中使用缓存逻辑
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, VivoBannerAdapter.class, new BYAbsCallBack<VivoBannerAdapter>() {
            @Override
            public void invoke(VivoBannerAdapter cacheAdapter) {

                //更新缓存广告得价格
//                updateBidding();
            }
        });
        if (hitCache) {
            return;
        }

        //如果不在需要使用到banner广告，请及时销毁
        if (vivoBannerAd != null) {
            vivoBannerAd.destroy();
        }
        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);
        if (builder != null) {
            //设置刷新频率
            if (bannerSetting != null) {
                builder.setRefreshIntervalSeconds(bannerSetting.getRefreshInterval());
            }
            adParams = builder.build();
        }
        //父容器，可能为空
        View container = getAdContainer();
        vivoBannerAd = new UnifiedVivoBannerAd(getRealActivity(container), adParams, new UnifiedVivoBannerAdListener() {
            @Override
            public void onAdShow() {
                LogUtil.simple(TAG + "onAdShow...");

                handleShow();
            }

            @Override
            public void onAdFailed(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onAdFailed... , vivoAdError = " + vivoAdError);

                VivoUtil.handleErr(VivoBannerAdapter.this, vivoAdError, AdvanceError.ERROR_LOAD_SDK, "onAdFailed");
            }

            @Override
            public void onAdReady(View view) {
                LogUtil.simple(TAG + "onAdReady...");
                adView = view;
//             todo  不支持bidding？
//                updateBidding(VivoUtil.getPrice(vivoBannerAd));
                handleSucceed(VivoBannerAdapter.this);
            }

            @Override
            public void onAdClick() {
                LogUtil.simple(TAG + "onAdClick...");

                handleClick();
            }

            @Override
            public void onAdClose() {
                LogUtil.simple(TAG + "onAdClose...");

                handleClose();
            }
        });
        vivoBannerAd.loadAd();
    }
}
