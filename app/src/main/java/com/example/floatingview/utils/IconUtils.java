package com.example.floatingview.utils;

import android.graphics.drawable.Drawable;

import com.example.floatingview.R;


/**
 * Created by Horrarndoo on 2022//156.
 * <p>
 * 图标工具类
 */
public class IconUtils {
    /**
     * 动态色值
     */
    public static int[] sIconColorRess = new int[]{
            R.color.icon_bg_color_0,
            R.color.icon_bg_color_1,
            R.color.icon_bg_color_2,
            R.color.icon_bg_color_3,
            R.color.icon_bg_color_4,
            R.color.icon_bg_color_5,
            R.color.icon_bg_color_6,
            R.color.icon_bg_color_7,
            R.color.icon_bg_color_8,
            R.color.icon_bg_color_9,
            R.color.icon_bg_color_10,
            R.color.icon_bg_color_11,
            R.color.icon_bg_color_12,
            R.color.icon_bg_color_13,
            R.color.icon_bg_color_14,
            R.color.icon_bg_color_15,
            R.color.icon_bg_color_16,
            R.color.icon_bg_color_17,
            R.color.icon_bg_color_18,
            R.color.icon_bg_color_19,
            R.color.icon_bg_color_20,
            R.color.icon_bg_color_21,
            R.color.icon_bg_color_22,
            R.color.icon_bg_color_23,
            R.color.icon_bg_color_24,
            R.color.icon_bg_color_25
    };

    /**
     * 获取icon drawable（圆）
     * <p>
     * 根据index动态配色
     *
     * @param index 下标
     * @return icon drawable
     */
    public static Drawable getIconCircleDrawable(int index) {
        //必须使用新的drawable，否则drawable对象是唯一的，即永远只有最后一个drawable生效
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.icon_vector_user_icon_bg_circle).mutate();
        drawable.setTint(ResourcesUtils.getColor(sIconColorRess[index % sIconColorRess.length]));
        return drawable;
    }

    /**
     * 获取icon drawable（矩形）
     * <p>
     * 根据index动态配色
     *
     * @param index 下标
     * @return icon drawable
     */
    public static Drawable getIconRectDrawable(int index) {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.shape_rect_theme_color).mutate();
        drawable.setTint(ResourcesUtils.getColor(sIconColorRess[index % sIconColorRess.length]));
        return drawable;
    }


    /**
     * 获取icon drawable（圆角矩形）
     * <p>
     * 根据index动态配色
     *
     * @param index 下标
     * @return icon drawable
     */
    public static Drawable getIconRectArcDrawable(int index) {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.icon_vector_user_icon_bg_rect_arc).mutate();
        drawable.setTint(ResourcesUtils.getColor(sIconColorRess[index % sIconColorRess.length]));
        return drawable;
    }

    /**
     * 获取灰色icon drawable（圆）
     *
     * @return icon drawable
     */
    public static Drawable getGreyIconCircleDrawable() {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.icon_vector_user_icon_bg_circle).mutate();
        drawable.setTint(ResourcesUtils.getColor(R.color.icon_bg_color_grey));
        return drawable;
    }

    /**
     * 获取灰色icon drawable（矩形）
     *
     * @return icon drawable
     */
    public static Drawable getGreyIconRectDrawable() {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.shape_rect_theme_color).mutate();
        drawable.setTint(ResourcesUtils.getColor(R.color.icon_bg_color_grey));
        return drawable;
    }

    /**
     * 获取灰色icon drawable（圆角矩形）
     *
     * @return icon drawable
     */
    public static Drawable getGreyIconRectArcDrawable() {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.icon_vector_user_icon_bg_rect_arc).mutate();
        drawable.setTint(ResourcesUtils.getColor(R.color.icon_bg_color_grey));
        return drawable;
    }

    /**
     * 获取默认icon drawable（矩形）
     *
     * @return icon drawable
     */
    public static Drawable getDefaultIconRectDrawable() {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.shape_rect_theme_color).mutate();
        drawable.setTint(ResourcesUtils.getColor(R.color.icon_bg_color_default));
        return drawable;
    }

    /**
     * 获取默认icon drawable（圆）
     *
     * @return icon drawable
     */
    public static Drawable getDefaultIconCircleDrawable() {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.icon_vector_user_icon_bg_circle).mutate();
        drawable.setTint(ResourcesUtils.getColor(R.color.icon_bg_color_default));
        return drawable;
    }

    /**
     * 获取默认icon drawable（圆角矩形）
     *
     * @return icon drawable
     */
    public static Drawable getDefaultIconRectArcDrawable() {
        Drawable drawable =
                ResourcesUtils.getDrawable(R.drawable.icon_vector_user_icon_bg_rect_arc).mutate();
        drawable.setTint(ResourcesUtils.getColor(R.color.icon_bg_color_default));
        return drawable;
    }
}
