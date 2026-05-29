package com.advance.supplier.gdt;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.AdvanceNativeExpressAdItem;
import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.qq.e.ads.cfg.DownAPPConfirmPolicy;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.advance.model.AdvanceError.ERROR_DATA_NULL;

public class GdtNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
    String TAG = "[GdtNativeExpressAdapter] ";
    NativeExpressADView adView;


    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        GdtUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAd();
            }
        });
    }
    public void loadAd() {

        //检查是否命中使用缓存逻辑
        boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this, GdtNativeExpressAdapter.class, new BYAbsCallBack<GdtNativeExpressAdapter>() {
            @Override
            public void invoke(GdtNativeExpressAdapter cacheAdapter) {
                //更新缓存广告得价格
                updateBidding(cacheAdapter.adView.getECPM());
            }
        });
        if (hitCache) {
            return;
        }

        int width = nativeExpressSetting.getExpressViewWidth();
        int height = nativeExpressSetting.getExpressViewHeight();
        if (height <= 0) {
            height = ADSize.AUTO_HEIGHT;
        }
//        if (width <= 0) {
//            width = ADSize.FULL_WIDTH;
//        }


        ADSize adSize = new ADSize(width, height);
        NativeExpressAD nativeExpressAd = new NativeExpressAD(activity, adSize, sdkSupplier.adspotid, new NativeExpressAD.NativeExpressADListener() {
            @Override
            public void onADLoaded(List<NativeExpressADView> list) {
                onADLoadedEV(list);
            }

            @Override
            public void onRenderFail(NativeExpressADView nativeExpressADView) {
                onRenderFailEV(nativeExpressADView);
            }

            @Override
            public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
                onRenderSuccessEV(nativeExpressADView);
            }

            @Override
            public void onADExposure(NativeExpressADView nativeExpressADView) {
                onADExposureEV(nativeExpressADView);
            }

            @Override
            public void onADClicked(NativeExpressADView nativeExpressADView) {
                onADClickedEV(nativeExpressADView);

            }

            @Override
            public void onADClosed(NativeExpressADView nativeExpressADView) {
                onADClosedEV(nativeExpressADView);

            }

            @Override
            public void onADLeftApplication(NativeExpressADView nativeExpressADView) {

            }

            @Override
            public void onNoAD(AdError adError) {
                onNoADEV(adError);
            }
        }); // 这里的Context必须为Activity
        VideoOption option = new VideoOption.Builder()
                .setAutoPlayMuted(nativeExpressSetting.isVideoMute())
                .build();
        nativeExpressAd.setVideoOption(option);
        nativeExpressAd.setMaxVideoDuration(nativeExpressSetting.getGdtMaxVideoDuration());
//        nativeExpressAd.setDownAPPConfirmPolicy(DownAPPConfirmPolicy.NOConfirm);
        nativeExpressAd.loadAD(sdkSupplier.adCount);

    }

    @Override
    protected void adPrepared() {

    }


    public void onADLoadedEV(List<NativeExpressADView> list) {
        LogUtil.simple(TAG + "onADLoadedEV");

        boolean isEmpty = list == null || list.isEmpty();
        if (isEmpty) {
            runParaFailed(AdvanceError.parseErr(ERROR_DATA_NULL));
            return;
        }

        boolean isAllDataNull = true;
        for (NativeExpressADView nativeExpressADView : list) {
            //判断view是否全部为空
            isAllDataNull = isAllDataNull && nativeExpressADView == null;
        }

        if (isAllDataNull) {
            runParaFailed(AdvanceError.parseErr(ERROR_DATA_NULL));
            return;
        }
        adView = list.get(0);
        if (adView == null) {
            runParaFailed(AdvanceError.parseErr(ERROR_DATA_NULL));
            return;
        }
        updateBidding(adView.getECPM());

        handleSucceed(this);
    }


    public void onRenderFailEV(View nativeExpressADView) {
        LogUtil.simple(TAG + "onRenderFailEV");

        handleRenderFailed(nativeExpressADView,AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED));

    }

    public void onRenderSuccessEV(View nativeExpressADView) {
        LogUtil.simple(TAG + "onRenderSuccessEV");

        handleRenderSuccess(nativeExpressADView);
    }

    public void onADExposureEV(View nativeExpressADView) {
        LogUtil.simple(TAG + "onADExposureEV");
        this.nativeExpressADView = nativeExpressADView;

        handleShow();
    }

    public void onADClickedEV(View nativeExpressADView) {
        LogUtil.simple(TAG + "onADClickedEV");

        handleClick();
    }

    public void onADClosedEV(View nativeExpressADView) {
        LogUtil.simple(TAG + "onADClosedEV");

        handleClose();
    }


    public void onNoADEV(AdError adError) {
        LogUtil.simple(TAG + "onADClosedEV");
        int code = -1;
        String msg = "default onNoAD";
        if (adError != null) {
            code = adError.getErrorCode();
            msg = adError.getErrorMsg();
        }
        handleFailed(code, msg);
    }

    @Override
    public void destroyAd() {

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra) {
        try {
            addADView(adView);
            adView.render();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public boolean isValid() {
        if (adView != null) {
            return adView.isValid();
        }
           return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, String price, Map<String, Object> referBidInfo) {

    }
}
