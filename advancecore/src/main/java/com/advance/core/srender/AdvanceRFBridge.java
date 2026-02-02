package com.advance.core.srender;

import com.advance.BaseAdEventListener;
import com.advance.model.SdkSupplier;

//自渲染配置信息传递桥梁
public interface AdvanceRFBridge extends BaseAdEventListener {
    //   获取到广告信息后， 更新基础data，用来回调开发者 。注意当前一次请求仅支持返回一个广告
    void adapterDidLoaded(AdvanceRFADData renderADData);

    void adapterDidClose(SdkSupplier sdkSupplier);//广告关闭

    int getADSizeW(); //获取期望素材大小，单位px

    int getADSizeH(); //获取期望素材大小，单位px

    AdvanceRFMaterialProvider getMaterialProvider();
}
