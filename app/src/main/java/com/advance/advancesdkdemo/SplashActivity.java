package com.advance.advancesdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.advance.advancesdkdemo.util.DemoManger;

import java.lang.reflect.Field;


public class SplashActivity extends Activity {
    TextView skipView;
    FrameLayout adContainer;
    private String TAG = "SplashActivity";
    AdvanceAD ad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //oppo广告必须要全屏展示才行
        fullScreenAndSetContent(this,R.layout.activity_splash_custom_logo,false);
        adContainer = findViewById(R.id.splash_container);
        skipView = findViewById(R.id.skip_view);


        /**
         * 加载并展示开屏广告
         */
        ad = new AdvanceAD(this);
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


    public void fullScreenAndSetContent(Activity activity, int layoutId, boolean is_over_status) {
        try {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏刘海
            if (Build.VERSION.SDK_INT >= 28 && is_over_status) {
                WindowManager.LayoutParams lp = activity.getWindow().getAttributes(); //sdk 28之前没有该属性，暂用反射获取
                lp.layoutInDisplayCutoutMode = 1;
                Class c = lp.getClass();
                Field field = c.getField("layoutInDisplayCutoutMode");
                field.set(lp, 1);
                activity.getWindow().setAttributes(lp);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } //真实设置layoutId
        activity.setContentView(layoutId);
        try { //后续隐藏虚拟键navbar
            View decorView = activity.getWindow().getDecorView();
            if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
                decorView.setSystemUiVisibility(View.GONE);
            } else if (Build.VERSION.SDK_INT >= 19) {
                int option = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
                decorView.setSystemUiVisibility(option);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


}
