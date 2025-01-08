package com.advance.supplier.gdt;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.advance.AdvanceConfig;
import com.advance.AdvanceNativeExpressAdItem;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.ads.nativ.NativeExpressMediaListener;
import com.qq.e.comm.pi.AdData;

@Deprecated
public class GdtNativeAdExpressAdItem implements AdvanceNativeExpressAdItem {
    private GdtNativeExpressAdapter gdtNativeExpressAdapter;
    private NativeExpressADView nativeExpressADView;
    private GdtEventListener2 listener2;

    public GdtNativeAdExpressAdItem(GdtNativeExpressAdapter gdtNativeExpressAdapter) {
        this.gdtNativeExpressAdapter = gdtNativeExpressAdapter;
    }


    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_GDT;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_GDT;
    }

    public GdtNativeAdExpressAdItem(GdtNativeExpressAdapter gdtNativeExpressAdapter, NativeExpressADView nativeExpressADView) {
        this.gdtNativeExpressAdapter = gdtNativeExpressAdapter;
        this.nativeExpressADView = nativeExpressADView;
    }

    public View getNativeExpressADView() {
        if (nativeExpressADView != null) {
            return nativeExpressADView;
        }

        return null;
    }

    @Override
    public void render() {
        try {
            boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
            if (isMainThread) {
                doRender();
            } else {
                //如果是非主线程，需要强制切换到主线程来进行初始化
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.high("force to main thread run render");
                        doRender();
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (gdtNativeExpressAdapter != null) {
                    gdtNativeExpressAdapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER));
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doRender() {
        try {
            if (null != nativeExpressADView) {
                nativeExpressADView.render();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setAdSize(ADSize adSize) {
        if (null != nativeExpressADView) {
            nativeExpressADView.setAdSize(adSize);
        }
    }

    @Override
    public void destroy() {
        if (null != nativeExpressADView) {
            nativeExpressADView.destroy();
        }
    }

    @Override
    public View getExpressAdView() {
        return getNativeExpressADView();
    }

    public AdData getBoundData() {
        try {
            return nativeExpressADView.getBoundData();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setMediaListener(NativeExpressMediaListener mediaListener) {
        if (nativeExpressADView != null) {
            nativeExpressADView.setMediaListener(mediaListener);
        }
    }

    public void setAdEventListener2(GdtEventListener2 listener2) {
        this.listener2 = listener2;
    }

}
