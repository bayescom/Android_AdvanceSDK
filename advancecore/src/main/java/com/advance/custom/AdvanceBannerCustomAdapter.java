package com.advance.custom;



public abstract class AdvanceBannerCustomAdapter extends AdvanceBaseCustomAdapter {

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
