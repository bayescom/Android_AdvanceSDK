package com.advance.custom;

import android.view.ViewGroup;

import com.advance.BaseParallelAdapter;

public abstract class AdvanceBaseCustomAdapter extends BaseParallelAdapter {

    public ViewGroup getAdContainer() {
        if (baseSetting != null) {
            return baseSetting.getAdContainer();
        }
        return null;
    }
}
