package com.app.process;

import android.app.Application;

import com.app.ui.activity.ActivityLifeCycle;
import com.app.utils.DebugCrashHandler;
import com.app.utils.DeviceHelper;


/**
 * Created by han.chen.
 * Date on 2019/4/2.
 **/
public class ProcessTasks {

    public static void commonLaunchTasks(Application app) {
        if (DeviceHelper.isApkInDebug()) {
            DebugCrashHandler.getInstance().init(); //崩溃日志收集
        }
        app.registerActivityLifecycleCallbacks(ActivityLifeCycle.getInstance());
    }
}
