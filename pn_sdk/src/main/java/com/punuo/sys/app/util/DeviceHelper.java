package com.punuo.sys.app.util;

import android.content.pm.ApplicationInfo;

import com.punuo.sys.app.PnApplication;

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
            ApplicationInfo info = PnApplication.getInstance().getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }
}
