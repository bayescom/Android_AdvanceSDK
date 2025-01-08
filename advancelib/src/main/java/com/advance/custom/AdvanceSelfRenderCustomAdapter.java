package com.advance.custom;

import android.content.Context;

import com.advance.core.srender.AdvanceRFBridge;

public abstract class AdvanceSelfRenderCustomAdapter extends AdvanceBaseCustomAdapter {



    public AdvanceSelfRenderCustomAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
        this.mAdvanceRFBridge = mAdvanceRFBridge;
    }

    public void handleClose() {
        try {
            try {
                mAdvanceRFBridge.getMaterialProvider().rootView.removeAllViews();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (mAdvanceRFBridge != null) {
                mAdvanceRFBridge.adapterDidClose(sdkSupplier);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


//    public void handleADSuccess
}
