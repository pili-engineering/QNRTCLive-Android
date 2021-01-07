package com.qiniu.droid.rtc.live.demo.activity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.utils.BarUtils;
import com.qiniu.droid.rtc.live.demo.view.LoadingDialog;

public class WebActivity extends AppCompatActivity {

    private static final String WEB_URL = "https://www.qiniu.com/products/rtn";

    private WebView mWebView;
    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStatusBar();
        setContentView(R.layout.activity_rtc_web);
        ImageView backBtn = findViewById(R.id.back_btn);
        TextView titleView = findViewById(R.id.tv_bar_title);
        mWebView = findViewById(R.id.qn_rtc_webview);

        mLoadingDialog = new LoadingDialog.Builder(this).create();
        mLoadingDialog.show();

        backBtn.setOnClickListener(v -> finish());
        titleView.setText("七牛实时音视频");

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        // 将图片调整到适合 webview 的大小
        webSettings.setUseWideViewPort(true);
        // 缩放至屏幕的大小
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mLoadingDialog.dismiss();
            }
        });

        mWebView.loadUrl(WEB_URL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        mWebView = null;
    }

    private void initStatusBar() {
        BarUtils.setStatusBarColor(this, R.color.blue);
        // 设置状态栏文字颜色及图标为浅色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
