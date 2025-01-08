package com.advance;

import android.app.Activity;

import com.advance.core.common.AdvanceErrListener;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

@Deprecated
public class AdvanceCustomizeAd extends AdvanceBaseAdspot implements BaseAdEventListener {
    private AdvanceCustomizeSupplierListener customizeSupplierListener;
    ArrayList<SdkSupplier> suppliers;

    public AdvanceCustomizeAd(Activity activity, String adspotId) {
        super(activity, "", adspotId);
    }

    public AdvanceCustomizeAd(SoftReference<Activity> activity, String adspotId) {
        super(activity, "", adspotId);
    }

    public void setSupplierListener(AdvanceCustomizeSupplierListener listener) {
        this.customizeSupplierListener = listener;
    }

    @Override
    public void initAdapterData(SdkSupplier sdkSupplier, String clzName) {

    }

    @Override
    public void initSdkSupplier() {
        initSupplierAdapterList();


    }

    @Override
    public void onAdvanceError(AdvanceErrListener listener, final AdvanceError advanceError) {
//        super.onAdvanceError(listener, advanceError);
        onFailed(advanceError);
    }


    @Override
    public void selectSdkSupplier() {
        if (supplierGroups == null || supplierGroups.size() == 0) {
            noneSDK();
            return;
        }
        currentGroupInf = supplierGroups.get(0);

        if (currentGroupInf == null || currentGroupInf.paraSupplierMembers == null || currentGroupInf.paraSupplierMembers.isEmpty()) {
            noneSDK();
            return;
        }
        try {
            suppliers = currentGroupInf.paraSupplierMembers;
            currentSdkSupplier = suppliers.get(0);
            suppliers.remove(0);
            reportAdvanceLoaded();
            LogUtil.simple("selected sdk id:" + currentSdkSupplier.id);
            if (null != customizeSupplierListener) {
                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                    @Override
                    public void call() {
                        customizeSupplierListener.onSupplierSelected(currentSdkSupplier);
                    }
                });
            }
        } catch (Throwable e) {
            e.printStackTrace();
            LogUtil.high("当前策略加载异常");
            adapterDidFailed(AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT));
        }
    }

    private void noneSDK() {
        LogUtil.high("没有可执行的策略了");
        LogUtil.simple("None SDK: sdk suppliers is empty, callback failed");
        if (null != customizeSupplierListener) {
            if (advanceError == null) {
                advanceError = AdvanceError.parseErr(AdvanceError.ERROR_NONE_SDK);
            }
            onFailed(advanceError);
        }
    }

    private void onFailed(final AdvanceError err) {
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                if (null != customizeSupplierListener) {
                    customizeSupplierListener.onSupplierFailed(err);
                }
            }
        });
    }

    @Override
    public void selectSdkSupplierFailed() {
        LogUtil.high("未返回有效策略");
        onFailed(AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT_FAILED));

    }


    @Override
    public void adapterDidSucceed(SdkSupplier supplier) {
        reportAdSucceed(supplier);
    }

    public void adapterDidSucceed() {
        adapterDidSucceed(currentSdkSupplier);
    }

    @Override
    public void adapterDidShow(SdkSupplier supplier) {
        reportAdShow(supplier);
    }

    public void adapterDidShow() {
        adapterDidShow(currentSdkSupplier);
    }

    @Override
    public void adapterDidClicked(SdkSupplier supplier) {
        reportAdClicked(supplier);
    }

    public void adapterDidClicked() {
        adapterDidClicked(currentSdkSupplier);
    }

}
