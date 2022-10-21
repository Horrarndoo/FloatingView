package com.example.floatingview.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.floatingview.global.BaseApplication;
import com.example.floatingview.helper.ActivityLifecycleHelper;

import java.io.File;
import java.lang.reflect.Method;

import androidx.annotation.RequiresPermission;

import static android.Manifest.permission.KILL_BACKGROUND_PROCESSES;

/**
 * Created by Horrarndoo on 2018/10/25.
 * <p>
 * App工具类
 */
public class AppUtils {

    /**
     * 获取上下文对象
     *
     * @return 上下文对象
     */
    public static Context getContext() {
        return BaseApplication.getContext();
    }

    /**
     * 获取全局handler
     *
     * @return 全局handler
     */
    public static Handler getHandler() {
        return BaseApplication.getHandler();
    }

    /**
     * 获取主线程id
     *
     * @return 主线程id
     */
    public static int getMainThreadId() {
        return BaseApplication.getMainThreadId();
    }

    /**
     * 获取全局ContentResolver
     *
     * @return ContentResolver
     */
    public static ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }

    /**
     * 获取全局资源
     *
     * @return 全局资源
     */
    public static Resources getResources() {
        return getContext().getResources();
    }

    /**
     * 获取全局Asset管理
     *
     * @return 全局Asset管理
     */
    public static AssetManager getAssetManager() {
        return getContext().getAssets();
    }

    /**
     * 获取包管理
     *
     * @return 包管理
     */
    public static PackageManager getPackageManager() {
        return getContext().getPackageManager();
    }

    /**
     * 获取系统服务
     *
     * @param name  服务名
     * @param clazz 服务类
     * @param <T>
     * @return 系统服务
     */
    public static <T> T getSystemService(String name, Class<T> clazz) {
        return getSystemService(getContext(), name, clazz);
    }

    /**
     * 获取系统服务
     *
     * @param context 上下文
     * @param name    服务名
     * @param clazz   服务类
     * @param <T>
     * @return 系统服务
     */
    public static <T> T getSystemService(Context context, String name, Class<T> clazz) {
        if (!TextUtils.isEmpty(name) && clazz != null && context != null) {
            Object obj = context.getSystemService(name);
            return clazz.isInstance(obj) ? (T) obj : null;
        } else {
            return null;
        }
    }

    /**
     * 获取生命周期管理
     *
     * @return 生命周期管理
     */
    public static ActivityLifecycleHelper getActivityLifecycleHelper() {
        return BaseApplication.sLifecycleHelper;
    }

    /**
     * 获取版本名称
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    /**
     * 获取版本号
     */
    public static int getAppVersionCode(Context context) {
        int versioncode = -1;
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versioncode = pi.versionCode;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versioncode;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getIMEI(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context
                .TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * 获取SD卡路径
     *
     * @return 如果sd卡不存在则返回null
     */
    public static File getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment
                .MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir;
    }

    /**
     * 安装文件
     *
     * @param data
     */
    public static void promptInstall(Context context, Uri data) {
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(data, "application/vnd.android.package-archive");
        // FLAG_ACTIVITY_NEW_TASK 可以保证安装成功时可以正常打开 app
        promptInstall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(promptInstall);
    }

    public static void copy2clipboard(Context context, String text) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context
                .CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("clip", text);
        cm.setPrimaryClip(clip);
    }

    /**
     * 判断是否运行在主线程
     *
     * @return true：当前线程运行在主线程
     * fasle：当前线程没有运行在主线程
     */
    public static boolean isRunOnUIThread() {
        // 获取当前线程id, 如果当前线程id和主线程id相同, 那么当前就是主线程
        int myTid = android.os.Process.myTid();
        return myTid == getMainThreadId();
    }

    /**
     * 运行在主线程
     *
     * @param r 运行的Runnable对象
     */
    public static void runOnUIThread(Runnable r) {
        if (isRunOnUIThread()) {
            // 已经是主线程, 直接运行
            r.run();
        } else {
            // 如果是子线程, 借助handler让其运行在主线程
            getHandler().post(r);
        }
    }

    /**
     * 关机
     */
    public static void shutDown() {
        try {
            //获得ServiceManager类
            Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
            //获得ServiceManager的getService方法
            Method getService = ServiceManager.getMethod("getService", String.class);
            //调用getService获取RemoteService
            Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);
            //获得IPowerManager.Stub类
            Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
            //获得asInterface方法
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            //调用asInterface方法获取IPowerManager对象
            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
            //获得shutdown()方法
            Method shutdown = oIPowerManager.getClass().getMethod("shutdown", boolean.class,
                    boolean.class);
            //调用shutdown()方法
            shutdown.invoke(oIPowerManager, false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出 App
     */
    @RequiresPermission(KILL_BACKGROUND_PROCESSES)
    public static void exitApp() {
        if (getActivityLifecycleHelper() != null) {
            getActivityLifecycleHelper().exit();
        }

        BaseApplication.getInstance().finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * 获取 App 包名
     *
     * @return App 包名
     */
    public static String getAppPackageName() {
        return BaseApplication.getContext().getPackageName();
    }
}
