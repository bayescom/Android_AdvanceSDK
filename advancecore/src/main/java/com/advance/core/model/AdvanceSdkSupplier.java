package com.advance.core.model;

import androidx.annotation.Keep;

//对外暴露的广告渠道信息
@Keep
public class AdvanceSdkSupplier {
    //SDK唯一标识，区分不同adn
    public String adnId;
    //渠道代码位id
    public String adspotId;
    //优先级信息
    public int priority;
}
