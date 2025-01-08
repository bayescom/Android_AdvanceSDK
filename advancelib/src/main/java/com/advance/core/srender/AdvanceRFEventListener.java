package com.advance.core.srender;

import com.advance.model.AdvanceError;

//广告得曝光等事件回调
public interface AdvanceRFEventListener {
    void onAdShow(AdvanceRFADData adData);

    void onAdClicked(AdvanceRFADData adData);

    void onAdClose(AdvanceRFADData adData);

    void onAdErr(AdvanceRFADData adData, AdvanceError advanceError);
}
