package com.advance.supplier.tanx;


import com.alimm.tanx.core.ad.ad.table.screen.model.TableScreenParam;
import com.alimm.tanx.core.ad.bean.RewardParam;
import com.alimm.tanx.core.image.ILoader;

public class AdvanceTanxSetting {
    private static AdvanceTanxSetting instance;

    private AdvanceTanxSetting() {
    }

    public static synchronized AdvanceTanxSetting getInstance() {
        if (instance == null) {
            instance = new AdvanceTanxSetting();
        }
        return instance;
    }


    public ILoader iLoader = null;
    //激励设置
    public String mediaUID = "";
    public RewardParam rewardParam = null;

    //    插屏设置
    boolean interClickAdClose = false;
    TableScreenParam tableScreenParam = new TableScreenParam();
}
