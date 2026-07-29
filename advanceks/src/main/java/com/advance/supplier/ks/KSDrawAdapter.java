package com.advance.supplier.ks;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

import com.advance.custom.AdvanceDrawCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsDrawAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.model.AdExposureFailureCode;

import java.util.List;
import java.util.Map;

public class KSDrawAdapter extends AdvanceDrawCustomAdapter {
    private String TAG = "[KSDrawAdapter] ";
    private KsDrawAd drawAD;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);

        if (isWin){
            drawAD.setBidEcpm((long) winPrice,0);
        }else {
            drawAD.reportAdExposureFailed(AdExposureFailureCode.BID_FAILED, KSUtil.getFailedReason(winPrice,referBidInfo));
        }
    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        //场景设置
        KsScene scene = new KsScene.Builder(KSUtil.getADID(sdkSupplier)).build();
        KsAdSDK.getLoadManager().loadDrawAd(scene, new KsLoadManager.DrawAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogUtil.simple(TAG + " onError " + code + msg);

                handleFailed(code, msg);
            }

            @Override
            public void onDrawAdLoad(@Nullable List<KsDrawAd> list) {
                LogUtil.simple(TAG + "onInterstitialAdLoad");

                try {
                    if (list == null || list.size() == 0 || list.get(0) == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                    } else {
                        drawAD = list.get(0);

                        handleSucceed(drawAD.getECPM());
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

    

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        if (drawAD == null) {
            handleFailed(AdvanceError.ERROR_DATA_NULL, "ad is empty");
            return;
        }
        try {
            //回调监听
            drawAD.setAdInteractionListener(new KsDrawAd.AdInteractionListener() {

                /**
                 * ks回调事件
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
                public void onVideoPlayStart() {
                    LogUtil.simple(TAG + " onVideoPlayStart");

                }

                @Override
                public void onVideoPlayPause() {
                    LogUtil.simple(TAG + " onVideoPlayPause");

                }

                @Override
                public void onVideoPlayResume() {
                    LogUtil.simple(TAG + " onVideoPlayResume");

                }

                @Override
                public void onVideoPlayEnd() {
                    LogUtil.simple(TAG + " onVideoPlayEnd");

                }

                @Override
                public void onVideoPlayError() {
                    LogUtil.simple(TAG + " onVideoPlayError");

                }
            });

            View drawVideoView = drawAD.getDrawView(activity);
            if (isADViewAdded(drawVideoView)) {

            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

}
