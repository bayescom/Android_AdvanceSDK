package com.advance;

import android.view.View;
import android.view.ViewGroup;

import com.advance.model.SdkSupplier;

import java.util.List;

public interface NativeExpressSetting extends BaseSetting {

    int getExpressViewWidth();

    int getExpressViewHeight();

    boolean getGdtAutoHeight();

    boolean getGdtFullWidth();

    boolean isVideoMute();

    void adapterAdDidLoaded(List<AdvanceNativeExpressAdItem> advanceNativeExpressAdItemList, SdkSupplier supplier);

    void adapterRenderFailed(View nativeExpressADView);

    void adapterRenderSuccess(View nativeExpressADView);

    void adapterDidShow(View nativeExpressADView, SdkSupplier supplier);

    void adapterDidClicked(View nativeExpressADView, SdkSupplier supplier);

    void adapterDidClosed(View nativeExpressADView);

    int getGdtMaxVideoDuration();

    int getCsjImageWidth();

    int getCsjImageHeight();

    ViewGroup getAdContainer();
}
