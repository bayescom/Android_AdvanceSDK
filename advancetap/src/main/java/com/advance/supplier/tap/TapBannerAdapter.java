package com.advance.supplier.tap;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapBannerAd;

import java.util.Map;

public class TapBannerAdapter extends AdvanceBannerCustomAdapter {
    TapAdNative tapAdNative;
    TapBannerAd adData;


    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {

    }


    @Override
    protected void adPrepared() {

    }

    @Override
    public void destroyAd() {
        try {
            if (adData != null) {
                adData.dispose();
            }
            TapUtil.removeTapMap(getRealContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            adData.setBannerInteractionListener(new TapBannerAd.BannerInteractionListener() {

                @Override
                public void onAdShow() {
                    LogUtil.simple(TAG + " onAdShow");

                    handleShow();
                }

                @Override
                public void onAdClose() {
                    LogUtil.simple(TAG + " onAdClose");
                    handleClose();
                }

                @Override
                public void onAdClick() {
                    LogUtil.simple(TAG + " onAdClick");

                    handleClick();
                }

                @Override
                public void onDownloadClick() {
                    LogUtil.simple(TAG + " onDownloadClick");

                    handleClick();
                }

                @Override
                public void onAdValidShow() {
                    LogUtil.simple(TAG + " onAdValidShow");

                }

            });
            
            ViewGroup adContainer = getAdContainer();
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            boolean add = AdvanceUtil.addADView(adContainer, adData.getBannerView(), lp);
            if (!add) {
                doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            tapAdNative = TapUtil.getTapADManger(getRealContext());

            int spaceId = TapUtil.getPlaceId(getPosID());
            AdRequest request = new AdRequest.Builder().withSpaceId(spaceId)
                    .withUserId(AdvanceTapManger.getInstance().customTapUserId)
                    .build();

            tapAdNative.loadBannerAd(request, new TapAdNative.BannerAdListener() {
                @Override
                public void onBannerAdLoad(TapBannerAd tapBannerAd) {
                    try {
                        if (tapBannerAd == null) {
                            String nMsg = TAG + " tapBannerAd null";
                            handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                            return;
                        }
                        adData = tapBannerAd;

                        handleSucceed(TapUtil.getBiddingPrice(adData.getMediaExtraInfo()));

                    } catch (Throwable e) {
                        e.printStackTrace();
                        doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                    }
                }

                @Override
                public void onError(int code, String message) {
                    LogUtil.e(TAG + " onError ");

                    AdvanceError error = AdvanceError.parseErr(code, message);
                    doBannerFailed(error);
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD, "out"));
        }
    }

}
