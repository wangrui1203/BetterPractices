package com.example.myview.utils;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * @author ray
 * @date 2023/8/15 09:38
 */
public class AppUtils {

    /**
     *  判断进程是否存活
     */
    public static boolean isProcessExist(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> lists;
        if (am != null) {
            lists = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo appProcess : lists) {
                if (appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getCurProcessName(Context context) {
        return getProcessNameByPid(context, Process.myPid());
    }

    public static String getProcessNameByPid(Context context, int pid) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    return processInfo.processName;
                }
            }
        }
        return "";
    }

    public static String getAppVersionName(Context context, String packageName) {
        String version = "";
        PackageManager pm = context.getPackageManager();
        try {
            version = pm.getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return version;
    }

    public static String getAppVersionCode(Context context, String packageName) {
        String version = "";
        PackageManager pm = context.getPackageManager();
        try {
            version = pm.getPackageInfo(packageName, 0).versionCode+"";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("", e.getMessage());
        }
        return version;
    }

//
//    public static void startActivity(Context context, Intent intent, ActivityOptions options) {
//        try {
//            context.startActivityAsUser(intent, options.toBundle(),new UserHandle(UserHandle.USER_CURRENT));
//        } catch (ActivityNotFoundException e) {
//            Log.e("", e.getMessage());
//        }
//
//    }
//
//    public static void startActivity(Context context, Intent intent) {
//        try {
//            context.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
//        } catch (ActivityNotFoundException e) {
//            Log.e("", e.getMessage());
//        }
//    }
//
//    public static void startService(Context context, Intent intent) {
//        context.startServiceAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
//    }
//
//    public static void sendBroadcast(Context context, Intent intent) {
//        context.sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
//    }
//
//    public static void sendBroadcastAll(Context context, Intent intent) {
//        context.sendBroadcastAsUser(intent, UserHandle.ALL);
//    }

//    public static boolean bindService(Context context, Intent intent, ServiceConnection connection) {
//        return context.bindServiceAsUser(intent, connection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT);
//    }

    /**
     * 检查包是否存在
     */
    public static boolean checkPackInfo(Context context, String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return false;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> pkgInfos = packageManager.getInstalledPackages(0);
            if (pkgInfos != null && pkgInfos.size() > 0) {
                for (PackageInfo pkgInfo : pkgInfos) {
                    if (pkgName.equals(pkgInfo.packageName)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("", e.getMessage());
        }
        return false;
    }
}
