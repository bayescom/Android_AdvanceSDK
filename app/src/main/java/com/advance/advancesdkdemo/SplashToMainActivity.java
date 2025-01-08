package com.advance.advancesdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.advance.utils.AdvanceSplashPlusManager;

public class SplashToMainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_to_main);

        //开屏v+、点睛广告后续效果启动。当开屏页和首页为不同activity时，需要调用该方法以唤起开屏效果。
        AdvanceSplashPlusManager.startZoom(this);
    }
}
