package com.advance.custom;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import com.advance.BaseParallelAdapter;
import com.advance.BaseSetting;

import java.lang.ref.SoftReference;

public abstract class AdvanceBaseCustomAdapter extends BaseParallelAdapter {
    public AdvanceBaseCustomAdapter() {
    }


    public ViewGroup getAdContainer() {


        if (nativeExpressSetting != null) {
            return nativeExpressSetting.getAdContainer();
        }

        if (bannerSetting != null) {
            return bannerSetting.getContainer();
        }

        if (drawSetting != null) {
            return drawSetting.getContainer();
        }


        return null;
    }
}
