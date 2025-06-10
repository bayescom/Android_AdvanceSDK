package com.advance.advancesdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.advance.advancesdkdemo.util.DemoManger;


public class SplashActivity extends Activity {
    TextView skipView;
    FrameLayout adContainer;
    private String TAG = "SplashActivity";
    AdvanceAD ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_custom_logo);
        adContainer = findViewById(R.id.splash_container);
        skipView = findViewById(R.id.skip_view);


        /**
         * 加载并展示开屏广告
         */
        ad = new AdvanceAD(this);
        //建议传递logo信息给SDK，使展示效果更美观
        ad.loadSplash(DemoManger.getInstance().currentDemoIds.splash, adContainer, new AdvanceAD.SplashCallBack() {
            @Override
            public void jumpMain() {
                goToMainActivity();
            }
        });
    }

    /**
     * 跳转到主页面
     */
    private void goToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, SplashToMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        this.finish();
    }


    /**
     * 开屏页禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
