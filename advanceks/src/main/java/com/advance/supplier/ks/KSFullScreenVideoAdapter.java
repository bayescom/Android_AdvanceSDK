package com.advance.supplier.ks;

import android.app.Activity;

import com.advance.FullScreenVideoSetting;
import com.advance.custom.AdvanceFullScreenCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsFullScreenVideoAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.List;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import androidx.annotation.Nullable;

public class KSFullScreenVideoAdapter extends AdvanceFullScreenCustomAdapter implements KsFullScreenVideoAd.FullScreenVideoAdInteractionListener {

    public FullScreenVideoSetting setting;
    private String TAG = "[KSFullScreenVideoAdapter] ";

    private List<KsFullScreenVideoAd> list;
    KsFullScreenVideoAd ad;

    public KSFullScreenVideoAdapter(Activity activity, FullScreenVideoSetting baseSetting) {
        super(activity, baseSetting);
        setting = baseSetting;
    }

    @Override
    protected void paraLoadAd() {
//初始化快手SDK
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
        long adid = KSUtil.getADID(sdkSupplier);
        if (BYUtil.isDev()) {
//                adid = 90009002;
        }
        KsScene scene = new KsScene.Builder(adid).build(); // 此为测试posId，请联系快手平台申请正式posId
        KsAdSDK.getLoadManager().loadFullScreenVideoAd(scene, new KsLoadManager.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogUtil.simple(TAG + " onError " + code + msg);

                    handleFailed(code, msg);
                }

                @Override
                public void onFullScreenVideoResult(@Nullable List<KsFullScreenVideoAd> adList) {
                    LogUtil.simple(TAG + "onFullScreenVideoResult, ");
                    try {
                        list = adList;
                        if (list == null || list.size() == 0 || list.get(0) == null) {
                            handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                        } else {
                            ad = list.get(0);
                            fullScreenItem = new KSFullScreenItem(activity, KSFullScreenVideoAdapter.this, ad);
                            //回调监听
                            if (ad.isAdEnable()) {
                                ad.setFullScreenVideoAdInteractionListener(KSFullScreenVideoAdapter.this);
                            }
                            updateBidding(ad.getECPM());

                            handleSucceed();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
                    }
                }

//                @Override
//                public void onRequestResult(int adNumber) {
//                    LogUtil.simple(TAG + "onRequestResult, 广告填充数量：" + adNumber);
//                }

                @Override
                public void onFullScreenVideoAdLoad(@Nullable List<KsFullScreenVideoAd> adList) {
                    LogUtil.simple(TAG + " onFullScreenVideoAdLoad");

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


    //--------广告回调--------
    @Override
    public void onAdClicked() {
        LogUtil.simple(TAG + " onAdClicked");
        handleClick();
    }

    @Override
    public void onPageDismiss() {
        LogUtil.simple(TAG + " onPageDismiss");
        if (setting != null) {
            setting.adapterClose();
        }
    }

    @Override
    public void onVideoPlayError(int code, int extra) {
        String msg = " onVideoPlayError,code = " + code + ",extra = " + extra;
        LogUtil.e(TAG + msg);

        try {
            if (setting != null) {
                AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER, msg);
                runParaFailed(error);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVideoPlayEnd() {
        LogUtil.simple(TAG + " onVideoPlayEnd");
        if (setting != null) {
            setting.adapterVideoComplete();
        }
    }

    @Override
    public void onVideoPlayStart() {
        LogUtil.simple(TAG + " onVideoPlayStart");
        handleShow();
    }

    @Override
    public void onSkippedVideo() {
        LogUtil.simple(TAG + " onSkippedVideo");
        if (setting != null) {
            setting.adapterVideoSkipped();
        }
    }

    @Override
    public void show() {
        try {
            ad.showFullScreenVideoAd(activity, AdvanceKSManager.getInstance().fullScreenVideoConfig);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        try {
            if (ad != null) {
                return ad.isAdEnable();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return super.isValid();
    }
}
