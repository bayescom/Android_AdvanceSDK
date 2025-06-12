package com.advance.itf;

//广告渲染相关抽象方法，目前仅对信息流、插屏、激励视频、全屏视频、draw视频位置生效，
public interface RenderEvent {
    //开屏、banner adapter渠道若未支持单独show方法，不需要处理此方法下内容
    void show();

    boolean isValid();// 广告是否有效
}
