package com.advance.supplier.csj;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.advance.AdvanceConfig;
import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bykv.vk.openvk.TTNtExpressObject;
import com.bykv.vk.openvk.TTVfDislike;
import com.bykv.vk.openvk.TTVfManager;
import com.bykv.vk.openvk.TTVfNative;
import com.bykv.vk.openvk.TTVfSdk;
import com.bykv.vk.openvk.VfSlot;

import java.util.List;

/**
 * 如果网络异常，不会进行刷新行为，且不会回调失败。当网络正常，会继续定时刷新。视为内部闭环了刷新行为，一旦失败就流转下一优先级
 */
public class CsjBannerAdapter extends AdvanceBannerCustomAdapter implements TTVfNative.NtExpressVfListener {
    private BannerSetting advanceBanner;
    private long startTime = 0;
    private String TAG = "[CsjBannerAdapter] ";
    private TTNtExpressObject ad;

    public CsjBannerAdapter(Activity activity, final BannerSetting advanceBanner) {
        super(activity, advanceBanner);
        this.advanceBanner = advanceBanner;
    }

    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onError(int code, String message) {
        LogUtil.e(TAG + " onError: code = " + code + " msg = " + message);
        handleFailed(code + "", message);
    }

    @Override
    public void onNtExpressVnLoad(List<TTNtExpressObject> ads) {
        try {
            LogUtil.simple(TAG + "onNativeExpressAdLoad");
            if (ads == null || ads.size() == 0) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "广告列表数据为空");
                return;
            }
            ad = ads.get(0);
            // 加载成功的回调，接入方可在此处做广告的展示，请确保您的代码足够健壮，能够处理异常情况；
            if (null == ad) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "广告数据为空");
                return;
            }

            updateBidding(CsjUtil.getEcpmValue(TAG, ad.getMediaExtraInfo()));

            handleSucceed();
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    private void bindAdListener(TTNtExpressObject ad) {
        try {
            if (null != advanceBanner) {
                ad.setSlideIntervalTime(advanceBanner.getRefreshInterval() * 1000);
            }
            ad.setExpressInteractionListener(new TTNtExpressObject.ExpressNtInteractionListener() {
                @Override
                public void onClicked(View view, int i) {
                    LogUtil.simple(TAG + "ExpressView onAdClicked , type :" + i);

                    handleClick();
                }

                @Override
                public void onShow(View view, int i) {
                    LogUtil.simple(TAG + "ExpressView onAdShow, type :" + i + ",cost time = " + (System.currentTimeMillis() - startTime));

                    handleShow();
                }

                @Override
                public void onRenderFail(View view, String msg, int code) {
                    LogUtil.simple(TAG + "ExpressView render fail:" + (System.currentTimeMillis() - startTime));

                    doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, TAG + code + "， " + msg));
                }

                @Override
                public void onRenderSuccess(View view, float v, float v1) {
                    LogUtil.simple(TAG + "ExpressView render suc:" + (System.currentTimeMillis() - startTime));

                    if (null != advanceBanner) {
                        ViewGroup adContainer = advanceBanner.getContainer();
                        if (adContainer != null) {
//                            adContainer.removeAllViews();
                            boolean add = AdvanceUtil.addADView(adContainer, view);
                            if (!add) {
                                doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
                            }
//                            adContainer.addView(view);
                        }
                    }
                }
            });

            Activity showAct = activity;
            if (advanceBanner != null && advanceBanner.getContainer() != null) {
                showAct = getRealActivity(advanceBanner.getContainer());
            }
            //使用默认模板中默认dislike弹出样式
            ad.setDislikeCallback(showAct, new TTVfDislike.DislikeInteractionCallback() {
                @Override
                public void onShow() {

                }

                @Override
                public void onSelected(int position, String value, boolean enforce) {
                    if (null != advanceBanner) {
                        //用户选择不喜欢原因后，移除广告展示
                        ViewGroup adContainer = advanceBanner.getContainer();
                        if (adContainer != null) {
                            adContainer.removeAllViews();
                        }

                        advanceBanner.adapterDidDislike();
                    }
                }

                @Override
                public void onCancel() {
                }

//                @Override
//                public void onRefuse() {
//
//                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    @Override
    public void doDestroy() {
        try {
            if (ad != null)
                ad.destroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paraLoadAd() {
        CsjUtil.initCsj(this, new CsjUtil.InitListener() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
                startLoad();
            }

            @Override
            public void fail(int code, String msg) {
                doBannerFailed(AdvanceError.parseErr(code, msg));
            }
        });
    }

    private void startLoad() {
        final TTVfManager ttAdManager = TTVfSdk.getVfManager();
        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        TTVfNative ttAdNative = ttAdManager.createVfNative(activity);
        VfSlot adSlot = new VfSlot.Builder()
                // 必选参数 设置您的CodeId
                .setCodeId(sdkSupplier.adspotid)
                //期望模板广告view的size,单位dp
                .setExpressViewAcceptedSize(advanceBanner.getCsjExpressViewAcceptedWidth(), advanceBanner.getCsjExpressViewAcceptedHeight())
                // 必选参数 设置广告图片的最大尺寸及期望的图片宽高比，单位Px
                .setImageAcceptedSize(advanceBanner.getCsjAcceptedSizeWidth(), advanceBanner.getCsjAcceptedSizeHeight())
                // 可选参数 设置是否支持deeplink
                .setSupportDeepLink(true)
                //请求原生广告时候需要设置，参数为TYPE_BANNER或TYPE_INTERACTION_AD
//                .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                .build();
        ttAdNative.loadBnExpressVb(adSlot, this);
    }


    @Override
    protected void adReady() {
//        startTime = System.currentTimeMillis();
//        if (ad != null) {
//            ad.render();
//        }
    }

    @Override
    public void show() {
        try {
            startTime = System.currentTimeMillis();
            bindAdListener(ad);
            ad.render();
        } catch (Throwable e) {
            e.printStackTrace();
            doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        if (ad != null && ad.getMediationManager() != null) {
            return ad.getMediationManager().isReady();
        }
        return super.isValid();
    }
}
