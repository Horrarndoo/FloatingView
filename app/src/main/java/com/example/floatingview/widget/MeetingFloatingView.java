package com.example.floatingview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.example.floatingview.R;
import com.example.floatingview.utils.BitmapUtils;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Horrarndoo on 2022/10/17.
 * <p>
 * 仿微信语音通话悬浮弹窗
 * <p>
 * 内部管理实现悬浮窗功能，
 * 实现拖动时判断左右方向自动粘边效果，
 * 实现粘边处圆角转直角、非粘边时直角转圆角效果
 */
public class MeetingFloatingView extends View {
    /**
     * 默认宽高与当前View实际宽高
     */
    private int mDefaultWidth, mDefaultHeight;
    private int mWidth, mHeight;
    /**
     * 当前View绘制相关
     */
    private Paint mPaint;
    private Bitmap mBitmap;
    private PorterDuffXfermode mPorterDuffXfermode;
    /**
     * 停留方向
     */
    private Direction mDirection = Direction.right;
    /**
     * 当前屏幕方向
     */
    private int mOrientation;
    /**
     * 屏幕宽度像素值
     */
    private int mWidthPixels;
    /**
     * 悬浮窗管理相关
     */
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    /**
     * 是否为显示状态
     */
    private boolean isShowing;
    /**
     * 是否拖动控件（只要拖动就消费掉touch，即不作为点击事件处理，避免拖动控件触发点击事件的情况）
     */
    private boolean isDraged;
    /**
     * 判断为拖拽的滑动阈值
     */
    private static final int JUDGE_EVENT_DRAGED_THRESHOLD = 5;
    /**
     * 当前的x
     */
    private int mCurrentX;
    /**
     * 当前的y
     */
    private int mCurrentY;
    /**
     * 手指按下时的x
     */
    private int mDownX;
    /**
     * 手指按下时的y
     */
    private int mDownY;

    public MeetingFloatingView(Context context) {
        super(context);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init() {
        //悬浮窗管理相关
        mWindowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        //                | WindowManager.LayoutParams.FLAG_FULLSCREEN
        //                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //当前View绘制相关
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPorterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP);
        mBitmap = BitmapUtils.getBitmap(getContext(), R.drawable.ic_vector_media_meeting);
        mDefaultHeight = 210;
        mDefaultWidth = 210;

        //记录当前屏幕方向和屏幕宽度
        recordScreenWidth();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = measureSize(mDefaultWidth, heightMeasureSpec);
        mHeight = measureSize(mDefaultHeight, widthMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //间隔和圆角
        int padding = 20;
        int rOut = 30;
        int rInner = (int) (rOut * (mWidth - padding) * 1.f / mWidth);
        //画透明色圆角背景
        mPaint.setColor(Color.parseColor("#D9E1E1E1"));
        canvas.drawRoundRect(0, 0, mWidth, mHeight, rOut, rOut, mPaint);
        //根据最后停留方向（left or right）绘制多一层直角矩形，覆盖圆角
        switch (mDirection) {
            default:
            case right:
                mPaint.setXfermode(mPorterDuffXfermode);
                canvas.drawRoundRect(mWidth / 2.f, 0, mWidth, mHeight, 0, 0, mPaint);
                break;
            case left:
                mPaint.setXfermode(mPorterDuffXfermode);
                canvas.drawRoundRect(0, 0, mWidth / 2.f, mHeight, 0, 0, mPaint);
                break;
            case move:
                break;
        }
        mPaint.setXfermode(null);
        //画实色圆角矩形
        mPaint.setColor(Color.WHITE);
        canvas.drawRoundRect(padding, padding, mWidth - padding, mHeight - padding, rInner,
                rInner, mPaint);
        //居中填充icon
        canvas.drawBitmap(mBitmap, (mWidth - mBitmap.getWidth()) / 2.f,
                (mHeight - mBitmap.getHeight()) / 2.f, mPaint);
    }

    /**
     * 处理触摸事件，实现拖动、形状变更和粘边效果
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mWindowManager != null) {
            if (getResources().getConfiguration().orientation != mOrientation) {
                //屏幕方向翻转了，重新获取并记录屏幕宽度
                recordScreenWidth();
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mCurrentX = (int) event.getRawX();
                    mCurrentY = (int) event.getRawY();
                    mDownX = (int) event.getRawX();
                    mDownY = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    if (!isDraged) {
                        isDraged = Math.abs(mDownX - nowX) > JUDGE_EVENT_DRAGED_THRESHOLD
                                || Math.abs(mDownY - nowY) > JUDGE_EVENT_DRAGED_THRESHOLD;
                    }
                    int movedX = nowX - mCurrentX;
                    int movedY = nowY - mCurrentY;
                    mCurrentX = nowX;
                    mCurrentY = nowY;
                    mLayoutParams.x = mLayoutParams.x + movedX;
                    mLayoutParams.y = mLayoutParams.y + movedY;
                    if (mLayoutParams.x < 0) {
                        mLayoutParams.x = 0;
                    }
                    if (mLayoutParams.y < 0) {
                        mLayoutParams.y = 0;
                    }
                    if (mDirection != Direction.move) {
                        mDirection = Direction.move;
                        invalidate();
                    }
                    mWindowManager.updateViewLayout(this, mLayoutParams);
                    break;
                case MotionEvent.ACTION_UP:
                    handleDirection((int) event.getRawX(), (int) event.getRawY());
                    invalidate();
                    mWindowManager.updateViewLayout(this, mLayoutParams);
                    if (isDraged) {
                        isDraged = false;
                        return true;
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 计算宽高
     */
    private int measureSize(int defaultSize, int measureSpec) {
        int result = defaultSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        //UNSPECIFIED	父容器没有对当前View有任何限制，当前View可以任意取尺寸
        //EXACTLY	当前的尺寸就是当前View应该取的尺寸
        //AT_MOST	当前尺寸是当前View能取的最大尺寸
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = Math.min(result, specSize);
        }
        return result;
    }

    /**
     * 记录当前屏幕方向和屏幕宽度
     */
    private void recordScreenWidth() {
        mOrientation = getResources().getConfiguration().orientation;
        DisplayMetrics outMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
        mWidthPixels = outMetrics.widthPixels;
    }

    /**
     * 判定所处方向
     */
    private void handleDirection(int x, int y) {
        if (x > (mWidthPixels / 2)) {
            mDirection = Direction.right;
            mLayoutParams.x = mWidthPixels - getMeasuredWidth();
        } else {
            mDirection = Direction.left;
            mLayoutParams.x = 0;
        }
    }

    /**
     * 显示（默认位置，右上）
     */
    public void show() {
        int x = mLayoutParams.x;
        int y = mLayoutParams.y;
        if (!isShowing) {
            if (mDirection == Direction.right) {
                x = mWidthPixels - mDefaultWidth;
            }
            if (mDirection == Direction.move) {
                if (x > (mWidthPixels / 2)) {
                    mDirection = Direction.right;
                    x = mWidthPixels - getMeasuredWidth();
                } else {
                    mDirection = Direction.left;
                    x = 0;
                }
            }
            if (mDirection == Direction.left) {
                x = 0;
            }
            show(x, y);
        }
    }

    /**
     * 在指定位置显示
     *
     * @param x x
     * @param y y
     */
    public void show(int x, int y) {
        if (!isShowing) {
            mLayoutParams.x = x;
            mLayoutParams.y = y;
            handleDirection(x, y);
            mWindowManager.addView(this, mLayoutParams);
            isShowing = true;
        }
    }

    /**
     * 调整悬浮窗位置
     * 根据提供坐标自动判断粘边
     */
    public void updateViewLayout(int x, int y) {
        if (isShowing) {
            handleDirection(x, y);
            invalidate();
            mLayoutParams.y = y;
            mWindowManager.updateViewLayout(this, mLayoutParams);
        }
    }

    /**
     * dismiss
     */
    public void dismiss() {
        if (isShowing) {
            //避免窗体内存泄露
            mWindowManager.removeViewImmediate(this);
            isShowing = false;
        }
    }

    /**
     * 方向
     */
    public enum Direction {
        left,
        right,
        move
    }
}
