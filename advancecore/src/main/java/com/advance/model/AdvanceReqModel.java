package com.advance.model;

public class AdvanceReqModel {
    public String adspotId = "";
    public String mediaId = "";
    public String reqId = "";//SDK端生成的请求唯一标记。当使用策略缓存时，需要和延迟请求的策略使用同一个id，方便日志统计join
    public boolean forceCache = false; //强制缓存标记，true则强制进行策略的缓存。
    public boolean isFromImm = false; //true 代表此次广告加载的第二次策略请求，用于日志去重。
    public boolean isCacheEffect = false; //策略缓存是否生效，用来标记当前执行情况。true 代表此次请求缓存策略生效
    public boolean requestForceTimeout = false; //是否强制校验网络超时

}
