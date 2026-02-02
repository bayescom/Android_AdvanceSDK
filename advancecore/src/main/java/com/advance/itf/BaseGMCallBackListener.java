package com.advance.itf;

import com.advance.model.AdvanceError;

public interface BaseGMCallBackListener {
    void onAdSuccess();

    void onAdShow();

    void onAdClick();

    void renderFailed();

    void allFailed(AdvanceError advanceError);
}
