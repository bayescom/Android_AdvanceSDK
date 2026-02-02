package com.advance;

import com.advance.model.SdkSupplier;

public interface BaseAdEventListener extends BaseSetting {
//    void adapterDidSucceed();
    void adapterDidSucceed(SdkSupplier supplier);

//    void adapterDidShow();
    void adapterDidShow(SdkSupplier supplier);

//    void adapterDidClicked();
    void adapterDidClicked(SdkSupplier supplier);
}
