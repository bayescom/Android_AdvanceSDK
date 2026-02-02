package com.advance.core.srender;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.advance.BaseParallelAdapter;
import com.advance.model.AdvanceError;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.device.BYDisplay;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYStringUtil;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.mercury.sdk.util.MercuryTool;

public class AdvanceRFUtil {
    public static final String TAG = "[AdvanceRFUtil] ";

    //复制子控件至新布局，并将新布局添加至旧父布局中，等于在中间插入一层
    public static void copyChild(final ViewGroup oriParent, final ViewGroup toParent) {
        try {
            BYThreadUtil.switchMainThread(new BYBaseCallBack() {
                @Override
                public void call() {
                    int childSize = oriParent.getChildCount();
                    LogUtil.devDebug(TAG + "  childSize = " + childSize);
                    if (childSize > 0) {
                        for (int i = 0; i < childSize; i++) {
                            View child = oriParent.getChildAt(0);
                            oriParent.removeView(child);
                            if (child != null) {
                                toParent.addView(child, i);
                            }
                            LogUtil.devDebug(TAG + "  toParent.addView  i= " + i + " child = " + child);
                        }
                    }
                    oriParent.addView(toParent);
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    /**
     * 判断是否可以调用自渲染展示方法
     *
     * @param adapter
     * @return
     */
    public static boolean skipRender(BaseParallelAdapter adapter) {
        boolean result = false;

        if (adapter == null) {
            return true;
        }
        if (adapter.mAdvanceRFBridge == null) {
            adapter.handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "advanceRFBridge  null");
            return true;
        }

        final AdvanceRFMaterialProvider rfMaterialProvider = adapter.mAdvanceRFBridge.getMaterialProvider();

        if (rfMaterialProvider == null) {
            adapter.handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "getMaterialProvider  null");
            return true;
        }
        if (rfMaterialProvider.rootView == null) {
            adapter.handleFailed(AdvanceError.ERROR_EXCEPTION_RENDER, "请设置  rootView 信息");
            return true;
        }
        return result;

    }

    /**
     * 渲染广告标识小icon
     *
     * @param targetView 展示的图片控件
     * @param url        网络地址
     * @param localRes   本地图片地址
     */
    public static void renderSourceLogo(ImageView targetView, String url, int localRes) {
        if (targetView == null) {
            return;
        }
        try {
            if (!BYStringUtil.isEmpty(url)) {
                //调用渲染图片方法
                MercuryTool.renderNetImg(url, targetView);
            } else {
                renderImgRes(targetView, localRes);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            renderImgRes(targetView, localRes);
        }
    }


    private static void renderImgRes(ImageView targetView, int localRes) {
        try {
            targetView.setBackgroundResource(localRes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 针对部分自渲染渠道，对于视频布局高度敏感得，需要适配添加
     *
     * @param rfMaterialProvider 自渲染提供器
     * @param sdkVideoView       视频view
     * @param videoMaterialWH    素材的宽高比，用来兜底时确定video展示高度
     */
    public static void addVideoView(final AdvanceRFMaterialProvider rfMaterialProvider, final View sdkVideoView, float videoMaterialWH) {
        try {
            int height = rfMaterialProvider.rootView.getHeight();
            int width = rfMaterialProvider.rootView.getWidth();
            int VH = rfMaterialProvider.videoView.getHeight();

            ViewGroup.LayoutParams lp = rfMaterialProvider.rootView.getLayoutParams();
            int lpW = lp.width;
            int lpH = lp.height;
            ViewGroup.LayoutParams vlp = rfMaterialProvider.videoView.getLayoutParams();
            int vlpW = vlp.width;
            int vlpH = vlp.height;
            ViewGroup.LayoutParams vPlp = ((View) (rfMaterialProvider.videoView.getParent())).getLayoutParams();
            int vPlpW = vPlp.width;
            int vPlpH = vPlp.height;

            //是否有设置高度值， 任一空间高度值大于0，均代表用户手动设置过期望高度，不再进行默认高度赋值
            boolean hasHeightSet = lpH > 0 || vlpH > 0 || vPlpH > 0;

            LogUtil.devDebug(TAG + "【addVideoView】 root height = " + height + " ,root width = " + width
                    + "\n ，videoView  height = " + VH
                    + "\n ,root lpW = " + lpW + " ,root lpH = " + lpH
                    + "\n ,videoView vlpW = " + vlpW + " ,videoView vlpH = " + vlpH
                    + "\n ,videoView vPlpW = " + vPlpW + " ,videoView vPlpH = " + vPlpH
                    + "\n ,hasHeightSet = " + hasHeightSet
            );

            if (hasHeightSet) {
                addVideo(rfMaterialProvider, sdkVideoView, 0);
            } else {

                //自适应高度时，需要指定展示高度，通过容器宽度*素材比例来得到展示高度值
                float showMaterialWh = 1080 / (float) 720;
                if (videoMaterialWH > 0) {
                    showMaterialWh = videoMaterialWH;
                }
                LogUtil.devDebug(TAG + " showMaterialWh = " + showMaterialWh);

                if (width > 0) {
                    int showH = (int) (width / showMaterialWh);
                    addVideo(rfMaterialProvider, sdkVideoView, showH);
                } else {
                    final float finalShowMaterialWh = showMaterialWh;
                    rfMaterialProvider.rootView.post(new Runnable() {
                        @Override
                        public void run() {
                            int width = rfMaterialProvider.rootView.getWidth();
                            if (width <= 0) {
                                width = BYDisplay.getScreenWPx();
                            }
                            int showH = (int) (width / finalShowMaterialWh);
                            addVideo(rfMaterialProvider, sdkVideoView, showH);
                        }
                    });
                }
            }


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static void addVideo(AdvanceRFMaterialProvider rfMaterialProvider, View sdkVideoView, int showH) {
        try {
            if (sdkVideoView != null) {
                if (sdkVideoView.getParent() == null) {
                    rfMaterialProvider.videoView.removeAllViews();
                    //指定高度添加
                    if (showH > 0) {
                        rfMaterialProvider.videoView.addView(sdkVideoView, ViewGroup.LayoutParams.MATCH_PARENT, showH);
                    } else {
                        rfMaterialProvider.videoView.addView(sdkVideoView);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
