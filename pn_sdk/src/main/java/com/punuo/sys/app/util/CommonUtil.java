package com.punuo.sys.app.util;

import android.util.DisplayMetrics;

import com.punuo.sys.app.PnApplication;

/**
 * Created by han.chen.
 * Date on 2019/4/4.
 **/
public class CommonUtil {

    public static int getWidth() {
        DisplayMetrics dm = PnApplication.getInstance().getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getHeight() {
        DisplayMetrics dm = PnApplication.getInstance().getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
