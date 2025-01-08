package com.advance.supplier.ks;

import android.app.Activity;
import android.view.View;

import com.advance.AdvanceConfig;
import com.advance.AdvanceNativeExpressAdItem;
import com.advance.utils.LogUtil;
import com.kwad.sdk.api.KsFeedAd;
@Deprecated
public class KSNativeExpressItem implements AdvanceNativeExpressAdItem {

    Activity activity;
    KSNativeExpressAdapter adapter;
    public KsFeedAd ad;

    private String TAG = "[KSNativeExpressItem] ";

    public KSNativeExpressItem(final Activity activity, final KSNativeExpressAdapter adapter, final KsFeedAd ad) {
        this.activity = activity;
        this.adapter = adapter;
        this.ad = ad;


    }


    @Override
    public String getSdkTag() {
        return AdvanceConfig.SDK_TAG_KS;
    }

    @Override
    public String getSdkId() {
        return AdvanceConfig.SDK_ID_KS;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void render() {
    }

    @Override
    public View getExpressAdView() {
        try {
            if (ad != null) {
                return ad.getFeedView(activity);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
