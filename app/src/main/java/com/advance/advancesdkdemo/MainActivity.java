package com.advance.advancesdkdemo;

import static com.advance.advancesdkdemo.util.DemoUtil.logAndToast;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;


import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.advance.AdvanceConfig;
import com.advance.AdvanceRewardVideo;
import com.advance.AdvanceRewardVideoItem;
import com.advance.AdvanceRewardVideoListener;
import com.advance.RewardServerCallBackInf;
import com.advance.advancesdkdemo.custom.SelfRenderActivity;
import com.advance.advancesdkdemo.util.BaseCallBack;
import com.advance.advancesdkdemo.util.DemoIds;
import com.advance.advancesdkdemo.util.DemoManger;
import com.advance.advancesdkdemo.util.UserPrivacyDialog;
import com.advance.model.AdvanceError;
import com.advance.supplier.tanx.TanxGlobalConfig;
import com.advance.utils.SupplierBridgeUtil;
import com.bayes.sdk.basic.util.BYStringUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

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
                "Sigmob SDK 版本号：" + SupplierBridgeUtil.getSupVersion(AdvanceConfig.SDK_ID_SIG) + "\n" +
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

    AdvanceRewardVideo advanceRewardVideo;
    boolean hasRewardShow = false;

    public void onRewardVideo(View view) {
        advanceRewardVideo = new AdvanceRewardVideo(DemoManger.getInstance().currentDemoIds.reward);

        //若集成
        TanxGlobalConfig.setMediaUID("tanxMUID");

        //todo 服务端验证相关信息填写---start
//        advanceRewardVideo.setUserId("用户唯一标识，服务端验证必须");
//        advanceRewardVideo.setRewardName("激励名称，非必填，透传给广告SDK、app服务器使用");
//        advanceRewardVideo.setRewardCount(1); //激励数量，非必填，透传给广告SDK、app服务器使用
//        advanceRewardVideo.setExtraInfo("补充信息，服务端验证时，透传给app服务端");
        //服务端验证相关信息填写---end

        //设置通用事件监听器
        advanceRewardVideo.setAdListener(new AdvanceRewardVideoListener() {
            @Override
            public void onAdLoaded(AdvanceRewardVideoItem advanceRewardVideoItem) {
                logAndToast("广告加载成功");
                hasRewardShow = false;
            }


            @Override
            public void onAdShow() {
                logAndToast("广告展示");
                hasRewardShow = true;
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
            public void onVideoCached() {
                logAndToast("广告缓存成功");
            }

            @Override
            public void onVideoComplete() {
                logAndToast("视频播放完毕");
            }

            @Override
            public void onVideoSkip() {

            }

            @Override
            public void onAdClose() {
                logAndToast("广告关闭");
            }

            @Override
            public void onAdReward() {
                logAndToast("激励发放");
            }

            @Override
            public void onRewardServerInf(RewardServerCallBackInf inf) {
                //广点通和穿山甲支持回调服务端激励验证信息，详见RewardServerCallBackInf中字段信息
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        logAndToast("onRewardServerInf" + inf);
                    }
                }, 1200);
            }
        });
        advanceRewardVideo.loadOnly();
    }

    public void onRewardShow(View view) {
        if (hasRewardShow) {
            logAndToast("激励已展示过");
            return;
        }
        // 如果有业务需求，可以提前加载广告，在需要的时候调用show进行展示
        // 为了方便理解，这里在收到广告后直接调用广告展示，有可能会出现一段时间的缓冲状态。
        if (advanceRewardVideo != null && advanceRewardVideo.isValid()) {
            //展示广告
            advanceRewardVideo.show(this);
        } else {
            logAndToast("激励广告不存在或者已失效");
        }
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
