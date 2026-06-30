package com.advance.custom;

public abstract class AdvanceSplashCustomAdapter extends AdvanceBaseCustomAdapter {
    public boolean isCountingEnd = false;//用来判断是否倒计时走到了最后，false 回调dismiss的话代表是跳过，否则倒计时结束


    protected void handleSkip() {
        try {
            if (splashSetting != null) {
                splashSetting.adapterDidSkip();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    protected void handleTimeOver() {
        try {
            if (splashSetting != null) {
                splashSetting.adapterDidTimeOver();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

//    protected void handleClose(){
//        try {
//            if (splashSetting != null) {
//                if (isCountingEnd) {
//                    splashSetting.adapterDidTimeOver();
//                } else {
//                    splashSetting.adapterDidSkip();
//                }
//            }
//        } catch (Exception e) {
//
//        }
//    }
}
