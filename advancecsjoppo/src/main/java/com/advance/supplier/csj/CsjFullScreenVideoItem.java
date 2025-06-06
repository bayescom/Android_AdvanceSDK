package com.advance.supplier.csj;

import android.app.Activity;

import com.advance.AdvanceConfig;
import com.advance.AdvanceFullScreenItem;
import com.advance.FullScreenVideoSetting;
import com.advance.model.AdvanceError;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bykv.vk.openvk.TTAppDownloadListener;
import com.bykv.vk.openvk.TTFullVideoObject;
import com.bykv.vk.openvk.TTVfConstant;

@Deprecated
public class CsjFullScreenVideoItem implements AdvanceFullScreenItem {
    private TTFullVideoObject ad;
    private FullScreenVideoSetting advanceFullScreenVideo;
    private Activity activity;
    private TTFullVideoObject.FullVideoVsInteractionListener listener;
    CsjFullScreenVideoAdapter baseParallelAdapter;
    String TAG = "[CsjFullScreenVideoItem] ";

    CsjFullScreenVideoItem(Activity activity, CsjFullScreenVideoAdapter baseParallelAdapter, FullScreenVideoSetting advanceFullScreenVideo, TTFullVideoObject ad) {
        this.advanceFullScreenVideo = advanceFullScreenVideo;
        this.ad = ad;
        this.baseParallelAdapter = baseParallelAdapter;
        this.activity = activity;
    }

    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_CSJ;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_CSJ;
    }

    @Deprecated
    public void setCsjFullScreenListener(TTFullVideoObject.FullVideoVsInteractionListener listener) {
        this.listener = listener;
    }

    @Override
    public void showAd() {
        try {
            if (ad == null) {
                if (baseParallelAdapter != null)
                    baseParallelAdapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, "showAd ad null"));
                return;
            }
            ad.setFullScreenVideoAdInteractionListener(baseParallelAdapter);
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    ad.showFullVideoVs(activity, TTVfConstant.RitScenes.GAME_GIFT_BONUS, null);
                    ad = null;
                }
            });

        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (baseParallelAdapter != null)
                    baseParallelAdapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setDownloadListener(TTAppDownloadListener downloadListener) {
        try {
            if (ad != null)
                ad.setDownloadListener(downloadListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
