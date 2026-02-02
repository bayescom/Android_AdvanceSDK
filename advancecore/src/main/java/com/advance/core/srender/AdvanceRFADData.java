package com.advance.core.srender;

import androidx.annotation.Keep;

import com.advance.core.model.AdvanceSdkSupplier;

import java.util.List;

@Keep
public interface AdvanceRFADData {

    //获取当前的SDK渠道信息
    AdvanceSdkSupplier getSdkSupplier();

    String getTitle(); //获取广告标题，短文字

    String getDesc();//获取广告描述，长文字

    String getIconUrl(); //获取 Icon 图片地址

    String getSourceText(); //获取来源信息

    String getVideoImageUrl();//获取视频定帧图

    List<String> getImgList(); //获取图片地址、可能为多图广告

    boolean isDownloadAD(); //判断广告策略是否为app下载类广告

    boolean isVideo(); //判断是否为视频类广告

    int getECPM(); //价格

    AdvanceRFDownloadElement getDownloadElement(); //获取下载六要素信息

}
