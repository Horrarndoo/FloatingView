package com.example.floatingview.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Horrarndoo on 2022/10/17.
 * <p>
 * 悬浮可拖动的FrameLayout
 * <p>
 * 内部管理实现悬浮窗功能，
 * 实现拖动时判断左右方向自动粘边效果，
 * 实现粘边处圆角转直角、非粘边时直角转圆角效果
 */
public class FloatingFrameLayout extends FrameLayout {
    /**
     * 默认高
     */
    protected final static int DEFAULT_HEIGHT = 100;
    /**
     * 默认宽
     */
    protected final static int DEFAULT_WIDTH = 100;
    /**
     * 默认宽高与当前View实际宽高
     */
    protected int mDefaultWidth, mDefaultHeight;
    protected int mWidth, mHeight;
    /**
     * 停留方向
     */
    protected Direction mStayDirection = Direction.right;
    /**
     * 当前屏幕方向
     */
    protected int mOrientation;
    /**
     * 屏幕宽度像素值
     */
    protected int mWidthPixels;
    /**
     * 屏幕高度像素值
     */
    protected int mHeightPixels;
    /**
     * 悬浮窗管理相关
     */
    protected WindowManager mWindowManager;
    protected WindowManager.LayoutParams mLayoutParams;
    /**
     * 是否为显示状态
     */
    protected boolean isShowing;
    /**
     * 是否拖动控件（只要拖动就消费掉touch，即不作为点击事件处理，避免拖动控件触发点击事件的情况）
     */
    protected boolean isDraged;
    /**
     * 判断为拖拽的滑动阈值
     */
    protected static final int JUDGE_EVENT_DRAGED_THRESHOLD = 2;
    /**
     * 当前的x
     */
    protected int mCurrentX;
    /**
     * 当前的y
     */
    protected int mCurrentY;
    /**
     * 手指按下时的x
     */
    protected int mDownX;
    /**
     * 手指按下时的y
     */
    protected int mDownY;
    /**
     * 是否正在执行动画
     */
    protected volatile boolean inAnim;
    /**
     * 是否首次layout
     */
    protected volatile boolean isFirstLayout = true;

    public FloatingFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public FloatingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatingFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    protected void init() {
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
        //FLAG_LAYOUT_NO_LIMITS 这里不限定区域，是为了属性动画，可以从外部滚动进来
        //滑动边界通过move事件来处理
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        //                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        //                | WindowManager.LayoutParams.FLAG_FULLSCREEN
        //                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        mLayoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        mLayoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

        //记录当前屏幕方向和屏幕宽度
        recordScreenWidth();

        mDefaultWidth = DEFAULT_WIDTH;
        mDefaultHeight = DEFAULT_HEIGHT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 设置wrap_content的默认宽 / 高值
        // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽 / 高值有特殊处理
        // 获取宽-测量规则的模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        // 获取高-测量规则的模式和大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        View childRootView = getChildAt(0);
        if (childRootView == null) {
            mWidth = mDefaultWidth;
            mHeight = mDefaultHeight;
            setMeasuredDimension(mWidth, mHeight);
        } else {
            mWidth = childRootView.getMeasuredWidth() + getPaddingStart() + getPaddingRight();
            mHeight = childRootView.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();

            if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT
                    && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                setMeasuredDimension(mWidth, mHeight);
                // 宽 / 高任意一个布局参数为= wrap_content时，都设置默认值
            } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                setMeasuredDimension(mWidth, heightSize);
            } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                setMeasuredDimension(widthSize, mHeight);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (isFirstLayout) {
            isFirstLayout = false;
            handleStayDirection(mLayoutParams.x, mLayoutParams.y);
            invalidate();
            //等待首次layout完成后再显示，layout之前几十毫秒的时间，不隐藏的话可能会闪烁
            setVisibility(VISIBLE);
            showWithAnim();
        }
    }

    /**
     * 处理触摸事件，实现拖动、形状变更和粘边效果
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mWindowManager != null && !inAnim) {
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
                    //限定拖动边界
                    if (mLayoutParams.x < 0) {
                        mLayoutParams.x = 0;
                    }
                    if (mLayoutParams.x > mWidthPixels - mWidth) {
                        mLayoutParams.x = mWidthPixels - mWidth;
                    }
                    if (mLayoutParams.y < 0) {
                        mLayoutParams.y = 0;
                    }
                    if (mLayoutParams.y > mHeightPixels - mHeight) {
                        mLayoutParams.y = mHeightPixels - mHeight;
                    }
                    if (mStayDirection != Direction.move) {
                        mStayDirection = Direction.move;
                        invalidate();
                    }
                    mWindowManager.updateViewLayout(this, mLayoutParams);

                    if (event.getRawX() < mWidthPixels / 2.f) {
                        moveOnLeft();
                    } else {
                        moveOnRight();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (isDraged) {
                        animToDirection(getFinalDirectionX((int) event.getRawX()));
                        isDraged = false;
                        return true;
                    } else {
                        //判定为点击处理时，移动距离不超过滑动阈值，直接吸边，并且重绘
                        handleStayDirection((int) event.getRawX(), (int) event.getRawY());
                        invalidate();
                    }
                    break;
                default:
                    break;
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 动画吸边
     *
     * @param targetX 目标X坐标
     */
    private void animToDirection(int targetX) {
        inAnim = true;
        ValueAnimator anim = ValueAnimator.ofInt(mLayoutParams.x, targetX);
        //减速插值器
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLayoutParams.x = (int) animation.getAnimatedValue();
                if (mWindowManager != null) {
                    mWindowManager.updateViewLayout(FloatingFrameLayout.this, mLayoutParams);
                }
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                handleStayDirection(mLayoutParams.x, mLayoutParams.y);
                invalidate();
                inAnim = false;
            }
        });
        anim.setDuration(300);
        anim.start();
    }

    /**
     * 记录当前屏幕方向和屏幕宽度
     */
    protected void recordScreenWidth() {
        mOrientation = getResources().getConfiguration().orientation;
        DisplayMetrics outMetrics = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(outMetrics);
        mWidthPixels = outMetrics.widthPixels;
        mHeightPixels = outMetrics.heightPixels - getStatusBarHeight(getContext());
    }

    // 获取状态栏高度
    private int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen",
                "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }

    /**
     * 处理最终吸边停留方向
     */
    protected void handleStayDirection(int x, int y) {
        if (x > (mWidthPixels / 2)) {
            mStayDirection = Direction.right;
            stayOnDirectionRight();
        } else {
            mStayDirection = Direction.left;
            stayOnDirectionLeft();
        }
        mLayoutParams.x = getFinalDirectionX(x);
    }

    /**
     * 获取吸边时x值
     *
     * @param eventX eventX
     * @return 吸边时x值
     */
    protected int getFinalDirectionX(int eventX) {
        int x;
        if (eventX > (mWidthPixels / 2)) {
            x = mWidthPixels - getMeasuredWidth();
        } else {
            x = 0;
        }
        return x;
    }

    /**
     * 显示（默认位置，右上）
     */
    public void show() {
        int x = mLayoutParams.x;
        int y = mLayoutParams.y;
        if (!isShowing) {
            if (mStayDirection == Direction.right) {
                x = mWidthPixels - mDefaultWidth;
            }
            if (mStayDirection == Direction.move) {
                if (x > (mWidthPixels / 2)) {
                    mStayDirection = Direction.right;
                    x = mWidthPixels - getMeasuredWidth();
                } else {
                    mStayDirection = Direction.left;
                    x = 0;
                }
            }
            if (mStayDirection == Direction.left) {
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
            handleStayDirection(x, y);
            //等待首次layout完成后再显示，layout之前几十毫秒的时间，不隐藏的话可能会闪烁
            setVisibility(INVISIBLE);
            mWindowManager.addView(this, mLayoutParams);
            isShowing = true;
        }
    }

    /**
     * 获取layout的x坐标
     *
     * @return layout的x坐标
     */
    public int getLayoutParamsX() {
        return mLayoutParams.x;
    }

    /**
     * 获取layout的y坐标
     *
     * @return layout的y坐标
     */
    public int getLayoutParamsY() {
        return mLayoutParams.y;
    }

    /**
     * dismiss
     */
    public void dismiss() {
        dismissWithAnim();
    }

    /**
     * 首次layout的时候执行
     */
    private void showWithAnim() {
        inAnim = true;
        ValueAnimator anim;

        if (mStayDirection == Direction.left) {
            anim = ValueAnimator.ofInt(-getMeasuredWidth(), 0);
        } else {
            anim = ValueAnimator.ofInt(mWidthPixels, mWidthPixels - getMeasuredWidth());
        }

        //减速插值器
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLayoutParams.x = (int) animation.getAnimatedValue();
                if (mWindowManager != null) {
                    mWindowManager.updateViewLayout(FloatingFrameLayout.this, mLayoutParams);
                }
                requestLayout();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                inAnim = false;
            }
        });
        anim.setDuration(300);
        anim.start();
    }

    private void dismissWithAnim() {
        inAnim = true;
        ValueAnimator anim;

        if (mStayDirection == Direction.left) {
            anim = ValueAnimator.ofInt(0, -getMeasuredWidth());
        } else {
            anim = ValueAnimator.ofInt(mWidthPixels - getMeasuredWidth(), mWidthPixels);
        }

        //减速插值器
        anim.setInterpolator(new DecelerateInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLayoutParams.x = (int) animation.getAnimatedValue();
                if (mWindowManager != null) {
                    mWindowManager.updateViewLayout(FloatingFrameLayout.this, mLayoutParams);
                }
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (isShowing && mWindowManager != null) {
                    //避免窗体内存泄露
                    mWindowManager.removeViewImmediate(FloatingFrameLayout.this);
                    isShowing = false;
                }
                inAnim = false;
            }
        });
        anim.setDuration(300);
        anim.start();
    }

    /**
     * 移动到左边
     */
    protected void moveOnLeft() {

    }

    /**
     * 移动到右边
     */
    protected void moveOnRight() {

    }

    /**
     * 停留在左侧
     */
    protected void stayOnDirectionLeft() {

    }

    /**
     * 停留在右侧
     */
    protected void stayOnDirectionRight() {

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
