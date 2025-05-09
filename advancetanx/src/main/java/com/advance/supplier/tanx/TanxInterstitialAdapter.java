package com.advance.supplier.tanx;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.ad.ITanxAd;
import com.alimm.tanx.core.ad.ad.template.rendering.table.screen.ITanxTableScreenExpressAd;
import com.alimm.tanx.core.ad.bean.TanxBiddingInfo;
import com.alimm.tanx.core.ad.listener.ITanxAdLoader;
import com.alimm.tanx.core.ad.view.TanxAdView;
import com.alimm.tanx.core.request.TanxAdSlot;
import com.alimm.tanx.core.request.TanxError;
import com.alimm.tanx.ui.TanxSdk;

import java.util.List;

public class TanxInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    ITanxAdLoader iTanxAdLoader;
    ITanxTableScreenExpressAd interExpressAD;
    InterstitialSetting interstitialSetting;

    public TanxInterstitialAdapter(Activity activity, InterstitialSetting setting) {
        super(activity, setting);
        interstitialSetting = setting;
    }

    @Override
    public void orderLoadAd() {
        initAD();
    }

    @Override
    protected void paraLoadAd() {
        initAD();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (iTanxAdLoader != null) {
                iTanxAdLoader.destroy();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            if (interExpressAD == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "  interExpressAD null"));
                return;
            }
            TanxBiddingInfo biddingResult = new TanxBiddingInfo();
            biddingResult.setBidResult(true);
            //上报竞价成功
            interExpressAD.setBiddingResult(biddingResult);
            interExpressAD.setOnTableScreenAdListener(new ITanxTableScreenExpressAd.OnTableScreenAdListener() {
                @Override
                public void onError(TanxError tanxError) {
                    String msg = "展示广告失败";
                    if (tanxError != null) {
                        msg = "[tanx onError when show] " + tanxError;
                    }
                    LogUtil.simple(TAG + "onShowError   " + ", msg = " + msg);
                    handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + "onAdClose");

                    if (interstitialSetting != null) {
                        interstitialSetting.adapterDidClosed();
                    }
                }

                @Override
                public void onAdShake() {

                    LogUtil.simple(TAG + "onAdShake");

                    handleClick();
                }

                @Override
                public void onAdClicked(TanxAdView tanxAdView, ITanxAd iTanxAd) {

                    LogUtil.simple(TAG + "onAdClicked");

                    handleClick();
                }

                @Override
                public void onClickCommitSuccess(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onClickCommitSuccess");

                }

                @Override
                public void onAdShow(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onAdShow");

                    handleShow();
                }

                @Override
                public void onExposureCommitSuccess(ITanxAd iTanxAd) {
                    LogUtil.simple(TAG + "onExposureCommitSuccess");

                }
            });
            interExpressAD.showAd(activity, AdvanceTanxSetting.getInstance().tableScreenParam);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    private void initAD() {
        TanxUtil.initTanx(this, new TanxUtil.InitListener() {
            @Override
            public void success() {
                loadOnly();
            }

            @Override
            public void fail(int code, String msg) {
                handleFailed(code, msg);
            }
        });
    }


    private void loadOnly() {
        try {
            iTanxAdLoader = TanxSdk.getSDKManager().createAdLoader(getRealContext());

            boolean clickClose = AdvanceTanxSetting.getInstance().interClickAdClose;
            TanxAdSlot adSlot = new TanxAdSlot.Builder()
                    .pid(sdkSupplier.adspotid)
                    //2.9.5新增，默认为false，传ture可实现点击跳转后关闭广告
                    .setClickAdClose(clickClose)
                    .build();
            if (iTanxAdLoader != null) {
                iTanxAdLoader.loadTableScreenAd(adSlot, new ITanxAdLoader.OnAdLoadListener<ITanxTableScreenExpressAd>() {
                    @Override
                    public void onLoaded(List<ITanxTableScreenExpressAd> adList) {
                        try {
                            if (adList.size() == 0 || adList.get(0) == null) {
                                handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                                return;
                            }
                            LogUtil.simple(TAG + "onLoaded");
                            interExpressAD = adList.get(0);
                            updateBidding(interExpressAD.getBidInfo().getBidPrice());

                            handleSucceed();

                        } catch (Throwable e) {
                            e.printStackTrace();
                            handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                        }
                    }

                    @Override
                    public void onTimeOut() {
                        String msg = "获取广告超时";
                        LogUtil.simple(TAG + "onTimeOut   " + ", msg = " + msg);
                        handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);
                    }

                    @Override
                    public void onError(TanxError tanxError) {
                        String msg = "广告请求失败";
                        if (tanxError != null) {
                            msg = "[tanxError] " + tanxError;
                        }
                        LogUtil.simple(TAG + "onError   " + ", msg = " + msg);
                        handleFailed(AdvanceError.ERROR_TANX_FAILED, msg);
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
