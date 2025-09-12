package com.advance.supplier.huawei;

import android.content.Context;

import com.advance.BaseParallelAdapter;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.huawei.hms.ads.BiddingInfo;
import com.huawei.hms.ads.HwAds;

public class HWUtil {
    public static synchronized void initAD(BaseParallelAdapter adapter) {
        try {
            final String tag = "[HWUtil.initAD] ";
            String eMsg;
            if (adapter == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";
                LogUtil.e(eMsg);
                return;
            }

            SdkSupplier supplier = adapter.sdkSupplier;
            if (supplier == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";

                LogUtil.e(eMsg);
                return;
            }

            boolean hasInit = AdvanceHWManager.getInstance().hasInit;
            if (hasInit) {
                LogUtil.simple(tag + " already init");
                return;
            }
            Context context = adapter.getRealContext();

            HwAds.init(context.getApplicationContext());
        } catch (Exception e) {

        }

    }

    public static synchronized double getPrice(BiddingInfo biddingInfo) {
        double result = 0;
        try {
            if (biddingInfo != null) {
                //接口获取本条广告的eCPM出价（元/千次展示）；
                result = biddingInfo.getPrice() * 1000;
            }
        } catch (Exception e) {
        }
        return result;
    }
}
