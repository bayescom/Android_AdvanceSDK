package com.advance;

import androidx.annotation.Nullable;
import android.view.View;

import java.util.List;

public interface AdvanceNativeExpressListener extends AdvanceSelectListener {
    void onAdClose(@Nullable View view);

    void onAdShow(@Nullable View view);

    void onAdRenderFailed(@Nullable View view);

    void onAdRenderSuccess(@Nullable View view);

    void onAdClicked(@Nullable View view);

    void onAdLoaded(@Nullable List<AdvanceNativeExpressAdItem> list);
}
