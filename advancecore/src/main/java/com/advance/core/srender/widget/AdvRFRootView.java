package com.advance.core.srender.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

//广告根布局，自渲染广告布局需要在当前根布局内进行
public class AdvRFRootView extends FrameLayout {

    public AdvRFRootView(@NonNull Context context) {
        super(context);
    }

    public AdvRFRootView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvRFRootView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
