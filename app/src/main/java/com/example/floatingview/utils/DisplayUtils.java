package com.example.floatingview.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Horrarndoo on 2018/6/5.
 * <p>
 * 显示相关工具类
 */
public class DisplayUtils {
    /**
     * 将px值转换为dp值
     *
     * @param pxValue px值
     * @return 转换后的dp值
     */
    public static int px2dp(float pxValue) {
        final float scale = AppUtils.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dp值转换为px值
     *
     * @param dpValue dp值
     * @return 转换后的px值
     */
    public static int dp2px(float dpValue) {
        final float scale = AppUtils.getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值
     *
     * @param pxValue px值
     * @return 转换后的sp值
     */
    public static int px2sp(float pxValue) {
        final float scale = AppUtils.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将sp值转换为px值
     *
     * @param spValue sp值
     * @return 转换后的px值
     */
    public static int sp2px(float spValue) {
        final float scale = AppUtils.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * scale + 0.5f);
    }

    /**
     * 获取屏幕高度
     *
     * @param activity activity
     * @return 屏幕高度
     */
    public static int getScreenHeightPixels(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @param activity activity
     * @return 屏幕宽度
     */
    public static int getScreenWidthPixels(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context context
     * @return 屏幕宽度
     */
    @SuppressLint("ObsoleteSdkInt")
    public static int getScreenWidthPixels(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null)
            return 0;
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }

    /**
     * 获取屏幕高度
     *
     * @param context context
     * @return 屏幕高度
     */
    public static int getScreenHeightPixels(Context context) {
        if (!isFullScreenDevice()) {
            return getScreenHeight(context);
        }
        return getScreenRealHeight(context);
    }

    private static final int PORTRAIT = 0;
    private static final int LANDSCAPE = 1;

    @NonNull
    private volatile static Point[] mRealSizes = new Point[2];

    @SuppressLint("ObsoleteSdkInt")
    public static int getScreenRealHeight(@Nullable Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return getScreenHeight(context);
        }

        if (context == null) {
            context = AppUtils.getContext();
        }

        int orientation = context.getResources().getConfiguration().orientation;
        orientation = orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT : LANDSCAPE;

        if (mRealSizes[orientation] == null) {
            WindowManager windowManager =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return getScreenHeight(context);
            }
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            mRealSizes[orientation] = point;
        }
        return mRealSizes[orientation].y;
    }

    public static int getScreenHeight(@Nullable Context context) {
        if (context != null) {
            return context.getResources().getDisplayMetrics().heightPixels;
        }
        return 0;
    }

    /***
     * 获取当前手机是否是全面屏
     * @return
     */

    @SuppressLint("ObsoleteSdkInt")
    public static boolean isFullScreenDevice() {
        // 低于 API 21的，都不会是全面屏。。。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        WindowManager windowManager =
                (WindowManager) AppUtils.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            float width, height;
            if (point.x < point.y) {
                width = point.x;
                height = point.y;
            } else {
                width = point.y;
                height = point.x;
            }
            return height / width >= 1.97f;
        }
        return false;
    }

    /**
     * 当前是否横屏
     *
     * @param activity Activity
     * @return 当前是否横屏
     */
    public static boolean isLandscape(Activity activity) {
        return getScreenWidthPixels(activity) - getScreenHeightPixels(activity) > 0;
    }

    /**
     * 当前是否横屏
     *
     * @param context context
     * @return 当前是否横屏
     */
    public static boolean isLandscape(Context context) {
        return getScreenWidthPixels(context) - getScreenHeightPixels(context) > 0;
    }

    // 获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }


    // 获取导航栏高度
    public static int getNavigationBarHeight(Context context) {
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool",
                "android");
        if (rid != 0) {
            int resourceId = context.getResources().getIdentifier("navigation_bar_height",
                    "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        } else
            return 0;

    }
}
