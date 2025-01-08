package com.advance.supplier.ks;

import android.app.Activity;

import com.advance.AdvanceConfig;
import com.advance.AdvanceRewardVideoItem;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.kwad.sdk.api.KsRewardVideoAd;
@Deprecated
public class KSRewardItem implements AdvanceRewardVideoItem {
    KsRewardVideoAd ad;
    Activity activity;
    KSRewardAdapter adapter;

    public KSRewardItem(Activity activity, KSRewardAdapter adapter, KsRewardVideoAd ad) {
        this.ad = ad;
        this.adapter = adapter;
        this.activity = activity;
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
    public void showAd() {
        try {
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    doShow();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            try {
                if (adapter != null)
                    adapter.runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW));
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doShow() {
        if (ad != null) {
            ad.showRewardVideoAd(adapter.setting.getShowActivity(), AdvanceKSManager.getInstance().rewardVideoConfig);
        }
    }
}
