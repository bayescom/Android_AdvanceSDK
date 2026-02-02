package com.advance.custom;

import android.app.Activity;

import com.advance.BannerSetting;

public abstract class AdvanceBannerCustomAdapter extends AdvanceBaseCustomAdapter {
    protected BannerSetting bannerSetting;
    public AdvanceBannerCustomAdapter(Activity activity, BannerSetting setting) {
        super(activity, setting);
        this.bannerSetting = setting;
    }

    protected void handleClose(){
        if (bannerSetting!=null){
            bannerSetting.adapterDidDislike();
        }
    }
//
//    @Override
//    public void show() {
//
//    }
}
