package com.advance;

import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;

public interface AdvanceCustomizeSupplierListener {
    void onSupplierFailed(AdvanceError advanceError);
    void onSupplierSelected(SdkSupplier selectedSupplier);
}
