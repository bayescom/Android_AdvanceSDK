package com.advance.custom;


public abstract class AdvanceFullScreenCustomAdapter extends AdvanceBaseCustomAdapter {


    public void handleCached() {
        try {
            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onCached();
                }
            } else {
                if (null != fullScreenVideoSetting) {
                    fullScreenVideoSetting.adapterVideoCached();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void handleClose(){
        try {
            if (fullScreenVideoSetting != null)
                fullScreenVideoSetting.adapterClose();
        }  catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void handleComplete(){
        try {
            if (fullScreenVideoSetting != null)
                fullScreenVideoSetting.adapterVideoComplete();
        }  catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public void handleSkip(){
        try {
            if (fullScreenVideoSetting != null)
                fullScreenVideoSetting.adapterVideoSkipped();
        }  catch (Throwable e) {
            e.printStackTrace();
        }
    }



}
