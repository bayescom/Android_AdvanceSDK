package com.advance.net;

import android.content.Context;

import com.bayes.sdk.basic.trackretry.BYTrackLevel;
import com.bayes.sdk.basic.trackretry.BYTrackNetReceiver;
import com.bayes.sdk.basic.trackretry.BYTrackRetry;

import org.json.JSONObject;

/**
 * advance 侧「上报补发」入口（薄持有层）。
 *
 * 与 {@code FreqCapManager} 同一套路：持有基础库 BYTrackRetry 的独立实例，
 * namespace 固定为 "advance"。这样  SDK 各自持有不同 namespace 的实例，
 * 同进程内共享 ClassLoader 时也能做到队列与磁盘 key 物理隔离，互不串数据。
 *
 * 业务侧只跟本类打交道，不直接碰基础库：
 * - GET 上报链接失败 → {@link #onReportFailed(String, int, String)}
 * - 计费类（曝光/点击）要"先落库" → {@link #prepare(String)} + {@link #ack(String)}
 *
 * 补发参数由服务端下发（sdk_config 的 tk_retry_config），
 * 未下发 enable=1 时整条链路不生效，既有上报行为完全不变。
 */
public final class TrackRetryManager {

    private static final String NAMESPACE = "advance";

    static {
        // 让基础库内部（如默认实例查找）也落到本 namespace 上
        BYTrackRetry.setDefaultNamespace(NAMESPACE);
    }

    private static final BYTrackRetry INSTANCE = BYTrackRetry.of(NAMESPACE);

    private TrackRetryManager() {
        // 工具类，禁止实例化
    }

    /** 一次性下发补发配置（sdk_config 中的 tk_retry_config 节点）。 */
    public static void updateConfig(JSONObject tkRetryConfig) {
        INSTANCE.pushConfig(tkRetryConfig);
    }

    /** 注册网络可用性监听（幂等，建议 SDK 初始化时调用一次）。 */
    public static void registerNetReceiver(Context context) {
        BYTrackNetReceiver.register(context);
    }

    /**
     * GET 上报链接发送失败 → 入待补发池（"失败才落库"模型）。
     *
     * @param url   已完成宏替换的完整上报 url（同时作为唯一键）
     * @param level {@link BYTrackLevel#L0_BILLING} 或 {@link BYTrackLevel#L1_NORMAL}
     * @param from  来源 tag，仅作诊断信息随记录落盘
     */
    public static void onReportFailed(String url, int level, String from) {
        INSTANCE.enqueue(url, level, from);
    }

    /** 默认按 L1（普通广告事件）入池。 */
    public static void onReportFailed(String url, String from) {
        INSTANCE.enqueue(url, BYTrackLevel.L1_NORMAL, from);
    }

    /** L0（计费/对账类）专用：发送前先落库。返回 null 表示未开启。 */
    public static String prepare(String url) {
        return INSTANCE.prepare(url, BYTrackLevel.L0_BILLING, null);
    }

    /** L0 首次发送成功：按 url 出池。 */
    public static void ack(String url) {
        INSTANCE.ack(url);
    }

    /** 立即排空一次（不等退避）。 */
    public static void flush() {
        INSTANCE.flush();
    }

    /** App 回前台：只提前执行时刻，不清零退避计数。 */
    public static void onAppForeground() {
        INSTANCE.onAppForeground();
    }

    /** 是否开启 */
    public static boolean isEnabled() {
        return INSTANCE.isEnabled();
    }

    /** 调试用 */
    public static String dump() {
        return INSTANCE.dump();
    }
}
