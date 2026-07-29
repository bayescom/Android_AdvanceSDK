package com.advance.supplier.ks;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.Nullable;

import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsInterstitialAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.model.AdExposureFailureCode;

import java.util.List;
import java.util.Map;

public class KSInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    KsInterstitialAd interstitialAD;
    List<KsInterstitialAd> list;
    private String TAG = "[KSInterstitialAdapter] ";

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);


        if (isWin){
            interstitialAD.setBidEcpm((long) winPrice,0);
        }else {
            interstitialAD.reportAdExposureFailed(AdExposureFailureCode.BID_FAILED, KSUtil.getFailedReason(winPrice,referBidInfo));
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            //回调监听
            interstitialAD.setAdInteractionListener(new KsInterstitialAd.AdInteractionListener() {

                /**
                 * 广告事件回调
                 */

                @Override
                public void onAdClicked() {
                    LogUtil.simple(TAG + " onAdClicked");
                    handleClick();
                }

                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + " onAdShow");
                    handleShow();
                }

                @Override
                public void onAdClosed() {
                    LogUtil.simple(TAG + " onAdClosed");

                    handleClose();
                }

                @Override
                public void onPageDismiss() {
                    LogUtil.simple(TAG + " onPageDismiss");

                    handleClose();
                }

                @Override
                public void onVideoPlayError(int code, int extra) {
                    LogUtil.e(TAG + " onVideoPlayError,code = " + code + ",extra = " + extra);
                    try {
                        AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER, "onVideoPlayError");

                        runParaFailed(error);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onVideoPlayEnd() {
                    LogUtil.simple(TAG + " onVideoPlayEnd");
                }

                @Override
                public void onVideoPlayStart() {
                    LogUtil.simple(TAG + " onVideoPlayStart");
                }

                @Override
                public void onSkippedAd() {
                    LogUtil.simple(TAG + " onSkippedAd");

                    handleClose();
                }
            });

            interstitialAD.showInterstitialAd(activity, AdvanceKSManager.getInstance().interstitialVideoConfig);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        //场景设置
        long adid = KSUtil.getADID(sdkSupplier);
        if (BYUtil.isDev()) {
//                adid = 4000000276L;
        }
        KsScene scene = new KsScene.Builder(adid).build();
        KsAdSDK.getLoadManager().loadInterstitialAd(scene,
                new KsLoadManager.InterstitialAdListener() {
                    @Override
                    public void onError(int code, String msg) {
                        LogUtil.simple(TAG + " onError " + code + msg);

                        handleFailed(code, msg);
                    }

                    @Override
                    public void onRequestResult(int adNumber) {
                        LogUtil.simple(TAG + "onRequestResult，广告填充数量：" + adNumber);

                    }


                    @Override
                    public void onInterstitialAdLoad(@Nullable List<KsInterstitialAd> adList) {
                        LogUtil.simple(TAG + "onInterstitialAdLoad");

                        try {
                            list = adList;
                            if (list == null || list.size() == 0 || list.get(0) == null) {
                                handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                            } else {
                                interstitialAD = list.get(0);

                                handleSucceed(interstitialAD.getECPM());
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                        }

                    }
                });
    }


    @Override
    protected void adPrepared() {
    }

    @Override
    public void destroyAd() {

    }


}
