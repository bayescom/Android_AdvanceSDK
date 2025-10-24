package com.advance.supplier.vv;

import android.app.Activity;
import android.view.View;

import com.advance.AdvanceSetting;
import com.advance.RewardServerCallBackInf;
import com.advance.RewardVideoSetting;
import com.advance.custom.AdvanceRewardCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.vivo.mobilead.unified.base.AdParams;
import com.vivo.mobilead.unified.base.VivoAdError;
import com.vivo.mobilead.unified.base.callback.MediaListener;
import com.vivo.mobilead.unified.reward.UnifiedVivoRewardVideoAd;
import com.vivo.mobilead.unified.reward.UnifiedVivoRewardVideoAdListener;
import com.vivo.mobilead.unified.splash.UnifiedVivoSplashAd;
import com.vivo.mobilead.unified.splash.UnifiedVivoSplashAdListener;

public class VivoRewardAdapter extends AdvanceRewardCustomAdapter {
    UnifiedVivoRewardVideoAd rewardVideoAd;

    public VivoRewardAdapter(Activity activity, RewardVideoSetting setting) {
        super(activity, setting);
    }

    @Override
    protected void paraLoadAd() {
        VivoUtil.initAD(this, new AdvanceADNInitResult() {
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
        if (rewardVideoAd != null)
            rewardVideoAd.destroy();
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            rewardVideoAd.showAd(getRealActivity(null));
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {
        AdParams adParams = null;
        AdParams.Builder builder = VivoUtil.getAdParamsBuilder(this);
        if (builder != null) {
            adParams = builder.build();
        }
        rewardVideoAd = new UnifiedVivoRewardVideoAd(getRealContext(), adParams, new UnifiedVivoRewardVideoAdListener() {
            @Override
            public void onAdReady() {
                LogUtil.simple(TAG + "onAdReady...");

                updateBidding(VivoUtil.getPrice(rewardVideoAd));
                handleSucceed();
            }

            @Override
            public void onAdFailed(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onAdFailed... , vivoAdError = " + vivoAdError);

                VivoUtil.handleErr(VivoRewardAdapter.this, vivoAdError, AdvanceError.ERROR_LOAD_SDK, "onAdFailed");

            }

            @Override
            public void onAdClick() {
                LogUtil.simple(TAG + "onAdClick...");

                handleClick();
            }

            @Override
            public void onAdShow() {
                LogUtil.simple(TAG + "onAdShow...");

                handleShow();
            }

            @Override
            public void onAdClose() {
                LogUtil.simple(TAG + "onAdClose...");

                handleClose();
            }

            @Override
            public void onRewardVerify() {
                LogUtil.simple(TAG + "onRewardVerify...");

                RewardServerCallBackInf inf = new RewardServerCallBackInf();
                inf.rewardVerify = true;
                handleRewardInf(inf);

                handleReward();

            }
        });
        rewardVideoAd.setMediaListener(new MediaListener() {
            @Override
            public void onVideoStart() {
                LogUtil.simple(TAG + "onVideoStart...");

            }

            @Override
            public void onVideoPause() {
                LogUtil.simple(TAG + "onVideoPause...");

            }

            @Override
            public void onVideoPlay() {
                LogUtil.simple(TAG + "onVideoPlay...");

            }

            @Override
            public void onVideoError(VivoAdError vivoAdError) {
                LogUtil.simple(TAG + "onVideoError...");

                handleFailed(AdvanceError.ERROR_EXCEPTION_SHOW, "onVideoError");
            }

            @Override
            public void onVideoCompletion() {
                LogUtil.simple(TAG + "onVideoCompletion...");


            }

            @Override
            public void onVideoCached() {
                LogUtil.simple(TAG + "onVideoCached...");

                handleCached();

            }
        });
        rewardVideoAd.loadAd();
    }

}
