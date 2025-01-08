package com.advance;

public class AdvanceConstant {
    /**
     * 并行请求广告状态标志
     **/
    //初始值
    public static final int AD_STATUS_DEFAULT = -1;
    //发起了请求
    public static final int AD_STATUS_LOADING = 0;
    //广告请求成功
    public static final int AD_STATUS_LOAD_SUCCESS = 1;
    //广告失败
    public static final int AD_STATUS_LOAD_FAILED = 2;
    //广告收到后请求展示
    public static final int AD_STATUS_LOADED_SHOW = 3;
    //广告未收到，但请求了展示
    public static final int AD_STATUS_LOADING_SHOW = 4;
    //请求结束
    public static final int AD_STATUS_END = 5;
    //bidding完成后重排序，再执行广告展示流程
    public static final int AD_STATUS_BIDDING = 6;

    public static final int strategyTimeOutDur = 5000;//策略请求超时时长，单位毫秒。超过该时长会回调开发者，策略失败。不再进行广告填充


    /**
     * 并行事件的type值
     */
    static final int EVENT_TYPE_ORDER = -1; //转串行标记

    static final int EVENT_TYPE_LOADED = 0;
    static final int EVENT_TYPE_SUCCEED = 1;
    static final int EVENT_TYPE_SHOW = 2;
    static final int EVENT_TYPE_ERROR = 3;
    static final int EVENT_TYPE_CACHED = 4;


    /**
     * 缓存标志状态
     */
    public static final int STATUS_UNCACHED = 0; //未缓存成功，初始状态
    public static final int STATUS_CACHED_CALL = 1; //进行缓存成功的回调
    public static final int STATUS_CACHED = 2; //缓存成功

    //聚合埋点的code值
    public static final String TRACE_SPLASH_ERROR = "1000";
    public static final String TRACE_SPLASH_FORCE_CLOSE = "1001";
    @Deprecated
    public static final String TRACE_NO_ACTIVITY = "1010";
    public static final String TRACE_STRATEGY_ERROR = "1012";
    public static final String TRACE_TOTAL_TIME = "1020";


    public static final String BOTTOM_DEFAULT_TAG = "bottom_default";
    public static final String URL_REQID_TAG = "reqid";
    //标记位，并行的缓存成功
    public static final String TAG_PARA_CACHED = "10000";


    /**
     * sp存储标记
     */

    public static final String SP_SETTING_REPORT = "sp_setting_report";




    //默认的并行组超时时间
    public static final int DEFAULT_PARA_TIMEOUT = 5000;
    //默认bidding方式， gromore不参与
    public static final int DEFAULT_BIDDING_TYPE = 0;

    /**
     * SDK执行结果标记，，仅支持串行为-1 , 成功此值置为1，失败为2，超时为4 值大于0都是代表得执行结果
     */
    public static final int SDK_RESULT_CODE_DEFAULT = 0;
    public static final int SDK_RESULT_CODE_ORDER = -1;
    public static final int SDK_RESULT_CODE_SUCC = 1;
    public static final int SDK_RESULT_CODE_FAILED = 2;
    public static final int SDK_RESULT_CODE_TIMEOUT = 4;
}
