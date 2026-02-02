package com.advance.model;

import java.io.Serializable;
import java.util.ArrayList;

public class ElevenModel implements Serializable {
    public int code;
    public String msg;
    public String reqid;
    public SupplierSettingModel setting;
    public ArrayList<SdkSupplier> suppliers;
    public GMParams gmParams;
    public ServerRewardModel serverReward;

    public String httpResult="";

    /**
     *
     * @return 是否需要延迟上报、同时适用于并行、串行
     */
    public boolean isReportDelay() {
        if (setting == null) {
            return false;
        }
        return setting.delayReport == 1;
    }
}
