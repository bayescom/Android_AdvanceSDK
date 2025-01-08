package com.advance.core.common;

import com.advance.model.AdvanceError;

//通用渲染回调
public interface AdvanceRenderListener {
    void onRenderSuccess();

    void onRenderFailed(AdvanceError advanceError);

    void onClick();

}
