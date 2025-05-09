package com.advance.supplier.tap;//package com.advance.supplier.tap;
//
//import android.app.Activity;
//
//import com.advance.NativeExpressSetting;
//import com.advance.custom.AdvanceNativeExpressCustomAdapter;
//import com.bayes.sdk.basic.itf.BYBaseCallBack;
//import com.tapsdk.tapad.TapAdNative;
//import com.tapsdk.tapad.TapFeedAd;
//

//tap没有原生模板信息流，只有自渲染类型
//public class TapNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter {
//    TapAdNative tapAdNative;
//    TapFeedAd adData;
//
//    public TapNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
//        super(activity, baseSetting);
//    }
//
//    @Override
//    public void orderLoadAd() {
//        TapUtil.initAD(this, new BYBaseCallBack() {
//            @Override
//            public void call() {
//                loadAD();
//            }
//        });
//
//    }
//
//    @Override
//    protected void paraLoadAd() {
//        TapUtil.initAD(this, new BYBaseCallBack() {
//            @Override
//            public void call() {
//                loadAD();
//            }
//        });
//
//    }
//
//
//    @Override
//    protected void adReady() {
//
//    }
//
//    @Override
//    public void doDestroy() {
//        try {
//            if (adData != null) {
//                adData.dispose();
//            }
//            TapUtil.removeTapMap(getRealContext());
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public void show() {
//
//    }
//
//    private void loadAD(){
//
//    }
//}
