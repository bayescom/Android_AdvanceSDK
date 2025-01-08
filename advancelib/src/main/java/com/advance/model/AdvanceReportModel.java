package com.advance.model;

public class AdvanceReportModel {
    public String code;
    public String msg;
    public int status = -1; // 0代表SDK渠道获取异常、无网络、无策略等 ，2代表SDK渠道全部加载失败了

    public String reqid;
    public String adspotid;
    public String supadspotid;
    public String ext;

    public boolean needDelay =false;
}
