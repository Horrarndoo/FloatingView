package com.example.floatingview.global;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Process;

import com.example.floatingview.helper.ActivityLifecycleHelper;

/**
 * Created by Horrarndoo on 2022/8/30.
 * <p>
 * Application基类
 */
public class BaseApplication extends Application {
    private static final String LOG_TAG = "CONSYS_LOG";
    protected static Context context;
    protected static Handler handler;
    protected static int mainThreadId;
    private static BaseApplication mApp;
    //activity生命周期管理
    public static ActivityLifecycleHelper sLifecycleHelper = new ActivityLifecycleHelper();

    public static synchronized BaseApplication getInstance() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        context = getApplicationContext();
        handler = new Handler();
        mainThreadId = Process.myTid();
        init();
    }

    private void init() {
        //注册Activity声明周期
        registerActivityLifecycleCallbacks(sLifecycleHelper);
    }

    /**
     * 获取上下文对象
     *
     * @return context
     */
    public static Context getContext() {
        return context;
    }

    /**
     * 获取全局handler
     *
     * @return 全局handler
     */
    public static Handler getHandler() {
        return handler;
    }

    /**
     * 获取主线程id
     *
     * @return 主线程id
     */
    public static int getMainThreadId() {
        return mainThreadId;
    }

    public void finish() {
        onDestroy();
    }

    public void onDestroy() {
    }
}
