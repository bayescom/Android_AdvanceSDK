package com.advance.supplier.baidu;//package com.advance.supplier.baidu;
//
//import android.app.Activity;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//
//import com.advance.BannerSetting;
//import com.advance.BaseParallelAdapter;
//import com.advance.custom.AdvanceBannerCustomAdapter;
//import com.advance.model.AdvanceError;
//import com.advance.utils.AdvanceUtil;
//import com.advance.utils.LogUtil;
//import com.baidu.mobads.sdk.api.AdView;
//import com.baidu.mobads.sdk.api.AdViewListener;
//
//import org.json.JSONObject;
//
///**
// * 横幅会自动刷新，刷新间隔30秒。如果网络异常，下一次加载将回调加载失败，网络恢复，不再执行刷新。故失败逻辑直接进入下一优先级的加载
// */
//public class BDBannerAdapter extends AdvanceBannerCustomAdapter implements AdViewListener {
//
//    private BannerSetting setting;
//    private AdView adView;
//    private String TAG = "[BDBannerAdapter] ";
//
//    public BDBannerAdapter(Activity activity, BannerSetting baseSetting) {
//        super(activity, baseSetting);
//        setting = baseSetting;
//        supportPara = false;
//    }
//
//    @Override
//    protected void paraLoadAd() {
//
//        if (sdkSupplier != null) {
//            BDUtil.initBDAccount(this);
//            adView = new AdView(activity, sdkSupplier.adspotid);
//            adView.setListener(this);
//        }
//
////     todo  23-05-24 验证是否可以分离show广告，当前版本还不支持分离  （移除此处调用，改为show时调用，且supportPara = true，这样banner加载&展示流程不再是同步的了，可以实现load、show分离，执行bidding逻辑）
//
////        addADView(new ViewGroup.LayoutParams(1, 1));
//        addADView(null);
//    }
//
//    private void addADView(ViewGroup.LayoutParams params) {
//        //必须要添加布局后，才会返回广告
//        if (null != setting) {
//            ViewGroup adContainer = setting.getContainer();
//            if (adContainer != null) {
////                adContainer.removeAllViews();
//                int width = adContainer.getWidth();
//                LogUtil.max(TAG + "adContainer width = " + width);
//                if (width <= 0) {
//                    width = ViewGroup.LayoutParams.MATCH_PARENT;
//                }
//                boolean add = AdvanceUtil.addADView(adContainer, adView, params);
//                if (!add) {
//                    doBannerFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
//                }
////                adContainer.addView(adView, new RelativeLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
//            }
//        }
//    }
//
//    @Override
//    protected void adReady() {
//    }
//
//    @Override
//    public void doDestroy() {
//        try {
//            if (adView != null) {
//                adView.destroy();
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void orderLoadAd() {
//        try {
//            paraLoadAd();
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
//
//        }
//    }
//
//    @Override
//    public void onAdReady(AdView adView) {
//        LogUtil.simple(TAG + "onAdReady" + adView);
////        if (setting!=null){
////            setting.getContainer().removeAllViews();
////        }
//        handleSucceed();
//    }
//
//    @Override
//    public void onAdShow(JSONObject jsonObject) {
//        LogUtil.simple(TAG + "onAdShow " + jsonObject);
//
//        handleShow();
//    }
//
//    @Override
//    public void onAdClick(JSONObject jsonObject) {
//        LogUtil.simple(TAG + "onAdClick " + jsonObject);
//
//        handleClick();
//    }
//
//    @Override
//    public void onAdFailed(String s) {
//        LogUtil.e(TAG + "onAdFailed " + s);
//
//        handleFailed(AdvanceError.ERROR_BD_FAILED, s);
//    }
//
//    @Override
//    public void onAdSwitch() {
//        LogUtil.simple(TAG + "onAdSwitch");
//
//    }
//
//    @Override
//    public void onAdClose(JSONObject jsonObject) {
//        LogUtil.simple(TAG + "onAdClose " + jsonObject);
//
//        if (null != setting) {
//            setting.adapterDidDislike();
//        }
//    }
//
//    @Override
//    public void show() {
////        addADView(null);
//    }
//}
