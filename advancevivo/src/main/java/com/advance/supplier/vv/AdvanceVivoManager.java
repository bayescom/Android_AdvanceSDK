package com.advance.supplier.vv;

public class AdvanceVivoManager {
    private static AdvanceVivoManager instance;

    public static synchronized AdvanceVivoManager getInstance() {
        if (instance == null) {
            instance = new AdvanceVivoManager();
        }
        return instance;
    }

    //SDK是否执行完成初始化
    public boolean hasInit = false;

    //是否允许个性化广告
    public boolean allowPersonalRecommend = true;
}