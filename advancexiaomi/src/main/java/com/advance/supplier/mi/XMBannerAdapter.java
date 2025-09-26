package com.advance.supplier.mi;

import android.app.Activity;
import android.view.ViewGroup;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.BannerAd;

public class XMBannerAdapter extends AdvanceBannerCustomAdapter {
    BannerAd bannerAd;

    public XMBannerAdapter(Activity activity, BannerSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        XMUtil.initAD(this, new AdvanceADNInitResult() {
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
        if (bannerAd != null)
            bannerAd.destroy();
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            ViewGroup adContainer = bannerSetting.getContainer();

            bannerAd.showAd(getRealActivity(adContainer), adContainer, new BannerAd.BannerInteractionListener() {

                @Override
                public void onAdShow() {
                    LogUtil.d(TAG+"onAdShow");

                    // 广告展示
                    handleShow();
                }

                @Override
                public void onAdClick() {
                    LogUtil.d(TAG+"onAdClick");

                    // 广告被点击
                    handleClick();
                }


                @Override
                public void onAdDismiss() {
                    LogUtil.d(TAG+"onAdDismiss");

                    // 点击关闭按钮广告消失回调
                    handleClose();
                }

                @Override
                public void onRenderSuccess() {
                    // 广告渲染成功
                    LogUtil.d(TAG+"onRenderSuccess");

                }

                @Override
                public void onRenderFail(int errorCode, String errorMsg) {
                    LogUtil.d(TAG+"onRenderFail");

                    //广告渲染失败
//                container.setVisibility(View.GONE)
                    handleFailed(errorCode, errorMsg);

                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        bannerAd = new BannerAd();
        //(5.3.4新增接口) 请使用最新接口集成
        ADParams params = new ADParams.Builder().setUpId(sdkSupplier.adspotid).build();
        bannerAd.loadAd(params, new BannerAd.BannerLoadListener() {
            //请求成功回调
            @Override
            public void onBannerAdLoadSuccess() {
                LogUtil.d(TAG+"onBannerAdLoadSuccess");

                updateBidding(XMUtil.getPrice(bannerAd.getMediaExtraInfo()));

                handleSucceed();
            }

            //请求失败回调
            @Override
            public void onAdLoadFailed(int errorCode, String errorMsg) {
                LogUtil.d(TAG+"onAdLoadFailed");

                handleFailed(errorCode, errorMsg);
            }
        });
    }
}
