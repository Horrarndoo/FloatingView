package com.example.floatingview.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Created by Horrarndoo on 2017/9/1.
 * <p>
 * 资源工具类-加载资源文件
 */

public class ResourcesUtils {
    /**
     * 获取strings.xml资源文件字符串数组
     *
     * @param id 资源文件id
     * @return 资源文件对应字符串数组
     */
    public static String[] getStringArray(int id) {
        return AppUtils.getContext().getResources().getStringArray(id);
    }

    /**
     * 获取strings.xml资源文件整型数组
     *
     * @param id 资源文件id
     * @return 资源文件对应整型数组
     */
    public static int[] getIntArray(int id) {
        return AppUtils.getContext().getResources().getIntArray(id);
    }

    /**
     * 获取arrays.xml资源文件资源数组
     *
     * @param id 资源文件id
     * @return 资源文件对应资源数组
     */
    public static TypedArray obtainTypedArray(int id) {
        return AppUtils.getContext().getResources().obtainTypedArray(id);
    }

    /**
     * 获取strings.xml资源文件字符串
     *
     * @param id 资源文件id
     * @return 资源文件对应字符串
     */
    public static String getString(int id) {
        return AppUtils.getContext().getResources().getString(id);
    }

    /**
     * 获取drawable资源文件图片
     *
     * @param id 资源文件id
     * @return 资源文件对应图片
     */
    public static Drawable getDrawable(int id) {
        return AppUtils.getContext().getResources().getDrawable(id);
    }

    /**
     * 获取colors.xml资源文件颜色
     *
     * @param id 资源文件id
     * @return 资源文件对应颜色值
     */
    public static int getColor(int id) {
        return AppUtils.getContext().getResources().getColor(id);
    }

    /**
     * 获取颜色的状态选择器
     *
     * @param id 资源文件id
     * @return 资源文件对应颜色状态
     */
    public static ColorStateList getColorStateList(int id) {
        return AppUtils.getContext().getResources().getColorStateList(id);
    }

    /**
     * 获取dimens资源文件中具体像素值
     *
     * @param id 资源文件id
     * @return 资源文件对应像素值
     */
    public static int getDimen(int id) {
        return AppUtils.getContext().getResources().getDimensionPixelSize(id);// 返回具体像素值
    }

    /**
     * 加载布局文件
     *
     * @param context context
     * @param id      布局文件id
     * @return 布局view
     */
    public static View inflate(Context context, int id) {
        return View.inflate(context, id, null);
    }
}
