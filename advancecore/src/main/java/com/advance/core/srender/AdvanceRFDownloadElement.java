package com.advance.core.srender;

import com.bayes.sdk.basic.itf.BYAbsCallBack;

import java.io.Serializable;
import java.util.ArrayList;

//下载要素信息
public interface AdvanceRFDownloadElement {


    /**
     * 获取应用名称
     */
    String getAppName();

    /**
     * 获取应用版本号
     */
    String getAppVersion();

    /**
     * 获取开发者公司名称
     */
    String getAppDeveloper();

    /**
     * 获取隐私协议
     */
    String getPrivacyUrl();


    /**
     * 获取权限列表url
     */
    String getPermissionUrl();

    /**
     * 权限信息列表，如果没有url信息，使用此信息来展示权限页面、部分SDK会返回
     */
    void getPermissionList(BYAbsCallBack<ArrayList<AdvDownloadPermissionModel>> callBack);

    class AdvDownloadPermissionModel implements Serializable {
        public String permTitle;
        public String permDesc;
    }


    /**
     * 获取产品功能url ,可能为空，穿山甲v5.4.0.3版本之前不支持该信息返回
     */
    String getFunctionDescUrl();


    /**
     * 获取产品功能说明文字，可能为空，穿山甲、优量汇无此信息
     */
    String getFunctionDescText();


    /**
     * 获取目标APP文件大小，单位byte
     */
    long getPkgSize();


}
