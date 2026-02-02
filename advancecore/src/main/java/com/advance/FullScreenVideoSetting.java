package com.advance;

import com.advance.model.SdkSupplier;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;

public interface FullScreenVideoSetting extends BaseSetting {
    void adapterAdDidLoaded(AdvanceFullScreenItem item, SdkSupplier supplier);

    void adapterVideoCached();

    void adapterVideoComplete();

    void adapterClose();

    void adapterDidShow(SdkSupplier supplier);

    void adapterDidClicked(SdkSupplier supplier);

    void adapterVideoSkipped();

    UnifiedInterstitialMediaListener getGdtMediaListener();

    VideoOption getGdtVideoOption();

    boolean isCsjExpress();

    int getCsjExpressHeight();

    int getCsjExpressWidth();

//    void setParaCachedSupId(String id);//设置并行时当前缓存成功的渠道id
}
