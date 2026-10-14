package com.advance.freqcap;

import com.advance.model.SdkSupplier;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.freqcap.BYFreqCap;
import com.bayes.sdk.basic.freqcap.BYFreqCapResult;
import com.bayes.sdk.basic.util.BYCatch;
import com.bayes.sdk.basic.util.BYStringUtil;

import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * advance 侧渠道级（ADN）频次控制入口（通用薄封装）。
 *
 * <p>与 {@link AdvanceFreqCapManager}（媒体广告位级）并行，持有基础库 BYFreqCap 的
 * 独立实例，namespace 固定为 {@value #NAMESPACE}，两层计数物理隔离、互不串数据。
 *
 * <p>与媒体广告位级的三点差异：
 * <ul>
 *   <li>freqId 为渠道唯一 id {@link SdkSupplier#sdkSupID}（策略下发的 sdk_id）</li>
 *   <li>配置<strong>跟随策略下发</strong>（supplier 内嵌 request_limit 节点），
 *       不走 advance_sdk_config，字段名 device_daily_* 需映射为基础库 schema</li>
 *   <li>超限后果是<strong>跳过该渠道</strong>（其余渠道继续，降级），而非整体失败</li>
 * </ul>
 *
 * 配置同步语义见 {@link #syncFromSuppliers(List)}；未下发配置时所有方法等价于放行，
 * 既有广告链路行为完全不变。
 */
public final class AdvanceAdnFreqCapManager {

    /** 与媒体广告位级（"advance"）隔离的命名空间，基础库据此隔离计数存储。 */
    public static final String NAMESPACE = "advance_adn";

    private static final String TAG = "[AdnFreqCap] ";

    private static final BYFreqCap INSTANCE = new BYFreqCap(NAMESPACE);

    /**
     * 跨策略 merge 后的配置表（sdkSupID -> 基础库 schema）。
     *
     * <p>必须宿主侧维护：{@link BYFreqCap#updateConfig(JSONObject)} 是整体原子替换，
     * 而配置跟随策略逐次下发，同进程多个聚合广告位的策略会交错到达，
     * 直接把「本次策略的渠道 map」传进去会抹掉其他广告位先前下发的渠道配置。
     */
    private static final ConcurrentHashMap<String, JSONObject> MERGED = new ConcurrentHashMap<>();

    private AdvanceAdnFreqCapManager() {
        // 工具类，禁止实例化
    }

    /**
     * 策略下发时同步渠道频控配置（在 {@code AdvanceBaseAdspot.dispatchSuppliers} 内调用，
     * 实时策略与缓存策略共用同一解析函数，此处单点同步即可覆盖两条链路）。
     *
     * <p>语义：
     * <ul>
     *   <li>渠道带有效 request_limit → 字段映射后 merge，同 sdkSupID <b>后到覆盖</b></li>
     *   <li>渠道在策略里但 request_limit 为 null 或全 0 → 视为服务端明确取消，从表中移除</li>
     *   <li>渠道不在本次策略里 → 保留原配置（可能来自其他广告位的策略）</li>
     * </ul>
     *
     * @param suppliers 本次策略解析出的渠道列表
     */
    public static void syncFromSuppliers(List<SdkSupplier> suppliers) {
        try {
            if (suppliers == null || suppliers.isEmpty()) {
                return;
            }
            boolean changed = false;
            for (SdkSupplier s : suppliers) {
                if (s == null || BYStringUtil.isEmpty(s.sdkSupID)) {
                    continue;
                }
                if (s.hasAnyFreqLimit()) {
                    JSONObject cfg = new JSONObject();
                    cfg.putOpt("req_limit", s.dailyReqLimit);
                    cfg.putOpt("imp_limit", s.dailyImpLimit);
                    cfg.putOpt("click_limit", s.dailyClickLimit);
                    cfg.putOpt("req_interval", s.dailyReqInterval);
                    MERGED.put(s.sdkSupID, cfg);
                    LogUtil.devDebug(TAG + "同步渠道频控 sdkSupID=" + s.sdkSupID
                            + " req=" + s.dailyReqLimit + " imp=" + s.dailyImpLimit
                            + " click=" + s.dailyClickLimit + " interval=" + s.dailyReqInterval);
                } else {
                    // 服务端明确取消该渠道频控
                    if (MERGED.remove(s.sdkSupID) != null) {
                        LogUtil.devDebug(TAG + "移除渠道频控 sdkSupID=" + s.sdkSupID);
                    }
                }
                changed = true;
            }
            // 配置整体原子替换下发；计数在 store 中，不受影响。
            // 注意不能用 !MERGED.isEmpty() 做条件：否则当策略把所有渠道的 request_limit 都取消时，
            // MERGED 被清空却不调用 updateConfig，基础库里的旧配置仍会继续生效。
            if (changed) {
                INSTANCE.updateConfig(new JSONObject(MERGED));
            }
        } catch (Throwable t) {
            BYCatch.onCaught(t, true, BYCatch.LEVEL_WARN);
        }
    }

    /** freqId 派生唯一入口：渠道唯一 id（策略 sdk_id）。为空返回 ""，调用方一律放行。 */
    public static String freqIdOf(SdkSupplier supplier) {
        if (supplier == null) {
            return "";
        }
        return supplier.sdkSupID == null ? "" : supplier.sdkSupID;
    }

    /**
     * 渠道请求前置判定（覆盖 req_limit / req_interval / imp_limit / click_limit 四类）。
     *
     * @return true 表示该渠道本轮应被跳过（跳过该渠道，其余渠道继续）
     */
    public static boolean isRequestBlocked(SdkSupplier supplier) {
        String freqId = freqIdOf(supplier);
        if (BYStringUtil.isEmpty(freqId)) {
            return false;
        }
        BYFreqCapResult result = INSTANCE.checkRequest(freqId);
        return result != null && result.isBlock();
    }

    /** 渠道请求已放行，提交计数。 */
    public static void consumeRequest(SdkSupplier supplier) {
        String freqId = freqIdOf(supplier);
        if (BYStringUtil.isEmpty(freqId)) {
            return;
        }
        INSTANCE.consumeRequest(freqId);
    }

    /**
     * 渠道曝光是否已达上限：展示前闸门（对齐 mercury isValid 的曝光闸门）。
     *
     * <p>注意只判曝光配额，不判点击配额——click_limit 超限后仍允许展示
     * （与 mercury 源码一致，属刻意设计）。
     */
    public static boolean isImpressionBlocked(SdkSupplier supplier) {
        String freqId = freqIdOf(supplier);
        if (BYStringUtil.isEmpty(freqId)) {
            return false;
        }
        return INSTANCE.isImpLimitReached(freqId);
    }

    /** 渠道曝光已提交，提交计数。 */
    public static void consumeImpression(SdkSupplier supplier) {
        String freqId = freqIdOf(supplier);
        if (BYStringUtil.isEmpty(freqId)) {
            return;
        }
        INSTANCE.consumeImpression(freqId);
    }

    /** 渠道点击是否已达上限：点击闸门，true 表示需静默拦截。 */
    public static boolean isClickBlocked(SdkSupplier supplier) {
        String freqId = freqIdOf(supplier);
        if (BYStringUtil.isEmpty(freqId)) {
            return false;
        }
        return INSTANCE.isClickLimitReached(freqId);
    }

    /** 渠道点击已上报，提交计数。 */
    public static void consumeClick(SdkSupplier supplier) {
        String freqId = freqIdOf(supplier);
        if (BYStringUtil.isEmpty(freqId)) {
            return;
        }
        INSTANCE.consumeClick(freqId);
    }

    /**
     * 渠道点击被拦截时上报（走基础库内置默认上报 eventid=605，ext 带 ns=advance_adn 可区分层级）。
     */
    public static void notifyClickBlocked(SdkSupplier supplier, String reqId) {
        String freqId = freqIdOf(supplier);
        if (BYStringUtil.isEmpty(freqId)) {
            return;
        }
        INSTANCE.notifyClickBlocked(freqId, reqId);
    }
}
