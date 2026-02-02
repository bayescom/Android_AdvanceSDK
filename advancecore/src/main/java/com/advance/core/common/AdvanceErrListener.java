package com.advance.core.common;

import com.advance.model.AdvanceError;

public interface AdvanceErrListener {
    void onAdFailed(AdvanceError advanceError);
}
