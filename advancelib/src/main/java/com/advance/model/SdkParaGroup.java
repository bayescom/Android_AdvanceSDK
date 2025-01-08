package com.advance.model;

import java.util.ArrayList;

//并行组信息类
public class SdkParaGroup {
    public int groupID = 0;
    //并行的组成员(包含bidding成员)
    public ArrayList<SdkSupplier> paraSupplierMembers = new ArrayList<>();
    //bidding的组成员
//    public ArrayList<SdkSupplier> biddingSuppliers = new ArrayList<>();
    //有广告返回的 最优渠道，一般是比价后胜出的那一个渠道、如果未包含比较渠道，则是价格最高的渠道
    public SdkSupplier bestSupplier;

    public double maxPrice;//组内最高价格
    public double minPrice;//组内最低价格

    //是否包含bidding广告位的组
    public boolean isBiddingGroup = false;
    //是否所有bidding已有返回结果
    public boolean isBiddingAllResult = false;
    //组内成员已经全部返回
    public boolean isGroupAllResult = false;
    //组内是否有成功返回广告的渠道,true 代表组内SDK有成功返回广告，false代表都失败、超时等因素
//    public boolean isGroupHasSuccess = false;
    //是否当前层级已经请求超时
    public boolean isTimeOut = false;

    //组内非bidding渠道的最高优先级，如果执行到当前优先级，且不支持并行，需要自动发起串行请求逻辑，否则会导致流程卡死
    public int groupFirstUnbiddingPri = -1;




    @Override
    public String toString() {
        return "SdkParaGroup{" +
                "groupID=" + groupID +
                ", paraSupplierMembers size = " + paraSupplierMembers.size() +
                ", bestSupplier=" + bestSupplier +
                ", maxPrice=" + maxPrice +
                ", minPrice=" + minPrice +
                ", isBiddingGroup=" + isBiddingGroup +
                ", isBiddingAllResult=" + isBiddingAllResult +
                ", isGroupAllResult=" + isGroupAllResult +
                ", isTimeOut=" + isTimeOut +
                ", groupFirstUnbiddingPri=" + groupFirstUnbiddingPri +
                '}';
    }
}
