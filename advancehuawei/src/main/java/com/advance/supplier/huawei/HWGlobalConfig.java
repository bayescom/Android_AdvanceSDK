package com.advance.supplier.huawei;

import static com.huawei.hms.ads.NonPersonalizedAd.ALLOW_ALL;
import static com.huawei.hms.ads.NonPersonalizedAd.ALLOW_NON_PERSONALIZED;

import com.advance.itf.AdvancePrivacyController;
import com.advance.itf.AdvanceSupplierBridge;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.RequestOptions;

public class HWGlobalConfig implements AdvanceSupplierBridge {
    @Override
    public void setCustomPrivacy(AdvancePrivacyController advancePrivacyController) {

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
    public void setPersonalRecommend(boolean allow) {
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


}

