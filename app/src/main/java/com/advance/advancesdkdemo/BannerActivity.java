package com.advance.advancesdkdemo;

import static com.advance.advancesdkdemo.util.DemoUtil.logAndToast;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.advance.AdvanceBanner;
import com.advance.AdvanceBannerListener;
import com.advance.advancesdkdemo.util.DemoManger;
import com.advance.model.AdvanceError;

public class BannerActivity extends Activity {
    FrameLayout adContainer;
    AdvanceBanner advanceBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        adContainer = findViewById(R.id.banner_layout);

    }


    public void showBanner(View view) {
        if (advanceBanner != null) {
            advanceBanner.show();
        }
    }

    public void loadBanner(View view) {

        advanceBanner = new AdvanceBanner(this, adContainer, DemoManger.getInstance().currentDemoIds.banner);
        //注意！！！：设置穿山甲布局尺寸填入具体dp值，尺寸必须要和穿山甲后台中的"代码位尺寸"宽高比例一致，高度不能设置为0。
        advanceBanner.setCsjExpressViewAcceptedSize(360, 120);
        //推荐：核心事件监听回调
        advanceBanner.setAdListener(new AdvanceBannerListener() {
            @Override
            public void onDislike() {
                logAndToast("广告关闭");

                adContainer.removeAllViews();
            }

            @Override
            public void onAdShow() {
                logAndToast("广告展现");
            }

            @Override
            public void onAdFailed(AdvanceError advanceError) {
                logAndToast("广告加载失败 code=" + advanceError.code + " msg=" + advanceError.msg);
            }

            @Override
            public void onSdkSelected(String id) {
            }

            @Override
            public void onAdClicked() {
                logAndToast("广告点击");
            }


            @Override
            public void onAdLoaded() {
                logAndToast("广告加载成功");
            }

        });
        advanceBanner.loadOnly();

        logAndToast("banner广告请求中");
    }
}
