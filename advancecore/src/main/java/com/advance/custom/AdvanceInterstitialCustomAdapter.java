package com.advance.custom;


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
