package com.advance.supplier.csj;

import android.app.Activity;
import android.view.View;

import com.advance.AdvanceConfig;
import com.advance.AdvanceNativeExpressAdItem;
import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYLog;
import com.bykv.vk.openvk.TTNtExpressObject;
import com.bykv.vk.openvk.TTVfDislike;
import com.bykv.vk.openvk.TTVfManager;
import com.bykv.vk.openvk.TTVfNative;
import com.bykv.vk.openvk.TTVfSdk;
import com.bykv.vk.openvk.VfSlot;

import java.util.ArrayList;
import java.util.List;

public class CsjNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter implements TTVfNative.NtExpressVfListener {

    private List<TTNtExpressObject> ads;
    TTNtExpressObject ttNativeExpressAd;
    private NativeExpressSetting advanceNativeExpress;
    private String TAG = "[CsjNativeExpressAdapter] ";


    public CsjNativeExpressAdapter(Activity activity, NativeExpressSetting advanceNativeExpress) {
        super(activity, advanceNativeExpress);
        this.advanceNativeExpress = advanceNativeExpress;

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

    private void startLoad() {
        final TTVfManager ttAdManager = TTVfSdk.getVfManager();

        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        BYLog.dev(TAG + "advanceNativeExpress.getExpressViewWidth() = " + advanceNativeExpress.getExpressViewWidth());

        TTVfNative ttAdNative = ttAdManager.createVfNative(activity);
        VfSlot adSlot = new VfSlot.Builder()
                .setCodeId(sdkSupplier.adspotid) //广告位id
                .setSupportDeepLink(true)
                .setAdCount(sdkSupplier.adCount) //请求广告数量为1到3条
//                .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                .setExpressViewAcceptedSize(advanceNativeExpress.getExpressViewWidth(), advanceNativeExpress.getExpressViewHeight()) //期望模板广告view的size,单位dp
                .setImageAcceptedSize(advanceNativeExpress.getCsjImageWidth(), advanceNativeExpress.getCsjImageHeight())
                .build();
        //加载广告
        ttAdNative.loadNtExpressVn(adSlot, this);
    }

    @Override
    protected void adReady() {

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
    public void onError(int i, String s) {
        handleFailed(i, s);
    }

    @Override
    public void onNtExpressVnLoad(List<TTNtExpressObject> ads) {
        this.ads = ads;
        try {
            LogUtil.simple(TAG + "onNativeExpressAdLoad");
            if (ads == null || ads.size() == 0) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "ads empty");
            } else {
                nativeExpressAdItemList = new ArrayList<>();
                for (TTNtExpressObject ttNativeExpressAd : ads) {
                    AdvanceNativeExpressAdItem advanceNativeExpressAdItem = new CsjNativeExpressAdItem(activity, this, ttNativeExpressAd);
                    nativeExpressAdItemList.add(advanceNativeExpressAdItem);
                }
                ttNativeExpressAd = ads.get(0);

                if (ttNativeExpressAd == null) {
                    String nMsg = TAG + "ttNativeExpressAd  null";
                    AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, nMsg);
                    runParaFailed(error);
                    return;
                }
                updateBidding(CsjUtil.getEcpmValue(TAG, ttNativeExpressAd.getMediaExtraInfo()));

                handleSucceed();


            }
        } catch (Throwable e) {
            e.printStackTrace();
            handleFailed(AdvanceError.ERROR_EXCEPTION_LOAD, "");
        }
    }

    public void onAdItemShow(View view) {
        LogUtil.simple(TAG + "onAdItemShow");

        handleShow();
    }

    public void onAdItemClicked(View view) {
        LogUtil.simple(TAG + "onAdItemClicked");
        handleClick();

    }

    public void onAdItemRenderFailed(View view, String msg, int code) {
        LogUtil.simple(TAG + "onAdItemRenderFailed");

        if (null != advanceNativeExpress) {
            advanceNativeExpress.adapterRenderFailed(view);
        }

        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, TAG + code + "， " + msg));
        removeADView();
    }

    public void onAdItemRenderSuccess(View view) {
        LogUtil.simple(TAG + "onAdItemRenderSuccess");

        if (null != advanceNativeExpress) {
            advanceNativeExpress.adapterRenderSuccess(view);
        }

    }

    public void onAdItemClose(View view) {
        LogUtil.simple(TAG + "onAdItemClose");

        if (null != advanceNativeExpress) {
            advanceNativeExpress.adapterDidClosed(view);
        }
        removeADView();
    }

    public void onAdItemErr(AdvanceError advanceError) {
        LogUtil.simple(TAG + "onAdItemErr ");

        runParaFailed(advanceError);
    }


    @Override
    public void doDestroy() {

    }

    @Override
    public void show() {
        if (ttNativeExpressAd == null) {
            LogUtil.e("无广告内容");
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL));
            return;
        }
        try {
            ttNativeExpressAd.setExpressInteractionListener(new TTNtExpressObject.ExpressNtInteractionListener() {
                @Override
                public void onClicked(View view, int i) {
                    onAdItemClicked(view);
                }

                @Override
                public void onShow(View view, int i) {
                    onAdItemShow(view);
                }

                @Override
                public void onRenderFail(View view, String s, int i) {

                    onAdItemRenderFailed(view, s, i);
                }

                @Override
                public void onRenderSuccess(View view, float v, float v1) {
                    onAdItemRenderSuccess(view);
                }
            });
            Activity showAct = activity;
            if (mSetting != null && mSetting.getAdContainer() != null) {
                showAct = getRealActivity(mSetting.getAdContainer());
            }
            ttNativeExpressAd.setDislikeCallback(showAct, new TTVfDislike.DislikeInteractionCallback() {
                @Override
                public void onShow() {

                }

                @Override
                public void onSelected(int i, String s, boolean enforce) {
                    LogUtil.simple(TAG + "DislikeInteractionCallback_onSelected , int i = +" + i + ", String s" + s + ", boolean enforce" + enforce + " ;");
                    onAdItemClose(null);
                }

                @Override
                public void onCancel() {

                }
            });
            addADView(ttNativeExpressAd.getExpressNtView());
            ttNativeExpressAd.render();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }

    }


    @Override
    public boolean isValid() {
        if (ttNativeExpressAd != null && ttNativeExpressAd.getMediationManager() != null) {
            return ttNativeExpressAd.getMediationManager().isReady();
        }
        return super.isValid();
    }
}
