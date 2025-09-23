package com.advance;

import static com.advance.model.AdvanceError.ERROR_EXCEPTION_LOAD;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.advance.core.srender.AdvanceRFADData;
import com.advance.core.srender.AdvanceRFBridge;
import com.advance.net.AdvanceReport;
import com.advance.utils.ActivityTracker;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.itf.RenderEvent;
import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;
import com.advance.model.SdkSupplier;
import com.advance.model.SupplierSettingModel;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bayes.sdk.basic.util.BYUtil;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseParallelAdapter implements AdvanceBaseAdapter, ParaAdapterListener, DestroyListener, RenderEvent {
    public String TAG = "[" + this.getClass().getSimpleName() + "] ";

    protected Activity activity;
    protected Context mContext;
    protected SoftReference<Activity> softReferenceActivity;

    public BaseSetting baseSetting;
    public SdkSupplier sdkSupplier;
    //是否为异步请求
    public boolean isParallel = false;
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

    //回调所需参数
    public View nativeExpressADView;
    public AdvanceRewardVideoItem rewardVideoItem;
    public AdvanceFullScreenItem fullScreenItem;
    public List<AdvanceNativeExpressAdItem> nativeExpressAdItemList;
    //获取聚合具体设置项
    public AdvanceRFBridge mAdvanceRFBridge;
    //   基础数据信息
    public AdvanceRFADData dataConverter;

//    public HashMap<Integer, ParaStatusModel> statusMap = new HashMap<>();

    protected int adNum = 0;//记录广告数量，每load一次+1.每失败一次-1.如果此值为负，那么可能为回调了多次的失败回调，需抛弃处理此次回调。


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
    public Activity getRealActivity(View adContainerView) {
        Activity result = null;
        try {
            if (adContainerView != null) {
                result = AdvanceUtil.getActivityFromView(adContainerView);
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
            if (result == null) {
                result = BYUtil.getCtx();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    public BaseParallelAdapter(SoftReference<Activity> softReferenceActivity, final BaseSetting baseSetting) {
        this.softReferenceActivity = softReferenceActivity;
        this.baseSetting = baseSetting;
        initPara();
    }

    public BaseParallelAdapter(Activity activity, final BaseSetting baseSetting) {
        this.activity = activity;
        this.baseSetting = baseSetting;
        initPara();
    }

    public BaseParallelAdapter(Context context, final BaseSetting baseSetting) {
        this.mContext = context;
        this.baseSetting = baseSetting;
        initPara();
    }

    private void initPara() {
        adStatus = AdvanceConstant.AD_STATUS_DEFAULT;
        try {
            isDestroy = false;
            parallelListener = new NativeParallelListener() {
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
                        //如果是等待中发起了展示需求，直接展示，否则标记为1 成功获得广告
                        if (adStatus == AdvanceConstant.AD_STATUS_LOADING_SHOW) {
                            adStatus = AdvanceConstant.AD_STATUS_LOADED_SHOW;
                            doMainLoad();
                        } else {
                            adStatus = AdvanceConstant.AD_STATUS_LOAD_SUCCESS;
                        }
                    }


                    //并行上报广告加载成功
                    if (sdkSupplier != null) {
                        String reqid = baseSetting == null ? "" : baseSetting.getAdvanceId();
                        ArrayList<String> succTk;
                        //有返回bid价格信息时，进行上报拼接
                        if (isCurrentSupBidding() && sdkSupplier.bidResultPrice > 0) {
                            succTk = AdvanceReport.getReplacedBidding(sdkSupplier.succeedtk, reqid, sdkSupplier.bidResultPrice);
                        } else {
                            succTk = AdvanceReport.getReplacedTime(sdkSupplier.succeedtk, reqid);
                        }
                        switchReport(succTk);
                    }
                }

                @Override
                public void onFailed(AdvanceError error) {
                    advanceError = error;
                    loadFailed();

                }
            };
        } catch (Throwable e) {
            e.printStackTrace();
            advanceError = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD);
            loadFailed();
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

    /**
     * 并行请求广告。并行请求拿到广告信息后，会存储结果状态。
     *
     * @see AdvanceBaseAdapter#orderLoadAd() 串行请求广告方法
     */
    protected abstract void paraLoadAd();

    //广告就绪，可以进行后续广告展示方法，串行or并行均会执行到此方法，区别是串行是广告加载成功后立即执行到此方法，并行时广告成功，也要等到选中改广告才执行此方法。
    protected abstract void adReady();

    //销毁广告
    public abstract void doDestroy();

    public void destroy() {
        try {
            isDestroy = true;
            adStatus = AdvanceConstant.AD_STATUS_DEFAULT;
            parallelListener = null;
            doDestroy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void startOrderLoad() {
        ++adNum;
        isParallel = false;
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                orderLoadAd();
            }
        });
    }

    public void load() {
        try {
            ++adNum;
            isParallel = true;
            adStatus = AdvanceConstant.AD_STATUS_LOADING;
            reportLoaded();
//            根据设置，选择不进入主线程load
            if (baseSetting != null && baseSetting.isLoadAsync()) {
                paraLoadAd();
                return;
            }
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    paraLoadAd();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            advanceError = AdvanceError.parseErr(AdvanceError.ERROR_EXCEPTION_LOAD, "BaseParallelAdapter load Throwable");
            reportFailed();
            //标记为失败
            adStatus = AdvanceConstant.AD_STATUS_LOAD_FAILED;
        }
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
    private void doMainLoad() {
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
                    if (rewardVideoItem == null) {
                        LogUtil.devDebug("未定义 rewardVideoItem，需要在调用 handleSucceed() 方法前赋值为基于 AdvanceRewardVideoItem 的广告渲染处理类");
                    }
                    setting.adapterAdDidLoaded(rewardVideoItem, sdkSupplier);
                } else if (baseSetting instanceof FullScreenVideoSetting) {
                    FullScreenVideoSetting setting = (FullScreenVideoSetting) baseSetting;
                    if (fullScreenItem == null) {
                        LogUtil.devDebug("未定义 fullScreenItem，需要在调用 handleSucceed() 方法前赋值为基于 AdvanceFullScreenItem 的广告渲染处理类");
                    }
                    setting.adapterAdDidLoaded(fullScreenItem, sdkSupplier);
                } else if (baseSetting instanceof NativeExpressSetting) {
                    NativeExpressSetting setting = (NativeExpressSetting) baseSetting;
                    if (nativeExpressAdItemList == null) {
                        LogUtil.devDebug("未定义 nativeExpressAdItemList，需要在调用 handleSucceed() 方法前赋值为基于 AdvanceNativeExpressAdItem 的广告渲染处理类列表");
                    }
                    setting.adapterAdDidLoaded(nativeExpressAdItemList, sdkSupplier);
                }
                adReady();
            }
        });
    }

    private void reportLoaded() {
        try {
            if (sdkSupplier != null) {
                String reqid = baseSetting == null ? "" : baseSetting.getAdvanceId();
                long reqTime = baseSetting == null ? 0 : baseSetting.getRequestTime();
                ArrayList<String> ltk = baseSetting == null ? AdvanceReport.getReplacedTime(sdkSupplier.loadedtk, reqid) : AdvanceReport.getReplacedLoaded(sdkSupplier.loadedtk, reqTime, reqid);
                switchReport(ltk);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void reportFailed() {
        try {
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

    /**
     * 广告展示方法，串并行均可调用，不支持并行时自动转为串行加载
     */
    public void prepareShow() {
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
            if (!supportPara) {
                if (hasOrderRun) {
                    LogUtil.high("已串行执行过");
                    return;
                }
                //如果当前不支持并行，自动改用串行的加载方式
                LogUtil.high("当前不支持并行，自动转串行");
                isParallel = false;
                if (null != baseSetting) {
                    baseSetting.paraEvent(AdvanceConstant.EVENT_TYPE_ORDER, null, sdkSupplier);
                }
                reportLoaded();
                orderLoadAd();
                hasOrderRun = true;
                return;
            }
            //加载成功了，需要回调loaded信息
            if (adStatus == AdvanceConstant.AD_STATUS_LOAD_SUCCESS) {
                LogUtil.simple(TAG + "加载成功，回调成功信息");
                doMainLoad();
                adStatus = AdvanceConstant.AD_STATUS_LOADED_SHOW;
            } else if (adStatus == AdvanceConstant.AD_STATUS_LOADING) {
                LogUtil.high(TAG + "广告请求中，成功后自动回调");
                adStatus = AdvanceConstant.AD_STATUS_LOADING_SHOW;
            } else if (adStatus == AdvanceConstant.AD_STATUS_DEFAULT) {
                LogUtil.high(TAG + "广告未调用，立即调用，成功后自动回调");
                load();
                adStatus = AdvanceConstant.AD_STATUS_LOADING_SHOW;
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
            doDestroy();
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
        if (baseSetting != null) {
            boolean isReportDelay = baseSetting.needDelayReport();
            ArrayList<ArrayList<String>> savedDelayReportList = baseSetting.getSavedReportUrls();
            needDelay = isReportDelay && savedDelayReportList != null;

            if (needDelay) {
                savedDelayReportList.add(tk);
            }
        }
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


    public void handleSucceed() {
        try {
            isSuccess = true;
            if (baseSetting == null) {
                return;
            }
            LogUtil.simple(TAG + "handleSucceed");
            if (isParallel) {
                if (parallelListener != null) {
                    parallelListener.onSucceed();
                }
            } else {
                doMainLoad();
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
    protected void updateBidding(double price) {
        try {
            LogUtil.devDebug(TAG + "result price = " + price + ", need update bidding price : " + sdkSupplier.enableBidding);
            if (sdkSupplier.enableBidding) {//如果是竞价开启状态，才会执行
                if (price > 0) {
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
    @Override
    public boolean isValid() {
        return isSuccess;
    }

    /**
     * --------- 以上是公共处理核心回调事件方法  ----------
     */


    public interface NativeParallelListener {
        void onCached();

        void onSucceed();

        void onFailed(AdvanceError error);
    }
}
