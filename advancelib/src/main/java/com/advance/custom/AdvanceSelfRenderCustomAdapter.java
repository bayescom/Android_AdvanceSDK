package com.advance.custom;

import android.content.Context;
import android.view.ViewGroup;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.utils.LogUtil;

public abstract class AdvanceSelfRenderCustomAdapter extends AdvanceBaseCustomAdapter {



    public AdvanceSelfRenderCustomAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
        this.mAdvanceRFBridge = mAdvanceRFBridge;
    }

    public void handleClose() {
        try {
            removeADView();

            if (mAdvanceRFBridge != null) {
                mAdvanceRFBridge.adapterDidClose(sdkSupplier);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void removeADView() {
        try {
            ViewGroup adContainer = mAdvanceRFBridge.getMaterialProvider().rootView;
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
//    public void handleADSuccess
}
