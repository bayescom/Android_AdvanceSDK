package com.advance.supplier.sigmob;

import android.content.Context;

import com.advance.core.srender.AdvanceRFBridge;
import com.advance.custom.AdvanceSelfRenderCustomAdapter;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceError;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.natives.WindNativeAdData;
import com.sigmob.windad.natives.WindNativeAdRequest;
import com.sigmob.windad.natives.WindNativeUnifiedAd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SigmobRenderFeedAdapter extends AdvanceSelfRenderCustomAdapter {
    WindNativeUnifiedAd windNativeUnifiedAd;
    WindNativeAdData mRenderAD;

    public SigmobRenderFeedAdapter(Context context, AdvanceRFBridge mAdvanceRFBridge) {
        super(context, mAdvanceRFBridge);
    }

    @Override
    public void orderLoadAd() {
        paraLoadAd();
    }

    @Override
    protected void paraLoadAd() {
        SigmobUtil.initAD(this, new AdvanceADNInitResult() {
            @Override
            public void success() {
                //只有在成功初始化以后才能调用load方法
                startLoad();
            }

            @Override
            public void fail(String code, String msg) {
                handleFailed(code, msg);
            }
        });
    }

    @Override
    protected void adReady() {

    }

    @Override
    public void doDestroy() {

    }

    private void startLoad() {
        try {
            String userId = SigmobSetting.getInstance().userId;
            Map<String, Object> options = new HashMap<>();
            options.put("user_id", userId);

            WindNativeAdRequest nativeAdRequest = new WindNativeAdRequest(sdkSupplier.adspotid, userId, options);

            windNativeUnifiedAd = new WindNativeUnifiedAd(nativeAdRequest);
            windNativeUnifiedAd.setNativeAdLoadListener(new WindNativeUnifiedAd.WindNativeAdLoadListener() {
                @Override
                public void onAdError(WindAdError error, String placementId) {
                    SigmobUtil.handlerErr(SigmobRenderFeedAdapter.this, error, "");

                }

                @Override
                public void onAdLoad(List<WindNativeAdData> list, String placementId) {
                    if (list == null || list.size() == 0) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "ads empty");
                        return;
                    }
                    mRenderAD = list.get(0);
                    if (mRenderAD == null) {
                        handleFailed(AdvanceError.ERROR_DATA_NULL, "mRenderAD null");
                        return;
                    }

                    if (windNativeUnifiedAd != null)
                        //更新ecpm价格信息
                        updateBidding(SigmobUtil.getEcpmNumber(windNativeUnifiedAd.getEcpm()));

                    //转换返回广告model为聚合通用model
                    dataConverter = new SigmobRenderDataConverter(mRenderAD, sdkSupplier);

                    //标记广告成功
                    handleSucceed();
                }
            });
            windNativeUnifiedAd.loadAd(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void show() {

    }
}
