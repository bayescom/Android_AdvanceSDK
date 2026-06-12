package com.advance.supplier.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.util.Map;

public class CustomADNYLHSplashAdapter extends AdvanceSplashCustomAdapter {

    private long remainTime = 5000;
    private boolean isClicked = false;
    protected SplashAD splashAD;
    private final String TAG = "[CustomADNYLHSplashAdapter:" + this + "] ";


    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        LogUtil.devDebug(TAG + " show");
//        if (AdvanceUtil.isDev()) {//todo 测试逻辑，正式上线需移除
//            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "测试渲染异常");
//                }
//            },1000);
////            handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "测试渲染异常");
//            return;
//        }
        try {
            if (splashAD == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, "splashAd null"));
                return;
            }
            if (isParallel) {
                splashAD.showAd(splashSetting.getAdContainer());
            }

            TextView skipView = splashSetting.getSkipView();
            if (null != skipView) {
                skipView.setVisibility(View.INVISIBLE);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    //调用展示方法
    @Override
    public void adPrepared() {
//        if (splashAD != null && isParallel) {
//            splashAD.showAd(adContainer);
//        }
    }

    @Override
    public void destroyAd() {

    }

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        int timeout = sdkSupplier.timeout <= 0 ? 5000 : sdkSupplier.timeout;


        SplashADListener listener = new SplashADListener() {

            @Override
            public void onADDismissed() {
                LogUtil.simple(TAG + "onADDismissed ");
                if (null != splashSetting) {
                    boolean clickSkip = splashSetting.isGdtClickAsSkip();
                    //如果开启广点通点击广告等于跳过设置，isClicked手动置为未点击状态，保证点击了也是走跳过回调
                    if (clickSkip) {
                        isClicked = false;
                    }
                    checkAndReview();
                    //剩余时长在600ms以上，且未点击才按照跳过
                    if (remainTime >= 600 && !isClicked) {
                        handleSkip();
                    } else {
                        handleTimeOver();
                    }
                }
            }

            @Override
            public void onNoAD(AdError adError) {
                int code = -1;
                String msg = "default onNoAD";
                if (adError != null) {
                    code = adError.getErrorCode();
                    msg = adError.getErrorMsg();
                }
                LogUtil.simple(TAG + "onNoAD");

                handleFailed(code, msg);
//                AdvanceError error = AdvanceError.parseErr(code, msg);
//                if (isParallel) {
//                    if (parallelListener != null) {
//                        parallelListener.onFailed(error);
//                    }
//                } else {
//                    runBaseFailed(error);
//                }
                preLoad();
            }

            @Override
            public void onADPresent() {
                LogUtil.simple(TAG + "onADPresent ");


//                if (skipView != null) {
//                    skipView.setVisibility(View.VISIBLE);
//                }
            }

            @Override
            public void onADClicked() {
                LogUtil.simple(TAG + "onADClicked ");

                handleClick();
                isClicked = true;
            }

            @Override
            public void onADTick(long l) {
                LogUtil.simple(TAG + "onADTick :" + l);
                remainTime = l;
//                if (null != skipView) {
//                    skipView.setText(String.format(skipText, Math.round(l / 1000f)));
//                }
            }

            @Override
            public void onADExposure() {
                LogUtil.simple(TAG + "onADExposure ");
                handleShow();

                preLoad();
            }

            @Override
            public void onADLoaded(long expireTimestamp) {
                try {
                    LogUtil.simple(TAG + "onADLoaded expireTimestamp:" + expireTimestamp);
                    double ecpm = 0;

                    if (splashAD != null) {
                        LogUtil.devDebug(TAG + "getECPMLevel = " + splashAD.getECPMLevel() + ", getECPM = " + splashAD.getECPM());
                        ecpm = (splashAD.getECPM());

//                        if (AdvanceUtil.isDev()) {//测试bidding
//                            sdkSupplier.price = 700;
//                            sdkSupplier.bidResultPrice = sdkSupplier.price;
//                        }
                    }

                    handleSucceed(ecpm);

                    long rt = SystemClock.elapsedRealtime();
                    long expire = expireTimestamp - rt;
                    LogUtil.high(TAG + "ad will expired in :" + expire + " ms");

                } catch (Throwable e) {
                    e.printStackTrace();
                    runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                }

            }
        };
//        splashAD.setDownloadConfirmListener();
        splashAD = new SplashAD(getRealContext(), sdkSupplier.adspotid, listener, timeout);

        LogUtil.simple(TAG + "fetchAdOnly ");

        if (splashAD != null) {
            splashAD.fetchAdOnly();
        }
    }


    //检查是否需要对holder进行遮罩图层赋值
    private void checkAndReview() {
        try {
            ImageView splashHolder = splashSetting.getGDTHolderView();
            if (splashHolder != null) {
                //防止移除view后显示底图导致屏幕闪烁
                Bitmap b = splashAD.getZoomOutBitmap();
                if (b != null) {
                    splashHolder.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    splashHolder.setImageBitmap(b);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //当广告曝光、广告失败的时候再执行广告预加载，避免影响当前展示
    private void preLoad() {
        try {
            //预加载素材，会有频次限制，目前是交给广点通自己来控制，不做额外频次控制
            if (splashAD != null)
                splashAD.preLoad();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        if (splashAD != null) {
            return splashAD.isValid();
        }
        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);
        

    }
}
