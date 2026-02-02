package com.advance.model;

import java.io.Serializable;

public class StrategyReadyInf implements Serializable {
    //是否需要集成gromore
    public boolean needGM = false;
    //配在策略上的单层超时时间
    public int singleTimeOut = 5000;

    //gromore
    public SdkSupplier gmInf ;

    @Override
    public String toString() {
        return "StrategyReadyInf{" +
                "needGM=" + needGM +
                ", singleTimeOut=" + singleTimeOut +
                ", gmInf=" + gmInf +
                '}';
    }
}
