package com.advance.supplier.tanx;

import android.app.Application;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.alimm.tanx.core.TanxInitListener;
import com.alimm.tanx.core.config.TanxConfig;
import com.alimm.tanx.core.image.ILoader;
import com.alimm.tanx.ui.TanxSdk;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.util.BYUtil;

public class TanxUtil {
    public synchronized static void initTanx(final BaseParallelAdapter adapter, final InitListener listener) {

        try {
            if (adapter == null) {
                String msg = "[TanxUtil.initTanx] initAD failed BaseParallelAdapter null";
                if (listener != null) {
                    listener.fail(AdvanceError.ERROR_INIT_DEFAULT, msg);
                }
                return;
            }
            final String appid = adapter.getAppID();
            boolean hasInit = AdvanceSetting.getInstance().hasTANXInit;
            boolean isSame = AdvanceSetting.getInstance().lastTANXAID.equals(appid);
            if (hasInit && isSame) {
                LogUtil.simple("[TanxUtil.initTanx] tanx already init");
                if (listener != null) {
                    listener.success();
                }
                return;
            }

//            AdvanceUtil advanceUtil = new AdvanceUtil(adapter.getADActivity());
            String oaid = BYDevice.getOaidValue();
            String imei = BYDevice.getImeiValue();
//设置图片加载自定义loader

            TanxConfig config = new TanxConfig.Builder()
                    .appName(AdvanceConfig.getInstance().getAppName())
                    .appId(appid)
                    .appKey(adapter.sdkSupplier.mediakey)
//                    .appSecret(adapter.sdkSupplier.mediaSecret)
                    .oaid(oaid)
                    //是不是开启自动获取oaid开关
                    .oaidSwitch(true)
                    .imei(imei)
//                    .imageLoader(iLoader)
                    .debug(BYUtil.isDebug())
                    //                .dark(new SettingConfig().setNightConfig())
                    .build();

            ILoader iLoader = AdvanceTanxSetting.getInstance().iLoader;
            if (iLoader != null) {
                config.setImageLoader(iLoader);
            }
            Application application = (Application) adapter.getRealContext().getApplicationContext();
            LogUtil.simple("[TanxUtil.initTanx] tanx init start");

            TanxSdk.init(application, config, new TanxInitListener() {
                @Override
                public void succ() {
                    LogUtil.high("[TanxUtil.initTanx] TanxInitListener succ");
                    AdvanceSetting.getInstance().hasTANXInit = true;
                    AdvanceSetting.getInstance().lastTANXAID = appid;
                    if (listener != null) {
                        listener.success();
                    }
                }


                @Override
                public void error(int code, String msg) {
                    LogUtil.e("[TanxUtil.initTanx] TanxInitListener err code = " + code + " ,msg = " + msg);
                    if (listener != null) {
                        listener.fail(AdvanceError.ERROR_INIT_DEFAULT, msg);
                    }
                }
            });
            LogUtil.simple("[TanxUtil.initTanx] tanx init end");
        } catch (Throwable e) {
            e.printStackTrace();
            if (listener != null) {
                listener.fail(AdvanceError.ERROR_INIT_DEFAULT, "tanx 初始化执行异常");
            }
        }

    }

    public interface InitListener {
        void success();

        void fail(int code, String msg);
    }
}
