package com.advance.advancesdkdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;


public class NativeExpressActivity extends AppCompatActivity {
    private FrameLayout container;
    AdvanceAD ad;
    boolean adSucc = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_express);
        container = findViewById(R.id.native_express_container);


        //加载模板信息流，传入承载布局，加载成功后直接展示。如需分步加载，请参考loadOnly 方法
        new AdvanceAD(this).loadNativeExpressAndShow(container);
    }

    public void loadOnly(View view) {
        //每次加载要新建广告处理实例
        ad = new AdvanceAD(this);
        adSucc = false;
        ad.loadNativeExpressOnly(Constants.TestIds.nativeExpressAdspotId, new AdvanceAD.LoadCallBack() {
            @Override
            public void adSuccess() {
                adSucc = true;
            }
        });
    }

    public void show(View view) {
        //广告未成功不可以调用show方法，否则无法展示广告
        if (!adSucc) {
            ad.logAndToast(this, "广告成功后才可调用show");
            return;
        }
        ad.showNativeExpress(container);
    }

    public void loadAndShow(View view) {
        new AdvanceAD(this).loadNativeExpressAndShow(container);
    }
}
