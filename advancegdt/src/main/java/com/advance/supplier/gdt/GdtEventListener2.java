package com.advance.supplier.gdt;

import android.view.View;

public interface GdtEventListener2 {

    void onRenderSuccess(View nativeExpressADView);

    void onRenderFail(View nativeExpressADView);

    void onAdClosed(View nativeExpressADView);
}
