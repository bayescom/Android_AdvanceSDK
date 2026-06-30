package com.advance;

public interface AdvanceFullScreenVideoListener extends AdvanceBaseListener {
    void onAdLoaded();

    void onAdClose();

    void onVideoComplete();

    void onVideoSkipped();

    void onVideoCached();
}
