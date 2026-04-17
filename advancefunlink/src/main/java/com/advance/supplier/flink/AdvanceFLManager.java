package com.advance.supplier.flink;

public class AdvanceFLManager {
    private static AdvanceFLManager instance;

    public static synchronized AdvanceFLManager getInstance() {
        if (instance == null) {
            instance = new AdvanceFLManager();
        }
        return instance;
    }

    public boolean hasInit = false;

    public boolean canUseBootId = true;

}
