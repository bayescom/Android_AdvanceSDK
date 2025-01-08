package com.advance.supplier.tap;

import com.tapsdk.tapad.CustomUser;
import com.tapsdk.tapad.TapAdNative;

import java.util.HashMap;

public class AdvanceTapManger {
    private static AdvanceTapManger instance;

    private AdvanceTapManger() {
    }

    public static synchronized AdvanceTapManger getInstance() {
        if (instance == null) {
            instance = new AdvanceTapManger();
        }
        return instance;
    }

    //初始化时传递得用户画像信息
    public CustomUser customUser;
    public String customMediaName = "";
    public String customMediaVersion = "";
    public String customGameChannel = "";
    public String customTapClientId = "";


    //根据activity的hashcode，记录每一个activity的TapAdNative信息，进行复用，防止异常
    public HashMap<String, TapAdNative> tapADMap = new HashMap<>();

    //广告位初始化传递参数信息
    public String customTapUserId = "";


}
