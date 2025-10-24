package com.advance.supplier.vv;

import android.app.Activity;
import android.view.View;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.vivo.mobilead.unified.banner.UnifiedVivoBannerAd;
import com.vivo.mobilead.unified.banner.UnifiedVivoBannerAdListener;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;


//注意：此广告类型不支持
public class VivoBannerAdapter extends AdvanceBannerCustomAdapter {
    UnifiedVivoBannerAd vivoBannerAd;
    View adView;

    public VivoBannerAdapter(Activity activity, BannerSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
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
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (vivoBannerAd != null) {
            vivoBannerAd.destroy();
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            if (adView == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "adView null"));
                return;
            }

            //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
            boolean add = AdvanceUtil.addADView(bannerSetting.getContainer(), adView);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        //如果不在需要使用到banner广告，请及时销毁
        if (vivoBannerAd != null) {
            vivoBannerAd.destroy();
        }
        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);
        if (builder != null) {
            //设置刷新频率
            if (bannerSetting!=null){
                builder.setRefreshIntervalSeconds(bannerSetting.getRefreshInterval());
            }
            adParams = builder.build();
        }
        //父容器，可能为空
        View container = null ;
        if (bannerSetting!=null){
            container = bannerSetting.getContainer();
        }
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
                handleSucceed();
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
