//package com.advance.supplier.sigmob;
//
//import android.app.Activity;
//
//import com.advance.NativeExpressSetting;
//import com.advance.custom.AdvanceNativeExpressCustomAdapter;
//import com.advance.itf.AdvanceADNInitResult;
//
//public class SigmobNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
//
//    public SigmobNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
//        super(activity, baseSetting);
//    }
//
//    @Override
//    public void orderLoadAd() {
//        paraLoadAd();
//    }
//
//    @Override
//    protected void paraLoadAd() {
//        SigmobUtil.initAD(this, new AdvanceADNInitResult() {
//            @Override
//            public void success() {
//                //只有在成功初始化以后才能调用load方法
//                startLoad();
//            }
//
//            @Override
//            public void fail(String code, String msg) {
//                handleFailed(code, msg);
//            }
//        });
//    }
//
//    @Override
//    protected void adReady() {
//
//    }
//
//    @Override
//    public void doDestroy() {
//
//    }
//
//    private void startLoad() {
//
//    }
//
//    @Override
//    public void show() {
//
//    }
//}
