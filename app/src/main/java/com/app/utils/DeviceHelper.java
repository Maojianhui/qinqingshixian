package com.app.utils;

import android.content.pm.ApplicationInfo;

import com.app.friendCircleMain.adapter.AppApplication;

/**
 * Created by han.chen.
 * Date on 2019/4/2.
 **/
public class DeviceHelper {

    /**
     * 判断当前应用是否是debug状态
     */
    public static boolean isApkInDebug() {
        try {
            ApplicationInfo info = AppApplication.getInstance().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
