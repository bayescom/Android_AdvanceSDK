package com.advance.model;

import java.io.Serializable;

public class BiddingInf implements Serializable {
    public int priority = 0;//当前bidding对应的优先级（index）
    public double winPrice = 0; //竞价价格
    public boolean hasBiddingResult = false; //bidding 已返回结果，两种可能：1、超时 2、正常返回了价格、3 失败
    public boolean containsBidding = false; //当前策略中包含了bidding方式
    public boolean isCurrentFirstGroup = false; //当前为第一优先级组

    public boolean doBiddingAllReady = false;//是否当前bidding已全部完成竞价
    public boolean isBiddingWin = false;//是否当前bidding竞价成功

    public boolean isUnBiddingAllFailed = false;//非bidding是否全部失败
    public boolean isUnBiddingWin = false;//非bidding是否全部失败

    @Override
    public String toString() {
        return "BiddingInf{" +
                "priority=" + priority +
                ", winPrice=" + winPrice +
                ", hasBiddingResult=" + hasBiddingResult +
                ", containsBidding=" + containsBidding +
                ", doBiddingAllReady=" + doBiddingAllReady +
                ", isBiddingWin=" + isBiddingWin +
                ", isUnBiddingAllFailed=" + isUnBiddingAllFailed +
                '}';
    }
}
