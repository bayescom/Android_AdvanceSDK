package com.advance.advancesdkdemo;

import static com.advance.advancesdkdemo.util.DemoUtil.logAndToast;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.advance.AdvanceSplash;
import com.advance.AdvanceSplashListener;
import com.advance.advancesdkdemo.util.DemoManger;
import com.advance.model.AdvanceError;

import java.lang.reflect.Field;


public class SplashActivity extends Activity {
    TextView skipView;
    FrameLayout adContainer;
    private String TAG = "SplashActivity";
    AdvanceSplash advanceSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //oppo广告必须要全屏展示才行
        fullScreenAndSetContent(this, R.layout.activity_splash_custom_logo, false);
        adContainer = findViewById(R.id.splash_container);
        skipView = findViewById(R.id.skip_view);


        loadAD(null);
    }

    /**
     * 展示开屏广告
     */
    public void showAD(View v) {

        advanceSplash.show();
    }

    public void loadAD(View v) {

        //开屏初始化；adspotId代表广告位id，adContainer为广告容器，skipView不需要自定义可以为null
        advanceSplash = new AdvanceSplash(this, DemoManger.getInstance().currentDemoIds.splash, adContainer, null);
        //注意！！：如果开屏页是fragment或者dialog实现，这里需要置为true。不设置时默认值为false，代表开屏和首页为两个不同的activity
//        advanceSplash.setShowInSingleActivity(true);
//        按需：设置底部logo布局及高度值（单位px）
//        advanceSplash.setLogoLayout(R.layout.splash_logo_layout, mActivity.getResources().getDimensionPixelSize(R.dimen.logo_layout_height));
        //必须：设置开屏核心回调事件的监听器。
        advanceSplash.setAdListener(new AdvanceSplashListener() {
            /**
             * @param id 代表当前被选中的策略id，值为"1" 代表mercury策略 ，值为"2" 代表广点通策略， 值为"3" 代表穿山甲策略
             */
            @Override
            public void onSdkSelected(String id) {

            }

            @Override
            public void onAdLoaded() {

                logAndToast("广告加载成功");
            }

            @Override
            public void jumpToMain() {
//                1; //广告执行失败，对应onAdFailed回调
//                2; //用户点击了广告跳过，对应旧onAdSkip回调
//                3; //广告倒计时结束，对应旧onAdTimeOver回调
                int jumpType = 0;
                if (advanceSplash != null) {
                    jumpType = advanceSplash.getJumpType();
                }
                logAndToast("跳转首页,jumpType = " + jumpType);

                goToMainActivity();
            }

            @Override
            public void onAdShow() {
                logAndToast("广告展示成功");
            }

            @Override
            public void onAdFailed(AdvanceError advanceError) {
                logAndToast("广告加载失败 code=" + advanceError.code + " msg=" + advanceError.msg);
            }

            @Override
            public void onAdClicked() {
                logAndToast("广告点击");
            }

        });
        //自定义adn必须添加，未支持得sdkID具体值需联系我们获取，不得和现有sdkID重复
//        advanceSplash.addCustomSupplier("自定义得sdkID，请联系我们获取","com.advance.supplier.custom.CustomADNYLHSplashAdapter");
        //必须：请求广告
        advanceSplash.loadOnly();
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
