package com.advance.custom;

import android.app.Activity;
import android.content.Context;

import com.advance.BaseParallelAdapter;
import com.advance.BaseSetting;

import java.lang.ref.SoftReference;

public abstract class AdvanceBaseCustomAdapter extends BaseParallelAdapter {

    public AdvanceBaseCustomAdapter(SoftReference<Activity> softReferenceActivity, BaseSetting baseSetting) {
        super(softReferenceActivity, baseSetting);
    }

    public AdvanceBaseCustomAdapter(Activity activity, BaseSetting baseSetting) {
        super(activity, baseSetting);
    }

    public AdvanceBaseCustomAdapter(Context context, BaseSetting baseSetting) {
        super(context, baseSetting);
    }

}
