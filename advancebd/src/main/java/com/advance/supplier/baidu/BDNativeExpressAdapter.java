package com.advance.supplier.baidu;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_SHOW;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.advance.NativeExpressSetting;
import com.advance.custom.AdvanceNativeExpressCustomAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.BaiduNativeManager;
import com.baidu.mobads.sdk.api.ExpressResponse;
import com.baidu.mobads.sdk.api.RequestParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板信息流 对应了百度的智能优选信息流广告位
 */
public class BDNativeExpressAdapter extends AdvanceNativeExpressCustomAdapter implements BaiduNativeManager.ExpressAdListener, ExpressResponse.ExpressInteractionListener {
    protected NativeExpressSetting setting;
    private BaiduNativeManager mBaiduNativeManager;
    private RequestParameters parameters;
    private List<ExpressResponse> ads;
    private String TAG = "[BDNativeExpressAdapter] ";
    ExpressResponse nativeResponse = null;

    public BDNativeExpressAdapter(Activity activity, NativeExpressSetting baseSetting) {
        super(activity, baseSetting);
        setting = baseSetting;

        parameters = AdvanceBDManager.getInstance().nativeExpressParameters;
    }

    @Override
    protected void paraLoadAd() {

        if (sdkSupplier != null) {
            BDUtil.initBDAccount(this);

            /**
             * Step 1. 创建BaiduNative对象，参数分别为： 上下文context，广告位ID
             * 注意：请将adPlaceId替换为自己的广告位ID
             * 注意：信息流广告对象，与广告位id一一对应，同一个对象可以多次发起请求
             */
            mBaiduNativeManager = new BaiduNativeManager(activity, sdkSupplier.adspotid);
            //设置广告的底价，单位：分（仅支持bidding模式，需通过运营单独加白）
            int bidFloor = AdvanceBDManager.getInstance().nativeExpressBidFloor;
            if (bidFloor > 0) {
                mBaiduNativeManager.setBidFloor(bidFloor);
            }
//            if (parameters ==null){
//                parameters = new RequestParameters
//            }
            mBaiduNativeManager.loadExpressAd(parameters, this);
        }

    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {

    }

    @Override
    public void orderLoadAd() {
        try {
            paraLoadAd();
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }


    @Override
    public void onNativeLoad(List<ExpressResponse> list) {
        LogUtil.simple(TAG + "onNativeLoad");
        try {
            ads = list;
            if (ads == null || ads.size() == 0) {
                handleFailed(AdvanceError.ERROR_DATA_NULL, "");
            } else {
                //赋值item信息
                nativeExpressAdItemList = new ArrayList<>();
                nativeResponse = ads.get(0);

                try { //避免方法有异常，catch一下，不影响success逻辑
                    if (nativeResponse != null) {
                        updateBidding(BDUtil.getEcpmValue(nativeResponse.getECPMLevel()));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                handleSucceed();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }

    @Override
    public void onNativeFail(int i, String s, ExpressResponse expressResponse) {
        handleFailed(i + "", s);
    }

    @Override
    public void onNoAd(int i, String s, ExpressResponse expressResponse) {
        handleFailed(i + "", s);
    }


    @Override
    public void onVideoDownloadSuccess() {
        LogUtil.simple(TAG + "onVideoDownloadSuccess");

    }

    @Override
    public void onVideoDownloadFailed() {
        LogUtil.simple(TAG + "onVideoDownloadFailed");

    }

    @Override
    public void onLpClosed() {
        LogUtil.simple(TAG + "onLpClosed");

    }


    public void onADClose() {
        LogUtil.simple(TAG + "onADClose");
        if (null != setting) {
            setting.adapterDidClosed(nativeExpressADView);
        }
    }


    @Override
    public boolean isValid() {
        if (nativeResponse != null) {
            return nativeResponse.isReady(getRealContext());
        }
        return super.isValid();
    }

    @Override
    public void show() {
        try {
            // 添加view
            View adView = nativeResponse.getExpressAdView();
            Log.i(TAG, "getExpressAdView styleType: " + nativeResponse.getStyleType());
            if (adView != null) {

                /**
                 * ===【 注意 】===
                 * 1. 展示前需要绑定当前activity，否则负反馈弹框无法弹出（负反馈无响应）
                 * 2. 如果你配置了{@link com.baidu.mobads.sdk.api.BDAdConfig.Builder#useActivityDialog(Boolean)}为 false
                 *    那么请务必在展现前调用该方法绑定activity，否则会使下载弹框无法弹出（下载类无响应）
                 */
                nativeResponse.bindInteractionActivity(getRealActivity(setting.getAdContainer()));
                addADView(adView);
            }

            nativeResponse.setInteractionListener(this);
            nativeResponse.setAdDislikeListener(new ExpressResponse.ExpressDislikeListener() {
                @Override
                public void onDislikeWindowShow() {
                    LogUtil.simple(TAG + "onDislikeWindowShow ");

                }

                @Override
                public void onDislikeItemClick(String s) {
                    try {
                        LogUtil.simple(TAG + "onDislikeItemClick: s = " + s);

                        onADClose();
                        // 点击了负反馈渠道的回调
                        removeADView();

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDislikeWindowClose() {
                    LogUtil.simple(TAG + "onDislikeWindowClose ");

                }
            });


            nativeResponse.render();
        } catch (Throwable e) {
            e.printStackTrace();
            runParaFailed(AdvanceError.parseErr(ERROR_EXCEPTION_SHOW));
        }
    }

    @Override
    public void onAdClick() {
        handleClick();

        String title = "";
        try {
            if (nativeResponse != null) {
                title = nativeResponse.getAdData().getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        LogUtil.simple(TAG + "onAdClick: title = " + title);
    }

    @Override
    public void onAdExposed() {
        handleShow();

        String title = "";
        try {
            if (nativeResponse != null) {
                title = nativeResponse.getAdData().getTitle();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        LogUtil.simple(TAG + "onADExposed: title = " + title);
    }

    @Override
    public void onAdRenderFail(View view, String s, int i) {
        String tip = "onAdRenderFail , inf : reason = " + s + ", code =" + i;
        LogUtil.simple(TAG + tip);

        handleFailed(i, tip);
    }

    @Override
    public void onAdRenderSuccess(View view, float width, float height) {
        LogUtil.simple(TAG + "onAdRenderSuccess: " + width + ", " + height);
    }

    @Override
    public void onAdUnionClick() {
        LogUtil.simple(TAG + "onADUnionClick");

        handleClick();
    }

}
