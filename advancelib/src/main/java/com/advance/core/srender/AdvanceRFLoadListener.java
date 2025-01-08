package com.advance.core.srender;

import android.support.annotation.Keep;

import com.advance.core.common.AdvanceErrListener;

import java.util.List;

//自渲染广告加载回调
@Keep
public interface AdvanceRFLoadListener extends AdvanceErrListener {

    void onADLoaded(AdvanceRFADData adData);

}
