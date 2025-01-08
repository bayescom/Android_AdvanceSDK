package com.advance;

import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;

public interface InterstitialSetting extends BaseAdEventListener {
    void adapterDidClosed();

    float getCsjExpressViewWidth();

    float getCsjExpressViewHeight();

    UnifiedInterstitialMediaListener getGdtMediaListener();

    @Deprecated
    boolean isCsjNew();

}
