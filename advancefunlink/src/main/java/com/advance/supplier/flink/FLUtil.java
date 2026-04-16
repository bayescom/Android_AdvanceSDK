package com.advance.supplier.flink;

import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.utils.LogUtil;
import com.fl.saas.adx.api.FLConfig;
import com.fl.saas.adx.api.FLParamConfig;
import com.fl.saas.adx.base.exception.FLError;

public class FLUtil {
    public static void initAD(BaseParallelAdapter adapter) {
        try {
            final String tag = "[FLUtil.initAD] ";
            String eMsg;
            if (adapter == null) {
                eMsg = tag + "initAD failed BaseParallelAdapter null";
                LogUtil.e(eMsg);
                //            if (initResult != null) {
                //                initResult.fail(AdvanceError.ERROR_INIT_DEFAULT + "", eMsg);
                //            }
                return;
            }
            boolean hasInit = AdvanceFLManager.getInstance().hasInit;
            if (hasInit) {
                LogUtil.simple(tag + " already init");
                //            if (initResult != null) {
                //                initResult.success();
                //            }
                return;
            }
            FLParamConfig.Builder builder = new FLParamConfig.Builder();
            boolean disableShake = AdvanceSetting.getInstance().disableShake;
            builder.setCanUseSensor(!disableShake);

            FLParamConfig config = builder.build();

            FLConfig.getInstance().init(adapter.getRealContext(), adapter.getAppID(), config);
        } catch (Exception e) {

        }
    }

    public static void handleErr(BaseParallelAdapter adapter, FLError error){
        try {
            if (adapter!=null && error!=null){
                adapter.handleFailed(error.getCode(),error.getMsg());
            }
        } catch (Exception e) {

        }
    }
}
