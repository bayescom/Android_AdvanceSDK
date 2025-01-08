package com.advance;

import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;
import com.advance.model.BiddingInf;
import com.advance.model.SdkSupplier;
import com.advance.model.SupplierSettingModel;

import java.util.ArrayList;

public interface BaseSetting {
    boolean needDelayReport(); //是否进行延迟上报

    @Deprecated
    void adapterDidFailed(AdvanceError advanceError);//旧聚合失败回调

    void adapterDidFailed(AdvanceError advanceError, SdkSupplier sdkSupplier);//聚合失败回调,回传sdkSupplier信息

    Long getRequestTime();//获取聚合请求发起的时间戳

    String getAdvanceId();//获取聚合请求的唯一策略id

    void paraEvent(int type, AdvanceError advanceError, SdkSupplier sdkSupplier);//并行回调事件,type 为事件类型

    void trackReport(AdvanceReportModel trackModel);

    ArrayList<ArrayList<String>> getSavedReportUrls();//获取保存了的延迟上报数组

    SdkSupplier getCurrentSupplier(); //获取当前的渠道策略信息

    SupplierSettingModel.ParaGroupSetting getCurrentParaGroupSetting();//获取当前并行组的设置信息

    BiddingInf getBiddingResultInf();//获取bidding价格

    boolean isLoadAsync();//是否异步线程中 load
//    boolean isCurrentGroupTimeOut();
}
