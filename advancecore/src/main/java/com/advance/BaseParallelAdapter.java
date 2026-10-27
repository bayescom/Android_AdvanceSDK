package com.advance;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFBridge;
import com.advance.custom.AdvanceAdapterItf;
import com.advance.custom.AdvanceCustomInit;
import com.advance.itf.AdvanceADNInitResult;
import com.advance.model.AdvanceSDKCacheModel;
import com.advance.net.AdvanceReport;
import com.advance.utils.ActivityTracker;
import com.advance.utils.AdvanceCacheUtil;
import com.advance.utils.CustomADNUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;
import com.advance.model.SdkSupplier;
import com.advance.model.SupplierSettingModel;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYCatch;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bayes.sdk.basic.util.BYUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseParallelAdapter implements AdvanceAdapterItf {
    public String TAG = "[" + this.getClass().getSimpleName() + "] ";

    protected Activity activity;
    protected Context mContext;
    protected SoftReference<Activity> softReferenceActivity;

    public BaseSetting baseSetting;
    //各个广告位内部传递setting
    protected SplashSetting splashSetting;
    protected BannerSetting bannerSetting;
    protected InterstitialSetting interstitialSetting;
    protected RewardVideoSetting rewardSetting;
    protected FullScreenVideoSetting fullScreenVideoSetting;
    protected NativeExpressSetting nativeExpressSetting;
    protected AdvanceRFBridge nativeSetting;
    protected AdvanceDrawSetting drawSetting;
    Map<String, Object> localExtra = new HashMap<>();
    Map<String, Object> serverExtra = new HashMap<>();


    public SdkSupplier sdkSupplier;
    //是否为异步请求
    public boolean isParallel = true;
    public boolean hasOrderRun = false;

    public int adStatus = AdvanceConstant.AD_STATUS_DEFAULT; //AdvanceConstant.AD_STATUS_DEFAULT 初始值
    protected AdvanceError advanceError;
    public NativeParallelListener parallelListener;

    //是否支持并行请求，默认true；部分SDK的广告位、部分版本SDK可能无法支持并行的方式分步加载广告，统一通过这里需要标记支持情况。
    public boolean supportPara = true;
    public int cacheStatus = AdvanceConstant.STATUS_UNCACHED;
    public boolean isSuccess = false; //广告返回标记
    public boolean isDestroy = false;
    public boolean refreshing = false;

    public boolean hasFailed = false;
    public boolean hasShown = false;
    public int lastFailedPri = -1;

    /**
     * 预加载模式：无会话绑定（baseSetting 为 null），加载完成只写缓存，不走任何会话回调与上报。
     */
    protected boolean isPreload = false;
    /**
     * 本 adapter 实例是否已完成过一次「曝光后预加载」，用于 banner/信息流等多曝光形态防重复请求。
     */
    private boolean preloadOnce = false;

    //回调所需参数
    public View nativeExpressADView;
    //获取聚合具体设置项
//    public AdvanceRFBridge mAdvanceRFBridge;
    //   基础数据信息
    public AdvanceRFADData dataConverter;

//    public HashMap<Integer, ParaStatusModel> statusMap = new HashMap<>();

    protected int adNum = 0;//记录广告数量，每load一次+1.每失败一次-1.如果此值为负，那么可能为回调了多次的失败回调，需抛弃处理此次回调。
    protected AdvanceSDKCacheModel cacheModel = null;


    private Activity getADActivity() {
        if (softReferenceActivity != null) {
            return softReferenceActivity.get();
        }
        if (activity != null) {
            return activity;
        }
        if (mContext != null) {
            Activity ctxAct = AdvanceUtil.getActivityFromCtx(mContext);
            if (ctxAct != null) {
                return ctxAct;
            }
        }
        //兜底返回当前页面
        activity = ActivityTracker.getInstance().getCurrentActivity();
        if (activity != null) {
            return activity;
        }
        return null;
    }

    //获取activity信息新方法，首先通过view寻找activity信息（可能为空），然后再通过传递参数获取
    public Activity getRealActivity() {
        return getRealActivity(null);
    }

    public Activity getRealActivity(View adContainerView) {
        Activity result = null;
        try {
            if (rewardSetting != null && rewardSetting.getShowActivity() != null) {
                activity = rewardSetting.getShowActivity();
            }
            if (adContainerView != null) {
                result = AdvanceUtil.getActivityFromView(adContainerView);
            } else {
                ViewGroup adContainer = null;
                if (splashSetting != null) {
                    adContainer = splashSetting.getAdContainer();
                }
                if (adContainer == null && nativeExpressSetting != null) {
                    adContainer = nativeExpressSetting.getAdContainer();
                }
                if (adContainer != null) {
                    result = AdvanceUtil.getActivityFromView(adContainer);
                }
            }
            LogUtil.devDebug(TAG + " getActivityFromView result = " + result);
            if (result == null) {
                result = getADActivity();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    //获取上下文信息，优先取activity信息，如果没有则使用基础库中保存的appContext
    public Context getRealContext() {
        Context result = null;
        try {
            result = getADActivity();
            if (mContext != null) {
                result = mContext;
            }
            if (result == null) {
                result = BYUtil.getCtx();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public BaseParallelAdapter() {
        LogUtil.d("init adapter:" + this);
    }

    public void initContext(Context context) {
        try {
            LogUtil.devDebug("initContext:" + context);

            this.mContext = context;
            this.activity = AdvanceUtil.getActivityFromCtx(context);

            initPara();
        } catch (Exception e) {

        }
    }

    public void initSplashSetting(SplashSetting setting) {
        this.baseSetting = setting;
        this.splashSetting = setting;
    }

    public void initBannerSetting(BannerSetting setting) {
        this.baseSetting = setting;
        this.bannerSetting = setting;
    }

    public void initInterstitialSetting(InterstitialSetting setting) {
        this.baseSetting = setting;
        this.interstitialSetting = setting;
    }

    public void initRewardSetting(RewardVideoSetting setting) {
        this.baseSetting = setting;
        this.rewardSetting = setting;
    }

    public void initFullScreenVideoSetting(FullScreenVideoSetting setting) {
        this.baseSetting = setting;
        this.fullScreenVideoSetting = setting;
    }

    public void initNativeExpressSetting(NativeExpressSetting setting) {
        this.baseSetting = setting;
        this.nativeExpressSetting = setting;
    }

    public void initNativeSetting(AdvanceRFBridge setting) {
        this.baseSetting = setting;
        this.nativeSetting = setting;
    }

    public void initDrawerSetting(AdvanceDrawSetting setting) {
        this.baseSetting = setting;
        this.drawSetting = setting;
    }

    /**
     * 热替换桥接类（缓存 adapter 复用时由 {@code AdvanceCacheUtil.loadWithCacheAdapter} 调用）。
     *
     * <p>必须成对刷新 baseSetting 与类型化 setting 字段：缓存持有的是 adapter 对象本身，
     * 复用时若只替换 baseSetting，splashSetting 等类型化字段仍指向上一个会话的广告位实例
     * （其 activity 可能已销毁、listener 已被置空），会导致 adapter 的 handleSkip /
     * adapterDidSkip 等回调走到旧实例，广告事件与跳转全部失效。
     *
     * <p>同时失效 localExtra / serverExtra 派生缓存，使其基于新 setting 重新推导。
     */
    public void updateBaseSetting(BaseSetting setting) {
        if (setting == null) {
            return;
        }
        this.baseSetting = setting;
        if (setting instanceof SplashSetting) {
            this.splashSetting = (SplashSetting) setting;
        } else if (setting instanceof BannerSetting) {
            this.bannerSetting = (BannerSetting) setting;
        } else if (setting instanceof InterstitialSetting) {
            this.interstitialSetting = (InterstitialSetting) setting;
        } else if (setting instanceof RewardVideoSetting) {
            this.rewardSetting = (RewardVideoSetting) setting;
        } else if (setting instanceof FullScreenVideoSetting) {
            this.fullScreenVideoSetting = (FullScreenVideoSetting) setting;
        } else if (setting instanceof NativeExpressSetting) {
            this.nativeExpressSetting = (NativeExpressSetting) setting;
        } else if (setting instanceof AdvanceRFBridge) {
            this.nativeSetting = (AdvanceRFBridge) setting;
        } else if (setting instanceof AdvanceDrawSetting) {
            this.drawSetting = (AdvanceDrawSetting) setting;
        }
        //派生缓存失效：localExtra 源自 baseSetting.getCustomData()，
        //serverExtra 源自 sdkSupplier.ext，均需基于新会话重新推导
        this.localExtra = new HashMap<>();
        this.serverExtra = new HashMap<>();
    }


//    public BaseParallelAdapter(SoftReference<Activity> softReferenceActivity, final BaseSetting baseSetting) {
//        this.softReferenceActivity = softReferenceActivity;
//        this.baseSetting = baseSetting;
//        initPara();
//    }
//
//    public BaseParallelAdapter(Activity activity, final BaseSetting baseSetting) {
//        this.activity = activity;
//        this.baseSetting = baseSetting;
//        initPara();
//    }
//
//    public BaseParallelAdapter(Context context, final BaseSetting baseSetting) {
//        this.mContext = context;
//        this.baseSetting = baseSetting;
//        initPara();
//    }

    private void initPara() {
        adStatus = AdvanceConstant.AD_STATUS_DEFAULT;
        try {
            isDestroy = false;
            //每次发起请求都重置成功标记，避免同一 adapter 实例二次 load 时残留上一次的 true，
            //导致各渠道 isValid() 以 isSuccess 兜底时误判广告仍有效（缓存复用路径 resetForReuse 会重新置 true）
            isSuccess = false;
            parallelListener = createParallelListener();
        } catch (Throwable e) {
            e.printStackTrace();
            advanceError = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD);
            loadFailed();
        }
    }

    /**
     * 创建并行监听器。initPara 与 resetForReuse 共用：
     * destroy() 会把 parallelListener 置空，缓存复用时必须重建才能继续收到成功/失败回调。
     */
    private NativeParallelListener createParallelListener() {
        return new NativeParallelListener() {
                @Override
                public void onCached() {
                    if (cacheStatus == AdvanceConstant.STATUS_CACHED_CALL) {
                        cacheEvent();
                    }
                    cacheStatus = AdvanceConstant.STATUS_CACHED;
                }

                @Override
                public void onSucceed() {
                    if (isEarlySuccess()) {
                        //等待再次被调用展示
                        LogUtil.high("isEarlySuccess_update_status");
                        adStatus = AdvanceConstant.AD_STATUS_LOAD_SUCCESS;

                        //传递成功事件给基类，标记为成功
                        baseSetting.paraEvent(AdvanceConstant.EVENT_TYPE_SUCCEED, null, sdkSupplier);
//                    } else if (isBiddingMode()) {
//                        LogUtil.simple("isBiddingMode_update_status");
//                        adStatus = AdvanceConstant.AD_STATUS_LOAD_SUCCESS;
//                        //传递成功事件给基类，标记为成功
//                        baseSetting.paraEvent(AdvanceConstant.EVENT_TYPE_SUCCEED, null, sdkSupplier);
                    } else {
                        //传递成功事件给基类，标记为成功
                        baseSetting.paraEvent(AdvanceConstant.EVENT_TYPE_SUCCEED, null, sdkSupplier);
                        LogUtil.high(TAG + "onSucceed adStatus = " + adStatus);
                        LogUtil.devDebug(TAG + "onSucceed isValid()  = " + isValid() );
                        //如果是等待中发起了展示需求，直接展示，否则标记为1 成功获得广告
                        if (adStatus == AdvanceConstant.AD_STATUS_LOADING_SHOW) {
                            adStatus = AdvanceConstant.AD_STATUS_LOADED_SHOW;
                            callbackLoad();
                        } else {
                            adStatus = AdvanceConstant.AD_STATUS_LOAD_SUCCESS;
                        }
                    }


                    //并行上报广告加载成功
                    if (sdkSupplier != null) {

                        AdvanceReport.replaceReportSuccess(sdkSupplier, BaseParallelAdapter.this);

//                        String reqid = baseSetting == null ? "" : baseSetting.getAdvanceId();
//                        ArrayList<String> succTk;
//                        //有返回bid价格信息时，进行上报拼接
//                        if (isCurrentSupBidding() && sdkSupplier.bidResultPrice > 0) {
//                            succTk = AdvanceReport.getReplacedBidding(sdkSupplier.succeedtk, reqid, sdkSupplier.bidResultPrice);
//                        } else {
//                            succTk = AdvanceReport.getReplacedTime(sdkSupplier.succeedtk, reqid);
//                        }
//                        switchReport(succTk);
                    }
                }

                @Override
                public void onFailed(AdvanceError error) {
                    advanceError = error;
                    loadFailed();

                }
        };
    }

    /**
     * 从缓存复用时的状态重置。
     *
     * <p>渠道 SDK 的广告对象在 destroy 之后仍可正常播放（已实测），故此处只重置聚合侧状态，
     * 不重新加载、不调用 destroyAd。主要用于修复：缓存对象若曾被 destroy 污染
     * （isDestroy=true、parallelListener=null、adStatus=DEFAULT），复用时将无法回调与展示，
     * 媒体主动 show() 也会因 isDestroy 被拒。
     */
    public void resetForReuse() {
        try {
            LogUtil.devDebug(TAG + "resetForReuse ，从缓存复用重置聚合侧状态");
            isDestroy = false;
            //destroy 会把 parallelListener 置空，先重建再重置状态
            initPara();
            //缓存前的广告已加载完成，直接进入「加载成功」态，使 callLoaded 能立即回调展示
            if (adStatus != AdvanceConstant.AD_STATUS_LOADED_SHOW) {
                adStatus = AdvanceConstant.AD_STATUS_LOAD_SUCCESS;
            }
            isSuccess = true;
        } catch (Throwable t) {
            BYCatch.onCaught(t, false, BYCatch.LEVEL_WARN);
        }
    }

    /** 当前 adapter 是否正是缓存中留待复用的对象（销毁时需要跳过，避免污染缓存）。 */
    public boolean isCachedForReuse() {
        try {
            return AdvanceCacheUtil.isCachedAdapter(this);
        } catch (Throwable t) {
            return false;
        }
    }

    private SupplierSettingModel.ParaGroupSetting getCurrentParaGroupSetting() {
        if (baseSetting != null) {
            SupplierSettingModel.ParaGroupSetting paraGroupSetting = baseSetting.getCurrentParaGroupSetting();
            return paraGroupSetting;
        }
        return null;
    }

    //当前并行组是否为取早逻辑配置
    protected boolean isCurrentParaEarlyType() {
        boolean isEarlyType = false;
        if (getCurrentParaGroupSetting() != null) {
            isEarlyType = getCurrentParaGroupSetting().isEarlyType();
        }
        return isEarlyType;
    }

    //是否有一个并行组成员已经成功load到广告，如果load到了，需要收到广告后进行等待
    protected boolean isEarlySuccess() {
        boolean result = false;

        if (isCurrentParaEarlyType() && getCurrentParaGroupSetting() != null) {
            result = getCurrentParaGroupSetting().successList != null && getCurrentParaGroupSetting().successList.size() > 0;
            LogUtil.max("getCurrentParaGroupSetting().successList = " + getCurrentParaGroupSetting().successList.toString());
        }
        return result;
    }


    private void loadFailed() {
        try {
            //预加载失败：静默，不重试、不上报、不回调任何会话
            if (isPreload) {
                LogUtil.w(TAG + "[Preload] 预加载失败，静默忽略（不重试）");
                adStatus = AdvanceConstant.AD_STATUS_LOAD_FAILED;
                AdvanceCacheUtil.onPreloadFinished(sdkSupplier);
                return;
            }
            LogUtil.high(TAG + "loadFailed_adStatus = " + adStatus);
            reportFailed();

            adStatus = AdvanceConstant.AD_STATUS_LOAD_FAILED;
            doFailed();
//
//            if (isCurrentSupBidding()) {//bidding渠道仅回调失败即可
//                doFailed();
//                return;
//            }
//            //从调用load方法以后执行时需要进行失败回调
//            if (adStatus == AdvanceConstant.AD_STATUS_LOADING_SHOW || adStatus == AdvanceConstant.AD_STATUS_LOADED_SHOW || adStatus == AdvanceConstant.AD_STATUS_LOADING) {
//                adStatus = AdvanceConstant.AD_STATUS_LOAD_FAILED;
//                doFailed();
//            } else {
//                adStatus = AdvanceConstant.AD_STATUS_LOAD_FAILED;
//            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    void setSDKSupplier(SdkSupplier sdkSupplier) {
        try {
            this.sdkSupplier = sdkSupplier;


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //初始化获取广告缓存model信息，在基类广告load之前进行
    public void initCacheInf() {
        cacheModel = AdvanceCacheUtil.getCachedSDKInf(sdkSupplier);
    }

    // ==================== 曝光后预加载（Preload） ====================

    /** 本 adapter 实例是否已完成过一次曝光后预加载（多曝光形态防重复请求）。 */
    public boolean hasPreloadOnce() {
        return preloadOnce;
    }

    /** 标记本 adapter 实例已完成一次曝光后预加载。 */
    public void markPreloadOnce() {
        this.preloadOnce = true;
    }

    /**
     * 标记为预加载 adapter 并绑定渠道参数。
     *
     * <p>预加载 adapter 没有会话绑定（baseSetting 为 null），加载完成后只写缓存，
     * 不执行任何 adapterDidShow / paraEvent 等会话回调。
     */
    public void markAsPreload(SdkSupplier supplier) {
        this.isPreload = true;
        setSDKSupplier(supplier);
    }

    /** 预加载启动入口：复用既有 load 链路，内部由 isPreload 分支保证静默。 */
    public void startPreload() {
        try {
            LogUtil.devDebug(TAG + "[Preload] startPreload");
            load();
        } catch (Throwable t) {
            LogUtil.w(TAG + "[Preload] startPreload 异常 " + t);
            AdvanceCacheUtil.onPreloadFinished(sdkSupplier);
        }
    }

//    /**
//     * 并行请求广告。并行请求拿到广告信息后，会存储结果状态。
//     *
//     * @see AdvanceBaseAdapter#orderLoadAd() 串行请求广告方法
//     */
////    protected abstract void paraLoadAd();

    //广告就绪，可以进行后续广告展示方法，串行or并行均会执行到此方法，区别是串行是广告加载成功后立即执行到此方法，并行时广告成功，也要等到选中改广告才执行此方法。
    protected void adPrepared() {
    }


    //销毁广告
//    public   void doDestroy(){};

    protected void destroy() {
        try {
            //缓存中留待后续复用的对象不销毁：否则下次命中缓存拿到的是已销毁对象，
            //复用时无法回调、媒体主动 show() 也会因 isDestroy 被拒。
            //（渠道 SDK 的广告对象本身在 destroy 后仍可播放，已实测）
            if (isCachedForReuse()) {
                LogUtil.devDebug(TAG + "当前 adapter 正处于缓存复用中，跳过销毁");
                return;
            }
            isDestroy = true;
            adStatus = AdvanceConstant.AD_STATUS_DEFAULT;
            parallelListener = null;
            destroyAd();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public AdvanceSDKCacheModel getCacheModel() {
        return cacheModel;
    }

//    public void startOrderLoad() {
//        ++adNum;
//        isParallel = false;
//        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
//            @Override
//            public void call() {
//                orderLoadAd();
//            }
//        });
//    }

    //策略层发起的广告请求
    protected void load() {
        try {

//           初始化前进行启动上报
            reportLoaded();

            //获取初始化类
            AdvanceCustomInit init = CustomADNUtil.getCustomInitClass(sdkSupplier);
            if (init == null) {
                runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_INIT_DEFAULT, "未获取到初始化类"));
                return;
            }
            init.addSdkSupplier(sdkSupplier);
            init.addInitListener(new AdvanceADNInitResult() {
                @Override
                public void success() {
                    callADNLoad();
                }

                @Override
                public void fail(String code, String msg) {

                    runParaFailed(AdvanceError.parseErr(code, msg));
                }
            });
            init.innerInitSDK(getRealContext());
        } catch (Throwable e) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD, "BaseParallelAdapter load init Throwable"));
//            advanceError =
//            reportFailed();
//            //标记为失败
//            adStatus = AdvanceConstant.AD_STATUS_LOAD_FAILED;
            e.printStackTrace();
        }
    }

    private void callADNLoad() {
        try {
            ++adNum;
            //仅当仍处于初始态时才置为加载中，避免把 callLoaded 预先置好的
            //AD_STATUS_LOADING_SHOW 覆盖回 LOADING（那会重新打开同步递归的口子）
            if (adStatus == AdvanceConstant.AD_STATUS_DEFAULT) {
                adStatus = AdvanceConstant.AD_STATUS_LOADING;
            }

            //统一进行缓存adapter检查
            boolean hitCache = AdvanceCacheUtil.loadWithCacheAdapter(this);
            if (hitCache) {
                return;
            }

//            根据设置，选择不进入主线程load
            if (baseSetting != null && baseSetting.isLoadAsync()) {
                loadAd(getRealContext(), getLocalExtra(), getServerExtra());
                reportStart();
                return;
            }
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    try {
                        loadAd(getRealContext(), getLocalExtra(), getServerExtra());
                        reportStart();
                    } catch (Throwable e) {
                        runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD, "BaseParallelAdapter main load init Throwable"));
                        e.printStackTrace();
                    }
                }
            });
        } catch (Throwable e) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD, "BaseParallelAdapter load init Throwable"));
            e.printStackTrace();
        }
    }

    // TODO: 2026/5/27 添加获取逻辑，从本地、后端服务器返回的json对象中解析转换成map结构
    private Map<String, Object> getLocalExtra() {
        try {
            if (localExtra != null && localExtra.isEmpty() && baseSetting != null) {
                localExtra = baseSetting.getCustomData();
            }
        } catch (Exception e) {

        }
        return localExtra;
    }

    private Map<String, Object> getServerExtra() {
        try {
            if (serverExtra != null && serverExtra.isEmpty() && sdkSupplier != null) {
                serverExtra = CustomADNUtil.getServerCustomExtData(sdkSupplier.ext);
            }
        } catch (Exception e) {

        }
        return serverExtra;
    }

    // TODO: 2022/7/18 超时问题待验证及确定解决思路，问题1：并行组执行超时后，回调事件依然在进行，可能会影响下一组执行  问题2：此处的判断会不会受下一组数据影响
    private boolean isTimeOut(String event) {
//        if (baseSetting!=null && baseSetting.isCurrentGroupTimeOut()){
//            LogUtil.e("已超时，不再处理后续事件："+event);
//            return true;
//        }
        return false;
    }

    /**
     * 保证在主线程执行广告展示
     */
    private void callbackLoad() {
        if (isTimeOut("doMainLoad")) {
            return;
        }
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
//                isParallel = false;
                //这里集中处理回调事件
                if (baseSetting instanceof BaseAdEventListener) {
                    BaseAdEventListener setting = (BaseAdEventListener) baseSetting;
                    if (baseSetting instanceof AdvanceRFBridge) {
                        LogUtil.high(TAG + "AdvanceRFBridge adapterDidLoaded ");
                        //自渲染广告位
                        ((AdvanceRFBridge) baseSetting).adapterDidLoaded(dataConverter);
                    } else {
                        setting.adapterDidSucceed(sdkSupplier);
                    }
//                    setting.adapterDidSucceed(sdkSupplier);
                } else if (baseSetting instanceof RewardVideoSetting) {
                    RewardVideoSetting setting = (RewardVideoSetting) baseSetting;
                    setting.adapterAdDidLoaded(sdkSupplier);
                } else if (baseSetting instanceof FullScreenVideoSetting) {
                    FullScreenVideoSetting setting = (FullScreenVideoSetting) baseSetting;
                    setting.adapterAdDidLoaded(sdkSupplier);
                } else if (baseSetting instanceof NativeExpressSetting) {
                    NativeExpressSetting setting = (NativeExpressSetting) baseSetting;
                    setting.adapterAdDidLoaded(sdkSupplier);
                }
                adPrepared();
            }
        });
    }

    private void reportLoaded() {
        try {
            //预加载无策略口径，不上报 loadedtk
            if (isPreload) {
                return;
            }
            if (sdkSupplier != null) {
                ArrayList<String> tks = sdkSupplier.loadedtk;

                if (tks != null) {
                    for (String tk : tks) {
                        String tag = "SDK 启动";
                        tk = AdvanceReport.reportReplacedCommon(tk, this, tag);

                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //SDK渠道执行完广告load以后，调用埋点starttk埋点上报
    private void reportStart() {
        try {
            //预加载无策略口径，不上报 starttk
            if (isPreload) {
                return;
            }
            if (sdkSupplier != null) {
                ArrayList<String> startTKS = sdkSupplier.starttk;

                if (startTKS != null)
                    for (String tk : startTKS) {
                        String tag = "SDK start";
                        tk = AdvanceReport.reportReplacedCommon(tk, this, tag);
                    }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void reportFailed() {
        try {
            //预加载无策略口径，不上报 failedtk
            if (isPreload) {
                return;
            }
            if (sdkSupplier != null) {
                LogUtil.devDebug(TAG + "[reportFailed] sdkSupplier = " + sdkSupplier.toString());
                String reqid = baseSetting == null ? "" : baseSetting.getAdvanceId();
                switchReport(AdvanceReport.getReplacedFailed(sdkSupplier.failedtk, advanceError, reqid));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void runParaFailed(AdvanceError error) {
        try {
            if (error != null) {
                LogUtil.simple(TAG + "runParaFailed ,  isParallel = " + isParallel + ", error = " + error.toString());
            }

            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onFailed(error);
                }
            } else {
                runBaseFailed(error);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //统一处理，告诉基础类失败了，需要走下一优先级的渠道
    public void runBaseFailed(AdvanceError advanceError) {
        try {
            //避免重复执行失败任务
            checkFailed();

            if (baseSetting != null) {
                baseSetting.adapterDidFailed(advanceError, sdkSupplier);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void checkFailed() throws Exception {
        String exInf = "  --check failed--  ,  already failed , skip callback onFailed";

        boolean numberCheck = adStatus != AdvanceConstant.AD_STATUS_LOAD_FAILED;
        // 本身失败状态时 不进行数量减少
        if (numberCheck) {
            --adNum;
        }
        LogUtil.max(TAG + "[checkFailed] adNum = " + adNum + "(numberCheck = " + numberCheck + ")");
        if (sdkSupplier != null) {
            //避免重复执行失败任务
            if (hasFailed && lastFailedPri == sdkSupplier.priority && adNum < 0) {
                LogUtil.high(TAG + exInf);
//                if (isBidding()) {
//                    LogUtil.devDebug(TAG + "[checkFailed] bidding下跳过异常事件认定");
//                    return;
//                }
                throw new Exception(exInf);
            }
            hasFailed = true;
            lastFailedPri = sdkSupplier.priority;
        }
    }

    String logTag = "ad win";

//    protected void callWin() {
//
//    }


    /**
     * 广告展示方法，串并行均可调用，不支持并行时自动转为串行加载
     */
    protected void callLoaded() {
        try {
            String logMsg = TAG + " adStatus ==  " + adStatus;
            String devMsg = "";
            if (sdkSupplier != null) {
                devMsg = "channel name = " + sdkSupplier.name;
            }
            LogUtil.devDebugAuto(devMsg, logMsg);

            if (isTimeOut("prepareShow")) {
                return;
            }
//            if (!supportPara) {
//                if (hasOrderRun) {
//                    LogUtil.high("已串行执行过");
//                    return;
//                }
//                //如果当前不支持并行，自动改用串行的加载方式
//                LogUtil.high("当前不支持并行，自动转串行");
//                isParallel = false;
//                if (null != baseSetting) {
//                    baseSetting.paraEvent(AdvanceConstant.EVENT_TYPE_ORDER, null, sdkSupplier);
//                }
//                reportLoaded();
//                orderLoadAd();
//                hasOrderRun = true;
//                return;
//            }
            //加载成功了，需要回调loaded信息
            if (adStatus == AdvanceConstant.AD_STATUS_LOAD_SUCCESS) {
                LogUtil.simple(TAG + "加载成功，回调成功信息");
                callbackLoad();
                adStatus = AdvanceConstant.AD_STATUS_LOADED_SHOW;
            } else if (adStatus == AdvanceConstant.AD_STATUS_LOADING) {
                LogUtil.high(TAG + "广告请求中，成功后自动回调");
                adStatus = AdvanceConstant.AD_STATUS_LOADING_SHOW;
            } else if (adStatus == AdvanceConstant.AD_STATUS_DEFAULT) {
                LogUtil.high(TAG + "广告未调用，立即调用，成功后自动回调");
                //先置位再 load：load() 内部在「SDK 已初始化成功 + 缓存命中」时是一条同步回调链
                //（callInitSuccess → callADNLoad → handleSucceed → paraEvent → prepareShowAd → callLoaded），
                //会绕回本方法。若置位晚于 load()，重入时状态仍是 DEFAULT，将形成同步递归。
                adStatus = AdvanceConstant.AD_STATUS_LOADING_SHOW;
                load();
            } else if (adStatus == AdvanceConstant.AD_STATUS_LOAD_FAILED) {

                if (adNum > 0) {//广告加载
                    adStatus = AdvanceConstant.AD_STATUS_LOADING_SHOW;
                    LogUtil.max(TAG + "when AD_STATUS_LOAD_FAILED case to AD_STATUS_LOADING_SHOW : cause adNum = " + adNum + " still has ad to show");

                } else {
                    String errCode = advanceError == null ? "" : advanceError.code;
                    LogUtil.high(TAG + "广告请求失败,errCode=" + errCode);
                    doFailed();
                }
            } else {
                if (isCurrentParaEarlyType()) {
                    LogUtil.high(TAG + "并行取早下，不作处理的load");
//                } else if (isBidding()) {
//                    LogUtil.high(TAG + "含bidding时执行");
//                    doFailed();
                } else {
                    LogUtil.high(TAG + " 不作处理的load");
                }
            }
            //如果是缓存成功的话，进行对应事件的传递
            if (cacheStatus == AdvanceConstant.STATUS_CACHED) {
                cacheEvent();
            } else if (cacheStatus == AdvanceConstant.STATUS_UNCACHED) {
                cacheStatus = AdvanceConstant.STATUS_CACHED_CALL;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD, "BaseParallelAdapter load Throwable"));
//            //展示异常回调
//            if (null != baseSetting) {
//                baseSetting.adapterDidFailed(AdvanceError.parseErr(ERROR_EXCEPTION_LOAD, "BaseParallelAdapter load Throwable"));
//            }
        }
    }

    //聚合策略层调用的show方法
    protected void show() {
        if (!isSuccess) {
            LogUtil.e(TAG + "广告未成功返回，无法show");
            return;
        }
        //若广告无效，直接回调失败
        if (!isValid()) {
            runParaFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_SHOW, "BaseParallelAdapter show ad invalid"));
            return;
        }

        showAd(getRealActivity(), getLocalExtra(), getServerExtra());
    }


    private void doFailed() {
        try {
            //避免重复执行失败任务
            checkFailed();

            if (null != baseSetting) {
                baseSetting.paraEvent(AdvanceConstant.EVENT_TYPE_ERROR, advanceError, sdkSupplier);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void doBannerFailed(AdvanceError advanceError) {
        if (isBannerFailed()) {
            runParaFailed(advanceError);
            //广告失败并进行销毁
            destroyAd();
        }
    }

    //广告是否算失败，因为有刷新的逻辑，如果刷新中失败，是可以按照不失败继续执行刷新的
    private boolean isBannerFailed() {
        //如果未在展示中，失败了需要进行销毁，否则会在后台自动进行请求
        boolean isRunning = true;
        if (sdkSupplier != null) {
            int pri = sdkSupplier.priority;
            if (baseSetting != null && baseSetting.getCurrentSupplier() != null) {
                int curPri = baseSetting.getCurrentSupplier().priority;
                LogUtil.high("curPri = " + curPri + " pri = " + pri);
                isRunning = curPri == pri;
            }
        }
        LogUtil.high("refreshing = " + refreshing + " isRunning = " + isRunning);

        if (refreshing && isRunning) {
            LogUtil.high("等待刷新中，即使失败也不进行销毁操作");
            return false;
        }
        LogUtil.simple("广告失败，进行销毁操作");
        return true;
    }


    /**
     * 仅当调用了广告加载方法以后才会执行cache回调，但是无法得知当前回调的是哪一个平台
     */
    private void cacheEvent() {
        if (null != baseSetting) {
            String msg = "";
            if (sdkSupplier != null) {
                msg = sdkSupplier.id;
            }
            baseSetting.paraEvent(AdvanceConstant.EVENT_TYPE_CACHED, new AdvanceError(AdvanceConstant.TAG_PARA_CACHED, msg), sdkSupplier);
        }
    }

    @Deprecated
    protected void doFailed(String TAG, int code, String message) {
        doFailed(TAG, code + "", message);
    }

    @Deprecated
    protected void doFailed(String TAG, String code, String message) {
        LogUtil.e(TAG + code + message);

        AdvanceError error = AdvanceError.parseErr(code, message);

        runParaFailed(error);
    }

    private void switchReport(ArrayList<String> tk) {
        LogUtil.devDebug(TAG + "switchReport");

        boolean needDelay = false;
//        if (baseSetting != null) {
//            boolean isReportDelay = baseSetting.needDelayReport();
//            ArrayList<ArrayList<String>> savedDelayReportList = baseSetting.getSavedReportUrls();
//            needDelay = isReportDelay && savedDelayReportList != null;
//
//            if (needDelay) {
//                savedDelayReportList.add(tk);
//            }
//        }
        if (!needDelay) {
            AdvanceReport.reportToUrls(tk);
        }
    }

    public boolean canOptInit() {
        boolean result = true;
        try {
            if (sdkSupplier != null) {
                result = sdkSupplier.initOpt == 1;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取策略中配置的媒体id。
     */
    public String getAppID() {
        String appID = "";
        if (sdkSupplier != null) {
            appID = sdkSupplier.mediaid;
        }
        return appID;
    }

    /**
     * 获取策略中配置的媒体id。
     */
    public String getAppKey() {
        String result = "";
        if (sdkSupplier != null) {
            result = sdkSupplier.mediakey;
        }
        return result;
    }

    /**
     * 获取策略中配置的广告位id。
     */
    public String getPosID() {
        String posID = "";
        if (sdkSupplier != null) {
            posID = sdkSupplier.adspotid;
        }
        return posID;
    }


    /**
     * --------- 以下是公共处理核心回调事件方法  ----------
     */


    public void handleClick() {
        try {
            if (baseSetting == null) {
                return;
            }
            LogUtil.simple(TAG + "handleClick");
            if (baseSetting instanceof BaseAdEventListener) {
                BaseAdEventListener setting = (BaseAdEventListener) baseSetting;
                setting.adapterDidClicked(sdkSupplier);
            } else if (baseSetting instanceof RewardVideoSetting) {
                RewardVideoSetting setting = (RewardVideoSetting) baseSetting;
                setting.adapterDidClicked(sdkSupplier);
            } else if (baseSetting instanceof FullScreenVideoSetting) {
                FullScreenVideoSetting setting = (FullScreenVideoSetting) baseSetting;
                setting.adapterDidClicked(sdkSupplier);
            } else if (baseSetting instanceof NativeExpressSetting) {
                NativeExpressSetting setting = (NativeExpressSetting) baseSetting;
                setting.adapterDidClicked(nativeExpressADView, sdkSupplier);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void handleShow() {
        try {
            //曝光后统一进行缓存移除操作了
            AdvanceCacheUtil.removeCache(sdkSupplier);
            //曝光后预加载：渠道开启 enableCache 时立即再请求一次广告并缓存，供下次策略复用
            AdvanceCacheUtil.preloadAfterShow(this);

            if (baseSetting == null) {
                return;
            }
            LogUtil.simple(TAG + "handleShow");
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    if (baseSetting instanceof BaseAdEventListener) {
                        BaseAdEventListener setting = (BaseAdEventListener) baseSetting;
                        setting.adapterDidShow(sdkSupplier);
                    } else if (baseSetting instanceof RewardVideoSetting) {
                        RewardVideoSetting setting = (RewardVideoSetting) baseSetting;
                        setting.adapterDidShow(sdkSupplier);
                    } else if (baseSetting instanceof FullScreenVideoSetting) {
                        FullScreenVideoSetting setting = (FullScreenVideoSetting) baseSetting;
                        setting.adapterDidShow(sdkSupplier);
                    } else if (baseSetting instanceof NativeExpressSetting) {
                        NativeExpressSetting setting = (NativeExpressSetting) baseSetting;
                        setting.adapterDidShow(nativeExpressADView, sdkSupplier);
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public void handleSucceed(AdvanceRFADData dataConverter,double price) {
        this.dataConverter = dataConverter;
        handleSucceed(price);
    }

    //统一处理广告成功，并传入实时获取到的广告价格
    public void handleSucceed(double price) {
        //尝试更新bidding价格
        updateBidding(price);

        handleSucceed();
    }

    public void handleSucceed() {
        try {
            //执行缓存
            AdvanceCacheUtil.cacheSDK(this);

            isSuccess = true;
            //预加载完成：渠道广告对象已随 adapter 进入缓存，等待下次策略复用；
            //无会话绑定，不执行任何 baseSetting 回调与上报
            if (isPreload) {
                LogUtil.devDebug(TAG + "[Preload] 预加载完成并已缓存");
                AdvanceCacheUtil.onPreloadFinished(sdkSupplier);
                return;
            }
            if (baseSetting == null) {
                return;
            }
            LogUtil.simple(TAG + "handleSucceed");
            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onSucceed();
                }
            } else {
                callbackLoad();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            runBaseFailed(AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD));
        }
    }


    public void handleFailed(int errCode, String errMsg) {
        handleFailed(errCode + "", errMsg);
    }

    public void handleFailed(String errCode, String errMsg) {
        try {
            AdvanceError error = AdvanceError.parseErr(errCode, errMsg);
            runParaFailed(error);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //当为第一优先级，并且bidding配置了的时候
//    protected boolean isBidding() {
//        boolean result = false;
//        try {
//            if (sdkSupplier != null) {
//                result = baseSetting.getBiddingResultInf().isCurrentFirstGroup && baseSetting.getBiddingResultInf().containsBidding;
//            }
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        return result;
//    }
//
    protected boolean isCurrentSupBidding() {
        boolean result = false;
        try {
            if (sdkSupplier != null) {
                result = sdkSupplier.useBidding();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    //对支持bidding的渠道进行比价逻辑
    public void updateBidding(double price) {
        try {
            LogUtil.devDebug(TAG + "result price = " + price + ", need update bidding price : " + sdkSupplier.enableBidding);
            if (sdkSupplier.enableBidding) {//如果是竞价开启状态，才会执行
                if (price > 0) {
                    //记录渠道原始价（未乘系数），作为缓存复用时重算展示价的幂等锚点，
                    //避免把「已乘系数的展示价」再次乘系数（bidRatio 双乘）
                    sdkSupplier.channelPrice = price;
                    double bidResultPrice = price * sdkSupplier.bidRatio;
                    sdkSupplier.price = bidResultPrice;
                    sdkSupplier.bidResultPrice = bidResultPrice;
                    LogUtil.devDebug(TAG + "updateBidding bidResultPrice = " + bidResultPrice + " sdkSupplier.priority = " + sdkSupplier.priority);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 上报crash发生时的异常信息（仅开屏使用）
     *
     * @param msg 异常log信息
     */
    public void reportCodeErr(String msg) {
        try {
            if (baseSetting != null) {
                AdvanceReportModel report = new AdvanceReportModel();
                report.code = AdvanceConstant.TRACE_SPLASH_ERROR;
                report.msg = msg;
                //开屏需要延迟进行上报
                report.needDelay = true;
                baseSetting.trackReport(report);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //大部分adn可能并不支持所有广告位可用性检查api，所以默认广告成功后即有效
//    @Override
//    public boolean isValid() {
//        return isSuccess;
//    }

    /**
     * --------- 以上是公共处理核心回调事件方法  ----------
     */


    public interface NativeParallelListener {
        void onCached();

        void onSucceed();

        void onFailed(AdvanceError error);
    }
}
