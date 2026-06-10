package com.advance.custom;

import android.app.Activity;
import android.content.Context;

import java.util.Map;

public interface AdvanceAdapterItf {
    void loadAd(Context context, Map<String, Object> localExtra, Map<String, Object> serverExtra);

    void showAd(Activity activity, Map<String, Object> localExtra, Map<String, Object> serverExtra);

    boolean isValid();

    void destroyAd();

    void notifyBiddingResult(boolean isWin, double winPrice, Map<String, Object> referBidInfo);

}
