package com.advance.custom;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.advance.NativeExpressSetting;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;

public abstract class AdvanceNativeExpressCustomAdapter extends AdvanceBaseCustomAdapter {
    public NativeExpressSetting mSetting;

    public AdvanceNativeExpressCustomAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
        mSetting = baseSetting;
    }

    public void addADView(View adView) {
        try {
            ViewGroup adContainer = mSetting.getAdContainer();
            boolean add = AdvanceUtil.addADView(adContainer, adView);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void removeADView() {
        try {
            ViewGroup adContainer = mSetting.getAdContainer();
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
            if (mSetting != null) {
                mSetting.adapterDidClosed(nativeExpressADView);
            }

            removeADView();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
