package com.advance;

import android.view.View;

@Deprecated
public interface AdvanceNativeExpressAdItem {
    @Deprecated
    String getSdkTag();

    String getSdkId();

    void destroy();

    void render();

    View getExpressAdView();
}
