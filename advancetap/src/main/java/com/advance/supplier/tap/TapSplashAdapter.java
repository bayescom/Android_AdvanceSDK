package com.advance.supplier.tap;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.advance.SplashSetting;
import com.advance.custom.AdvanceSplashCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapSplashAd;

import java.lang.ref.SoftReference;

public class TapSplashAdapter extends AdvanceSplashCustomAdapter {
    TapAdNative tapAdNative;
    TapSplashAd adData;

    public TapSplashAdapter(SoftReference<Activity> activity, SplashSetting advanceSplash) {
        super(activity, advanceSplash);
    }

    @Override
    public void orderLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });
    }

    @Override
    protected void paraLoadAd() {
        TapUtil.initAD(this, new BYBaseCallBack() {
            @Override
            public void call() {
                loadAD();
            }
        });
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {
        try {
            if (adData != null) {
                adData.dispose();
                adData.destroyView();
            }
            TapUtil.removeTapMap(getRealContext());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {
        try {
            if (adData == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "splashAd null"));
                return;
            }


            Activity activity = getRealActivity(splashSetting.getAdContainer());
            //获取SplashView
            View view = adData.getSplashView(activity);
            //渲染之前判断activity生命周期状态
            boolean isDestroy = AdvanceUtil.isActivityDestroyed(activity);
            if (isDestroy) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "ActivityDestroyed"));
                return;
            }
            //                adContainer.removeAllViews();
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕宽

            boolean add = AdvanceUtil.addADView(splashSetting.getAdContainer(), view);
            if (!add) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_ADD_VIEW));
            }


            TextView skipView = splashSetting.getSkipView();
            if (null != skipView) {
                skipView.setVisibility(View.INVISIBLE);
            }
//            adData.show(activity);
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
        }
    }


    private void loadAD() {
        try {

            //不支持使用applicationContext属性，必须为activity实例，否则报错
            //tapAdNative = TapAdManager.get().createAdNative(getRealContext());
            tapAdNative = TapUtil.getTapADManger(getRealContext());


            // TODO: 2023/10/27   withExpressViewAcceptedSize 设置影响展示效果。注意值为px像素值
            //  需要测试各种情况下的表现
            //  case1：填入了真实的广告承载父布局宽高，未设置logo信息 (默认场景：正常)
            //  case2：填入了真实的广告承载父布局宽高，设置了logo信息 (默认场景：正常)
            //  case3：填入了真实的广告承载父布局宽高-logo高度，设置了logo信息（正常非分离场景，不会出现，分离场景表现：默认场景：）
            //  case4: 填入了用户设置的宽高信息setCsjAcceptedSize（正常非分离场景，不会出现，分离场景表现：默认场景：）
            //  case5：用户分离模式调用开屏，且未在load时传入container信息，重复 case 1-4 测试

            int viewWidth = splashSetting.getCsjAcceptedSizeWidth();
            int viewHeight = splashSetting.getCsjAcceptedSizeHeight();
            LogUtil.high(TAG + " adview Accepted size :w = " + viewWidth + ", h = " + viewHeight);

            int spaceId = TapUtil.getPlaceId(getPosID());
            AdRequest request = new AdRequest.Builder().withSpaceId(spaceId)
                    .withUserId(AdvanceTapManger.getInstance().customTapUserId)
                    .withExpressViewAcceptedSize(viewWidth, viewHeight)
                    .build();

            LogUtil.d(TAG + " loadSplashAd start");
            tapAdNative.loadSplashAd(request, new TapAdNative.SplashAdListener() {
                @Override
                public void onSplashAdLoad(TapSplashAd tapSplashAd) {

                    try {
                        if (tapSplashAd == null) {
                            String nMsg = TAG + " tapSplashAd null";
                            handleFailed(AdvanceError.ERROR_DATA_NULL, nMsg);
                            return;
                        }
                        adData = tapSplashAd;

                        updateBidding(TapUtil.getBiddingPrice(tapSplashAd.getMediaExtraInfo()));



                        adData.setSplashInteractionListener(new TapSplashAd.AdInteractionListener() {
                            @Override
                            public void onAdSkip() {
                                LogUtil.simple(TAG + "onAdSkip");
                                if (splashSetting != null) {
                                    splashSetting.adapterDidSkip();
                                }

                                destroy();
                            }

                            @Override
                            public void onAdTimeOver() {
                                LogUtil.simple(TAG + "onAdTimeOver");

                                if (splashSetting != null) {
                                    splashSetting.adapterDidTimeOver();
                                }
                                destroy();
                            }

                            @Override
                            public void onAdClick() {
                                LogUtil.simple(TAG + "onAdClick");

                                handleClick();
                            }

                            @Override
                            public void onAdShow() {
                                LogUtil.simple(TAG + "onAdShow");

                                handleShow();

                            }

                            @Override
                            public void onAdValidShow() {
                                LogUtil.simple(TAG + "onAdValidShow");

                            }
                        });

                        handleSucceed();
                    } catch (Throwable e) {
                        e.printStackTrace();
                        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
                    }
                }

                @Override
                public void onError(int code, String message) {
                    LogUtil.e(TAG + " onError ");

                    handleFailed(code, message);
                }
            });

        } catch (Throwable e) {
            try {
                e.printStackTrace();
                runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD));
                String cause = e.getCause() != null ? e.getCause().toString() : "no cause";
                reportCodeErr(TAG + " Throwable" + cause);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}
