package com.advance.supplier.baidu;

import android.app.Activity;

import com.advance.AdvanceConfig;
import com.advance.AdvanceFullScreenItem;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.baidu.mobads.sdk.api.FullScreenVideoAd;
import com.bayes.sdk.basic.util.BYThreadUtil;

@Deprecated
public class BDFullScreenVideoItem implements AdvanceFullScreenItem {
    Activity activity;
    BDFullScreenVideoAdapter bdFullScreenVideoAdapter;
    FullScreenVideoAd mFullScreenVideoAd;
    String TAG = "[BDFullScreenVideoItem] ";

    public BDFullScreenVideoItem(Activity activity, BDFullScreenVideoAdapter bdFullScreenVideoAdapter, FullScreenVideoAd mFullScreenVideoAd) {
        this.activity = activity;
        this.bdFullScreenVideoAdapter = bdFullScreenVideoAdapter;
        this.mFullScreenVideoAd = mFullScreenVideoAd;
    }

    @Override
    public String getSdkTag() {
        return BDUtil.BD_TAG;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_BAIDU;
    }

    @Override
    public void showAd() {
        try {
            boolean isReady = mFullScreenVideoAd != null && mFullScreenVideoAd.isReady();
            LogUtil.simple(TAG + " isReady = " + isReady);
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (mFullScreenVideoAd != null) {
                        mFullScreenVideoAd.show();
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
