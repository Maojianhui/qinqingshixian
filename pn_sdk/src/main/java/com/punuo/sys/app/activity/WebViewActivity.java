package com.punuo.sys.app.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.punuo.sys.app.R;
import com.punuo.sys.app.util.StatusBarUtil;

/**
 * Created by han.chen.
 * Date on 2019/4/4.
 **/
public class WebViewActivity extends BaseSwipeBackActivity {
    private View mStatusBar;
    private WebView mWebView;
    protected String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent data = getIntent();
        mUrl = data.getStringExtra("url");
        setContentView(R.layout.webview_activity);
        mStatusBar = findViewById(R.id.status_bar);
        mWebView = (WebView) findViewById(R.id.web_view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mStatusBar.setVisibility(View.VISIBLE);
            mStatusBar.getLayoutParams().height = StatusBarUtil.getStatusBarHeight(this);
            mStatusBar.requestLayout();
        }
        StatusBarUtil.translucentStatusBar(this, Color.TRANSPARENT, false);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(mUrl);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
