package com.advance.supplier.huawei;

import static com.huawei.hms.ads.NonPersonalizedAd.ALLOW_ALL;
import static com.huawei.hms.ads.NonPersonalizedAd.ALLOW_NON_PERSONALIZED;

import android.content.Context;

import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.RequestOptions;

import java.util.Map;

public class HWGlobalConfig extends AdvanceCustomInit {
    @Override
    public void initADN(Context context, Map<String, Object> serverExtra) {
        HwAds.init(context.getApplicationContext());
        callInitSuccess();
    }

    @Override
    public String getSDKVersion() {
        String ksV = "";
        try {
            ksV = HwAds.getSDKVersion();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return ksV;
    }

    @Override
    public void switchPersonalRecommend(boolean allow) {
        try {
            RequestOptions requestOptions = HwAds.getRequestOptions();
            int allowType = ALLOW_NON_PERSONALIZED;
            if (allow) {
                allowType = ALLOW_ALL;
            }
            requestOptions = requestOptions
                    .toBuilder()
                    .setNonPersonalizedAd(allowType)
                    .build();
            HwAds.setRequestOptions(requestOptions);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void switchDisableShake(boolean disableShake) {

    }


}

