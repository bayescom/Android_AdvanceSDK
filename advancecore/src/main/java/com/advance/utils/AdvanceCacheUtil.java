package com.advance.utils;


import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.freqcap.AdvanceAdnFreqCapManager;
import com.advance.model.AdvanceSDKCacheModel;
import com.advance.model.SdkSupplier;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYUtil;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 缓存支持步骤：
 * 1.在adapter类的初始化成功后，调用startLoad方法时，使用loadWithCacheData判断是否使用缓存广告
 * 2.在广告成功回调时调用：handleSucceed(xxxxAd); 方法触发缓存行为
 * 3.
 */


//处理SDK缓存相关逻辑，主要是内存缓存变现SDK的广告实例
public class AdvanceCacheUtil {

    public static final String TAG = "[AdvanceCacheUtil] ";

    /** 正在预加载的 cacheKey，防重复发起（同一 key 同一时刻只允许一个预加载请求）。 */
    private static final Set<String> PRELOADING_KEYS =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    /** 该渠道当前是否存在未过期的缓存。 */
    public static boolean hasAliveCache(SdkSupplier supplier) {
        return getCachedSDKInf(supplier) != null;
    }

    /**
     * 曝光后预加载入口（由 {@link BaseParallelAdapter#handleShow()} 触发）。
     *
     * <p>触发条件（全部满足）：
     * <ol>
     *   <li>渠道开启缓存 {@code sdkSupplier.enableCache}（唯一开关，不设全局开关）</li>
     *   <li>当前 adapter 实例未预加载过（多曝光形态防重复请求）</li>
     *   <li>该 key 当前无存活缓存</li>
     *   <li>该 key 不在预加载中</li>
     *   <li>渠道级频控放行（预加载直连 load()，不经过 dispatchSuppliers 的闸门）</li>
     * </ol>
     *
     * <p>加载成功由 {@link com.advance.BaseParallelAdapter#handleSucceed()} 内的 isPreload 分支
     * 直接写入缓存；失败静默，不重试。
     */
    public static void preloadAfterShow(BaseParallelAdapter shownAdapter) {
        if (shownAdapter == null) {
            return;
        }
        String key = null;
        try {
            SdkSupplier supplier = shownAdapter.sdkSupplier;
            if (supplier == null) {
                return;
            }
            //① 唯一开关：渠道级缓存开关
            if (!supplier.enableCache) {
                return;
            }
            //② 同一 adapter 实例只预加载一次（banner/信息流多次刷新时避免请求量被放大）
            if (shownAdapter.hasPreloadOnce()) {
                return;
            }
            key = getCacheKey(supplier);
            //④ 并发防重
            if (!PRELOADING_KEYS.add(key)) {
                return;
            }
            //③ 已有存活缓存则不预载
            if (hasAliveCache(supplier)) {
                PRELOADING_KEYS.remove(key);
                return;
            }
            //⑤ 渠道级频控
            if (AdvanceAdnFreqCapManager.isRequestBlocked(supplier)) {
                LogUtil.w(TAG + "[Preload] 渠道频控拦截，跳过预加载 sdkSupID=" + supplier.sdkSupID);
                PRELOADING_KEYS.remove(key);
                return;
            }

            //反射创建同类 adapter（与 initAdapterData 相同的无参构造方式）
            BaseParallelAdapter preload = AdvanceLoader.getAdapter(shownAdapter.getClass().getName());
            if (preload == null) {
                LogUtil.w(TAG + "[Preload] 创建预加载 adapter 失败，class=" + shownAdapter.getClass().getName());
                PRELOADING_KEYS.remove(key);
                return;
            }

            shownAdapter.markPreloadOnce();
            preload.initContext(BYUtil.getCtx());
            //必须绑定桥接类：各渠道 loadAd() 内部会读取 splashSetting/bannerSetting 等类型化字段
            //（如尺寸、模板开关），不绑定会直接 NPE。updateBaseSetting 会成对刷新 baseSetting 与全部类型化字段。
            preload.updateBaseSetting(shownAdapter.baseSetting);
            preload.markAsPreload(supplier.copyForPreload());
            LogUtil.devDebug(TAG + "[Preload] 发起曝光后预加载 key=" + key);
            preload.startPreload();
        } catch (Throwable t) {
            LogUtil.w(TAG + "[Preload] 发起异常 " + t);
            if (key != null) {
                PRELOADING_KEYS.remove(key);
            }
        }
    }

    /** 预加载结束（成功/失败）时调用，释放防重标记。 */
    public static void onPreloadFinished(SdkSupplier supplier) {
        try {
            if (supplier != null) {
                PRELOADING_KEYS.remove(getCacheKey(supplier));
            }
        } catch (Throwable ignored) {
        }
    }

    //直接获取可用的缓存model
    public static AdvanceSDKCacheModel getCachedSDKInf(SdkSupplier supplier) {
        AdvanceSDKCacheModel result = null;
        try {
            if (supplier != null) {
                if (!supplier.enableCache) {
                    LogUtil.simple(TAG + "根据配置，SDK缓存功能未启用");
                    return null;
                }

                String key = getCacheKey(supplier);
                //首先检查缓存中是否存在缓存内容
                AdvanceSDKCacheModel cacheModel = AdvanceSetting.getInstance().cachedSDKs.get(key);

                if (cacheModel != null) {
                    long currentTime = System.currentTimeMillis();
                    long costSec = (currentTime - cacheModel.cacheTime) / 1000;

                    long cacheMaxSec = supplier.cacheMaxSec;
                    boolean cacheEnable = costSec <= cacheMaxSec;
                    LogUtil.simple(TAG + "getCachedSDK ,costSec = " + costSec + " , cacheMaxSec = " + cacheMaxSec + " , cacheEnable = " + cacheEnable);

                    //未过期才使用
                    if (cacheEnable) {
                        //检查缓存时间
                        result = cacheModel;
                    } else {
                        //过期进行移除操作
                        AdvanceSetting.getInstance().cachedSDKs.remove(key);
                    }
                }
            }
        } catch (Exception e) {
        }
        LogUtil.simple(TAG + "getCachedSDK ,result = " + result);

        return result;
    }

    //直接缓存adapter
    public static void cacheSDK(BaseParallelAdapter adapter) {
        if (adapter == null) {
            LogUtil.simple(TAG + "cacheSDK ,skip  adapter null");
            return;
        }


        SdkSupplier supplier = adapter.sdkSupplier;
        if (!supplier.enableCache) {
            LogUtil.simple(TAG + "根据配置，SDK缓存功能未启用");
            return;
        }

        String key = getCacheKey(supplier);
        LogUtil.simple(TAG + "cacheSDK ,key = " + key);


        //检查缓存中是否已存在
        AdvanceSDKCacheModel cacheModel = AdvanceSetting.getInstance().cachedSDKs.get(key);
        if (cacheModel != null) {
            LogUtil.simple(TAG + "cacheSDK skip ,缓存已存在");
            return;
        }

        //不存在的话新建并存入缓存。
        cacheModel = new AdvanceSDKCacheModel();
        //此处留存的为缓存对象的必要属性，待下次使用时，supplier对象对应的reqid已经不同了
        cacheModel.cacheTime = System.currentTimeMillis();
        cacheModel.cacheKey = key;
        cacheModel.adID = supplier.adspotid;
        cacheModel.cacheValue = adapter;
        cacheModel.serverReqID = supplier.serverReqID;
        if (adapter.baseSetting != null) {
            cacheModel.advanceReqID = adapter.baseSetting.getAdvanceId();
        }
        AdvanceSetting.getInstance().cachedSDKs.put(key, cacheModel);
    }

    @Deprecated
    public static void cacheSDK(BaseParallelAdapter adapter, Object value) {
        try {
            if (adapter == null) {
                LogUtil.simple(TAG + "cacheSDK ,skip  adapter null");
                return;
            }
            if (value == null) {
                LogUtil.simple(TAG + "cacheSDK ,skip  value null");
                return;
            }

            SdkSupplier supplier = adapter.sdkSupplier;
            if (!supplier.enableCache) {
                LogUtil.simple(TAG + "根据配置，SDK缓存功能未启用");
                return;
            }

            String key = getCacheKey(supplier);
            LogUtil.simple(TAG + "cacheSDK ,key = " + key);

            AdvanceSDKCacheModel cacheModel = new AdvanceSDKCacheModel();
            //此处留存的为缓存对象的必要属性，待下次使用时，supplier对象对应的reqid已经不同了
            cacheModel.cacheTime = System.currentTimeMillis();
            cacheModel.cacheKey = key;
            cacheModel.adID = supplier.adspotid;
            cacheModel.cacheValue = value;
            cacheModel.serverReqID = supplier.serverReqID;
            if (adapter.baseSetting != null) {
                cacheModel.advanceReqID = adapter.baseSetting.getAdvanceId();
            }
            AdvanceSetting.getInstance().cachedSDKs.put(key, cacheModel);
        } catch (Exception e) {

        }
    }

    /**
     * 当前 adapter 是否正是缓存中留待复用的对象。
     *
     * <p>用于销毁拦截：缓存持有的是 adapter 对象本身，若放任 destroy 会把它一并销毁，
     * 导致下次命中缓存时拿到已销毁对象（isDestroy=true、parallelListener=null）。
     */
    public static boolean isCachedAdapter(BaseParallelAdapter adapter) {
        try {
            if (adapter == null || adapter.sdkSupplier == null) {
                return false;
            }
            String key = getCacheKey(adapter.sdkSupplier);
            AdvanceSDKCacheModel cacheModel = AdvanceSetting.getInstance().cachedSDKs.get(key);
            return cacheModel != null && cacheModel.cacheValue == adapter;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void removeCache(SdkSupplier supplier) {
        try {
            String key = getCacheKey(supplier);
            LogUtil.simple(TAG + "removeCache ,key = " + key);
            AdvanceSetting.getInstance().cachedSDKs.remove(key);
        } catch (Exception e) {
        }
    }


    //获取缓存关联的唯一key。优先取服务端返回的唯一key标识，其次选择广告位id，
    //注意:服务端要保证两次策略下发的相同策略下得广告位id的唯一key标识是一样的。
    public static String getCacheKey(SdkSupplier supplier) {
        String result = "";
        try {
            if (supplier != null) {
                String unitKey = supplier.unitKey;
                if (BYStringUtil.isNotEmpty(unitKey)) {
                    result = unitKey;
                } else {
                    result = supplier.adspotid;
                }
            }
        } catch (Exception e) {

        }
        return result;
    }


    /**
     * 缓存SDK广告实现类的方式
     * 返回true代表load时，使用缓存的数据，false为不使用缓存数据
     */
//    public static <T> boolean loadWithCacheData(BaseParallelAdapter adapter, Class<T> tClass, BYAbsCallBack<T> callBack) {
//        boolean result = false;
//        try {
//            if (adapter != null) {
//                AdvanceSDKCacheModel cacheModel = adapter.getCacheModel();
//                if (cacheModel != null) {
//                    Object cacheValue = cacheModel.cacheValue;
//
//                    //转换为具体广告data
//                    if (tClass.isInstance(cacheValue)) {
//                        T cacheData = tClass.cast(cacheValue);
//
//
//                        adapter.sdkSupplier.useCachedSDK = true;
//
//                        try {
//                            //此处回调出去，用来进行bidding价格更新操作。
//                            if (callBack != null) {
//                                callBack.invoke(cacheData);
//                            }
//                        } catch (Exception e) {
//                        }
//                        adapter.handleSucceed(null);
//
//                        LogUtil.d(TAG + "loadWithCacheData 将使用缓存的广告");
//                        return true;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            LogUtil.d(TAG + "loadWithCacheData 异常");
//            e.printStackTrace();
//        }
//        return result;
//    }


    /**
     * 缓存adapter类的方式
     * 返回true代表load时，使用缓存的数据，false为不使用缓存数据
     */
//    public static <T extends BaseParallelAdapter> boolean loadWithCacheAdapter(BaseParallelAdapter adapter, Class<T> tClass, BYAbsCallBack<T> callBack) {
//        boolean result = false;
//        try {
//            if (adapter != null) {
//                AdvanceSDKCacheModel cacheModel = adapter.getCacheModel();
//                if (cacheModel != null) {
//                    Object cacheValue = cacheModel.cacheValue;
//
//                    //转换为具体广告data
//                    if (tClass.isInstance(cacheValue)) {
//                        T cacheData = tClass.cast(cacheValue);
//
//                        adapter.sdkSupplier.useCachedSDK = true;
//
//                        try {
//                            if (cacheData != null) {
//                                //此处回调出去，用来进行bidding价格更新操作。
//                                if (callBack != null) {
//                                    callBack.invoke(cacheData);
//                                }
//
//                                //替换缓存adapter得渠道参数信息为新的，这样在收到回调事件时上报时会使用新tk地址
//                                cacheData.sdkSupplier = adapter.sdkSupplier;
//
//                                //替换执行策略中得adapter
//                                if (adapter.baseSetting != null) {
//                                    //置换桥接类
//                                    cacheData.baseSetting = adapter.baseSetting;
//                                    //置换实现adapter
//                                    adapter.baseSetting.replaceCacheAdapter(adapter.sdkSupplier.priority + "", cacheData);
//                                }
//                            }
//                        } catch (Exception e) {
//                        }
//                        adapter.handleSucceed();
//
//                        LogUtil.d(TAG + "loadWithCacheData 将使用缓存的广告");
//                        return true;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            LogUtil.d(TAG + "loadWithCacheData 异常");
//            e.printStackTrace();
//        }
//        return result;
//    }
    public static boolean loadWithCacheAdapter(BaseParallelAdapter adapter) {
        boolean result = false;
        try {
            if (adapter != null) {
                //检查当前广告位id下是否存在已缓存adapter
                AdvanceSDKCacheModel cacheModel = adapter.getCacheModel();
                if (cacheModel != null) {
                    BaseParallelAdapter cacheValue = (BaseParallelAdapter) cacheModel.cacheValue;

                    //转换为具体广告data
                    if (cacheValue != null) {
                        adapter.sdkSupplier.useCachedSDK = true;
                        try {
                            //更新价格
                            //用缓存广告的「渠道原始价」重算展示价：避免把已乘系数的展示价再次乘系数（bidRatio 双乘）
                            double cachedOriginPrice = cacheValue.sdkSupplier.channelPrice;
                            if (cachedOriginPrice <= 0) {
                                //兼容：无 channelPrice 的旧缓存（内存缓存，进程重启即失效，概率极低）退化为原行为
                                cachedOriginPrice = cacheValue.sdkSupplier.price;
                            }
                            adapter.updateBidding(cachedOriginPrice);

                            //替换缓存adapter得渠道参数信息为新的，这样在收到回调事件时上报时会使用新tk地址
                            cacheValue.sdkSupplier = adapter.sdkSupplier;

                            //替换执行策略中得adapter
                            if (adapter.baseSetting != null) {
                                //置换桥接类：必须同步刷新类型化 setting，
                                //否则 splashSetting 等仍指向旧会话（其 listener 可能已被置空），
                                //复用后 adapter 的 skip/show/click 等回调会走到旧实例而全部失效
                                cacheValue.updateBaseSetting(adapter.baseSetting);
                                //置换实现adapter
                                adapter.baseSetting.replaceCacheAdapter(AdvanceUtil.getAdapterMapKey(adapter.sdkSupplier), cacheValue);
                            }

                        } catch (Exception e) {
                        }
                        //缓存复用：重置聚合侧状态（渠道广告对象仍可播放，不重新加载、不调 destroyAd），
                        //必须在 adapter.handleSucceed() 之前，保证其同步回调链能正常走到展示
                        cacheValue.resetForReuse();
                        //通知广告成功依然是由当前adapter进行，不过show时使用的adapter将由被replaceCacheAdapter操作更新后的缓存adapter进行。
                        adapter.handleSucceed();

                        LogUtil.d(TAG + "loadWithCacheAdapter 将使用缓存的广告");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.d(TAG + "loadWithCacheAdapter 异常");
            e.printStackTrace();
        }
        return result;
    }
}
