package com.punuo.sys.app.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.punuo.sys.app.activity.WebViewActivity;

/**
 * Created by han.chen.
 * Date on 2019/4/4.
 **/
public class IntentUtil {

    public static void openWebViewActivity(Context context, String url) {
        Intent intent = new Intent();
        intent.setClass(context, WebViewActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    public static void jumpActivity(Context context, Class classz) {
        Intent intent = getIntent(context, classz);
        context.startActivity(intent);
    }

    public static void jumpActivity(Context context, Class classz, Bundle bundle) {
        Intent intent = getIntent(context, classz, bundle);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, Intent intent) {

    }

    public static Intent getIntent(Context context, Class classz, Bundle bundle) {
        Intent intent = getIntent(context, classz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        return intent;
    }

    public static Intent getIntent(Context context, Class classz) {
        Intent intent = new Intent();
        intent.setClass(context, classz);
        return intent;
    }
}
