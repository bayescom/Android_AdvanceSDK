package com.advance;


public interface BannerSetting extends BaseAdEventListener {
    void adapterDidDislike();

    int getRefreshInterval();

    int getCsjAcceptedSizeWidth();

    int getCsjAcceptedSizeHeight();

    int getCsjExpressViewAcceptedWidth();

    int getCsjExpressViewAcceptedHeight();

//    ViewGroup getContainer();
}
