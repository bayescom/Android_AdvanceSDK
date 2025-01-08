package com.advance.supplier.gdt;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.advance.AdvanceConfig;
import com.advance.AdvanceFullScreenItem;
import com.advance.BaseParallelAdapter;
import com.advance.FullScreenVideoSetting;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;

@Deprecated
public class GdtFullScreenVideoItem implements AdvanceFullScreenItem {
    private Activity activity;
    private FullScreenVideoSetting advanceFullScreenVideo;
    private UnifiedInterstitialAD iad;
    BaseParallelAdapter adapter;

    public GdtFullScreenVideoItem(Activity activity, BaseParallelAdapter adapter, UnifiedInterstitialAD iad) {
        this.adapter = adapter;
        this.iad = iad;
        this.activity = activity;
    }

    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_GDT;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_GDT;
    }

    @Override
    public void showAd() {
        try {
            boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();

            if (isMainThread) {
                doShow();
            } else {
                //如果是非主线程，需要强制切换到主线程来进行初始化
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.high("force to main thread run show");
                        doShow();
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (adapter != null)
                    adapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doShow() {
        try {
            if (iad != null) {
                iad.showFullScreenAD(activity);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
