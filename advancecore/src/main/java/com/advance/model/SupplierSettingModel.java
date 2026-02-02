package com.advance.model;

import com.advance.AdvanceConstant;

import java.io.Serializable;
import java.util.ArrayList;

public class SupplierSettingModel implements Serializable {
    public int enableStrategyCache = -1;//后台下发配置类字段，是否启用策略缓存,1启用，0不启用，默认不配置 -1
    public int strategyCacheDuration = -1;//后台下发配置类字段，策略缓存的持续时间，超过此时间策略缓存过期，单位秒
    public int reportVersionInf = -1;//后台下发配置类字段，是否需要上报渠道版本号信息，默认-1不进行上报，1 启用上报，将在下次请求广告时，附带上各个SDK渠道的版本号信息。

    public ArrayList<Integer> biddingGroup = new ArrayList<>(); //v4.0.1 新增，后台下发的bidding组
    public ArrayList<ArrayList<Integer>> paraGroup = new ArrayList<>(); //后台下发并行的分组设置字段，二维数组，每一组内的成员进行并行请求，组之间串行(v3.5.0版本及之后的并行方式)
    public int delayReport = -1;//并行时是否进行延迟上报， 0 否，1 是，-1为后端未返回状态信息。默认不进行延迟上报，仅开屏比较需要此功能
    //v3.5.4 新增
    //对应并行组的一些配置字段
    public ArrayList<ParaGroupSetting> paraGroupSetting = new ArrayList<>();

    public int parallelTimeout = AdvanceConstant.DEFAULT_PARA_TIMEOUT;
    public int biddingType = AdvanceConstant.DEFAULT_BIDDING_TYPE;

    //并行组内的个性化设置
    public static class ParaGroupSetting {
        //并行组执行模式，0代表正常优先级顺序执行，1代表执行选早逻辑，不再严格按照顺序执行
        public int type = -1;
        //组内请求但未展示的渠道是否缓存； 0不缓存，1缓存
        public int cache = -1;

        //并行取早组，已成功的渠道列表，列表中内容为渠道对应的优先级字段
        public ArrayList<Integer> successList = new ArrayList<>();


        public boolean isEarlyType() {
            return type == 1;
        }

        public boolean canCache() {
            return cache == 1;
        }
    }
}
