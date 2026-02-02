package com.advance.core.srender;

import android.view.View;
import android.widget.ImageView;

import com.advance.core.srender.widget.AdvRFLogoView;
import com.advance.core.srender.widget.AdvRFRootView;
import com.advance.core.srender.widget.AdvRFVideoView;
import com.bayes.sdk.basic.model.BYBaseModel;

import java.util.ArrayList;

//开发者接入端，传递的必要信息，比如素材view、回调监听等信息，用来和渲染事件进行联动
public class AdvanceRFMaterialProvider extends BYBaseModel {
    //根布局view，必需
    public AdvRFRootView rootView;
    //视频view，必需
    public AdvRFVideoView videoView;
    //广告logo标识，必须
    public AdvRFLogoView logoView;
    //点击view，必需
    public ArrayList<View> clickViews = new ArrayList<>();
    //创意按钮view，可选，
    public ArrayList<View> creativeViews = new ArrayList<>();
    //关闭按钮view，不同adn可能广告回调关闭时机不同，穿山甲有dislike逻辑
    public View disLikeView;
    //主图片列表，仅sigmob会用到，且会自己执行渲染逻辑 (目前测试即使不给赋值，依然可以正常开发者自己调用url渲染广告，并且正常曝光)
    public ArrayList<ImageView> imageViews = new ArrayList<>();
    //下载监听器，仅穿山甲支持
    public AdvanceRFDownloadListener downloadListener;
    //视频播放监听器
    public AdvanceRFVideoEventListener videoEventListener;
    //视频设置选项，对优量汇、mercury生效
    public AdvanceRFVideoOption videoOption;

}
