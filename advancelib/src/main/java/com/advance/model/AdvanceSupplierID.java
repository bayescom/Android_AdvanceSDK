package com.advance.model;

import com.advance.AdvanceConfig;

public enum AdvanceSupplierID {
    /**
     * 倍联SDK ID
     */
    MERCURY(AdvanceConfig.SDK_ID_MERCURY),
    /**
     * 广点通SDK ID
     */
    GDT(AdvanceConfig.SDK_ID_GDT),
    /**
     * 穿山甲SDK ID
     */
    CSJ(AdvanceConfig.SDK_ID_CSJ),
    /**
     * 百度 ID
     */
    BD(AdvanceConfig.SDK_ID_BAIDU),
    /**
     * 快手 ID
     */
    KS(AdvanceConfig.SDK_ID_KS);

    AdvanceSupplierID(String ni) {
        nativeInt = ni;
    }

    final String nativeInt;
}
