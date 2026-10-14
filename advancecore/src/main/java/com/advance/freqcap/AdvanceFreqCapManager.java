package com.advance.freqcap;

import com.bayes.sdk.basic.freqcap.BYFreqCap;
import com.bayes.sdk.basic.freqcap.BYFreqCapResult;

import org.json.JSONObject;

/**
 * advance 侧频次控制入口（薄持有层）。
 *
 * 持有基础库 BYFreqCap 的独立实例，namespace 固定为 "advance"。
 * 这样与 mercury 等其它 SDK 各自持有不同 namespace 的 BYFreqCap 实例，
 * 同进程内共享 ClassLoader 时也能做到计数物理隔离，互不串数据。
 *
 * 所有方法签名与基础库 BYFreqCap 一致，接入点（AdvanceBaseAdspot 的请求/曝光/点击）仅需调用本类，逻辑零改动。
 *
 * 配置由 {@code AdvanceSdkConfigManager} 在 SDK 初始化与配置刷新时通过 {@link #updateConfig(JSONObject)} 一次性下发，
 * 未下发时所有方法等价于"放行"，既有广告链路行为完全不变。
 */
public final class AdvanceFreqCapManager {

    private static final BYFreqCap INSTANCE = new BYFreqCap("advance");

    private AdvanceFreqCapManager() {
        // 工具类，禁止实例化
    }

    /** 一次性下发频控配置（sdk_config 中的 adspot_configs 节点，基础库自解析为 Map<adspot_id, BYFreqCapConfig>）。 */
    public static void updateConfig(JSONObject adspotConfigs) {
        INSTANCE.updateConfig(adspotConfigs);
    }

    /** 请求前置判定。覆盖 req_limit / req_interval / imp_limit / click_limit 四类，返回 block=false 表示允许发起请求。 */
    public static BYFreqCapResult checkRequest(String freqId) {
        return INSTANCE.checkRequest(freqId);
    }

    /** 请求已真正发出后提交计数。 */
    public static void consumeRequest(String freqId) {
        INSTANCE.consumeRequest(freqId);
    }

    /** 曝光是否已达上限。返回 true 表示不可展示。 */
    public static boolean isImpLimitReached(String freqId) {
        return INSTANCE.isImpLimitReached(freqId);
    }

    /** 真正的曝光上报前/后提交计数。 */
    public static void consumeImpression(String freqId) {
        INSTANCE.consumeImpression(freqId);
    }

    /** 点击是否已达上限。返回 true 表示需静默拦截。 */
    public static boolean isClickLimitReached(String freqId) {
        return INSTANCE.isClickLimitReached(freqId);
    }

    /** 点击已上报后提交计数。 */
    public static void consumeClick(String freqId) {
        INSTANCE.consumeClick(freqId);
    }

    /** 点击被拦截时调用：组装事件并交由 listener 或基础库内置默认上报。 */
    public static void notifyClickBlocked(String freqId, String reqId) {
        INSTANCE.notifyClickBlocked(freqId, reqId);
    }
}
