package com.advance.supplier.mi;

import android.app.Activity;

import com.advance.InterstitialSetting;
import com.advance.custom.AdvanceInterstitialCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.miui.zeus.mimo.sdk.ADParams;
import com.miui.zeus.mimo.sdk.InterstitialAd;

public class XMInterstitialAdapter extends AdvanceInterstitialCustomAdapter {
    InterstitialAd interstitialAd;

    public XMInterstitialAdapter(Activity activity, InterstitialSetting setting) {
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
        if (interstitialAd != null)
            interstitialAd.destroy();
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    public void show() {
        try {
            interstitialAd.showAd(getRealActivity(null), new InterstitialAd.InterstitialAdInteractionListener() {

                @Override
                public void onAdClick() {
                    LogUtil.d(TAG+"onAdClick");

                    // 广告被点击
                    handleClick();
                }

                @Override
                public void onAdShow() {
                    // 广告展示
                    LogUtil.d(TAG+"onAdShow");

                    handleShow();
                }

                @Override
                public void onAdClosed() {
                    // 广告关闭
                    LogUtil.d(TAG+"onAdClosed");

                    handleClose();
                }

                @Override
                public void onRenderFail(int errorCode, String errorMsg) {
                    LogUtil.d(TAG+"onRenderFail");

                    // 广告渲染失败
                    handleFailed(errorCode, errorMsg);

                }

                public void onVideoStart() {
                    LogUtil.d(TAG+"onVideoStart");

                    //视频开始播放

                }

                @Override
                public void onVideoPause() {
                    //视频暂停
                    LogUtil.d(TAG+"onVideoPause");

                }

                @Override
                public void onVideoResume() {
                    //视频继续播放;
                    LogUtil.d(TAG+"onVideoResume");

                }

                @Override
                public void onVideoEnd() {
                    //视频播放结束;
                    LogUtil.d(TAG+"onVideoEnd");

                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    private void loadAd() {

        interstitialAd = new InterstitialAd();
        //(5.3.4新增接口) 请使用最新接口集成
        ADParams params = new ADParams.Builder().setUpId(getPosID()).build();
        interstitialAd.loadAd(params, new InterstitialAd.InterstitialAdLoadListener() {

            @Override
            public void onAdRequestSuccess() {
                //广告请求成功
                LogUtil.d(TAG+"onAdRequestSuccess");

                updateBidding(XMUtil.getPrice(interstitialAd.getMediaExtraInfo()));

                handleSucceed();
            }

            @Override
            public void onAdLoadSuccess() {
                //广告加载（缓存）成功，在需要的时候在此处展示广告
                LogUtil.d(TAG+"onAdLoadSuccess");

            }

            @Override
            public void onAdLoadFailed(int errorCode, String errorMsg) {
                // 请求加载失败
                LogUtil.d(TAG+"onAdLoadFailed");

                handleFailed(errorCode, errorMsg);

            }

        });
    }
}
