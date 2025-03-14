package com.advance.advancesdkdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.advance.AdvanceConfig;
import com.advance.advancesdkdemo.custom.SelfRenderActivity;
import com.advance.advancesdkdemo.util.BaseCallBack;
import com.advance.advancesdkdemo.util.UserPrivacyDialog;
import com.alimm.tanx.core.SdkConstant;
import com.baidu.mobads.sdk.api.AdSettings;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.kwad.sdk.api.KsAdSDK;
import com.mercury.sdk.core.config.MercuryAD;
import com.qq.e.comm.managers.status.SDKStatus;
import com.sigmob.windad.WindAds;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button fullVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fullVideo = findViewById(R.id.fullvideo_button);

        String csjV = TTAdSdk.getAdManager().getSDKVersion();
//        String csjop = TTVfSdk.getVfManager().getSDKVersion();
        String merV = MercuryAD.getVersion();
        String gdtV = SDKStatus.getSDKVersion();
        String bdV = AdSettings.getSDKVersion() + "";
        String ksV = KsAdSDK.getSDKVersion();
        String av = AdvanceConfig.AdvanceSdkVersion;

        TextView tv = findViewById(R.id.tv_version);
        tv.setText("Advance聚合 SDK 版本号： " + av + "\n" + "\n" +
                        "Mercury SDK 版本号： " + merV + "\n" +
                        "穿山甲 SDK 版本号： " + csjV + "\n" +
//                "穿山甲-op SDK 版本号： " + csjop + "\n" +
                        "广点通 SDK 版本号： " + gdtV + "\n" +
                        "百度 SDK 版本号： " + bdV + "\n" +
                        "快手 SDK 版本号： " + ksV + "\n" +
                        "tanx SDK 版本号：" + SdkConstant.getSdkVersion() + "\n" +
                        "Sigmob SDK 版本号：" + WindAds.getVersion() + "\n" +
                        "TapTap SDK 版本号： " + com.tapsdk.tapad.BuildConfig.VERSION_NAME + "\n"
        );

        boolean hasPri = getSharedPreferences(Constants.SP_NAME, Context.MODE_PRIVATE).getBoolean(Constants.SP_AGREE_PRIVACY, false);
        /**
         * 注意！：由于工信部对设备权限等隐私权限要求愈加严格，强烈推荐APP提前申请好权限，且用户同意隐私政策后再加载广告
         */
        if (!hasPri) {
            UserPrivacyDialog dialog = new UserPrivacyDialog(this);
            dialog.callBack = new BaseCallBack() {
                @Override
                public void call() {
                    //一定要用户授权同意隐私协议后，再申领必要权限
                    if (Build.VERSION.SDK_INT >= 23 && Build.VERSION.SDK_INT < 29) {
                        checkAndRequestPermission();
                    }
                }
            };
            dialog.show();
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<String>();
        if (!(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }


        // 缺少权限，进行申请
        if (lackedPermission.size() > 0) {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1024);
        }
    }

    public void onBanner(View view) {
        startActivity(new Intent(this, BannerActivity.class));
    }

    public void onSplash(View view) {
        startActivity(new Intent(this, SplashActivity.class));
    }

    public void onNativeExpress(View view) {
        startActivity(new Intent(this, NativeExpressActivity.class));
    }

    public void onRewardVideo(View view) {
        new AdvanceAD(this).loadReward(Constants.TestIds.rewardAdspotId);
    }

    public void onNativeExpressRecyclerView(View view) {
        startActivity(new Intent(this, NativeExpressRecyclerViewActivity.class));
    }

    public void onInterstitial(View view) {
        new AdvanceAD(this).loadInterstitial(Constants.TestIds.interstitialAdspotId);
    }

    public void onFullVideo(View view) {
        new AdvanceAD(this).loadFullVideo(Constants.TestIds.fullScreenVideoAdspotId);
    }


    public void draw(View view) {
        startActivity(new Intent(this, DrawActivity.class));

    }


    public void renderFeed(View view) {
        startActivity(new Intent(this, SelfRenderActivity.class));
    }

}
