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


}
