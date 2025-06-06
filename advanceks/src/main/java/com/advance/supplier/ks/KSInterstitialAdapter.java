package com.advance.supplier.ks;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsInterstitialAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.List;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import androidx.annotation.Nullable;

public class KSInterstitialAdapter extends AdvanceInterstitialCustomAdapter implements KsInterstitialAd.AdInteractionListener {
    private InterstitialSetting setting;
    KsInterstitialAd interstitialAD;
    List<KsInterstitialAd> list;
    private String TAG = "[KSInterstitialAdapter] ";

    public KSInterstitialAdapter(Activity activity, InterstitialSetting baseSetting) {
        super(activity, baseSetting);
        this.setting = baseSetting;
    }

    @Override
    public void show() {
        try {
            interstitialAD.showInterstitialAd(activity, AdvanceKSManager.getInstance().interstitialVideoConfig);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    protected void paraLoadAd() {
        KSUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法，否则穿山甲会抛错导致无法进行广告展示
                startLoad();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });

    }

    private void startLoad() {
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
                                    //回调监听
                                    interstitialAD.setAdInteractionListener(KSInterstitialAdapter.this);

                                    updateBidding(interstitialAD.getECPM());
                                    handleSucceed();
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                            }

                        }
                    });
        }


    @Override
    protected void adReady() {
    }

    @Override
    public void doDestroy() {

    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
        }
    }

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
        if (setting != null) {
            setting.adapterDidClosed();
        }
    }

    @Override
    public void onPageDismiss() {
        LogUtil.simple(TAG + " onPageDismiss");
        if (setting != null) {
            setting.adapterDidClosed();
        }
    }

    @Override
    public void onVideoPlayError(int code, int extra) {
        LogUtil.e(TAG + " onVideoPlayError,code = " + code + ",extra = " + extra);
        try {
            if (setting != null) {
                AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER, "onVideoPlayError");
                setting.adapterDidFailed(error, sdkSupplier);
            }
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
        if (setting != null) {
            setting.adapterDidClosed();
        }
    }
}
