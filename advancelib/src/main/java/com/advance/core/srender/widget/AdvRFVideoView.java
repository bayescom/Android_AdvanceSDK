package com.advance.core.srender.widget;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

//用来渲染视频得view，主要优量汇、mercury 需要用到特定的view来包裹
public class AdvRFVideoView extends FrameLayout {
    public AdvRFVideoView(@NonNull Context context) {
        super(context);
    }

    public AdvRFVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvRFVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
