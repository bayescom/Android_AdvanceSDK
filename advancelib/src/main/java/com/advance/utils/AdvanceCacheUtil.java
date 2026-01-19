package com.advance.utils;


import com.advance.AdvanceSetting;
import com.advance.BaseParallelAdapter;
import com.advance.model.AdvanceSDKCacheModel;
import com.advance.model.SdkSupplier;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;


/**
 * 缓存支持步骤：
 * 1.在adapter类的初始化成功后，调用startLoad方法时，使用loadWithCacheData判断是否使用缓存广告
 * 2.在广告成功回调时调用：handleSucceed(xxxxAd); 方法触发缓存行为
 * 3.
 */


//处理SDK缓存相关逻辑，主要是内存缓存变现SDK的广告实例
public class AdvanceCacheUtil {

    public static final String TAG = "[AdvanceCacheUtil] ";

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

    //将获取到的广告，进行缓存
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


    //返回true代表load时，使用缓存的数据，false为不使用缓存数据
    public static <T> boolean loadWithCacheData(BaseParallelAdapter adapter, Class<T> tClass, BYAbsCallBack<T> callBack) {
        boolean result = false;
        try {
            if (adapter != null) {
                AdvanceSDKCacheModel cacheModel = adapter.getCacheModel();
                if (cacheModel != null) {
                    Object cacheValue = cacheModel.cacheValue;

                    //转换为具体广告data
                    if (tClass.isInstance(cacheValue)) {
                        T cacheData = tClass.cast(cacheValue);


                        adapter.sdkSupplier.useCachedSDK = true;

                        //此处回调出去，用来进行bidding价格更新操作。
                        if (callBack != null) {
                            callBack.invoke(cacheData);
                        }
                        adapter.handleSucceed(null);

                        LogUtil.d(TAG + "loadWithCacheData 将使用缓存的广告");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.d(TAG + "loadWithCacheData 异常");
            e.printStackTrace();
        }
        return result;
    }
}
