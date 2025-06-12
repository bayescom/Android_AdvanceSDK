package com.advance.supplier.oppo;

public class AdvanceOppoManger {
    private static AdvanceOppoManger instance;

    private AdvanceOppoManger() {
    }

    public static synchronized AdvanceOppoManger getInstance() {
        if (instance == null) {
            instance = new AdvanceOppoManger();
        }
        return instance;
    }

    public boolean hasOppoInit = false;
    public String lastOppoAID = "";

}
