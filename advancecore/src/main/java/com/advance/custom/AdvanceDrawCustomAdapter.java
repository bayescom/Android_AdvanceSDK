package com.advance.custom;

import android.view.View;
import android.view.ViewGroup;

import com.advance.model.AdvanceError;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;

public abstract class AdvanceDrawCustomAdapter extends AdvanceBaseCustomAdapter {
    String TAG = "[AdvanceDrawCustomAdapter] ";


    public boolean isADViewAdded(View adView) {
        boolean hasAdded = false;
        try {

            if (drawSetting != null) {
                ViewGroup adC = drawSetting.getContainer();
                hasAdded = AdvanceUtil.addADView(adC, adView);
            } else {
                LogUtil.e(TAG + "无法展示广告，原因：内部处理异常，AdvanceDrawSetting为空");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (!hasAdded) {
            handleFailed(AdvanceError.ERROR_ADD_VIEW, "添加广告视图操作失败");
        } else {
            LogUtil.high(TAG + "ADView Added succ");
        }
        return hasAdded;
    }

}
