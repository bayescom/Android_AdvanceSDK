package com.advance.supplier.baidu;//package com.advance.supplier.baidu;
//
//import android.app.Activity;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.advance.AdvanceConfig;
//import com.advance.AdvanceNativeExpressAdItem;
//import com.advance.itf.BaseEnsureListener;
//import com.advance.model.AdvanceError;
//import com.advance.utils.AdvanceUtil;
//import com.advance.utils.LogUtil;
//import com.baidu.mobads.sdk.api.ExpressResponse;
//import com.baidu.mobads.sdk.api.FeedNativeView;
//import com.baidu.mobads.sdk.api.NativeResponse;
//import com.baidu.mobads.sdk.api.StyleParams;
//import com.baidu.mobads.sdk.api.XAdNativeResponse;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Deprecated
//public class BDNativeExpressItem implements AdvanceNativeExpressAdItem {
//
//    private Activity activity;
//    BDNativeExpressAdapter adapter;
//    ExpressResponse nativeResponse;
//    FeedNativeView feedNativeView;
//    StyleParams styleParams;
//
//    String TAG = "[BDNativeExpressItem] ";
//
//    public BDNativeExpressItem(Activity activity, BDNativeExpressAdapter adapter, ExpressResponse nativeResponse) {
//        this.adapter = adapter;
//        this.activity = activity;
//        this.nativeResponse = nativeResponse;
//
//        try {
//            styleParams = AdvanceBDManager.getInstance().nativeExpressSmartStyle;
//            feedNativeView = new FeedNativeView(activity);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public String getSdkTag() {
//        return BDUtil.BD_TAG;
//    }
//
//    @Override
//    public String getSdkId() {
//        return AdvanceConfig.SDK_ID_BAIDU;
//    }
//
//    @Override
//    public void destroy() {
//    }
//
//    @Override
//    public void render() {
//        BYThreadUtil.switchMainThread(new BaseEnsureListener() {
//            @Override
//            public void ensure() {
//                doRender();
//
//            }
//        });
//    }
//
//    private void doRender() {
//        try {
//
//            if (feedNativeView != null) {
//                feedNativeView.setAdData((XAdNativeResponse) nativeResponse);
//                if (styleParams != null) {
//                    feedNativeView.changeViewLayoutParams(styleParams);
//                }
//                feedNativeView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        nativeResponse.unionLogoClick();
//                    }
//                });
//                if (nativeResponse != null) {
//                    LogUtil.max("getImageUrl = " + nativeResponse.getImageUrl());
//                    XAdNativeResponse xad = (XAdNativeResponse) nativeResponse;
//                    xad.setAdDislikeListener(new NativeResponse.AdDislikeListener() {
//                        @Override
//                        public void onDislikeClick() {
//                            try {
//                                if (adapter != null) {
//                                    adapter.onADClose();
//                                    adapter.removeADView();
//                                }
//                                // 点击了负反馈渠道的回调
//
//                            } catch (Throwable e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//
//                    /**
//                     * registerViewForInteraction()与BaiduNativeManager配套使用
//                     * 警告：调用该函数来发送展现，勿漏！
//                     */
//                    List<View> clickViews = new ArrayList<>();
//                    List<View> creativeViews = new ArrayList<>();
//                    nativeResponse.registerViewForInteraction(adapter.setting.getAdContainer(), clickViews, creativeViews, null);
//
//                    nativeResponse.setAdPrivacyListener(new NativeResponse.AdPrivacyListener() {
//                        @Override
//                        public void onADPermissionShow() {
//                            LogUtil.simple(TAG + "onADPermissionShow");
//
//                        }
//
//                        @Override
//                        public void onADPermissionClose() {
//                            LogUtil.simple(TAG + "onADPermissionClose");
//                        }
//
//                        @Override
//                        public void onADPrivacyClick() {
//                            LogUtil.simple(TAG + "onADPrivacyClick");
//                        }
//                    });
//
//                }
//
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//            try {
//                if (adapter != null) {
//                    adapter.runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_RENDER));
//                }
//            } catch (Throwable ex) {
//                ex.printStackTrace();
//            }
//        }
//    }
//
//    @Override
//    public View getExpressAdView() {
//        return feedNativeView;
//    }
//
//
//}
