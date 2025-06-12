package com.advance.supplier.ks;

import android.app.Activity;
import android.view.View;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsAdVideoPlayConfig;
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.ArrayList;
import java.util.List;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import androidx.annotation.Nullable;

public class KSNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    private String TAG = "[KSNativeExpressAdapter] ";
    public NativeExpressSetting setting;
    List<KsFeedAd> list;
    KsFeedAd ad;

    public KSNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
        setting = baseSetting;
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
        int num = sdkSupplier != null ? sdkSupplier.adCount : 1;
        KsScene.Builder builder = new KsScene.Builder(KSUtil.getADID(sdkSupplier)).adNum(num);
        try {
            if (setting != null) {
                int widthDP = setting.getExpressViewWidth();
                int heightDP = setting.getExpressViewHeight();
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
                        nativeExpressAdItemList = new ArrayList<>();
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

                            try {
                                //设置静音
                                KsAdVideoPlayConfig nativeExpressConfig = AdvanceKSManager.getInstance().nativeExpressConfig;
                                if (nativeExpressConfig == null) {
                                    nativeExpressConfig = new KsAdVideoPlayConfig.Builder().videoSoundEnable(setting.isVideoMute()).build();
                                }
                                ad.setVideoPlayConfig(nativeExpressConfig);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            final KSNativeExpressItem advanceNativeExpressAdItem = new KSNativeExpressItem(activity, KSNativeExpressAdapter.this, adItem);
                            nativeExpressAdItemList.add(advanceNativeExpressAdItem);

//                    提前设置监听器
                            try {
                                final View adview = adItem.getFeedView(activity);
                                adItem.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
                                    @Override
                                    public void onAdClicked() {
                                        LogUtil.simple(TAG + " onAdClicked ");
                                        handleClick();
                                    }

                                    @Override
                                    public void onAdShow() {
                                        LogUtil.simple(TAG + " onAdShow ");

                                        nativeExpressADView = adview;
                                        handleShow();
                                    }

                                    @Override
                                    public void onDislikeClicked() {
                                        LogUtil.simple(TAG + " onDislikeClicked ");

                                        if (setting != null) {
                                            setting.adapterDidClosed(adview);
                                        }
                                        removeADView();
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
                            }

                        }
                        if (nativeExpressAdItemList != null && nativeExpressAdItemList.size() > 0 && ad != null) {
                            updateBidding(ad.getECPM());
                            handleSucceed();
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


    @Override
    public void show() {
        try {
            final View adv = ad.getFeedView(activity);
            addADView(adv);
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

                    if (setting != null) {
                        setting.adapterDidClosed(adv);
                    }
                    removeADView();
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
