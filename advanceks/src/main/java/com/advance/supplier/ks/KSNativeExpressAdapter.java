package com.advance.supplier.ks;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsAdVideoPlayConfig;
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.List;
import java.util.Map;

public class KSNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    private String TAG = "[KSNativeExpressAdapter] ";
    List<KsFeedAd> list;
    KsFeedAd ad;

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        int num = sdkSupplier != null ? sdkSupplier.adCount : 1;
        KsScene.Builder builder = new KsScene.Builder(KSUtil.getADID(sdkSupplier)).adNum(num);
        try {
            if (nativeExpressSetting != null) {
                int widthDP = nativeExpressSetting.getExpressViewWidth();
                int heightDP = nativeExpressSetting.getExpressViewHeight();
                LogUtil.devDebug(TAG + "getExpressViewWidth = " + widthDP);
                LogUtil.devDebug(TAG + "getExpressViewHeight = " + heightDP);
                if (widthDP > 0) {
                    builder.width(BYDisplay.dp2px(widthDP));
                }
                if (heightDP > 0) {
                    builder.height(BYDisplay.dp2px(heightDP));
                }
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }


        KsScene scene = builder.build(); // 此为测试posId，请联系快手平台申请正式posId
        KsAdSDK.getLoadManager().loadConfigFeedAd(scene, new KsLoadManager.FeedAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogUtil.simple(TAG + " onError " + code + msg);

                handleFailed(code, msg);

            }

            @Override
            public void onFeedAdLoad(@Nullable List<KsFeedAd> adList) {
                LogUtil.simple(TAG + " onFeedAdLoad");

                list = adList;
                try {
                    if (list == null || list.size() == 0 || list.get(0) == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                    } else {
                        for (final KsFeedAd adItem : list) {
                            if (adItem == null) {
                                continue;
                            }
                            if (ad == null) {
                                ad = adItem;
                            }
                        }

                        for (final KsFeedAd adItem : list) {
                            if (adItem == null) {
                                continue;
                            }


//

                        }
                        if (ad != null) {
                            handleSucceed(ad.getECPM());
                        } else {
                            handleFailed(AdvanceError.ERROR_DATA_NULL, "nativeExpressAdItemList empty");
                        }
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
        try {
            final View adv = ad.getFeedView(activity);
            addADView(adv);
            try {
                //设置静音
                KsAdVideoPlayConfig nativeExpressConfig = AdvanceKSManager.getInstance().nativeExpressConfig;
                if (nativeExpressConfig == null) {
                    nativeExpressConfig = new KsAdVideoPlayConfig.Builder().videoSoundEnable(nativeExpressSetting.isVideoMute()).build();
                }
                ad.setVideoPlayConfig(nativeExpressConfig);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            ad.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
                @Override
                public void onAdClicked() {

                    LogUtil.simple(TAG + " onAdClicked ");
                    handleClick();
                }

                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + " onAdShow ");
                    nativeExpressADView = adv;
                    handleShow();
                }

                @Override
                public void onDislikeClicked() {
                    LogUtil.simple(TAG + " onDislikeClicked ");

                    handleClose();
                }

                @Override
                public void onDownloadTipsDialogShow() {
                    LogUtil.simple(TAG + " onDownloadTipsDialogShow ");

                }

                @Override
                public void onDownloadTipsDialogDismiss() {
                    LogUtil.simple(TAG + " onDownloadTipsDialogDismiss ");

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }
}
