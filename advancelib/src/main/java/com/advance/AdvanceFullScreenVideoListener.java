package com.advance;

public interface AdvanceFullScreenVideoListener extends AdvanceBaseListener {
    void onAdLoaded(AdvanceFullScreenItem advanceFullScreenItem);

    void onAdClose();

    void onVideoComplete();

    void onVideoSkipped();

    void onVideoCached();
}
