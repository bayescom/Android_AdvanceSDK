package com.advance.advancesdkdemo.util;

public class DemoManger {

    public  DemoIds currentDemoIds;

    private static DemoManger instance;

    public static synchronized DemoManger getInstance() {
        if (instance == null) {
            instance = new DemoManger();
        }

        return instance;
    }

}
