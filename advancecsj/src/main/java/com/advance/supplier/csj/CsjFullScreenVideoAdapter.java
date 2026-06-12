package com.advance.supplier.csj;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;

import com.advance.AdvanceConfig;
import com.advance.custom.AdvanceFullScreenCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

import java.util.Map;

public class CsjFullScreenVideoAdapter extends AdvanceFullScreenCustomAdapter implements TTAdNative.FullScreenVideoAdListener, TTFullScreenVideoAd.FullScreenVideoAdInteractionListener {
    private TTFullScreenVideoAd ttFullScreenVideoAd;
    private String TAG = "[CsjFullScreenVideoAdapter] ";

   

    public void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra) {

        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdSdk.getAdManager();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        if (AdvanceConfig.getInstance().isNeedPermissionCheck()) {
            ttAdManager.requestPermissionIfNecessary(activity);
        }
        //step3:创建TTAdNative对象,用于调用广告请求接口
        TTAdNative mTTAdNative = ttAdManager.createAdNative(activity.getApplicationContext());

        boolean isPortrait = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        int orientation;
        if (isPortrait) { // 基于当前屏幕方向来设定广告期望方向
            orientation = TTAdConstant.VERTICAL;
        } else {
            orientation = TTAdConstant.HORIZONTAL;
        }

        AdSlot adSlot;
        if (fullScreenVideoSetting.isCsjExpress()) {

            adSlot = new AdSlot.Builder()
                    .setCodeId(sdkSupplier.adspotid)
                    .setExpressViewAcceptedSize(fullScreenVideoSetting.getCsjExpressWidth(), fullScreenVideoSetting.getCsjExpressHeight())
                    .setSupportDeepLink(true)
//                    .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                    .setOrientation(orientation)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                    .build();
        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(sdkSupplier.adspotid)
                    .setSupportDeepLink(true)
//                    .setDownloadType(AdvanceSetting.getInstance().csj_downloadType)
                    .setOrientation(orientation)//必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                    .build();
        }


//step5:请求广告
        mTTAdNative.loadFullScreenVideoAd(adSlot, this);
    }

    @Override
    protected void adPrepared() {
    }

    @Override
    public void onError(int i, String s) {
        handleFailed(i, s);
    }

    @Override
    public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {
        try {
            LogUtil.simple(TAG + "onFullScreenVideoAdLoad ");

            this.ttFullScreenVideoAd = ttFullScreenVideoAd;

            if (ttFullScreenVideoAd == null) {
                String nMsg = TAG + "ttFullScreenVideoAd  null";
                AdvanceError error = AdvanceError.parseErr(AdvanceError.ERROR_DATA_NULL, nMsg);
                runParaFailed(error);
                return;
            }
            handleSucceed(CsjUtil.getEcpmValue(TAG, ttFullScreenVideoAd.getMediaExtraInfo()));
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onFullScreenVideoCached() {
        LogUtil.simple(TAG + "onFullScreenVideoCached ");
    }

    @Override
    public void onFullScreenVideoCached(TTFullScreenVideoAd ttFullScreenVideoAd) {
        try {
            String ad = "";
            if (ttFullScreenVideoAd != null) {
                ad = ttFullScreenVideoAd.toString();
            }
            LogUtil.simple(TAG + "onFullScreenVideoCached( " + ad + ")");
        } catch (Throwable e) {
            e.printStackTrace();
        }

        handleCached();
    }


    @Override
    public void destroyAd() {

    }

    public void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra){
        try {
            ttFullScreenVideoAd.setFullScreenVideoAdInteractionListener(this);
            ttFullScreenVideoAd.showFullScreenVideoAd(activity, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }

    /**
     * 广告事件监听
     */

    @Override
    public void onAdShow() {
        LogUtil.simple(TAG + "onFullScreenVideo onAdShow");
        handleShow();

    }

    @Override
    public void onAdVideoBarClick() {
        LogUtil.simple(TAG + "onFullScreenVideo onAdVideoBarClick");
        handleClick();

    }

    @Override
    public void onAdClose() {
        LogUtil.simple(TAG + "onFullScreenVideo onAdClose");

        handleClose();
    }

    @Override
    public void onVideoComplete() {
        LogUtil.simple(TAG + "onFullScreenVideo onVideoComplete");

        handleComplete();
    }

    @Override
    public void onSkippedVideo() {
        LogUtil.simple(TAG + "onFullScreenVideo onSkippedVideo");

       handleSkip();
    }

    @Override
    public boolean isValid() {
        if (ttFullScreenVideoAd != null && ttFullScreenVideoAd.getMediationManager() != null) {
            return ttFullScreenVideoAd.getMediationManager().isReady();
        }

        return true;
    }

    @Override
    public void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo) {
        LogUtil.simple(TAG + "notifyBiddingResult , isWin = " + isWin + " , winPrice = " +winPrice+ ", referBidInfo = " + referBidInfo);
        

    }
}
