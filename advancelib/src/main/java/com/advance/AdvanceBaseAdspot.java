package com.advance;

import static com.advance.utils.AdvanceLoader.BASE_ADAPTER_PKG_PATH;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.advance.core.common.AdvanceErrListener;
import com.advance.custom.AdvanceBaseCustomAdapter;
import com.advance.itf.AdvanceLifecycleCallback;
import com.advance.net.AdvanceNetManger;
import com.advance.net.AdvanceReport;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.advance.itf.BaseGMCallBackListener;
import com.advance.itf.StrategyListener;
import com.advance.itf.RenderEvent;
import com.advance.model.AdStatus;
import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;
import com.advance.model.AdvanceReqModel;
import com.advance.model.BiddingInf;
import com.advance.model.ElevenModel;
import com.advance.model.GMParams;
import com.advance.model.SdkParaGroup;
import com.advance.model.SdkSupplier;
import com.advance.model.StrategyReadyInf;
import com.advance.model.SupplierSettingModel;
import com.advance.model.ValueDataModel;
import com.advance.utils.AdvanceUtil;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYAbsCallBack;
import com.bayes.sdk.basic.util.BYCacheUtil;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYThreadPoolUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bayes.sdk.basic.util.BYUtil;

import org.json.JSONObject;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public abstract class AdvanceBaseAdspot implements BaseSetting, RenderEvent {
    private Activity activity;
    protected Context mContext;
    protected SoftReference<Activity> softReferenceActivity;
    protected AdvanceSelectListener advanceSelectListener;
    public AdvanceError adError;

    AdvanceUtil advanceUtil;
    private boolean isUseCache = false;
    private boolean isCacheEffect = false; //策略缓存生效
    boolean needDelay = false;
    //    boolean needShow = false;//当用户提前调用了show方法，依然可以收到广告后执行show
    private int parallelTimeout = AdvanceConstant.DEFAULT_PARA_TIMEOUT;//并行层的超时时长，单位毫秒，根据后端下发值会改变
    private String reqId = ""; //请求唯一id
    long requestTime;
    protected String advanceAdspotId;

    protected ElevenModel mElevenModel;//策略信息的model
    //渠道并行组列表
    protected ArrayList<SdkParaGroup> supplierGroups;
    //bidding 组
    protected SdkParaGroup biddingGroup;
    protected SdkParaGroup currentGroupInf;

    protected SdkSupplier currentSdkSupplier;
    protected SupplierSettingModel.ParaGroupSetting currentParaGroupSetting;
    ArrayList<ArrayList<Integer>> paraPriGroup = new ArrayList<>(); //并行执行组的列表
    protected AdvanceError advanceError;
    //渠道adapter集合，初始化时传入，后续渠道选择时从map中选取
    HashMap<String, BaseParallelAdapter> supplierAdapters = new HashMap<>();
    //存放支持的渠道信息，key 为sdkid；value为类名信息
    HashMap<String, String> supportAdapterInf = new HashMap<>();
    //    自定义的adapter列表
    HashMap<String, String> customAdapterInf = new HashMap<>();
    private HashMap<Integer, Boolean> paraInitStatus = new HashMap<>();
    private ArrayList<ArrayList<String>> savedDelayReportList = new ArrayList<>();
    private boolean isReportDelay = false;//是否进行延迟上报，上报时机为：广告展示以后或者渠道全部失败
    private static String BTAG = "[AdvanceBaseAdspot] ";
    Application.ActivityLifecycleCallbacks alcb;
    protected AdvanceLifecycleCallback ownLifecycleCallback;//内部(splash)使用的回调
    private AdvanceLifecycleCallback advanceLifecycleCallback;//给外部（admore）调用的回调
    boolean fromActivityDestroy = false;
    String currentSDKId = "";

    boolean hasResult = false;
    boolean hasDelay = false;

    //bidding的最终结果价格信息
    private BiddingInf biddingPriceResult = new BiddingInf();

    //超时相关，有bidding层和普通层
    private Handler timeOutHandler; //主要是当前并行层的超时逻辑
    private Runnable timeoutRun; //主要是当前并行层的超时逻辑

    private Handler bidTimeOutHandler;//bidding层超时
    private Runnable bidTimeoutRun;//bidding层超时

    protected boolean loadAndShow = false;
    //兼容开屏\banner旧接口定义，加载广告成功后不需要单独调用展示方法
    protected boolean needForceLoadAndShow = false;

    //bid聚合相关--
    protected StrategyListener strategyListener;
    protected BaseGMCallBackListener baseGMCall;

    public boolean bidWin = false;//记录gromore中胜出时的标志
    public boolean gmStart = false;//记录gromore中开始时的标志
    public AdStatus adStatus = AdStatus.DEFAULT;//记录广告执行状态
    //bid聚合相关--

    protected boolean isSplitLoad = false;
    //    AdvanceLoadListener mLoadListener;
//    AdvanceSuccessData mSuccessData;
    protected boolean isSplash = false;

    //是否在子线程进行load，默认false，因为快手SDK不支持子线程调用load。
    protected boolean loadWithAsync = false;
    protected boolean isReard = false;

    public AdvanceBaseAdspot(Activity activity, String mediaId, String adspotId) {
        try {
            this.activity = activity;
            this.advanceAdspotId = adspotId;
            advanceUtil = new AdvanceUtil(activity);
            initLifecycle();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public AdvanceBaseAdspot(SoftReference<Activity> activity, String mediaId, String adspotId) {
        try {
            this.softReferenceActivity = activity;
//            this.advanceMediaId = mediaId;
            this.advanceAdspotId = adspotId;
            advanceUtil = new AdvanceUtil(activity.get());
            initLifecycle();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //基础的初始化方式，分离加载模式的一种，会传递context信息，可以从context中尝试获取activity信息
    public AdvanceBaseAdspot(Context context, String adspotId) {
        this.advanceAdspotId = adspotId;
        if (context != null) {
            this.mContext = context;
            //如果获取到activity信息，进行更新
            Activity activity = AdvanceUtil.getActivityFromCtx(context);
            if (activity != null) {
                updateADActivity(activity);
                //进行生命周期检测的初始化
                initLifecycle();
            }
        }
        isSplitLoad = true;
    }

    //最基础的初始化方式，后续如需要，context从基础库中取，如需要activity信息，从对应暴露得方法或者view中取
    public AdvanceBaseAdspot(String adspotId) {
        this.advanceAdspotId = adspotId;
        isSplitLoad = true;
    }

//
//    public void setLoadListener(AdvanceLoadListener loadListener){
//        mLoadListener = loadListener;
//    }

    //对于非activity中进行的广告构造，需要在展现时进行单独的init调用
    protected void reInit(View v) {
        try {
            activity = AdvanceUtil.getActivityFromView(v);
            initLifecycle();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void initLifecycle() {
        try {
            alcb = new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

                }

                @Override
                public void onActivityStarted(@NonNull Activity activity) {

                }

                @Override
                public void onActivityResumed(@NonNull Activity mActivity) {
                    LogUtil.max(BTAG + "---onActivityResumed activity = " + mActivity);
                    if (getADActivity() == mActivity) {
                        if (advanceLifecycleCallback != null) {
                            advanceLifecycleCallback.onActivityResumed();
                        }
                        if (ownLifecycleCallback != null) {
                            ownLifecycleCallback.onActivityResumed();
                        }
                    }
                }

                @Override
                public void onActivityPaused(@NonNull Activity mActivity) {
                    LogUtil.max(BTAG + "---onActivityPaused activity = " + mActivity);
                    if (getADActivity() == mActivity) {
                        if (advanceLifecycleCallback != null) {
                            advanceLifecycleCallback.onActivityPaused();
                        }
                        if (ownLifecycleCallback != null) {
                            ownLifecycleCallback.onActivityPaused();
                        }
                    }
                }

                @Override
                public void onActivityStopped(@NonNull Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(@NonNull Activity mActivity) {
                    LogUtil.max(BTAG + "---onActivityDestroyed activity = " + mActivity);

                    if (getADActivity() == mActivity) {
                        if (advanceLifecycleCallback != null) {
                            advanceLifecycleCallback.onActivityDestroyed();
                        }
                        if (ownLifecycleCallback != null) {
                            ownLifecycleCallback.onActivityDestroyed();
                        }
                        fromActivityDestroy = true;
                        destroy();
                    }
                }
            };

            getADActivity().getApplication().unregisterActivityLifecycleCallbacks(alcb);
            getADActivity().getApplication().registerActivityLifecycleCallbacks(alcb);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 以下为BaseSetting 相关接口方法
     */
    @Override
    public boolean needDelayReport() {
        return isReportDelay;
    }

    @Override
    public void adapterDidFailed(AdvanceError advanceError, SdkSupplier sdkSupplier) {
        reportAdFailed(advanceError, sdkSupplier);
        handleFailed(advanceError, sdkSupplier);
    }

    @Override
    public void adapterDidFailed(AdvanceError advanceError) {
        adapterDidFailed(advanceError, currentSdkSupplier);
    }

    @Override
    public String getAdvanceId() {
        return reqId;
    }

    @Override
    public void trackReport(AdvanceReportModel trackModel) {
        doTrackReport(trackModel);
    }

    @Override
    public Long getRequestTime() {
        return requestTime;
    }

    @Override
    public boolean isLoadAsync() {
        return loadWithAsync;
    }

    @Override
    public void paraEvent(int type, AdvanceError advanceError, SdkSupplier sdkSupplier) {
        String adId = "";
        if (sdkSupplier != null) {
            adId = sdkSupplier.adspotid + "(" + sdkSupplier.priority + ")";
        }
        LogUtil.max("[AdvanceBaseAdspot] paraEvent: type = " + type + ", adId = " + adId);
        switch (type) {
            case AdvanceConstant.EVENT_TYPE_ORDER:
                //转串行也要进行组执行状态的更新
                if (sdkSupplier != null) {
                    sdkSupplier.resultStatus = AdvanceConstant.SDK_RESULT_CODE_ORDER;
                    updateGroupResultInf(sdkSupplier);
                }
                break;
            case AdvanceConstant.EVENT_TYPE_ERROR:
                handleFailed(advanceError, sdkSupplier);
                break;
            case AdvanceConstant.EVENT_TYPE_SUCCEED:
                handlerSucc(sdkSupplier);
                break;
        }
    }

    @Override
    public SdkSupplier getCurrentSupplier() {
        return currentSdkSupplier;
    }

    @Override
    public ArrayList<ArrayList<String>> getSavedReportUrls() {
        return savedDelayReportList;
    }


    @Override
    public SupplierSettingModel.ParaGroupSetting getCurrentParaGroupSetting() {
        return currentParaGroupSetting;
    }


    @Override
    public BiddingInf getBiddingResultInf() {
        return biddingPriceResult;
    }


    //当前是否超时，如果超时，需要进行后续流程中断操作 是否直接传递 SdkParaGroup 信息更好？？？
//    public boolean isCurrentGroupTimeOut() {
//        boolean result = false;
//        if (currentGroupInf != null) {
//            result = currentGroupInf.isTimeOut;
//        }
//        return result;
//    }


    /**
     * BaseSetting 相关接口方法 ---end
     */

    public void setAdvanceLifecycleCallback(AdvanceLifecycleCallback callback) {
        advanceLifecycleCallback = callback;
    }

    //获取ecpm 价格，一定要在广告返回回调以后调用，才会有值，否则为0
    public double getEcpm() {
        double result = 0;
        try {
            if (currentGroupInf != null && currentGroupInf.bestSupplier != null) {
                result = currentGroupInf.bestSupplier.price;
            }
//            else {
//                //仅有bidding时，返回bidding的最高ecpm信息
//                if (!isBiddingEmpty()) {
//                    result = biddingGroup.bestBiddingSupplier.price;
//                }
//            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    //设置策略回调信息
    public void setStrategyListener(StrategyListener strategyListener) {
        this.strategyListener = strategyListener;
    }

    protected void setBaseGMCall(BaseGMCallBackListener baseGMCall) {
        this.baseGMCall = baseGMCall;
    }


    private boolean isCurrentBestSupplier(SdkSupplier sdkSupplier) {
        return currentGroupInf.bestSupplier != null && currentGroupInf.bestSupplier == sdkSupplier;
    }

    //是否可以继续执行下一步，超时相关因素导致的执行回调事件
    protected boolean canNextStep(SdkSupplier sdkSupplier) {
        boolean result = true;
        try {
            boolean currentNotBest = !isCurrentBestSupplier(sdkSupplier);
//            if (sdkSupplier.useBidding()) {
            if (sdkSupplier.enableBidding) {
                LogUtil.devDebug(BTAG + "enable bidding sdkSupplier");
                if (sdkSupplier.isSupportBidding()) {
                    //如果渲染失败，当前最优依然可以进行下一步，非当前最优得需要看是否已超时。超时了就无法执行下一步
                    if (currentNotBest) {
                        LogUtil.devDebug(BTAG + "not best sdkSupplier，  biddingGroup.isTimeOut =" + biddingGroup.isTimeOut);
                        result = !biddingGroup.isTimeOut;
                    }
                } else {
                    //23-06-26新增，走到这里代表，此渠道后台下发了bidding标志，但是adapter内未支持该渠道，那么需要按照普通的并行组考虑
                    if (currentNotBest) {
                        LogUtil.devDebug(BTAG + "not best sdkSupplier，  currentGroupInf.isTimeOut =" + currentGroupInf.isTimeOut);
                        result = !currentGroupInf.isTimeOut;
                    }
                }
            } else {
                //异步回调渠道和当前执行不在同组
                if (currentGroupInf.groupID != sdkSupplier.groupID) {
                    LogUtil.devDebug(BTAG + "not bidding sdkSupplier，different groupID ，currentGroupInf.groupID= " + currentGroupInf.groupID + "，sdkSupplier.groupID = " + sdkSupplier.groupID);
                    result = false;
                } else {
                    if (currentNotBest) {
                        LogUtil.devDebug(BTAG + "not best sdkSupplier，  currentGroupInf.isTimeOut =" + currentGroupInf.isTimeOut);
                        result = !currentGroupInf.isTimeOut;
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        LogUtil.simple(BTAG + " canNextStep check : " + result);
        return result;

    }

    //主要是更新当前并行组的执行结果、价格等
    private void updateGroupResultInf(SdkSupplier sdkSupplier) {
        try {
            //更新渠道信息
            if (currentGroupInf != null && sdkSupplier != null) {
                boolean allResult = true;
//                boolean allBidResult = true;
                ArrayList<SdkSupplier> paraSupplierMembers = currentGroupInf.paraSupplierMembers;
                if (paraSupplierMembers.size() > 0) {
                    for (int i = 0; i < paraSupplierMembers.size(); i++) {
                        SdkSupplier item = paraSupplierMembers.get(i);
                        if (item.priority == sdkSupplier.priority) {//替换渠道信息
                            paraSupplierMembers.set(i, sdkSupplier);
                        }
                        allResult = allResult && item.resultStatus > 0;
//                        if (item.useBidding()) {
//                            allBidResult = allBidResult && item.resultStatus > 0;
//                        }
                    }
                }
//                currentGroupInf.isBiddingAllResult = allBidResult;

                currentGroupInf.isGroupAllResult = allResult;

            }

            //检查bid组执行情况
            if (!isBiddingEmpty()) {
                boolean allBidResult = true;
                for (SdkSupplier bidItem : biddingGroup.paraSupplierMembers) {
                    allBidResult = allBidResult && bidItem.resultStatus > 0;
                    //更新最高价格
                    if (bidItem.resultStatus == AdvanceConstant.SDK_RESULT_CODE_SUCC) {
//                        biddingGroup.successBiddingSuppliers.add(bidItem);
//                        if (bidItem.price > biddingGroup.maxPrice) {
//                            LogUtil.devDebug(BTAG + "update maxPrice:" + bidItem.price);
//                            biddingGroup.maxPrice = bidItem.price;
//                        }
                    }
                }
                if (allBidResult) {
                    // TODO: 2022/8/26 如果此时并行还未执行结束，并无法进行bidding比价，浪费了时间。同时bid timeout时有一样得问题
                    LogUtil.devDebug(BTAG + "allBid hasResult");
//                    if (isParaGroupEmpty()) {
//                        handleOnlyBidding();
//                    }
                }
                biddingGroup.isBiddingAllResult = allBidResult;

            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 检查bidding组是否为空，运行前和执行中
     *
     * @return true代表空bidding，可忽略bidding逻辑
     */
    private boolean isBiddingEmpty() {
        try {
            String vTag = "(checkBiddingEmpty)";
            if (biddingGroup == null) {
                LogUtil.devDebug(BTAG + vTag + "biddingGroup null");
                return true;
            }
            if (biddingGroup.paraSupplierMembers == null || biddingGroup.paraSupplierMembers.size() == 0) {
                LogUtil.devDebug(BTAG + vTag + "biddingSuppliers empty");
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isValid() {
        try {
            if (TextUtils.isEmpty(currentSDKId)) {
                LogUtil.e(BTAG + "-isValid- 未选中任何SDK");
                return false;
            }
            if (supplierAdapters == null || supplierAdapters.size() == 0) {
                LogUtil.e(BTAG + "-isValid- 无可用渠道");
                return false;
            }
            if (currentSdkSupplier == null) {
                LogUtil.e(BTAG + "-isValid- 未找到当前执行渠道");
                return false;
            }
            String priority = currentSdkSupplier.priority + "";
            final BaseParallelAdapter adapter = supplierAdapters.get(priority);

            if (adapter == null) {
                LogUtil.e(BTAG + "-isValid- 未找到当前渠道下adapter，渠道id：" + currentSDKId + ", priority = " + priority);
                return false;
            }
            if (adapter.isDestroy) {
                LogUtil.e(BTAG + "-isValid- 广告已销毁，请重新发起请求");
                return false;
            }
            return adapter.isValid();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ShowListener , 用来统一调用adapter中的show方法
     */
    @Override
    public void show() {
        try {
            if (TextUtils.isEmpty(currentSDKId)) {
                LogUtil.e(BTAG + "未选中任何SDK");
                return;
            }
            if (supplierAdapters == null || supplierAdapters.size() == 0) {
                LogUtil.e(BTAG + "无可用渠道");
                return;
            }
            if (currentSdkSupplier == null) {
                LogUtil.e(BTAG + "未找到当前执行渠道");
                return;
            }
            String priority = currentSdkSupplier.priority + "";
            final BaseParallelAdapter adapter = supplierAdapters.get(priority);
            if (adapter == null) {
                LogUtil.e(BTAG + "未找到当前渠道下adapter，渠道id：" + currentSDKId + ", priority = " + priority);
                return;
            }
            if (adapter.isDestroy) {
                LogUtil.e(BTAG + "广告已销毁，无法展示，请重新初始化");
                return;
            }
            //成功状态下 调用过show，不再需要继续调用了
            if (currentSdkSupplier.resultStatus == AdvanceConstant.SDK_RESULT_CODE_SUCC && currentSdkSupplier.hasCallShow) {
                //仅tanx reward才可以多次调用show
                boolean canRepeatShow = isReard && BYStringUtil.isEqual(currentSdkSupplier.id, AdvanceConfig.SDK_ID_TANX);
                if (!canRepeatShow) {
                    LogUtil.e(BTAG + "广告成功后调用过show方法，不再重复调用");
                    return;
                }
                LogUtil.w(BTAG + "广告成功后调用过show方法，重复调用");
            }
//            检查广告页面生命状态
            if (AdvanceUtil.isActivityDestroyed(getADActivity())) {
                LogUtil.e(BTAG + "广告已经销毁，无法进行展现");
                handleFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, "ActivityDestroyed"), currentSdkSupplier);
                return;
            }
            if (!isValid()) {
                String tip = "广告已经失效，无法进行展现";
                LogUtil.e(BTAG + tip);
                // 此时如果广告失效，会执行失败逻辑，再次流转策略，选中下一优先级广告。如果没有广告了，会抛出对应错误码，如果有成功的下一优先级广告，则会回调广告成功，开发者再次调用展示将执行下一优先级的展示方法。
//              TODO: 2025/6/5   是否有必要直接对外抛错？？？亦或者有下一优先级广告不再回调成功事件，而是直接进行展示调用
                handleFailed(AdvanceError.parseErr(AdvanceError.ERROR_RENDER_FAILED, tip), currentSdkSupplier);
                return;
            }
            LogUtil.devDebug(BTAG + "start adapter show");
            currentSdkSupplier.hasCallShow = true;
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    adapter.show();
                }
            });

            //进行竞胜上报，或者说调用了show方法的SDK渠道，主要是多个并行时，需要筛选出真实得曝光率。
            reportAdWin(currentSdkSupplier);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void handleFailed(AdvanceError advanceError, SdkSupplier sdkSupplier) {
        if (canNextStep(sdkSupplier)) {
            try {

                //如果当期失败的是最优渠道，进行置空
                if (isCurrentBestSupplier(sdkSupplier)) {
                    currentGroupInf.bestSupplier = null;
                    //如果此时走到了failed，说明渲染失败了
                    if (baseGMCall != null) {
                        baseGMCall.renderFailed();
                    }
                }
                this.advanceError = advanceError;
//            标记结果状态
                sdkSupplier.resultStatus = AdvanceConstant.SDK_RESULT_CODE_FAILED;
                updateGroupResultInf(sdkSupplier);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (canCheckShow(sdkSupplier)) {
                checkShow();
            }
        }
    }

    private void handlerSucc(SdkSupplier sdkSupplier) {
        try {
            if (canNextStep(sdkSupplier)) {
                if (sdkSupplier != null) {
                    sdkSupplier.resultStatus = AdvanceConstant.SDK_RESULT_CODE_SUCC;
                }
                updateGroupResultInf(sdkSupplier);

                if (canCheckShow(sdkSupplier)) {
                    checkShow();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // 必须要检查此行为，保证已经在展示得广告，不会因为其他sup广告晚回调时，进入到展示逻辑，导致二次展示行为发生。尤其是激励视频位置
    private boolean canCheckShow(SdkSupplier sdkSupplier) {
        boolean result = true;
        try {
            boolean bestShowing = currentGroupInf.bestSupplier != null && currentGroupInf.bestSupplier.hasCallSelected && currentGroupInf.bestSupplier.hasCallShow;
            boolean currentNotBest = !isCurrentBestSupplier(sdkSupplier);
            LogUtil.max(BTAG + "canCheckShow ，  bestShowing  =" + bestShowing + "， currentNotBest = " + currentNotBest);

            //如果当前best已经吊起展示方法，并且当前回调的SDK不是best，此处直接跳过展示流程的执行
            if (bestShowing && currentNotBest) {
                LogUtil.devDebug(BTAG + "canCheckShow ，wait best show , skip call checkShow");
                return false;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    void initSupplierAdapterList() {
        try {
            if (supplierAdapters == null) {
                supplierAdapters = new HashMap<>();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void addCustomSupplier(String sdkID, AdvanceBaseCustomAdapter adapter) {
        try {
            String clz = "";
            if (adapter != null && adapter.getClass() != null) {
                clz = adapter.getClass().getName();
            }
            addCustomSupplier(sdkID, clz);
        } catch (Throwable e) {
//            e.printStackTrace();
        }
    }

    public void addCustomSupplier(String sdkID, String adapterClzName) {
        try {
            if (TextUtils.isEmpty(adapterClzName)) {
                LogUtil.e("该sdkID：" + sdkID + " 配置的自定义adapter为空，请检查参数设置");
                return;
            }
            if (customAdapterInf == null) {
                customAdapterInf = new HashMap<>();
            }
            if (supportAdapterInf == null) {
                supportAdapterInf = new HashMap<>();
            }
            String adapterSaved = customAdapterInf.get(sdkID);
            String adapterSaved2 = supportAdapterInf.get(sdkID);
//校验自定义和已有adapter中是否已经定义过adapter。
            if (TextUtils.equals(adapterSaved, adapterClzName) || TextUtils.equals(adapterSaved2, adapterClzName)) {
                LogUtil.simple("该sdkID：" + sdkID + "下已存在渠道adapter，无法重复添加");
            } else {
                customAdapterInf.put(sdkID, adapterClzName);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 策略缓存开关设置，命名容易产生误解，方法已废弃
     *
     * @param isUseCache 为true会缓存最近一次的SDK策略
     * @see AdvanceBaseAdspot#enableStrategyCache(boolean)
     */
    @Deprecated
    public void setUseCache(boolean isUseCache) {
        this.isUseCache = isUseCache;
    }


    /**
     * 是否开启策略缓存。注意这里缓存的是SDK的调度策略，而非缓存的广告信息。
     *
     * @param enableCache 为true会缓存最近一次的SDK策略
     */
    public void enableStrategyCache(boolean enableCache) {
        this.isUseCache = enableCache;
    }


    //仅加载广告方法
    public void loadOnly() {
        loadAndShow = false;
        loadAd(AdvanceConfig.DEFAULT_AD_COUNT);
    }

    //重命名加载渠道
    public void loadStrategy() {
        if (needForceLoadAndShow) {
            loadAndShow = true;
        }
        loadAd(AdvanceConfig.DEFAULT_AD_COUNT);
    }

    //不支持多条
    @Deprecated
    public void loadStrategy(int adCount) {
        if (needForceLoadAndShow) {
            loadAndShow = true;
        }
        loadAd(adCount);
    }

    public void loadAd() {
        if (needForceLoadAndShow) {
            loadAndShow = true;
        }
        loadAd(AdvanceConfig.DEFAULT_AD_COUNT);
    }

    /**
     * 可以自定义广告请求数load方法，目前只有模板信息流会生效
     *
     * @param adCount 希望获取的广告数量
     */
    @Deprecated
    public void loadAd(int adCount) {
        try {
            if (adCount <= 0) {
                adCount = AdvanceConfig.DEFAULT_AD_COUNT;
            }
            requestTime = System.currentTimeMillis();

//            if (advanceUtil != null)
//                advanceUtil.setAdCount(adCount);
            //清空并行渠道的初始化状态
            if (paraInitStatus != null)
                paraInitStatus.clear();
            initSdkSupplier();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        //生成当前请求的唯一id
        reqId = BYUtil.getUUID();
        if (isUseCache) {
            LogUtil.high(BTAG + "优先使用策略缓存模式");
            try {
                //获取缓存的策略
                ElevenModel elevenModel = AdvanceNetManger.requestSupplierInfFromCache(advanceAdspotId);

                //这里只有enableStrategyCache ==0 的时候代表了手动配了实时不走缓存，如果是-1代表默认值，要走缓存逻辑
                if (elevenModel != null && (elevenModel.setting != null && elevenModel.setting.enableStrategyCache == 0)) {
                    LogUtil.high(BTAG + "配置为实时模式");
                    startImmediatelyStrategy(4);
                } else if (isStrategyErr(elevenModel)) {


                    //实时请求策略
                    startImmediatelyStrategy(1);
                } else {
                    dispatchSuppliers(elevenModel);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                LogUtil.e(BTAG + "使用缓存策略时发生异常");
                receivedAdErr();
                reportCodeErr(t, "强缓存 catch Throwable ");
            }
        } else {
            try {
                LogUtil.high(BTAG + "优先使用实时策略模式");
                //获取缓存的策略
                ElevenModel elevenModel = AdvanceNetManger.requestSupplierInfFromCache(advanceAdspotId);

                //这里如果enableStrategyCache != 1 也按照实时请求走
                if (elevenModel != null && (elevenModel.setting != null && elevenModel.setting.enableStrategyCache != 1)) {
                    LogUtil.high(BTAG + "策略中配置为非缓存请求，发起实时策略");
                    //实时请求策略
                    startImmediatelyStrategy(5);
                } else if (isStrategyErr(elevenModel)) {
                    LogUtil.high(BTAG + "无缓存策略或者缓存已过期");
                    //实时请求策略
                    startImmediatelyStrategy(2);
                } else {
                    //存在可用缓存且后台返回的缓存配置为开启状态，使用该缓存策略
                    dispatchSuppliers(elevenModel);
                }
            } catch (Throwable t) {
                t.printStackTrace();
                LogUtil.e(BTAG + "非强制缓存策略时发生异常");
                receivedAdErr();
                reportCodeErr(t, "非强缓存 catch Throwable ");
            }
        }

    }

    //上报代码中执行的异常信息
    private void reportCodeErr(Throwable t, String typeMsg) {
        try {
            String msg = typeMsg + LogUtil.getThrowableLog(t);
            AdvanceReportModel report = new AdvanceReportModel();

            report.code = AdvanceConstant.TRACE_STRATEGY_ERROR;
            report.msg = msg;
            doTrackReport(report);

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置上报信息的公有参数,并执行上报操作
     *
     * @param report 上报类
     */
    public void doTrackReport(AdvanceReportModel report) {
        try {
            if (report != null) {
                report.adspotid = advanceAdspotId;
                if (currentSdkSupplier != null) {
                    report.supadspotid = currentSdkSupplier.adspotid;
                }
                //直接使用本地生成的reqid。
                report.reqid = reqId;
                AdvanceReport.newPackageError(getADActivity(), report);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否策略异常
     *
     * @param elevenModel 当前获取的策略信息
     * @return true 异常走打底逻辑或新发起实时请求逻辑
     */
    private boolean isStrategyErr(ElevenModel elevenModel) {
        boolean isEmptyErr = AdvanceConfig.getInstance().isSupplierEmptyAsErr();
        //设置了检验 isEmptyErr = true的话，如果空按照异常算；否则看是否有策略，策略有的话：1.强缓存、弱缓存下的缓存；按照正常算，可能无填充；2.其他再试一下实时策略【isEmptyErr =true空渠道尝试打底、false无填充】；无策略看打底设置
        boolean result;
        if (isEmptyErr) {
            result = elevenModel == null || elevenModel.suppliers == null || elevenModel.suppliers.size() == 0;
        } else {
            result = elevenModel == null;
        }
        LogUtil.max(BTAG + "[策略异常校验] isSupplierEmptyAsErr = " + isEmptyErr + " ，result=" + result);
        if (result) {
            if (isEmptyErr) {
                LogUtil.high(BTAG + "策略异常（无缓存、缓存已过期、渠道信息为空等原因）");
            } else {
                LogUtil.high(BTAG + "策略异常（无缓存、缓存已过期 等原因）");
            }
        }
        return result;
    }

    /**
     * 发起实时策略请求
     *
     * @param requestCode 标记发起请求的场景
     */
    private void startImmediatelyStrategy(final int requestCode) {
        try {
            LogUtil.simple(BTAG + "发起实时策略请求");
            AdvanceReqModel reqModel = new AdvanceReqModel();
            reqModel.adspotId = advanceAdspotId;
            reqModel.mediaId = "";
            reqModel.reqId = reqId;
            reqModel.requestForceTimeout = true;

            //todo 23-06-25 注意线程运行情况
            AdvanceNetManger.requestSupplierList(reqModel, new BYAbsCallBack<ElevenModel>() {
                @Override
                public void invoke(final ElevenModel elevenModel) {
                    //根据结果选择返回信息
                    //实时策略没返回(网络异常、服务器异常、配置异常)，对于媒体来说这里会是流量损失的缺口，如果包天的媒体需要承担这部分缺口，非包天那么可以通过设置打底渠道，就可以埋上这里的缺口
                    if (isStrategyErr(elevenModel)) {
                        HandleEmptySupplier();
                    } else {
                        //如果是开屏分离模式，不进入主线程执行
                        if (isSplitLoad && loadWithAsync) {
                            dispatchSuppliers(elevenModel, true);
                            return;
                        }
                        LogUtil.high(BTAG + "已获取实时策略");
                        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                            @Override
                            public void call() {
                                dispatchSuppliers(elevenModel, true);
                            }
                        });
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void HandleEmptySupplier() {
        LogUtil.high(BTAG + "实时策略请求未获取到策略信息");
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                selectSdkSupplierFailed();
            }
        });
    }

    /**
     * 分发获取到的渠道信息，如果当前配置为需要缓存，发起新策略以缓存
     *
     * @param elevenModel 获取到的渠道信息
     */
    private void dispatchSuppliers(@NonNull final ElevenModel elevenModel) {
        LogUtil.high(BTAG + "存在缓存策略");
        isCacheEffect = true;

        boolean isMainThread = Looper.myLooper() == Looper.getMainLooper();
        final boolean defaultFromImm = false;
        if (isMainThread) {
            dispatchSuppliers(elevenModel, defaultFromImm);
        } else {
            //如果是非主线程，需要强制切换到主线程来进行分发
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        LogUtil.high(BTAG + "force to main thread run dispatchSuppliers");
                        dispatchSuppliers(elevenModel, defaultFromImm);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void dispatchSuppliers(@NonNull ElevenModel elevenModel, boolean isFromImm) {
        LogUtil.simple(BTAG + "开始进行渠道分发");
        mElevenModel = elevenModel;
        SupplierSettingModel settingModel = null;

        //以优先级priority 为key，存储当前策略返回的所有supplier信息，方便后续取用
        HashMap<Integer, SdkSupplier> supplierMap = new HashMap<>();

        StrategyReadyInf strategyReadyInf = new StrategyReadyInf();

        if (elevenModel.suppliers != null && elevenModel.suppliers.size() > 0) {
            for (SdkSupplier item : elevenModel.suppliers) {
                supplierMap.put(item.priority, item);
            }
        }
        try {
            GMParams gmParams = elevenModel.gmParams;
            SdkSupplier gmSupInf = new SdkSupplier();

            //转换setting中gm信息至统一supplier中
            if (gmParams != null) {
                gmSupInf.adspotid = gmParams.adspotid;
                gmSupInf.mediaid = gmParams.appid;
                gmSupInf.timeout = gmParams.timeout;

                gmSupInf.loadedtk = gmParams.loadedtk;
                gmSupInf.succeedtk = gmParams.succeedtk;
                gmSupInf.failedtk = gmParams.failedtk;
                gmSupInf.clicktk = gmParams.clicktk;
                gmSupInf.imptk = gmParams.imptk;
            }
            strategyReadyInf.gmInf = gmSupInf;
        } catch (Throwable e) {
            e.printStackTrace();
        }

        try {
            settingModel = elevenModel.setting;
            if (settingModel != null) {

                //单层并行超时时间
                parallelTimeout = settingModel.parallelTimeout;
                strategyReadyInf.singleTimeOut = parallelTimeout;
                strategyReadyInf.needGM = settingModel.biddingType == 1;
                paraPriGroup = settingModel.paraGroup;
                //并行数组有内容代表当前是并行状态（如果app为支持当前并行渠道，可能不会执行并行请求）
                ArrayList<SdkParaGroup> paraGroups = new ArrayList<>();
                if (paraPriGroup != null && paraPriGroup.size() > 0) {
                    for (int i = 0; i < paraPriGroup.size(); i++) {
                        int gid = i;
                        SdkParaGroup groupItem = new SdkParaGroup();
                        ArrayList<Integer> paraList = paraPriGroup.get(i);


                        ArrayList<SdkSupplier> paraSupplierMembers = new ArrayList<>();
//                        ArrayList<SdkSupplier> biddingSupplier = new ArrayList<>();
                        double maxPrice = 0;
                        double minPrice = 0;
                        if (paraList != null && paraList.size() > 0) {
                            //先赋值最大最小price值为真实价格信息
                            SdkSupplier firstSup = supplierMap.get(paraList.get(0));
                            if (firstSup != null) {
                                maxPrice = firstSup.price;
                                minPrice = firstSup.price;
                            }
                            for (int j = 0; j < paraList.size(); j++) {
                                try {
                                    int pri = paraList.get(j);
                                    //获取并行组内的渠道信息
                                    SdkSupplier supplier = supplierMap.get(pri);
                                    if (supplier != null) {
                                        //设置group信息
                                        supplier.groupID = gid;
                                        //比较价格，并记录最大最小值
                                        double supPrice = supplier.price;
                                        if (supPrice > maxPrice) {
                                            maxPrice = supPrice;
                                        }
                                        if (supPrice < minPrice) {
                                            minPrice = supPrice;
                                        }
                                        paraSupplierMembers.add(supplier);

                                    }

                                } catch (Throwable e) { //避免找不到对应渠道信息
                                    e.printStackTrace();
                                }
                            }
                        }
                        //设置唯一id
                        groupItem.groupID = gid;
                        groupItem.paraSupplierMembers = paraSupplierMembers;
//                        groupItem.biddingSuppliers = biddingSupplier;
                        groupItem.maxPrice = maxPrice;
                        groupItem.minPrice = minPrice;
                        LogUtil.devDebug(BTAG + " groupItem = " + groupItem.toString());
                        paraGroups.add(groupItem);
                    }
                }
                LogUtil.devDebug(BTAG + " groupSize = " + paraGroups.size());
                supplierGroups = paraGroups;

            }
            //是否延迟上报，根据后台返回的字段来判断，默认false不延迟
            isReportDelay = elevenModel.isReportDelay();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (strategyListener != null) {
            strategyListener.onStrategyReady(strategyReadyInf);
        }

        //将获取的实时策略进行分发
        initAllAdapter(elevenModel.suppliers);
        LogUtil.high(BTAG + "(dispatchSuppliers) start selectSdkSupplier");
        //首先启动bidding组的请求，在新开线程中进行，注意超时相关处理
        startBiddingRequest(supplierMap);
        //启动瀑布流组得流转
        selectSdkSupplier();

        //强制缓存下如果配置为实时，此时需要将缓存清除，且不再发起用来缓存的策略
        if (isUseCache && (settingModel != null && settingModel.enableStrategyCache == 0)) {
            removeCache();
        } else if (isUseCache || (settingModel != null && settingModel.enableStrategyCache == 1)) {
            //如果是来自实时策略请求，那么直接缓存当前这次的策略信息即可，不需再延迟请求新策略用来缓存了
            if (isFromImm) {
                AdvanceUtil.saveElevenData(elevenModel, generateReq(isFromImm));
            } else {
                //延迟请求新策略以缓存
                delayRequestSupplierForCache(isFromImm);
            }

        }

    }

    //开始bidding组
    private void startBiddingRequest(HashMap<Integer, SdkSupplier> supplierMap) {
        String vTag = "(startBiddingRequest)";
        if (mElevenModel == null) {
            LogUtil.max(BTAG + vTag + " mElevenModel  empty");
            return;
        }
        if (supplierMap == null || supplierMap.size() == 0) {
            LogUtil.max(BTAG + vTag + " supplierMap  empty");
            return;
        }
        if (mElevenModel.setting == null) {
            LogUtil.max(BTAG + vTag + " SupplierSettingModel  empty");
            return;
        }
        ArrayList<Integer> biddingPriorityList = mElevenModel.setting.biddingGroup;
        if (biddingPriorityList == null || biddingPriorityList.size() == 0) {
            LogUtil.max(BTAG + vTag + " biddingGroup  empty");
            return;
        }

//        给bidding组赋值渠道信息
        SdkParaGroup bidInf = new SdkParaGroup();
        bidInf.paraSupplierMembers = new ArrayList<>();
        for (int i = 0; i < biddingPriorityList.size(); i++) {
            int pri = biddingPriorityList.get(i);
            final SdkSupplier sdkSupplier = supplierMap.get(pri);
            if (sdkSupplier != null) {
                bidInf.paraSupplierMembers.add(sdkSupplier);
            }
            BYThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    int paraResult = singleParaLoad(sdkSupplier);
                    //更新执行结果
                    if (paraResult < 0 && sdkSupplier != null) {
                        sdkSupplier.resultStatus = AdvanceConstant.SDK_RESULT_CODE_FAILED;
                    }
                }
            });
        }

        biddingGroup = bidInf;

        bidTimeOutHandler = new Handler(Looper.getMainLooper());
        bidTimeoutRun = new Runnable() {
            @Override
            public void run() {
                //  如果三个里面有一个是未返回，此时是否影响另外两个得渲染和展示？，此时强制设置isBiddingAllResult 为true即可
                try {
                    String bidTag = "biddingGroup超时检查：";
                    if (isBiddingEmpty()) {
                        LogUtil.high(BTAG + bidTag + "bidding组为空，跳过");
                        return;
                    }
                    if (biddingGroup.isBiddingAllResult) {
                        LogUtil.devDebug(BTAG + bidTag + "bidding组已全部返回广告结果，跳过");
                        return;
                    }
                    LogUtil.high(BTAG + bidTag + " bidding组请求超时， result has delay after " + parallelTimeout + " ms");

                    //对广告状态未返回的渠道，标记为超时
                    biddingGroup.isTimeOut = true;
                    biddingGroup.isBiddingAllResult = true;
                    advanceError = AdvanceError.parseErr(AdvanceError.ERROR_GROUP_TIMEOUT);

                    ArrayList<SdkSupplier> biddingSuppliers = biddingGroup.paraSupplierMembers;
                    if (biddingSuppliers.size() > 0) {
                        for (SdkSupplier supplier : biddingSuppliers) {
                            setSdkStatusTimeOut(supplier);
                        }
                    }
                    if (isParaGroupEmpty()) {
                        handleOnlyBidding();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
        bidTimeOutHandler.postDelayed(bidTimeoutRun, parallelTimeout);
    }

    private AdvanceReqModel generateReq(boolean isFromImm) {
        AdvanceReqModel reqModel = new AdvanceReqModel();
        reqModel.adspotId = advanceAdspotId;
        reqModel.mediaId = "";
        reqModel.reqId = reqId;
        reqModel.forceCache = isUseCache;
        reqModel.isFromImm = isFromImm;
        reqModel.isCacheEffect = isCacheEffect;
        return reqModel;
    }

    private void removeCache() {
        try {
            String key = AdvanceUtil.generateKey("", advanceAdspotId);
            boolean rv = BYCacheUtil.byCache().remove(key);
            LogUtil.high(BTAG + "移除已有无用缓存 " + rv);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 延迟请求新策略，延时是为了不影响当前应用的一些网络请求，尽量减少占用、抢占网络现象
     * <p>
     * 开屏：如果有设置延迟时间，使用APP端设置的时间，否则默认5s后;
     * 非开屏：200ms后请求下一条策略
     *
     * @param isFromImm 是否来自实时请求
     */
    private void delayRequestSupplierForCache(final boolean isFromImm) {
        try {

            int delay = AdvanceConfig.getInstance().getReportDelayTime();
            //如果用户未设置delay时间，按照默认的逻辑走，如果是开屏（needDelay = true）需要进行较长时间的延迟来给素材展示让路
            if (delay < 0) {
                if (needDelay) {
                    delay = 5000;
                } else {
                    delay = 800;
                }
            }

            LogUtil.high(BTAG + "延迟 " + delay + "ms 发起新策略请求");

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        final AdvanceReqModel reqModel = generateReq(isFromImm);
                        AdvanceNetManger.requestSupplierList(reqModel, new BYAbsCallBack<ElevenModel>() {
                            @Override
                            public void invoke(ElevenModel elevenModel) {
                                //follow旧逻辑，即使elevenModel是null也会进行报错，执行清除旧缓存逻辑。
                                AdvanceUtil.saveElevenData(elevenModel, reqModel);
                            }
                        });

                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }, delay);
        } catch (Throwable e) {
            e.printStackTrace();

        }
    }

    protected Activity getADActivity() {
        if (softReferenceActivity != null) {
            return softReferenceActivity.get();
        }
        return activity;
    }

    protected void updateADActivity(Activity activity) {
        if (activity != null) {
            this.activity = activity;
        }
    }

    //获取activity信息新方法，首先通过view寻找activity信息（可能为空），然后再通过传递参数获取
    public Activity getRealActivity(View adContainerView) {

        try {
            if (adContainerView != null) {
                activity = AdvanceUtil.getActivityFromView(adContainerView);
            }
            LogUtil.devDebug(BTAG + " getActivityFromView result = " + activity);
            if (activity == null) {
                activity = getADActivity();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return activity;
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

    //对所有广告加载adapter进行初始化
    private void initAllAdapter(ArrayList<SdkSupplier> suppliers) {
        try {

            if (suppliers != null && suppliers.size() > 0)
                for (SdkSupplier sdkSupplier : suppliers) {
                    boolean adapterInit = true;
                    //初始化需要适配的adapter
                    if (sdkSupplier == null) {
                        LogUtil.high(BTAG + "[before initAdapterData] 渠道信息为空");
                        return;
                    }
                    sdkSupplier.advanceAdspotId = advanceAdspotId;

                    String clzName = supportAdapterInf.get(sdkSupplier.id);
                    if (TextUtils.isEmpty(clzName)) {
                        LogUtil.high(BTAG + "[before initAdapterData] 未找到渠道适配类");
                        adapterInit = false;
                    }
                    if (adapterInit) {
                        String fullClz = clzName;
                        if (clzName != null && !clzName.contains(BASE_ADAPTER_PKG_PATH)) {
                            fullClz = BASE_ADAPTER_PKG_PATH + clzName;
                        }
                        initAdapterData(sdkSupplier, fullClz);
                    } else {
                        String customClzName = customAdapterInf.get(sdkSupplier.id);
                        if (customClzName != null) {
                            initAdapterData(sdkSupplier, customClzName);
                            LogUtil.high(BTAG + "[custom Adapter init] adapter = " + customClzName);
                        }
                    }
                }
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 发生异常，如果有打底设置走打底广告
     */
    private void receivedAdErr() {

        LogUtil.high(" receivedAdErr selectSdkSupplierFailed");
        selectSdkSupplierFailed();
    }


    //串行下的SDK启动成功上报
    void reportAdvanceLoaded() {
        try {
            if (currentSdkSupplier != null) {
                ArrayList<String> ltk = AdvanceReport.getReplacedLoaded(currentSdkSupplier.loadedtk, requestTime, getAdvanceId());
                switchReport(ltk);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //向外回调广告获取成功时，选择tk上报
    void reportAdSucceed(SdkSupplier supplier) {
        try {
            adStatus = AdStatus.SUCCESS;
            if (supplier != null && !isSupportParallel(supplier)) {

//            标记结果状态
                supplier.resultStatus = AdvanceConstant.SDK_RESULT_CODE_SUCC;
                updateGroupResultInf(supplier);

                ArrayList<String> stk;
                if (supplier.useBidding() && supplier.bidResultPrice > 0) {
                    stk = AdvanceReport.getReplacedBidding(supplier.succeedtk, getAdvanceId(), supplier.bidResultPrice);
                } else {
                    stk = AdvanceReport.getReplacedTime(supplier.succeedtk, getAdvanceId());
                }
                switchReport(stk);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //竞价胜出上报。当广告调用"展示"方法后，进行上报
    void reportAdWin(SdkSupplier supplier) {
        try {
            LogUtil.devDebug(BTAG + "reportAdWin start");
            if (supplier != null && supplier.wintk != null) {
                ArrayList<String> stk;
                if (supplier.useBidding() && supplier.bidResultPrice > 0) {
                    stk = AdvanceReport.getReplacedBidding(supplier.wintk, getAdvanceId(), supplier.bidResultPrice);
                } else {
                    stk = AdvanceReport.getReplacedTime(supplier.wintk, getAdvanceId());
                }
                switchReport(stk);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private boolean isSupportParallel(SdkSupplier supplier) {
        boolean result = false;
        try {
            if (supplierAdapters != null && supplier != null) {
                int pri = supplier.priority;
                BaseParallelAdapter parallelAdapter = supplierAdapters.get(pri + "");
                if (parallelAdapter != null) {
                    result = parallelAdapter.supportPara;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return result;
    }

    void reportAdShow(SdkSupplier supplier) {
        try {
            adStatus = AdStatus.SHOW;
            if (supplier != null) {
                ArrayList<String> imp;
                //bidding渠道需要记录价格，上报至服务端
                if (supplier.useBidding() && supplier.bidResultPrice > 0) {
                    imp = AdvanceReport.getBidReplacedImp(supplier.imptk, requestTime, getAdvanceId(), supplier.bidResultPrice);
                } else {
                    imp = AdvanceReport.getReplacedImp(supplier.imptk, requestTime, getAdvanceId());
                }
                AdvanceReport.reportToUrls(imp);
            }
            //展现上报发起后进行被延迟的部分上报
            startDelayReport();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    void reportAdFailed(AdvanceError advanceError, SdkSupplier sdkSupplier) {
        try {
            if (sdkSupplier != null) {
                ArrayList<String> ftk = AdvanceReport.getReplacedFailed(sdkSupplier.failedtk, advanceError, getAdvanceId());
                switchReport(ftk);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    void reportAdClicked(SdkSupplier supplier) {
        try {
            if (supplier != null) {
                ArrayList<String> ctk = AdvanceReport.getReplacedTime(supplier.clicktk, getAdvanceId());
                AdvanceReport.reportToUrls(ctk);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void switchReport(ArrayList<String> tk) {
        if (isReportDelay && savedDelayReportList != null) {
            savedDelayReportList.add(tk);
        } else {
            AdvanceReport.reportToUrls(tk);
        }
    }

    void dispatchSupplierFailed(AdvanceErrListener listener) {
        try {
            onAdvanceError(listener, advanceError);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 整体失败回调，包括任意场景下的失败，方便在这里统一进行信息埋点上报\以及延迟上报的处理
     *
     * @param listener     失败监听器
     * @param advanceError 失败详细内容
     */
    void onAdvanceError(final AdvanceErrListener listener, AdvanceError advanceError) {
        try {
            LogUtil.devDebug(BTAG + " onAdvanceError ");
            //先进行延迟上报
            startDelayReport();
            //回调开发者错误信息
            if (listener != null) {
                if (advanceError == null) {
                    LogUtil.simple("None SDK: sdk suppliers is empty, callback failed");
                    advanceError = AdvanceError.parseErr(AdvanceError.ERROR_NONE_SDK);
                }
                LogUtil.devDebug(BTAG + " onAdFailed :  advanceError = " + advanceError.toString());

                adError = advanceError;
                adStatus = AdStatus.FAILED;
                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                    @Override
                    public void call() {
                        listener.onAdFailed(adError);
                        if (baseGMCall != null) {
                            baseGMCall.allFailed(adError);
                        }
                    }
                });
                if (listener instanceof AdvanceSplashListener) {
                    final AdvanceSplashListener fListener = (AdvanceSplashListener) listener;
                    BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                        @Override
                        public void call() {
                            fListener.jumpToMain();
                        }
                    });
                }
            }
            //进行埋点sdkevent单独上报
            AdvanceReportModel report = new AdvanceReportModel();
            if (AdvanceError.ERROR_SUPPLIER_SELECT_FAILED.equals(advanceError.code)) {
                //SDK渠道获取异常、无网络、无策略等
                report.status = 0;
            } else {
                //SDK渠道全部加载失败了
                report.status = 2;
            }
            //从发起请求到策略、广告失败的时间差
            long cost = System.currentTimeMillis() - requestTime;
            report.code = AdvanceConstant.TRACE_TOTAL_TIME;
            report.msg = cost + "";

            if (AdvanceError.ERROR_NO_ACTIVITY.equals(advanceError.code)) {
                try {
                    JSONObject object = new JSONObject();
                    object.putOpt("e_code", advanceError.code);
                    object.putOpt("e_msg", advanceError.msg);
                    report.ext = object.toString();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            doTrackReport(report);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 延迟上报在这里（全部失败时、广告曝光时）进行上报
     */
    private void startDelayReport() {
        try {
            if (isReportDelay) {
                //延迟上报执行之后需要重置状态，否则并行的后来产生的上报请求，会无法进行上报。
                isReportDelay = false;

                int size = savedDelayReportList.size();
                LogUtil.max("delay report tkList size is " + size);
                for (int i = 0; i < size; i++) {
                    AdvanceReport.reportToUrls(savedDelayReportList.get(i));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 统一处理渠道选择流程，支持并行、串行
     */
    void selectSuppliers(AdvanceErrListener listener) {
        try {
            adStatus = AdStatus.START;
//            long start = System.currentTimeMillis();
            currentGroupInf = supplierGroups.get(0);
            if (currentGroupInf == null) {
                nextGroup();
            }

            //开始下一组之前，需要判断当前bidding结果，如果已全部执行完，且成功得价格比下一组最高价格高，直接胜出
            try {
                if (!isBiddingEmpty()) {
                    if (biddingGroup.isBiddingAllResult || biddingGroup.isTimeOut) {
                        ArrayList<SdkSupplier> succBidList = getSuccBidResult();

                        ArrayList<SdkSupplier> bidWinList = new ArrayList<>();
                        for (SdkSupplier bid : succBidList) {
                            if (bid.price >= currentGroupInf.maxPrice) {
                                LogUtil.devDebug(BTAG + "（selectSuppliers start）该 bidding 价格比组内价格高，进入bid胜出组");
                                bidWinList.add(bid);
                            }
                        }

                        if (bidWinList.size() > 0) {
                            LogUtil.devDebug(BTAG + "（selectSuppliers start）bidding胜出组 开始展示,bidWinList.size = " + bidWinList.size());
                            Collections.sort(bidWinList);
                            //将当前group进行重新定义，不和已有得组有重复
                            currentGroupInf = new SdkParaGroup();
                            currentGroupInf.groupID = 999;
                            currentGroupInf.paraSupplierMembers = bidWinList;
                            currentGroupInf.isBiddingGroup = true;
                            currentGroupInf.isBiddingAllResult = biddingGroup.isBiddingAllResult;
                            currentGroupInf.isTimeOut = biddingGroup.isTimeOut;
                            prepareShowAd(bidWinList);
                            return;
                        }
                    } else {
                        LogUtil.high(BTAG + "(bidding group)尚未就绪，不等待，直接run currentGroup");
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            LogUtil.devDebug(BTAG + "currentGroupInf = " + currentGroupInf.toString());
            if (AdvanceUtil.isActivityDestroyed(getADActivity())) {
                try {
                    LogUtil.e(BTAG + "当前activity已被销毁，不再请求广告");
                    onAdvanceError(listener, AdvanceError.parseErr(AdvanceError.ERROR_NO_ACTIVITY));
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return;
            }

            LogUtil.high(BTAG + "开启请求超时校验机制，Timeout = " + parallelTimeout);

            //启动超时检查
            timeOutHandler = new Handler(Looper.getMainLooper());
            timeoutRun = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (currentGroupInf == null) {
                            LogUtil.high(BTAG + "超时检查：并行组为空，跳过");
                            return;
                        }
                        //如果已经有结果返回，不执行强制返回
                        if (currentGroupInf.isGroupAllResult) {
                            LogUtil.devDebug(BTAG + "超时检查：并行组已全部返回广告结果，跳过");
                            return;
                        }
                        LogUtil.high(BTAG + "请求超时， result has delay after " + parallelTimeout + " ms");
                        //设置错误返回信息
                        advanceError = AdvanceError.parseErr(AdvanceError.ERROR_GROUP_TIMEOUT);
                        currentGroupInf.isTimeOut = true;
                        //对广告状态未返回的渠道，标记为超时
                        if (currentGroupInf.paraSupplierMembers.size() > 0) {
                            for (SdkSupplier supplier : currentGroupInf.paraSupplierMembers) {
                                setSdkStatusTimeOut(supplier);
                            }
                        }
                        //超时，直接开始比价流程，重排序，按照排序后顺序执行广告展示
                        if (canCheckShow(null)) {
                            checkShow();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            timeOutHandler.postDelayed(timeoutRun, parallelTimeout);

            final int groupSize = currentGroupInf.paraSupplierMembers.size();
            if (groupSize > 0) {
                final ValueDataModel dataModel = new ValueDataModel();
                dataModel.count = 0;
                dataModel.needNext = false;
                //遍历开始并行请求广告位
                for (int i = 0; i < groupSize; i++) {
                    final int finalI = i;
                    //当有多个时，开启线程池执行并行操作
                    if (groupSize > 1) {
                        dataModel.needNext = false;
                        BYThreadPoolUtil.execute(new Runnable() {
                            @Override
                            public void run() {
                                singleLoad(finalI, dataModel, groupSize);
                            }
                        });

                    } else {
                        singleLoad(finalI, dataModel, groupSize);
                    }
                }


            } else {
                nextGroup();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            advanceError = AdvanceError.parseErr(AdvanceError.ERROR_SUPPLIER_SELECT);
//            onAdvanceError(listener, );
        }
    }

    private void setSdkStatusTimeOut(SdkSupplier supplier) {
        try {
            if (supplier.resultStatus == AdvanceConstant.SDK_RESULT_CODE_DEFAULT || supplier.resultStatus == AdvanceConstant.SDK_RESULT_CODE_ORDER) {
                supplier.resultStatus = AdvanceConstant.SDK_RESULT_CODE_TIMEOUT;
            }
        } catch (Throwable e) {

        }
    }

    private void singleLoad(final int finalI, ValueDataModel dataModel, int groupSize) {
        try {
            //先进行并行请求，获取状态结果
            int status = singleParaLoad(finalI);

            dataModel.count++;
            SdkSupplier paraSupplier = currentGroupInf.paraSupplierMembers.get(finalI);
            if (paraSupplier != null) {
                boolean result = status == -3 && paraSupplier.priority == currentGroupInf.groupFirstUnbiddingPri
//                        && !currentGroupInf.isBiddingGroup
                        ;
                dataModel.needNext = dataModel.needNext || result;
                //组内全部执行状态异常
                dataModel.allErr = dataModel.allErr && status < 0;
            }

            //如果是非bidding组，并且当前优先级为最高，且不支持并行，那么需要直接进行show
            if (dataModel.count >= groupSize) {
                if (dataModel.needNext) {
                    checkShow();
                } else if (dataModel.allErr) {
                    LogUtil.devDebug(BTAG + "组内成员执行load全失败");
                    nextGroup();
                }
            }
            LogUtil.high(BTAG + " singleParaLoad status: " + status + ", needNext = " + dataModel.needNext);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //进行广告展示前的状态检查，回调成功事件为结束
    private void checkShow() {
        try {
            LogUtil.devDebug(BTAG + "(checkShow) start");
            ArrayList<SdkSupplier> groupList = currentGroupInf.paraSupplierMembers;
            //有成功的广告，开始重排序，并执行展示
            if (groupList != null && groupList.size() > 0) {
                prepareShowAd(groupList);

//                if (currentGroupInf.isBiddingGroup) { //包含bidding的group，需要等待bidding结果在进行重排序
//                    LogUtil.devDebug(BTAG + "(bidding group) checkShow  , isGroupAllResult = " + currentGroupInf.isGroupAllResult + ", isBiddingAllResult = " + currentGroupInf.isBiddingAllResult + ", isTimeOut = " + currentGroupInf.isTimeOut);
//
//                    //如果bidding都有结果了，对"成功列表"进行重排序
//                    if (currentGroupInf.isBiddingAllResult || currentGroupInf.isTimeOut) {
//                        //有成功的广告，开始重排序，并准备回调成功事件
//                        prepareShowAd(groupList);
//                    } else {
//                        LogUtil.high(BTAG + "(bidding group)尚未就绪");
//                    }
//                } else {//非bidding直接进行重排序
//                    prepareShowAd(groupList);
//                }

            } else { //无数据，进入下一组流程
                nextGroup();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //准备执行广告展示，此时广告成功流转会回调广告成功给调用者
    private void prepareShowAd(ArrayList<SdkSupplier> sdkSuppliers) {
        prepareShowAd(sdkSuppliers, new BYBaseCallBack() {
            @Override
            public void call() {
                nextGroup();
            }
        });
    }

    private void prepareShowAd(ArrayList<SdkSupplier> sdkSuppliers, BYBaseCallBack allFailed) {
        try {
            String tag = " (prepareShowAd) ";
            //todo 选早逻辑已移除，如果需要，可以在此开发对应逻辑
            Collections.sort(sdkSuppliers);


            int adSize = sdkSuppliers.size();
            int sizeCount = adSize;
            LogUtil.devDebug(BTAG + tag + "  ad size = " + adSize);
            for (int j = 0; j < adSize; j++) {
                SdkSupplier succSupplier = sdkSuppliers.get(j);
                if (succSupplier == null) {
                    LogUtil.high(BTAG + tag + "未找到渠道信息，跳过");
                    sizeCount--;
                    continue;
                }
                int pri = succSupplier.priority;
                LogUtil.devDebug(BTAG + tag + "check sdk :" + succSupplier.name + "(" + pri + ")");
                BaseParallelAdapter parallelAdapter = supplierAdapters.get(pri + "");
                if (parallelAdapter == null) {
                    LogUtil.high(BTAG + tag + "未定义该渠道并行方法，跳过");
                    sizeCount--;
                    continue;
                }

                int result = succSupplier.resultStatus;

                if (result == AdvanceConstant.SDK_RESULT_CODE_FAILED) {
                    LogUtil.high(BTAG + tag + "渠道已失败，跳过");
                    sizeCount--;
                    continue;
                }
                if (result == AdvanceConstant.SDK_RESULT_CODE_TIMEOUT) {
                    LogUtil.high(BTAG + tag + "并行组处理已超时，渠道可能还在请求中，跳过");
                    sizeCount--;
                    continue;
                }


                if (currentGroupInf.isBiddingGroup) {
                    boolean bidFinished;
                    if (currentGroupInf.isTimeOut) {
                        bidFinished = true;
                    } else {
                        bidFinished = currentGroupInf.isBiddingAllResult;
                    }
                    if (!bidFinished) {
                        LogUtil.high(BTAG + tag + "bidding组尚未执行完毕，跳过并等待");
                        return;
                    }
                }

                //自此之后代表当前并行组已有正确广告返回，需要检查bidding组信息
                //
                //
                // isBiddingGroup代表这一组本身就是bidding组，不需要插入逻辑了
                try {
                    if (!isBiddingEmpty() && !currentGroupInf.isBiddingGroup) {

                        //bidding组结束，需要对价格进行比较
                        if (biddingGroup.isBiddingAllResult || biddingGroup.isTimeOut) {
                            //更新数据结果
                            ArrayList<SdkSupplier> succBidList = getSuccBidResult();
                            ArrayList<SdkSupplier> bidRemoveList = new ArrayList<>();

                            boolean hasChange = false;
                            for (SdkSupplier bid : succBidList) {
                                //  价格相等时怎么办？，也会进入组内执行
                                //只要是价格比最低得价格低，直接塞入，并且移除bidding组中信息
                                if (bid.price >= currentGroupInf.minPrice) {
                                    LogUtil.devDebug(BTAG + tag + "该bidding  价格比组内价格高，移入并行执行组内,sup = " + bid.toString());
                                    if (!currentGroupInf.paraSupplierMembers.contains(bid)) {
                                        currentGroupInf.paraSupplierMembers.add(bid);
                                    }
                                    //记录bidding组内移除该元素
                                    bidRemoveList.add(bid);
                                    hasChange = true;
                                } else {
                                    LogUtil.devDebug(BTAG + tag + "该bidding 价格比组内价格低，不处理");
                                }
                            }
                            //代表有插入信息
                            if (hasChange) {
                                //移除
                                for (SdkSupplier removeBid : bidRemoveList) {
                                    biddingGroup.paraSupplierMembers.remove(removeBid);
                                    LogUtil.devDebug(BTAG + tag + "移除bidding组内信息，当前bid size = " + biddingGroup.paraSupplierMembers.size());
                                }
                                LogUtil.devDebug(BTAG + tag + "组内插入了bidding成员，重新执行展示步骤,currentGroup size = " + currentGroupInf.paraSupplierMembers.size());
                                //递归检查展示情况
                                prepareShowAd(currentGroupInf.paraSupplierMembers);
                                return;
                            }

                        } else {
                            LogUtil.high(BTAG + tag + "(bidding group)尚未就绪，等待return");
                            return;
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                //设置最优渠道信息
                if (currentGroupInf.bestSupplier == null) {
                    LogUtil.devDebug("当前最优：" + succSupplier.toString());
                    currentGroupInf.bestSupplier = succSupplier;
                }

                if (result == AdvanceConstant.SDK_RESULT_CODE_DEFAULT) {
                    LogUtil.high(BTAG + tag + "渠道未返回结果，跳过并等待");
//                    sizeCount--;
                    continue;
                }

                //运行最优渠道信息
                if (currentGroupInf.bestSupplier == succSupplier) {
                    parallelAdapter.setSDKSupplier(succSupplier);
                    LogUtil.devDebug(BTAG + tag + "  start show :" + succSupplier.toString());
                    //parallelAdapter 会处理串并行得展示
                    callSDKSelected(succSupplier);
                    //如果此时广告已经成功，会执行广告成功回调
                    parallelAdapter.prepareShow();
                    //如果已经进行或show方法调用，这里直接继续show逻辑，需要验证渲染失败
                    //  2024/5/28 激励视频位置，因为新引入了可多次展示逻辑（tanx特殊场景下需要多次show），存在执行上逻辑异常
                    //  当广告最优已选出时，且调用过show方法，但是接着又有某一个广告返回了，重新执行了此处的命中逻辑
                    LogUtil.devDebug(BTAG + tag + "check auto show;  currentSdkSupplier.hasCallShow :" + currentSdkSupplier.hasCallShow + " ,loadAndShow = " + loadAndShow);
                    if (currentSdkSupplier.hasCallShow || loadAndShow) {
                        show();
                    }
                    return;
                } else {
                    LogUtil.high(BTAG + tag + "非最优渠道，跳过");
                    sizeCount--;
                    continue;
                }

            }
            LogUtil.devDebug(BTAG + "sizeCount = " + sizeCount);
            if (sizeCount <= 0) {//全部渠道处理失败
                if (allFailed != null) {
                    allFailed.call();
                }
//                nextGroup();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }


    //单个渠道的并行执行方法
    private int singleParaLoad(int i) {
        try {
            SdkSupplier paraSupplier = currentGroupInf.paraSupplierMembers.get(i);
            return singleParaLoad(paraSupplier);
        } catch (Throwable e) {
            e.printStackTrace();
            return -6;
        }
    }

    //单个渠道的并行执行方法
    private int singleParaLoad(SdkSupplier paraSupplier) {
        try {
            if (paraSupplier == null) {
                LogUtil.high(BTAG + "未找到渠道信息，跳过");
                return -1;
            }
            int pri = paraSupplier.priority;
            BaseParallelAdapter parallelAdapter = supplierAdapters.get(pri + "");
            if (parallelAdapter == null) {
                LogUtil.high(BTAG + "未定义该渠道并行方法，跳过");
                return -2;
            }

            if (!parallelAdapter.supportPara) {
                LogUtil.high(BTAG + "该渠道不支持并行，跳过");
                updateFirstPri(paraSupplier);
                paraSupplier.resultStatus = AdvanceConstant.SDK_RESULT_CODE_ORDER;
                return -3;
            }

            boolean hasInited = false;
            if (paraInitStatus != null) {
                hasInited = paraInitStatus.get(pri) != null;
            }
            if (hasInited) {
                LogUtil.high(BTAG + "渠道已并行请求过");
                return -4;
            }
            updateFirstPri(paraSupplier);
            parallelAdapter.setSDKSupplier(paraSupplier);

            LogUtil.devDebugAuto(paraSupplier.name, "并行启动, this is :" + parallelAdapter);
            parallelAdapter.load();
            paraInitStatus.put(pri, true);
        } catch (Throwable e) {
            e.printStackTrace();
            return -5;
        }
        return 0;
    }

    private void updateFirstPri(SdkSupplier paraSupplier) {
        try {
            if (paraSupplier == null || currentGroupInf == null) {
                return;
            }
            if (!paraSupplier.useBidding() && currentGroupInf.groupFirstUnbiddingPri < 0) {
                LogUtil.devDebug("(updateFirstPri) pri = " + paraSupplier.priority);
                currentGroupInf.groupFirstUnbiddingPri = paraSupplier.priority;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新bid得结果，仅保留成功渠道，并进行重排序
     */
    private ArrayList<SdkSupplier> getSuccBidResult() {
        ArrayList<SdkSupplier> succBidList = new ArrayList<>();
        try {
            if (isBiddingEmpty()) {
                return succBidList;
            }
            for (SdkSupplier bidItem : biddingGroup.paraSupplierMembers) {
                //成功的
                if (bidItem.resultStatus == AdvanceConstant.SDK_RESULT_CODE_SUCC) {
                    succBidList.add(bidItem);
                }
            }
            LogUtil.devDebug(BTAG + "（getSuccBidResult）succBid size = " + succBidList.size());
            biddingGroup.paraSupplierMembers = succBidList;
            if (biddingGroup.paraSupplierMembers.size() > 0) {
                Collections.sort(biddingGroup.paraSupplierMembers);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return succBidList;
    }

    private void nextGroup() {
        try {
            LogUtil.high(BTAG + "group all failed，go next group");
            if (timeOutHandler != null && timeoutRun != null) {
                //移除超时回调
                timeOutHandler.removeCallbacks(timeoutRun);
            }

            //  这里有可能来自于bidding组，remove不会生效的
            if (supplierGroups != null) {
                supplierGroups.remove(currentGroupInf);
            }
            selectSdkSupplier();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    protected void callSDKSelected(SdkSupplier sdkSupplier) {
        try {
            currentSDKId = sdkSupplier.id;
            currentSdkSupplier = sdkSupplier;

            AdvanceSetting.getInstance().currentSupId = currentSDKId;
            AdvanceSetting.getInstance().isSplashSupportZoomOut = false;//初始化开屏v+支持状态
            LogUtil.simple(BTAG + "selected sdk_id :" + currentSDKId + "（sdk_name : " + sdkSupplier.name + "）");
            if (null != advanceSelectListener) {
                LogUtil.high(BTAG + "currentSdkSupplier.hasCallSelected = " + sdkSupplier.hasCallSelected);
                if (sdkSupplier.hasCallSelected) {
                    return;
                }
                BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                    @Override
                    public void call() {
                        advanceSelectListener.onSdkSelected(currentSDKId);
                    }
                });
                sdkSupplier.hasCallSelected = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //当前并行组是否为取早逻辑配置
    private boolean isCurrentParaEarlyType() {
        boolean isEarlyType = false;
        if (currentParaGroupSetting != null) {
            isEarlyType = currentParaGroupSetting.isEarlyType();
        }
        return isEarlyType;
    }


    //异常时的
    private void catchFailed(String extMsg) {
        try {
            this.advanceError = AdvanceError.parseErr(AdvanceError.ERROR_LOAD_SDK, extMsg);
            reportAdFailed(advanceError, currentSdkSupplier);
            selectSdkSupplier();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 销毁非正在进行的其他渠道，一般是在广告成功展示以后调用。
     *
     * @param sid 正在进行的渠道id
     */
    void destroyOtherSupplier(String sid) {
        try {
            LogUtil.devDebug(BTAG + "destroyOtherSupplier");
            //遍历调用销毁
            if (supplierAdapters != null && supplierAdapters.size() > 0) {
                for (String key : supplierAdapters.keySet()) {
                    //如果是非当前渠道，进行销毁操作
                    if (sid != null && !sid.equals(key)) {
                        BaseParallelAdapter bad = supplierAdapters.get(key);
                        if (bad != null) {
                            bad.destroy();
                        }
                    }
                }
            }
        } catch (Throwable e) {
            LogUtil.e(BTAG + "destroyOtherSupplier catch Throwable");
            e.printStackTrace();
        }
    }

    public void destroy() {
        try {
            //遍历调用销毁
            if (supplierAdapters != null && supplierAdapters.size() > 0) {
                for (String key : supplierAdapters.keySet()) {
                    // 进行销毁操作
                    BaseParallelAdapter bad = supplierAdapters.get(key);
                    if (bad != null) {
                        bad.destroy();
                    }
                }
            }
            if (bidTimeOutHandler != null && bidTimeoutRun != null) {
                //移除超时回调
                bidTimeOutHandler.removeCallbacks(bidTimeoutRun);
            }
            //清除引用信息

            if (getADActivity() != null && fromActivityDestroy) {
                getADActivity().getApplication().unregisterActivityLifecycleCallbacks(alcb);
            }
            advanceSelectListener = null;

            ownLifecycleCallback = null;
            advanceLifecycleCallback = null;
            alcb = null;

            activity = null;
            softReferenceActivity = null;
        } catch (Throwable e) {
            LogUtil.e(BTAG + " do destroy catch Throwable");
            e.printStackTrace();
        }
    }

    public void initAdapter(String sdkID, String clzName) {
        if (supportAdapterInf != null) {
            String fullClz = clzName;
            //针对旧处理方法得修正逻辑
            if (clzName != null && !clzName.contains(BASE_ADAPTER_PKG_PATH)) {
                fullClz = BASE_ADAPTER_PKG_PATH + clzName;
            }
            supportAdapterInf.put(sdkID, fullClz);
        }
    }


    public void initAdapterFullPath(String sdkID, String clzName) {
        if (supportAdapterInf != null) {
            supportAdapterInf.put(sdkID, clzName);
        }
    }

    //重新调度方法
    public void selectSdkSupplier() {
        try {
            //并行组为空，代表无瀑布流策略可执行了，需要检查bidding组是否还要执行
            if (isParaGroupEmpty()) {
                handleOnlyBidding();
            } else {
                selectSuppliers(advanceSelectListener);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 仅处理bidding策略。在所有bidding返回、超时后，进行展示尝试
     */
    private void handleOnlyBidding() {
        try {
            if (isBiddingEmpty()) {
                dispatchSupplierFailed(advanceSelectListener);
            } else {// 仅bidding组有渠道信息
                //将当前bidding组赋值给执行group
                currentGroupInf = biddingGroup;
                currentGroupInf.isBiddingGroup = true;

                if (currentGroupInf.isBiddingAllResult || currentGroupInf.isTimeOut) {
                    //尝试展示bidding组内的渠道，如果都失败了 直接回调失败
                    prepareShowAd(currentGroupInf.paraSupplierMembers, new BYBaseCallBack() {
                        @Override
                        public void call() {
                            dispatchSupplierFailed(advanceSelectListener);
                        }
                    });
                } else {
                    LogUtil.devDebug(BTAG + "等待bidding执行结果");
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 并行得策略组无内容
     *
     * @return true代表无策略信息了
     */
    private boolean isParaGroupEmpty() {
        return supplierGroups == null || supplierGroups.isEmpty();
    }


    //初始化adapter对象

    public abstract void initAdapterData(SdkSupplier sdkSupplier, String clzName);

    public abstract void initSdkSupplier();

//    public abstract void selectSdkSupplier();

    public abstract void selectSdkSupplierFailed();

}
