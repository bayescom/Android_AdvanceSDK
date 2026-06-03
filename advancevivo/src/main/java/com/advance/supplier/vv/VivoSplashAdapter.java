package com.advance.supplier.vv;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.splash.UnifiedVivoSplashAd;
import com.vivo.mobilead.unified.splash.UnifiedVivoSplashAdListener;
import com.vivo.mobilead.unified.splash.pro.ProVivoSplashAd;
import com.vivo.mobilead.unified.splash.pro.ProVivoSplashAdListener;
import com.vivo.mobilead.unified.splash.pro.VSplashAd;

import java.util.Map;

public class VivoSplashAdapter extends AdvanceSplashCustomAdapter {
    UnifiedVivoSplashAd vivoSplashAd;
    View adView;
    boolean usePro = true; //是否使用2.0版本开屏广告

    ProVivoSplashAd vivoSplashAd2;
    VSplashAd splashPro;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }



    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {

        if (vivoSplashAd != null) {
            vivoSplashAd.destroy();
        }
    }

    

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            if (usePro && splashPro != null) {
                adView = splashPro.getAdView();
                splashPro.setSplashInteractionListener(new VSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdShow() {
                        LogUtil.simple(TAG + "onAdShow...");

                        handleShow();
                    }

                    @Override
                    public void onAdClick() {
                        LogUtil.simple(TAG + "onAdClick...");

                        handleClick();
                    }

                    @Override
                    public void onAdSkip() {
                        LogUtil.simple(TAG + "onAdSkip...");

                        handleSkip();
                    }

                    @Override
                    public void onAdTimeOver() {
                        LogUtil.simple(TAG + "onAdTimeOver...");

                        handleTimeOver();
                    }

                    @Override
                    public void onAdFailed(VivoAdError vivoAdError) {
                        LogUtil.simple(TAG + "onAdFailed...");

                        VivoUtil.handleErr(VivoSplashAdapter.this, vivoAdError, AdvanceError.ERROR_RENDER_FAILED, "onAdFailed");
                    }
                });
            }
            if (adView == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "adView null"));
                return;
            }

            //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽
            boolean add = AdvanceUtil.addADView(splashSetting.getAdContainer(), adView);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
//        VivoUtil.initAD(this, new AdvanceADNInitResult() {
//            @Override
//            public void success() {
//                loadAd();
//            }
//
//            @Override
//            public void fail(String code, String msg) {
//                handleFailed(code, msg);
//            }
//        });
//
//    }
//    private void loadAd() {
//
//        //检查是否命中使用缓存逻辑
//        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, VivoSplashAdapter.class, new BYAbsCallBack<VivoSplashAdapter>() {
//            @Override
//            public void invoke(VivoSplashAdapter cacheAdapter) {
//
//                if (usePro){
//                    //更新缓存广告得价格
//                    updateBidding(VivoUtil.getPrice(cacheAdapter.splashPro));
//                }else {
//                    //更新缓存广告得价格
//                    updateBidding(VivoUtil.getPrice(cacheAdapter.vivoSplashAd));
//                }
//
//            }
//        });
//        if (hitCache) {
//            return;
//        }
        if (sdkSupplier.versionTag == 1) {
            usePro = false;
        }
        LogUtil.simple(TAG + "usePro = " + usePro);


        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);
        if (builder != null) {
            adParams = builder.build();
        }

        if (usePro) {
            load2(adParams);
        } else {
            load1(adParams);
        }

    }

    private void load2(AdParams adParams) {
        vivoSplashAd2 = new ProVivoSplashAd(getRealActivity(splashSetting.getAdContainer()), new ProVivoSplashAdListener() {
            @Override
            public void onAdFailed(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onAdFailed... , vivoAdError = " + vivoAdError);

                VivoUtil.handleErr(VivoSplashAdapter.this, vivoAdError, AdvanceError.ERROR_LOAD_SDK, "onAdFailed");
            }

            @Override
            public void onAdLoadSuccess(VSplashAd vSplashAd) {
                LogUtil.simple(TAG + "onAdLoadSuccess...");

                splashPro = vSplashAd;
                updateBidding(VivoUtil.getPrice(splashPro));
                handleSucceed(VivoSplashAdapter.this);
            }
        }, adParams);
        vivoSplashAd2.loadAd();

    }

    private void load1(AdParams adParams) {
        vivoSplashAd = new UnifiedVivoSplashAd(getRealActivity(splashSetting.getAdContainer()), new UnifiedVivoSplashAdListener() {
            @Override
            public void onAdShow() {
                LogUtil.simple(TAG + "onAdShow...");

                handleShow();
            }

            @Override
            public void onAdFailed(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onAdFailed... , vivoAdError = " + vivoAdError);

                VivoUtil.handleErr(VivoSplashAdapter.this, vivoAdError, AdvanceError.ERROR_LOAD_SDK, "onAdFailed");
            }

            @Override
            public void onAdReady(View view) {
                LogUtil.simple(TAG + "onAdReady...");
                adView = view;
                updateBidding(VivoUtil.getPrice(vivoSplashAd));
                handleSucceed(VivoSplashAdapter.this);
            }

            @Override
            public void onAdClick() {
                LogUtil.simple(TAG + "onAdClick...");

                handleClick();
            }

            @Override
            public void onAdSkip() {
                LogUtil.simple(TAG + "onAdSkip...");

                handleSkip();

            }

            @Override
            public void onAdTimeOver() {
                LogUtil.simple(TAG + "onAdTimeOver...");

                handleTimeOver();
            }
        }, adParams);
        vivoSplashAd.loadAd();
    }
}
