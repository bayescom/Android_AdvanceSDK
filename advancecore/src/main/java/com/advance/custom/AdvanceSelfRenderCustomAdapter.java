package com.advance.custom;

import android.content.Context;
import android.view.ViewGroup;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.core.srender.AdvanceRFMaterialProvider;
import com.advance.utils.LogUtil;

public abstract class AdvanceSelfRenderCustomAdapter extends AdvanceBaseCustomAdapter {


    public void handleClose() {
        try {
            removeADView();

            if (nativeSetting != null) {
                nativeSetting.adapterDidClose(sdkSupplier);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void removeADView() {
        try {
            ViewGroup adContainer = nativeSetting.getMaterialProvider().rootView;
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

    public AdvanceRFMaterialProvider getMaterialProvider(){
        AdvanceRFMaterialProvider result = null;
        if (nativeSetting != null){
            result = nativeSetting.getMaterialProvider();
        }
        return  result;
    }
//    public void handleADSuccess
}
