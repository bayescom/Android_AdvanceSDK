package com.advance.supplier.sigmob;

public class SigmobSetting {
    private static SigmobSetting instance;

    private SigmobSetting() {
    }

    public static synchronized SigmobSetting getInstance() {
        if (instance == null) {
            instance = new SigmobSetting();
        }
        return instance;
    }

    public String lastAppID = "";
    public boolean hasInit = false;

    public String userId = "";

    //开发者自定义传入设备信息
    public boolean isCanUseOaid = false;  // 是否允许sigmob来获取oaid，默认false，将由开发者配置或者基础库中获取的oaid值来代替
    public boolean isCanUseSimOperator = true; //  是否允许sigmob来获取运营商信息、
    public String devSimOperatorCode = "";  //  开发者指定运营商code值
    public String devSimOperatorName = ""; //  开发者指定运营商name


}
