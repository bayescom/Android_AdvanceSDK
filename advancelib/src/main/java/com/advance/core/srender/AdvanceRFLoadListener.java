package com.advance.core.srender;

import androidx.annotation.Keep;

import com.advance.core.common.AdvanceErrListener;

//自渲染广告加载回调
@Keep
public interface AdvanceRFLoadListener extends AdvanceErrListener {

    void onADLoaded(AdvanceRFADData adData);

}
