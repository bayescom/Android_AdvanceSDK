package com.advance.supplier.honor;

public class AdvanceHonorManager {
    private static AdvanceHonorManager instance;

    public static synchronized AdvanceHonorManager getInstance() {
        if (instance == null) {
            instance = new AdvanceHonorManager();
        }
        return instance;
    }

    public boolean hasInit = false;

}