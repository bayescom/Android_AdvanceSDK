package com.advance.supplier.ks;

import android.app.Activity;
import androidx.annotation.Nullable;
import android.view.View;

import com.advance.AdvanceDrawSetting;
import com.advance.custom.AdvanceDrawCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsDrawAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.List;

public class KSDrawAdapter extends AdvanceDrawCustomAdapter implements KsDrawAd.AdInteractionListener {
    private String TAG = "[KSDrawAdapter] ";
    private KsDrawAd drawAD;

    public KSDrawAdapter(Activity activity, AdvanceDrawSetting setting) {
        super(activity, setting);
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
        KsScene scene = new KsScene.Builder(KSUtil.getADID(sdkSupplier)).build();
        KsAdSDK.getLoadManager().loadDrawAd(scene, new KsLoadManager.DrawAdListener() {
            @Override
            public void onError(int code, String msg) {
                LogUtil.simple(TAG + " onError " + code + msg);

                handleFailed(code, msg);
            }

            @Override
            public void onDrawAdLoad(@Nullable List<KsDrawAd> list) {
                LogUtil.simple(TAG + "onInterstitialAdLoad");

                try {
                    if (list == null || list.size() == 0 || list.get(0) == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "");
                    } else {
                        drawAD = list.get(0);
                        //回调监听
                        drawAD.setAdInteractionListener(KSDrawAdapter.this);
                        updateBidding(drawAD.getECPM());

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
        paraLoadAd();
    }

    @Override
    public void show() {
        if (drawAD == null) {
            handleFailed(AdvanceError.ERROR_DATA_NULL, "ad is empty");
            return;
        }
        try {
            View drawVideoView = drawAD.getDrawView(activity);
            if (isADViewAdded(drawVideoView)) {

            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    /**
     * ks回调事件
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
    public void onVideoPlayStart() {
        LogUtil.simple(TAG + " onVideoPlayStart");

    }

    @Override
    public void onVideoPlayPause() {
        LogUtil.simple(TAG + " onVideoPlayPause");

    }

    @Override
    public void onVideoPlayResume() {
        LogUtil.simple(TAG + " onVideoPlayResume");

    }

    @Override
    public void onVideoPlayEnd() {
        LogUtil.simple(TAG + " onVideoPlayEnd");

    }

    @Override
    public void onVideoPlayError() {
        LogUtil.simple(TAG + " onVideoPlayError");

    }
}
