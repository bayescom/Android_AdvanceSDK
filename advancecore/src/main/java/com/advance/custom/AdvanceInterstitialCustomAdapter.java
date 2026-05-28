package com.advance.custom;

import android.app.Activity;

import com.advance.InterstitialSetting;

public abstract class AdvanceInterstitialCustomAdapter extends AdvanceBaseCustomAdapter {



    public void handleClose() {
        try {
            if (interstitialSetting != null) {
                interstitialSetting.adapterDidClosed();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
