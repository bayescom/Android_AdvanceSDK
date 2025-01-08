package com.advance.advancesdkdemo.util;

import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.widget.TextView;

import com.advance.advancesdkdemo.MyApplication;
import com.advance.advancesdkdemo.custom.DemoWebActivity;
import com.advance.core.srender.AdvanceRFDownloadElement;
import com.advance.utils.LogUtil;
import com.bayes.sdk.basic.itf.BYBaseCallBack;
import com.bayes.sdk.basic.util.BYThreadUtil;
import com.bayes.sdk.basic.util.BYToast;

import java.util.ArrayList;

public class DemoUtil {
    public static String TAG = "DemoUtil ";

    public static void logAndToast(String msg) {
        Log.d(TAG, msg);
        BYThreadUtil.switchMainThread(new BYBaseCallBack() {
            @Override
            public void call() {
                BYToast.showToast(TAG + msg);
            }
        });
    }

    public static void addTextLine(TextView textView) {
        textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        textView.getPaint().setAntiAlias(true);//抗锯齿
    }

    public static void openInWeb(String webUrl, String webText, ArrayList<AdvanceRFDownloadElement.AdvDownloadPermissionModel> pList) {
        Intent intent = new Intent();
        int pSzie = 0;
        if (pList != null) {
            pSzie = pList.size();
        }
        LogUtil.devDebug("webUrl = " + webUrl + ", webText = " + webText + "pList.size = " + pSzie);
        intent.putExtra("webUrl", webUrl);
        intent.putExtra("webText", webText);
        intent.putExtra("pList", pList);
        intent.setClass(MyApplication.getInstance(), DemoWebActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        MyApplication.getInstance().startActivity(intent);

    }

}
