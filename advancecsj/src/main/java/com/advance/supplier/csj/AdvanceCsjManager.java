package com.advance.supplier.csj;

public class AdvanceCsjManager {
    //默认值，会执行advance原有穿山甲初始化逻辑
    public static final int INIT_STATUS_DEFAULT = 0;
    //以下三个状态，会影响穿山甲初始化执行逻辑，由外部APP调用改变
    public static final int INIT_STATUS_CALLING = 1;
    public static final int INIT_STATUS_SUCCESS = 2;
    public static final int INIT_STATUS_FAILED = 3;

    public AdvanceCsjManager() {
    }

    public static AdvanceCsjManager get() {
        return configHolder.configIns;
    }

    public static class configHolder {
        private static final AdvanceCsjManager configIns = new AdvanceCsjManager();
    }

    //APP自己执行的穿山甲初始化方法，调用过穿山甲api后需要调用此方法通知advanceSDK
    public static void outerInitCalled() {
        try {
            AdvanceCsjManager.get().initStatus = INIT_STATUS_CALLING;
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    //通知初始化执行成功
    public static void outerInitSuccess() {
        try {
            AdvanceCsjManager.get().initStatus = INIT_STATUS_SUCCESS;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //通知初始化执行失败
    public static void outerInitFailed(int code, String msg) {
        try {
            AdvanceCsjManager.get().initStatus = INIT_STATUS_FAILED;
            AdvanceCsjManager.get().initErrCode = code;
            AdvanceCsjManager.get().initErrMsg = msg;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //记录初始化状态
    protected int initStatus = INIT_STATUS_DEFAULT;

    //初始化失败信息记录
    protected int initErrCode;
    protected String initErrMsg;


    //内部初始化状态标记
    public int innerInitStatus = INIT_STATUS_DEFAULT;
    public int innerInitErrCode;
    public String innerInitErrMsg;
}
