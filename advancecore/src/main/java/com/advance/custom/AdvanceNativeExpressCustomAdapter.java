package com.advance.custom;

import android.view.View;
import android.view.ViewGroup;

import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;

public abstract class AdvanceNativeExpressCustomAdapter extends AdvanceBaseCustomAdapter {
   

    public void addADView(View adView) {
        try {
            boolean add = AdvanceUtil.addADView(getAdContainer() , adView);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void removeADView() {
        try {
            ViewGroup adContainer = nativeExpressSetting.getAdContainer();
            if (adContainer == null) {
                LogUtil.e("adContainer 不存在");
                return;
            }
            LogUtil.max("remove adContainer = " + adContainer.toString());

            adContainer.removeAllViews();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void handleClose() {
        try {
            if (nativeExpressSetting != null) {
                nativeExpressSetting.adapterDidClosed(nativeExpressADView);
            }

            removeADView();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void handleRenderFailed(View view) {
        try {
            if (nativeExpressSetting != null) {
                nativeExpressSetting.adapterRenderFailed(view);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void handleRenderSuccess(View view) {
        try {
            if (nativeExpressSetting != null) {
                nativeExpressSetting.adapterRenderSuccess(view);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
