package com.advance.model;

import com.bayes.sdk.basic.model.BYError;

public class AdvanceError extends BYError {

    public static final String ERROR_DEFAULT = "99";

    public static final int ERROR_INIT_DEFAULT = 999;
    public static final String ERROR_DATA_NULL = "9901";
    public static final String ERROR_EXCEPTION_LOAD = "9902";
    public static final String ERROR_EXCEPTION_SHOW = "9903";
    public static final String ERROR_EXCEPTION_RENDER = "9904";
    public static final String ERROR_NONE_SDK = "9905";
    public static final String ERROR_SUPPLIER_SELECT = "9906";
    public static final String ERROR_SUPPLIER_SELECT_FAILED = "9907";
    public static final String ERROR_LOAD_SDK = "9908";
    public static final String ERROR_CSJ_SKIP = "9909";
    public static final String ERROR_CSJ_TIMEOUT = "9910";
    public static final String ERROR_BD_FAILED = "9911";
    public static final String ERROR_PARAM_FORMAT = "9912"; //参数异常
    public static final String ERROR_NO_ACTIVITY = "9913"; //无activity异常
    public static final String ERROR_KS_INIT = "9914"; //无activity异常
    public static final String ERROR_RENDER_FAILED = "9915"; //渲染失败异常
    public static final String ERROR_TANX_FAILED = "9916"; //tanx 失败code
    public static final String ERROR_GROUP_TIMEOUT = "9920"; //达到并行单层超时时间
    public static final String ERROR_ADD_VIEW = "9921"; //添加广告布局时失败，布局信息为空
    public static final String ERROR_TAP_INIT = "9922"; //tap sdk 初始化失败
    public static final String ERROR_TAP_RENDER_ERR = "9923"; //tap sdk 广告渲染失败，无具体错误码信息
    public static final String ERROR_VIDEO_RENDER_ERR = "9924"; //视频广告渲染失败
    public static final int ERROR_CSJ_NOT_READY = 9925; //穿山甲未准备好
    public static final int ERROR_REWARD_SERVER_VERIFY_EMPTY_SDK = 9930; //激励验证失败，无SDK广告信息
    public static final int ERROR_REWARD_SERVER_VERIFY_JSON_DECODE_FAILED = 9931; //服务端验证激励奖励，json结果解析失败


    public AdvanceError(String code, String msg) {
        super(code, msg);
        this.code = code;
        this.msg = msg;
    }

    public String code;
    public String msg;


    public static AdvanceError parseErr(String code) {
        return parseErr(code, "");
    }

    public static AdvanceError parseErr(int code, String extMsg) {
        return parseErr(code + "", extMsg);
    }

    public static AdvanceError parseErr(String code, String extMsg) {
        AdvanceError error;
        switch (code) {
            case ERROR_DATA_NULL:
                error = new AdvanceError(code, "data null ;" + extMsg);
                break;
            case ERROR_EXCEPTION_LOAD:
                error = new AdvanceError(code, "exception when load ;view System.err log for more ; msg = " + extMsg);
                break;
            case ERROR_EXCEPTION_SHOW:
                error = new AdvanceError(code, "exception when show ;view System.err log for more ; msg =  " + extMsg);
                break;
            case ERROR_EXCEPTION_RENDER:
                error = new AdvanceError(code, "exception when render ;view System.err log for more ; msg =  " + extMsg);
                break;
            case ERROR_NONE_SDK:
                error = new AdvanceError(code, "none sdk to show ;" + extMsg);
                break;
            case ERROR_SUPPLIER_SELECT:
                error = new AdvanceError(code, "策略调度异常，selectSdkSupplier Throwable ;" + extMsg);
                break;
            case ERROR_SUPPLIER_SELECT_FAILED:
                error = new AdvanceError(code, "无策略返回 selectSdkSupplierFailed " + extMsg);
                break;
            case ERROR_LOAD_SDK:
                error = new AdvanceError(code, "sdk 启动异常  " + extMsg);
                break;
            case ERROR_CSJ_SKIP:
                error = new AdvanceError(code, "穿山甲SDK加载超时，不再加载后续SDK渠道  " + extMsg);
                break;
            case ERROR_CSJ_TIMEOUT:
                error = new AdvanceError(code, "穿山甲SDK加载超时  " + extMsg);
                break;
            case ERROR_BD_FAILED:
                error = new AdvanceError(code, "百度SDK加载失败  " + extMsg);
                break;
            case ERROR_PARAM_FORMAT:
                error = new AdvanceError(code, "快手SDK加载失败，广告位id类型转换异常  " + extMsg);
                break;
            case ERROR_NO_ACTIVITY:
                error = new AdvanceError(code, "当前activity已被销毁，不再请求广告  " + extMsg);
                break;
            case ERROR_KS_INIT:
                error = new AdvanceError(code, "快手SDK初始化失败  " + extMsg);
                break;
            case ERROR_RENDER_FAILED:
                error = new AdvanceError(code, "广告渲染失败  " + extMsg);
                break;
            case ERROR_TANX_FAILED:
                error = new AdvanceError(code, "tanx请求失败" + extMsg);
                break;
            case ERROR_GROUP_TIMEOUT:
                error = new AdvanceError(code, "并行请求超时" + extMsg);
                break;
            case ERROR_ADD_VIEW:
                error = new AdvanceError(code, "添加广告布局失败，布局信息为空" + extMsg);
                break;
            case ERROR_INIT_DEFAULT + "":
                error = new AdvanceError(code, "广告SDK初始化失败  " + extMsg);
                break;

            default:
                //广点通的详细异常码
                String extCode = "";
                try {
                    if ("6000".equals(code) && !extMsg.isEmpty()) {
                        int startIndex = extMsg.lastIndexOf("详细码") + 4;
                        extCode = extMsg.substring(startIndex, startIndex + 6);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                error = new AdvanceError(ERROR_DEFAULT + "_" + code + extCode, "err from sdk callback : [" + code + "] " + extMsg);
        }


        return error;
    }
}
