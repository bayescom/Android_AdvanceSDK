package com.advance;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.advance.model.AdvanceError;
import com.advance.model.AdvanceReportModel;

import java.util.ArrayList;

public interface SplashSetting extends BaseAdEventListener {

    int getCsjAcceptedSizeWidth();

    int getCsjAcceptedSizeHeight();

    float getCsjExpressViewWidth();

    float getCsjExpressViewHeight();

    boolean getCsjShowAsExpress();

    void adapterDidSkip();

    void adapterDidQuit();//与adapterDidFailed渠道广告失败不同，quit代表直接返回失败，不再进行策略的调度了

    void adapterDidTimeOver();

    Drawable getLogoImage();

    Drawable getHolderImage();

//    View getGdtSkipContainer();

    boolean isGdtClickAsSkip();

//    boolean isCsjTimeOutQuit();

//    boolean isGdtCustomSkipHide();

    ViewGroup getAdContainer(); //去除logo部分的承载布局

    ViewGroup getAdContainerOri(); //原始的广告承载布局

    int getLogoLayoutRes();

    int getLogoLayoutHeight();

    TextView getSkipView();

    ImageView getGDTHolderView();

    String getSkipText();

    boolean isShowInSingleActivity();
}
