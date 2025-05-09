package com.advance.supplier.tap;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapBannerAd;

public class TapBannerAdapter extends AdvanceBannerCustomAdapter {
    TapAdNative tapAdNative;
    TapBannerAd adData;

    private final BannerSetting setting;

    public TapBannerAdapter(Activity activity, BannerSetting setting) {
        super(activity, setting);
        this.setting = setting;
    }

    @Override
    public void orderLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });

    }

    @Override
    protected void paraLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });

    }


    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (adData != null) {
                adData.dispose();
            }
            TapUtil.removeTapMap(getRealContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            ViewGroup adContainer = setting.getContainer();
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


    private void loadAD() {
        try {
//            tapAdNative = TapAdManager.get().createAdNative(getRealContext());
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

                        updateBidding(TapUtil.getBiddingPrice(adData.getMediaExtraInfo()));

                        handleSucceed();

                        adData.setBannerInteractionListener(new TapBannerAd.BannerInteractionListener() {

                            @Override
                            public void onAdShow() {
                                LogUtil.simple(TAG + " onAdShow");

                                handleShow();
                            }

                            @Override
                            public void onAdClose() {
                                LogUtil.simple(TAG + " onAdClose");
                                if (setting != null) {
                                    setting.adapterDidDislike();
                                }
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
