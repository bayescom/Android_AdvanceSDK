package com.advance.core.srender;

import com.advance.model.AdvanceError;

//自渲染视频播放事件回调
public interface AdvanceRFVideoEventListener {
    void onReady(AdvanceRFADData data);

    void onPlayStart(AdvanceRFADData data);

    void onPlaying(AdvanceRFADData data, long current, long duration);

    void onPause(AdvanceRFADData data);

    void onResume(AdvanceRFADData data);

    void onComplete(AdvanceRFADData data);

    void onError(AdvanceRFADData data, AdvanceError error);
}
