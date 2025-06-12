package com.advance.advancesdkdemo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.advance.advancesdkdemo.util.DemoManger;

public class BannerActivity extends AppCompatActivity {
    private AdvanceAD ad;
    FrameLayout rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        rl = findViewById(R.id.banner_layout);

        //初始化并加载banner广告
        ad = new AdvanceAD(this);
        ad.loadBanner(DemoManger.getInstance().currentDemoIds.banner, rl);
    }


    public void loadBanner(View view) {
        //初始化并加载banner广告
        ad = new AdvanceAD(this);
        ad.loadBanner(DemoManger.getInstance().currentDemoIds.banner, rl);
    }
}
