package com.advance.net;

import com.advance.AdvanceConfig;
import com.advance.AdvanceSetting;
import com.advance.freqcap.AdvanceFreqCapManager;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDevice;
import com.bayes.sdk.basic.net.BYNetRequest;
import com.bayes.sdk.basic.net.BYReqCallBack;
import com.bayes.sdk.basic.net.BYReqModel;
import com.bayes.sdk.basic.util.BYCacheUtil;
import com.bayes.sdk.basic.util.BYCatch;
import com.bayes.sdk.basic.util.BYStringUtil;

import org.json.JSONObject;

/**
 * 聚合 SDK 配置拉取（独立接口，与策略分发 cruiser 解耦）。
 *
 * 接口：POST /advance_sdk_config，请求体 { appid, appver, sdkver }，
 * 响应体 { "adspot_configs": { adspot_id: {...} }, "tk_retry_config": {...} }。
 *
 * 分发：
 * - adspot_configs → 频次控制（{@link AdvanceFreqCapManager#updateConfig}），字段 adspot_id / req_limit / imp_limit / click_limit / req_interval
 * - tk_retry_config → 上报补发（{@link TrackRetryManager#updateConfig}），文档未列出，遵循 mercury 节点格式
 *   （enable / base_ms / cap_ms / coalesce_ms / batch_size / max_retry / expire_ms / max_queue / timeout_ms / grace_ms）
 *
 * 流程：SDK 初始化时先同步恢复磁盘缓存（首屏不漏控），再异步拉取最新配置。
 * 未下发配置时两套能力均保持默认关闭/放行，既有行为零变化。
 */
public class AdvanceSdkConfigManager {

    private static final String TAG = "[AdvanceSdkConfig] ";

    private static final String CACHE_KEY = "advance_sdk_config_cache";
    private static final String URL_HTTP = "http://cruiser.bayescom.cn/advance_sdk_config";
    private static final String URL_HTTPS = "https://cruiser.bayescom.cn/advance_sdk_config";

    /** SDK init 时调用：先同步恢复磁盘缓存，再异步拉取最新配置。 */
    public static void init() {
        try {
            String cached = null;
            if (BYCacheUtil.byCache() != null) {
                cached = BYCacheUtil.byCache().getAsString(CACHE_KEY);
            }
            // 冷启动：先让磁盘缓存里的配置立即生效（首屏不漏控/可补发）
            dispatchConfig(cached);
            // 再异步拉取最新配置
            fetchAsync();
        } catch (Throwable t) {
            BYCatch.onCaught(t, true, BYCatch.LEVEL_WARN);
        }
    }

    private static void fetchAsync() {
        try {
            final BYReqModel req = new BYReqModel();
            req.reqUrl = AdvanceSetting.getInstance().useHttps ? URL_HTTPS : URL_HTTP;
            req.timeoutMs = 10000;
            req.reqBody = buildBody();
            BYNetRequest.post(req, new BYReqCallBack() {
                @Override
                public void onSuccess(String s) {
                    try {
                        if (BYStringUtil.isEmpty(s)) {
                            LogUtil.devDebug(TAG + "配置拉取返回为空，沿用磁盘缓存");
                            return;
                        }
                        if (BYCacheUtil.byCache() != null) {
                            BYCacheUtil.byCache().put(CACHE_KEY, s);   // 落盘
                        }
                        dispatchConfig(s);                              // 分发
                    } catch (Throwable t) {
                        BYCatch.onCaught(t, true, BYCatch.LEVEL_WARN);
                    }
                }

                @Override
                public void onFailed(int i, String s) {
                    LogUtil.devDebug(TAG + "配置拉取失败 code=" + i + "，沿用磁盘缓存");
                }
            });
        } catch (Throwable t) {
            BYCatch.onCaught(t, true, BYCatch.LEVEL_WARN);
        }
    }

    private static JSONObject buildBody() {
        try {
            JSONObject body = new JSONObject();
            body.putOpt("appid", AdvanceConfig.getInstance().getMercuryMediaId());
            body.putOpt("appver", BYDevice.getAppVersionValue());
            body.putOpt("sdkver", AdvanceConfig.AdvanceSdkVersion);
            return body;
        } catch (Throwable t) {
            BYCatch.onCaught(t, false, BYCatch.LEVEL_WARN);
            return new JSONObject();
        }
    }

    /** 只在配置更新时发生一次，不在热路径。失败一律忽略，绝不影响广告链路。 */
    private static void dispatchConfig(String json) {
        try {
            if (BYStringUtil.isEmpty(json)) {
                return;
            }
            JSONObject root = new JSONObject(json);
            // 频控配置语义：以服务端最新下发为准，整体替换。
            // 节点缺失（null）或为空对象均视为「清空频控」，基础库 updateConfig(null/空) 会整体置空 configs。
            // 注意：这与 tk_retry_config 的「缺失即沿用」不同，属于刻意设计——
            //       若服务端因异常漏发该节点，会导致线上频控被清空，需服务端保证节点必返。
            JSONObject adspotConfigs = root.optJSONObject("adspot_configs");
            AdvanceFreqCapManager.updateConfig(adspotConfigs);
            JSONObject tkRetryConfig = root.optJSONObject("tk_retry_config");
            if (tkRetryConfig != null) {
                TrackRetryManager.updateConfig(tkRetryConfig);
            }
        } catch (Throwable t) {
            BYCatch.onCaught(t, true, BYCatch.LEVEL_WARN);
        }
    }
}
