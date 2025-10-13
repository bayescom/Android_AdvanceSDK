package com.advance.advancesdkdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

//import com.polygamma.ogm.OriginMobile;
//import com.polygamma.ogm.antifraud.AntiFraudDescriptor;
//import com.polygamma.ogm.antifraud.AntiFraudModule;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class TestActivity extends Activity {

    TextView result;
    String key = "test_string_key";
    int pos = 0;
//    private @Nullable AntiFraudModule module;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        result = findViewById(R.id.tv_result);

//        OriginMobile.registerInstallCallback(sdk -> {
//            this.module = sdk.getModule(AntiFraudModule.class);
//            sdk.registerDescriptorsUpdateCallback(upd -> {
//                if (upd.containsKey(this.module))
//                    this.module.sdk().runInForeground(this::updateIvtInfo);
//            });
//            sdk.runInForeground(this::updateIvtInfo);
//        });
    }


//    private void updateIvtInfo() {
//        if (this.module == null)
//            return;
//
//        AntiFraudDescriptor desc = this.module.descriptor();
//
//        result.setText(String.format(
//                Locale.ROOT,
//                "Signature: %s, Status: %d, Confidence: %s",
//                desc.signature(), desc.status(), desc.confidence()
//        ));
//    }
//
//    public void saveData(View view) {
//        pos += 1;
//        BYCacheUtil.byCache().put(key, "我是测试的修改数据" + pos);
//    }
//
//    public void getData(View view) {
//
//        String rs = BYCacheUtil.byCache().getAsString(key);
//
//        result.setText(rs);
//    }
}
