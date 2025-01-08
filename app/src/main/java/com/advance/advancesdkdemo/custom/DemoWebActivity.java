package com.advance.advancesdkdemo.custom;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.advance.advancesdkdemo.R;
import com.advance.core.srender.AdvanceRFDownloadElement;

import java.util.ArrayList;

public class DemoWebActivity extends Activity {
    WebView webView;
    TextView textView;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_web);
        recyclerView = findViewById(R.id.rv_list);
        textView = findViewById(R.id.tv_txt);
        webView = findViewById(R.id.wv_web);

        String webUrl = getIntent().getStringExtra("webUrl");
        String webText = getIntent().getStringExtra("webText");
        ArrayList<AdvanceRFDownloadElement.AdvDownloadPermissionModel> pList = (ArrayList<AdvanceRFDownloadElement.AdvDownloadPermissionModel>) getIntent().getSerializableExtra("pList");


        final WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        //支持插件
        //webSettings.setPluginsEnabled(true);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        //其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(
                    WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        if (!TextUtils.isEmpty(webUrl)) {
            //加载网页
            webView.setVisibility(View.VISIBLE);
            webView.loadUrl(webUrl);
        } else if (!TextUtils.isEmpty(webText)) {
//            显示介绍文字
            textView.setVisibility(View.VISIBLE);
            textView.setText(webText);
        } else if (pList.size() > 0) {
//            显示权限列表信息
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new PermissionAdapter(pList));
        }
    }


    static class PermissionAdapter extends RecyclerView.Adapter<PermissionAdapter.PermissionHolder> {
        ArrayList<AdvanceRFDownloadElement.AdvDownloadPermissionModel> datas;

        PermissionAdapter(ArrayList<AdvanceRFDownloadElement.AdvDownloadPermissionModel> permissionModels) {
            datas = permissionModels;
        }

        @NonNull
        @Override
        public PermissionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_permission, viewGroup, false);
            return new PermissionHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PermissionHolder permissionHolder, int i) {
            permissionHolder.title.setText(datas.get(i).permTitle);
            permissionHolder.desc.setText(datas.get(i).permDesc);
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        static class PermissionHolder extends RecyclerView.ViewHolder {

            TextView title;
            TextView desc;

            public PermissionHolder(@NonNull View itemView) {
                super(itemView);

                title = itemView.findViewById(R.id.tv_title);
                desc = itemView.findViewById(R.id.tv_desc);
            }
        }
    }
}
