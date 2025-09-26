package com.advance.supplier.mi;

public class AdvanceXMManager {
    private static AdvanceXMManager instance;

    public static synchronized AdvanceXMManager getInstance() {
        if (instance == null) {
            instance = new AdvanceXMManager();
        }
        return instance;
    }

    public boolean hasInit = false;

}
