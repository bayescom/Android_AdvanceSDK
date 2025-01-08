package com.advance.supplier.csj;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.advance.AdvanceConfig;
import com.advance.AdvanceDrawSetting;
import com.advance.custom.AdvanceDrawCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;

import java.util.List;


public class CsjDrawAdapter extends AdvanceDrawCustomAdapter implements TTAdNative.NativeExpressAdListener {
    private TTAdNative mTTAdNative;
    private String TAG = "[CsjDrawAdapter] ";
    TTNativeExpressAd ad;

    public CsjDrawAdapter(Activity activity, AdvanceDrawSetting setting) {
        super(activity, setting);
    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
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
                handleFailed(code, msg);
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
    public void show() {
        try {
            if (isADViewAdded(ad.getExpressAdView())) {
                ad.render();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }

    }


    private void startLoad() {
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdSdk.getAdManager();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        //step3:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(activity.getApplicationContext());

        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(sdkSupplier.adspotid)
                .setSupportDeepLink(true)
                .setExpressViewAcceptedSize(setting.getCsjExpressWidth(), setting.getCsjExpressHeight()) //期望模板广告view的size,单位dp
                .setAdCount(1) //请求广告数量为1到3条
//                .setAdLoadType(PRELOAD)//推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                .build();

        mTTAdNative.loadExpressDrawFeedAd(adSlot, this);
    }

    @Override
    public void onError(int code, String message) {
        LogUtil.simple(TAG + "onError" + code + message);

        handleFailed(code, message);
    }

    @Override
    public void onNativeExpressAdLoad(List<TTNativeExpressAd> ads) {
        try {
            LogUtil.simple(TAG + "onNativeExpressAdLoad, ads = " + ads);

            if (ads == null || ads.isEmpty()) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "ads empty");
                return;
            }
            ad = ads.get(0);
            if (ad == null) {
                String nMsg = TAG + " ad null";
                AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, nMsg);
                runParaFailed(error);
                return;
            }
            updateBidding(CsjUtil.getEcpmValue(TAG, ad.getMediaExtraInfo()));

//            ad.setCanInterruptVideoPlay(false);
            ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
                //广告点击的回调
                @Override
                public void onAdClicked(View view, int type) {
                    LogUtil.simple(TAG + "onAdClicked, type = " + type);

                    handleClick();
                }

                //广告展示回调
                @Override
                public void onAdShow(View view, int type) {
                    LogUtil.simple(TAG + "onAdShow, type = " + type);

                    handleShow();
                }

                //广告渲染失败
                @Override
                public void onRenderFail(View view, String msg, int code) {
                    String log = "onRenderFail : code = " + code + ",msg =" + msg;
                    LogUtil.simple(TAG + "onRenderFail, log = " + log);

                    handleFailed(AdvanceError.ERROR_RENDER_FAILED, log);
                }

                //广告渲染成功
                @Override
                public void onRenderSuccess(View view, float width, float height) {
                    LogUtil.simple(TAG + "onRenderSuccess, width = " + width + ",height = " + height);

                }
            });

            handleSucceed();

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }
}
