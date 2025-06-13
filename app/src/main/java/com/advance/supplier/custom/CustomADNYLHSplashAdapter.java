package com.advance.supplier.custom;

import android.app.Activity;
import android.os.SystemClock;

import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.comm.util.AdError;

import java.lang.ref.SoftReference;

public class CustomADNYLHSplashAdapter extends AdvanceSplashCustomAdapter {

    private long remainTime = 5000;
    private boolean isClicked = false;
    private SplashAD splashAD;
    SplashSetting setting;
    private final String TAG = "[GdtSplashAdapter] ";

    public CustomADNYLHSplashAdapter(SoftReference<Activity> softReferenceActivity, SplashSetting splashSetting) {
        super(softReferenceActivity, splashSetting);
        this.setting = splashSetting;
    }


    //    串行方式加载广告，一般情况直接调用 paraLoadAd() 即可
    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    //    并行方式加载广告
    @Override
    protected void paraLoadAd() {
        //先调用adn得初始化方法，也可以在应用启动时调用
        CustomUtil.initAD(this);
        //请求广告
        adnLoadStart();
    }

    //   广告就绪，可以进行后续广告展示前特殊逻辑处理，一般不需要额外处理。串行or并行均会执行到此方法，区别是串行是广告加载成功后立即执行到此方法，并行时广告成功，也要等到选中改广告才执行此方法。
    @Override
    protected void adReady() {

    }

    //    广告销毁
    @Override
    public void doDestroy() {

    }

    //    广告展示
    @Override
    public void show() {
        if (splashAD == null) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, "splashAd null"));
            return;
        }
        splashAD.showAd(setting.getAdContainer());
    }

    //  广告有效性，如果adn支持广告有效性获取，建议复写此方法返回广告是否有效
    @Override
    public boolean isValid() {
        if (splashAD!=null){
           return splashAD.isValid();
        }
        return super.isValid();
    }


    private void adnLoadStart() {
        int timeout = sdkSupplier.timeout <= 0 ? 5000 : sdkSupplier.timeout;
        SplashADListener listener = new SplashADListener() {
            @Override
            public void onADDismissed() {
                LogUtil.simple(TAG + "onADDismissed ");
                if (null != setting) {
                    //                必须调用
                    //剩余时长在600ms以上，且未点击才按照跳过
                    if (remainTime >= 600 && !isClicked) {
                        setting.adapterDidSkip();
                    } else {
                        setting.adapterDidTimeOver();
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
//                必须调用
                handleFailed(code, msg);
            }

            @Override
            public void onADPresent() {
                LogUtil.simple(TAG + "onADPresent ");
            }

            @Override
            public void onADClicked() {
                LogUtil.simple(TAG + "onADClicked ");

                //                必须调用
                handleClick();
                isClicked = true;
            }

            @Override
            public void onADTick(long l) {
                LogUtil.simple(TAG + "onADTick :" + l);
                remainTime = l;
            }

            @Override
            public void onADExposure() {
                LogUtil.simple(TAG + "onADExposure ");

//                必须调用
                handleShow();
            }

            @Override
            public void onADLoaded(long expireTimestamp) {
                try {
                    LogUtil.simple(TAG + "onADLoaded expireTimestamp:" + expireTimestamp);
                    if (splashAD != null) {
                        LogUtil.devDebug(TAG + "getECPMLevel = " + splashAD.getECPMLevel() + ", getECPM = " + splashAD.getECPM());
                        updateBidding(splashAD.getECPM());
                    }

                    //                必须调用
                    handleSucceed();

                    long rt = SystemClock.elapsedRealtime();
                    long expire = expireTimestamp - rt;
                    LogUtil.high(TAG + "ad will expired in :" + expire + " ms");
                } catch (Throwable e) {
                    e.printStackTrace();
                    runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                }

            }
        };
        splashAD = new SplashAD(getRealContext(), getPosID(), listener, timeout);
        splashAD.fetchAdOnly();
    }


}
