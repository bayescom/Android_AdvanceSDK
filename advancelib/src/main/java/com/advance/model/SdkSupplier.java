package com.advance.model;

import android.text.TextUtils;

import com.advance.AdvanceConfig;
import com.advance.AdvanceConstant;
import com.advance.utils.LogUtil;

import org.json.JSONArray;

import java.util.ArrayList;

public class SdkSupplier implements Comparable<SdkSupplier> {
    public String id = "0";
    public String name = "默认SDK";
    public int priority = 1;
    public int timeout = 5000;
    public String adspotid;
    public String mediaid;
    public String mediakey;
    public String mediaSecret;
    public String sdkTag;
    public int versionTag = -1;
    public int adCount = AdvanceConfig.DEFAULT_AD_COUNT;
    public ArrayList<String> imptk;
    public ArrayList<String> clicktk;
    public ArrayList<String> succeedtk;
    public ArrayList<String> failedtk;
    public ArrayList<String> loadedtk;
    public ArrayList<String> starttk;
    //v4.3.0新增，sdk 竞胜上报(仅在开启竞价情况下返回)。SDK调用时机为，发起广告展现方法时
    public ArrayList<String> wintk;
    public JSONArray ext;
    public String advanceAdspotId = "";
    public int initOpt = 1; //初始化优化是否开启，1代表开启，开启后只进行一次初始化操作。当有可能填两个媒体id时，将该值置为false
    public boolean enableBidding = false; //是否开启bidding逻辑，前提是当前渠道支持bidding
    //    public int sortIndex = 1;//排序用的字段，正常情况下值和priority一样，当有bidding渠道时，会出现差异
    public double price = 0; //SDK价格，一般是手动输入，单位（分、人民币）
    public double bidResultPrice = 0; //竞价后返回的价格
    public double bidRatio = 1; //参与竞价比价系数，比价时需要乘以此系数
    public boolean isFirstGroup = false; //是否为第一优先级数组（主要是bidding逻辑中涉及判断）
    public boolean hasCallSelected = false; //是否已经回调过选中方法
    public boolean hasCallShow = false; //当用户提前调用了show方法，依然可以收到广告后执行show

    public int resultStatus = AdvanceConstant.SDK_RESULT_CODE_DEFAULT;//渠道执行结果标志，成功此值置为1，失败为2，仅支持串行为3
    public int groupID = -1; //当前渠道所在的组id

    //支持bidding的渠道，获取实时价格，只有部分渠道位置支持。支持的含义为此渠道会返回实时价格，且聚合内包含了相关处理和调度逻辑
    //
    //mercury 支持当前为仅接口支持，实际下发价格信息后端暂未支持
    // 2023-10-31 不再进行bidding support本地处理，全部通过接口下发控制
    public boolean isSupportBidding() {
        return true;
    }

    //是否能够bidding，后台开关打开+渠道可支持
    public boolean useBidding() {
        return enableBidding && isSupportBidding();
    }



    public SdkSupplier() {
    }

    /**
     * 自定义渠道（非mercury、gdt、穿山甲、）打底设置方法，除了要设置广告位和渠道supplierID，还要设置渠道的媒体id
     *
     * @param mediaid    自定义渠道的媒体id
     * @param adspotId   自定义渠道的广告位id
     * @param supplierID 自定义渠道的id
     */
    @Deprecated
    public SdkSupplier(String mediaid, String adspotId, String supplierID) {
        this.mediaid = mediaid;
        this.adspotid = adspotId;
        this.id = supplierID;
    }

    /**
     * 已有渠道（mercury、gdt、穿山甲、）打底设置方法，只需要设置广告位和对应的 supplierID即可
     *
     * @param adspotId
     * @param supplierID
     */
    @Deprecated
    public SdkSupplier(String mediaid, String adspotId, AdvanceSupplierID supplierID) {
        this.mediaid = mediaid;
        this.adspotid = adspotId;
        this.id = supplierID.nativeInt;
    }

    /**
     * 已有渠道（mercury、gdt、穿山甲、）打底设置方法，只需要设置广告位和对应的 supplierID即可
     *
     * @param adspotId
     * @param supplierID
     */
    @Deprecated
    public SdkSupplier(String adspotId, AdvanceSupplierID supplierID) {
        try {
            this.adspotid = adspotId;
            this.id = supplierID.nativeInt;
            switch (supplierID) {
                case CSJ:
                    this.sdkTag = AdvanceConfig.SDK_TAG_CSJ;
                    this.mediaid = AdvanceConfig.getInstance().getCsjAppId();
                    if (TextUtils.isEmpty(mediaid)) {
                        LogUtil.e("Advance初始时化未配置穿山甲媒体ID，打底设置未生效");
                    }
                    break;
                case GDT:
                    this.sdkTag = AdvanceConfig.SDK_TAG_GDT;
                    this.mediaid = AdvanceConfig.getInstance().getGdtMediaId();
                    if (TextUtils.isEmpty(mediaid)) {
                        LogUtil.e("Advance初始化时未配置广点通媒体ID，打底设置未生效");
                    }
                    break;
                case BD:
                    this.sdkTag = AdvanceConfig.SDK_TAG_BAIDU;
                    this.mediaid = AdvanceConfig.getInstance().getBdAppId();
                    if (TextUtils.isEmpty(mediaid)) {
                        LogUtil.e("Advance初始化时未配置百度媒体ID，打底设置未生效");
                    }
                    break;
                case KS:
                    this.sdkTag = AdvanceConfig.SDK_TAG_KS;
                    this.mediaid = AdvanceConfig.getInstance().getKsAppId();
                    if (TextUtils.isEmpty(mediaid)) {
                        LogUtil.e("Advance初始化时未配置快手媒体ID，打底设置未生效");
                    }
                    break;
                case MERCURY:
                    this.sdkTag = AdvanceConfig.SDK_TAG_MERCURY;
                    this.mediaid = AdvanceConfig.getInstance().getMercuryMediaId();
                    this.mediakey = AdvanceConfig.getInstance().getMercuryMediaKey();
                    if (TextUtils.isEmpty(mediaid) || TextUtils.isEmpty(mediakey)) {
                        LogUtil.e("Advance初始化时未配置Mercury媒体信息，打底设置未生效");
                    }
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    //新排序规则。优先比价格，价格越大排序越靠前，价格一样优先级越小，排序越靠前
    @Override
    public int compareTo(SdkSupplier o) {

        if (this.price > o.price) {
            return -1;
        } else if (this.price == o.price) {
            if (this.priority < o.priority) {
                return -1;
            } else if (this.priority == o.priority) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "SdkSupplier{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", priority=" + priority +
                ", timeout=" + timeout +
                ", adspotid='" + adspotid + '\'' +
                ", mediaid='" + mediaid + '\'' +
                ", mediakey='" + mediakey + '\'' +
                ", mediaSecret='" + mediaSecret + '\'' +
                ", sdkTag='" + sdkTag + '\'' +
                ", versionTag=" + versionTag +
                ", adCount=" + adCount +
                ", hasCallSelected=" + hasCallSelected +
//                ", imptk=" + imptk +
//                ", clicktk=" + clicktk +
//                ", succeedtk=" + succeedtk +
//                ", failedtk=" + failedtk +
                ", loadedtk=" + loadedtk +
                ", ext=" + ext +
                ", advanceAdspotId='" + advanceAdspotId + '\'' +
                ", initOpt=" + initOpt +
                ", enableBidding=" + enableBidding +
                ", useBidding=" + useBidding() +
                ", price=" + price +
//                ", bidRatio=" + bidRatio +
//                ", rp=" + getRealPrice() +
                ", isFirstGroup=" + isFirstGroup +
                ", resultStatus=" + resultStatus +
                ", groupID=" + groupID +
                '}';
    }
}
