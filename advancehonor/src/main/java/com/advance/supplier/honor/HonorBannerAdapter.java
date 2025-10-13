package com.advance.supplier.honor;

import android.app.Activity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.advance.BannerSetting;
import com.advance.custom.AdvanceBannerCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.hihonor.adsdk.banner.api.BannerAdLoad;
import com.hihonor.adsdk.base.AdSlot;
import com.hihonor.adsdk.base.api.banner.BannerAdLoadListener;
import com.hihonor.adsdk.base.api.banner.BannerExpressAd;
import com.hihonor.adsdk.base.callback.AdListener;

public class HonorBannerAdapter extends AdvanceBannerCustomAdapter {
    BannerExpressAd mBannerExpressAd;

    public HonorBannerAdapter(Activity activity, BannerSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        loadAd();
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        if (mBannerExpressAd != null) {
            mBannerExpressAd.release();
        }
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public boolean isValid() {
        if (HonorUtil.isAdExpire(mBannerExpressAd)) {
            return false;
        }
        return super.isValid();
    }

    @Override
    public void show() {

        try {
            if (mBannerExpressAd != null) {


                /**
                 * 广告事件监听器
                 */
                mBannerExpressAd.setAdListener(new AdListener() {
                    /**
                     * 开屏广告点击跳过或倒计时结束时回调
                     *
                     * @param type 0：点击跳过、1：倒计时结束
                     */
                    @Override
                    public void onAdSkip(int type) {
                        LogUtil.simple(TAG + "onAdSkip, type: " + type);

//                        handleClose();
//
                    }


                    /**
                     * 广告关闭时回调
                     */
                    @Override
                    public void onAdClosed() {
                        LogUtil.simple(TAG + "onAdClosed...");
                        handleClose();
                    }


                    /**
                     * 广告曝光时回调
                     */
                    @Override
                    public void onAdImpression() {
                        LogUtil.simple(TAG + "onAdImpression...");

                        handleShow();
                    }

                    /**
                     * 广告曝光失败时回调
                     *
                     * @param errCode 错误码
                     * @param msg 曝光失败信息
                     */
                    @Override
                    public void onAdImpressionFailed(int errCode, String msg) {
                        super.onAdImpressionFailed(errCode, msg);
                        LogUtil.simple(TAG + "onAdImpressionFailed, errCode: " + errCode + ", msg: " + msg);

                        handleFailed(errCode, msg);
                    }

                    /**
                     * 广告被点击时回调
                     */
                    @Override
                    public void onAdClicked() {
                        LogUtil.simple(TAG + "onAdClicked...");

                        handleClick();
                    }

                    /**
                     * 广告成功跳转小程序时回调
                     */
                    @Override
                    public void onMiniAppStarted() {
                        LogUtil.simple(TAG + "onMiniAppStarted...");

                    }
                });

                int refreshValue = 0;
                if (bannerSetting != null) {
                    refreshValue = bannerSetting.getRefreshInterval();
                }
                // 设置轮播间隔时间
//        间隔时间设置为0s，轮播不起效；
//间隔时间支持范围为[30s,120s]，若您设置的值大于0s小于30s，则按照30s轮播，同理，设置的值大于120s，按照120s轮播；
//当达到间隔时间且用户停留在广告所在页面时，会自动触发轮播。
                mBannerExpressAd.setIntervalTime(refreshValue * 1000L);

                ViewGroup adContainer = bannerSetting.getContainer();
                RelativeLayout.LayoutParams rbl = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                rbl.addRule(RelativeLayout.CENTER_HORIZONTAL);
                boolean add = AdvanceUtil.addADView(adContainer, mBannerExpressAd.getExpressAdView(), rbl);
                if (!add) {
                    doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
                    return;
                }
//                mBannerExpressAd.getExpressAdView().
            }
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        HonorUtil.initAD(this);

        // 创建广告请求参数对象（AdSlot）
        AdSlot adSlot = new AdSlot.Builder()
                .setSlotId(sdkSupplier.adspotid) // 必传,设置您的广告位ID。
//                .setLoadType(-1) // 非必传，设置广告请求方式  -1：默认请求方式，不进行缓存  0：普通请求，优先去读缓存  1：预缓存请求，数据保存至缓存
//                .setWidth(1200) // 设置宽度 单位像素
//                .setHeight(240) // 设置高度 单位像素
                .build();

        // 构建广告加载器，传入已创建好的广告请求参数对象与广告加载状态监听器。
        BannerAdLoad load = new BannerAdLoad.Builder()
                .setBannerAdLoadListener(new BannerAdLoadListener() {
                    @Override
                    public void onLoadSuccess(BannerExpressAd bannerExpressAd) {
                        LogUtil.d(TAG + "onLoadSuccess");

                        mBannerExpressAd = bannerExpressAd;

                        updateBidding(HonorUtil.getECPM(mBannerExpressAd));

                        handleSucceed();
                    }

                    @Override
                    public void onFailed(String code, String errorMsg) {
                        LogUtil.d(TAG + "onFailed");

                        handleFailed(code, errorMsg);
                    }
                }) // 必传，注册广告加载状态监听器。
                .setAdSlot(adSlot) // 必传，设置广告请求参数。
                .build();
// 加载广告
        load.loadAd();
    }
}
