package com.advance.advancesdkdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.advance.AdvanceConfig;
import com.advance.advancesdkdemo.custom.SelfRenderActivity;
import com.advance.advancesdkdemo.util.BaseCallBack;
import com.advance.advancesdkdemo.util.DemoIds;
import com.advance.advancesdkdemo.util.DemoManger;
import com.advance.advancesdkdemo.util.UserPrivacyDialog;
import com.advance.utils.SupplierBridgeUtil;
import com.bayes.sdk.basic.util.BYStringUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button fullVideo, banner, splash, interstitial, reward, nativeExpress, nativeExpressRV, nativeCustom, draw;
    Spinner sdKSNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取按钮
        fullVideo = findViewById(R.id.fullvideo_button);
        splash = findViewById(R.id.splash_button);
        banner = findViewById(R.id.banner_button);
        interstitial = findViewById(R.id.interstitial_button);
        reward = findViewById(R.id.rewardvideo_button);
        nativeExpress = findViewById(R.id.native_express_button);
        nativeExpressRV = findViewById(R.id.native_express_recycler_view_button);
        nativeExpress = findViewById(R.id.native_express_button);
        nativeCustom = findViewById(R.id.btn_rf);
        draw = findViewById(R.id.btn_draw);
//         = findViewById(R.id.);
//         = findViewById(R.id.);

        TextView title = findViewById(R.id.tv_title);
        title.setText("聚合demo(" + getPackageName() + ")");
        String date = "「" + BuildConfig.BUILD_DATA + "」";
        TextView da = findViewById(R.id.tv_run_inf);
        da.setText(date);

        sdKSNew = findViewById(R.id.sp_sdk);
        sdKSNew.setSelection(10);
        sdKSNew.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String sdkName = (String) sdKSNew.getItemAtPosition(position);
                updateIDInf(sdkName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        updateIDInf(sdKSNew.getSelectedItem().toString());


        printVersion();

        checkPermiss();

    }

    private void updateIDInf(String sdkName) {
        //存储id信息
        DemoIds ids = DemoIds.getDemoIds(sdkName);

        DemoManger.getInstance().currentDemoIds = ids;
//        显式展示出来
        updateButtonAct(banner, ids.banner);
        updateButtonAct(splash, ids.splash);
        updateButtonAct(reward, ids.reward);
        updateButtonAct(nativeExpress, ids.nativeExpress);
        updateButtonAct(nativeExpressRV, ids.nativeExpress);
        updateButtonAct(nativeCustom, ids.nativeCustom);
        updateButtonAct(fullVideo, ids.fullscreen);
        updateButtonAct(interstitial, ids.interstitial);
        updateButtonAct(draw, ids.draw);

    }

    //判断该类型广告位是否可用，不可用需要将按钮禁用，可用将添加广告位id在描述后面
    private boolean updateButtonAct(Button targetV, String id) {
        boolean result = BYStringUtil.isNotEmpty(id);
        String txt = targetV.getText().toString();
        int index = txt.indexOf("(");
        if (result) {
            if (index <= 0) {
                targetV.setText(txt + "(" + id + ")");
            } else {
                String typeTxt = txt.substring(0, index);
                targetV.setText(typeTxt + "(" + id + ")");
            }
        } else {
            if (index > 0) {
                String typeTxt = txt.substring(0, index);
                targetV.setText(typeTxt);
            }
        }
        targetV.setEnabled(result);
        return result;
    }

    private void printVersion() {

        String av = AdvanceConfig.AdvanceSdkVersion;

        TextView tv = findViewById(R.id.tv_version);
        tv.setText("Advance聚合 SDK 版本号： " + av + "\n" + "\n" +
                "Mercury SDK 版本号： " + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_MERCURY) + "\n" +
                "穿山甲 SDK 版本号： " + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_CSJ) + "\n" +
                "广点通 SDK 版本号： " + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_GDT) + "\n" +
                "百度 SDK 版本号： " + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_BAIDU) + "\n" +
                "快手 SDK 版本号： " + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_KS) + "\n" +
                "tanx SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_TANX) + "\n" +
                "Sigmob SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_SIG)+ "\n" +
                "oppo SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_OPPO) + "\n" +
                "华为 SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_HW) + "\n" +
                "小米 SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_XIAOMI) + "\n" +
                "vivo SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_VIVO) + "\n" +
                "荣耀 SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_HONOR) + "\n" +
                "TapTap SDK 版本号： " + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_TAP) + "\n"
        );

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, TestActivity.class));
            }
        });
    }


    private void checkPermiss() {
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
        new AdvanceAD(this).loadReward(DemoManger.getInstance().currentDemoIds.reward);
    }

    public void onNativeExpressRecyclerView(View view) {
        startActivity(new Intent(this, NativeExpressRecyclerViewActivity.class));
    }

    public void onInterstitial(View view) {
        new AdvanceAD(this).loadInterstitial(DemoManger.getInstance().currentDemoIds.interstitial);
    }

    public void onFullVideo(View view) {
        new AdvanceAD(this).loadFullVideo(DemoManger.getInstance().currentDemoIds.fullscreen);
    }


    public void draw(View view) {
        startActivity(new Intent(this, DrawActivity.class));

    }


    public void renderFeed(View view) {
        startActivity(new Intent(this, SelfRenderActivity.class));
    }

}
