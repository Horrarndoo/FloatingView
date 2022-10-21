package com.example.floatingview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.floatingview.R;
import com.example.floatingview.bean.FloatingMeetingInfo;
import com.example.floatingview.bean.MeetingInfo;
import com.example.floatingview.constants.MeetingControlStatus;
import com.example.floatingview.manager.ThreadManager;
import com.example.floatingview.utils.DisplayUtils;
import com.example.floatingview.utils.IconUtils;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

/**
 * Created by Horrarndoo on 2022/10/17.
 * <p>
 * 会议状态悬浮控件
 */
public class MeetingFloatingLayout extends FloatingFrameLayout {
    private FloatingMeetingInfo mFloatingMeetingInfo;
    private LinearLayout llRoot;
    private TextView tvTime;
    private TextView tvIcon;
    private ImageView ivMicLeft;
    private ImageView ivMicRight;
    private ImageView ivCameraLeft;
    private ImageView ivCameraRight;
    private ImageView ivArrowLeft;
    private ImageView ivArrowRight;
    private FrameLayout flLeft;
    private FrameLayout flRight;
    private LinearLayout llStatusLeft;
    private LinearLayout llStatusRight;

    /**
     * 当前View绘制相关
     */
    private Paint mBgPaint;
    private Paint mStrokePaint;
    private int mStrokeWidth;
    private float mRadiusBg;
    private float mRadiusStrokeOut;
    private float mRadiusStrokeInner;
    private int mBgColor;
    private int mOutStrokeColor;
    private int mInnerStrokeColor;

    public MeetingFloatingLayout(Context context, FloatingMeetingInfo meetingInfo) {
        super(context);
        this.mFloatingMeetingInfo = meetingInfo;
        initView(context);
    }

    public MeetingFloatingLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MeetingFloatingLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void init() {
        super.init();
        //允许绘制
        setWillNotDraw(false);

        //颜色
        mBgColor = getContext().getColor(R.color.meeting_floating_view_bg_color);
        mOutStrokeColor =
                getContext().getColor(R.color.meeting_floating_view_bg_stroke_out_color);
        mInnerStrokeColor =
                getContext().getColor(R.color.meeting_floating_view_bg_stroke_inner_color);

        //当前View绘制相关
        mBgPaint = new Paint();
        mBgPaint.setAntiAlias(true);
        mBgPaint.setColor(mBgColor);

        mStrokePaint = new Paint();
        mStrokePaint.setAntiAlias(true);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokeWidth = DisplayUtils.dp2px(1);
        mStrokePaint.setStrokeWidth(mStrokeWidth);
    }

    private void initView(Context context) {
        llRoot = (LinearLayout) LayoutInflater.from(context)
                .inflate(R.layout.floating_meeting_status, this, false);
        tvTime = llRoot.findViewById(R.id.tv_time);
        tvIcon = llRoot.findViewById(R.id.tv_icon);
        ivMicLeft = llRoot.findViewById(R.id.iv_mic_left);
        ivMicRight = llRoot.findViewById(R.id.iv_mic_right);
        ivCameraLeft = llRoot.findViewById(R.id.iv_camera_left);
        ivCameraRight = llRoot.findViewById(R.id.iv_camera_right);
        ivArrowLeft = llRoot.findViewById(R.id.iv_arrow_left);
        ivArrowRight = llRoot.findViewById(R.id.iv_arrow_right);
        flLeft = llRoot.findViewById(R.id.fl_left);
        flRight = llRoot.findViewById(R.id.fl_right);
        llStatusLeft = llRoot.findViewById(R.id.ll_status_left);
        llStatusRight = llRoot.findViewById(R.id.ll_status_right);

        addView(llRoot);

        updateView();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRadiusBg = DisplayUtils.dp2px(15);
        mRadiusStrokeOut = mRadiusBg * (mHeight - mStrokeWidth) * 1.f / mHeight;
        mRadiusStrokeInner = mRadiusBg * (mHeight - 2.f * mStrokeWidth) / mHeight;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        drawBackground(canvas);
    }

    /**
     * 绘制圆角边线
     *
     * @param canvas canvas
     */
    private void drawStrokeRect(Canvas canvas) {
        RectF strokeOutRect = new RectF(mStrokeWidth, mStrokeWidth, mWidth - mStrokeWidth,
                mHeight - mStrokeWidth);
        RectF strokeInnerRect = new RectF(2 * mStrokeWidth, 2 * mStrokeWidth,
                mWidth - 2 * mStrokeWidth, mHeight - 2 * mStrokeWidth);

        //外边线
        mStrokePaint.setColor(mOutStrokeColor);
        canvas.drawRoundRect(strokeOutRect, mRadiusStrokeOut, mRadiusStrokeOut, mStrokePaint);
        //内边线
        mStrokePaint.setColor(mInnerStrokeColor);
        canvas.drawRoundRect(strokeInnerRect, mRadiusStrokeInner, mRadiusStrokeInner, mStrokePaint);
    }

    /**
     * 绘制右侧直线边线
     *
     * @param canvas canvas
     */
    private void drawHorizontalStrokeRight(Canvas canvas) {
        //外边线
        mStrokePaint.setColor(mOutStrokeColor);
        canvas.drawLine(mWidth / 2.f, mStrokeWidth, mWidth, mStrokeWidth, mStrokePaint);
        canvas.drawLine(mWidth / 2.f, mHeight - mStrokeWidth, mWidth,
                mHeight - mStrokeWidth, mStrokePaint);
        //内边线
        mStrokePaint.setColor(mInnerStrokeColor);
        canvas.drawLine(mWidth / 2.f, mStrokeWidth * 2, mWidth, mStrokeWidth * 2,
                mStrokePaint);
        canvas.drawLine(mWidth / 2.f, mHeight - mStrokeWidth * 2, mWidth,
                mHeight - mStrokeWidth * 2, mStrokePaint);
    }

    /**
     * 绘制左侧直线边线
     *
     * @param canvas canvas
     */
    private void drawHorizontalStrokeLeft(Canvas canvas) {
        //外边线
        mStrokePaint.setColor(mOutStrokeColor);
        canvas.drawLine(0, mStrokeWidth, mWidth / 2.f, mStrokeWidth, mStrokePaint);
        canvas.drawLine(0, mHeight - mStrokeWidth, mWidth / 2.f, mHeight - mStrokeWidth,
                mStrokePaint);
        //内边线
        mStrokePaint.setColor(mInnerStrokeColor);
        canvas.drawLine(0, mStrokeWidth * 2, mWidth / 2.f, mStrokeWidth * 2,
                mStrokePaint);
        canvas.drawLine(0, mHeight - mStrokeWidth * 2, mWidth / 2.f,
                mHeight - mStrokeWidth * 2, mStrokePaint);
    }

    /**
     * 绘制边线
     *
     * @param canvas canvas
     */
    private void drawBackground(Canvas canvas) {
        //画圆角背景（边线内内容区域）
        canvas.drawRoundRect(0, 0, mWidth, mHeight, mRadiusBg, mRadiusBg, mBgPaint);
        //画圆角边线
        drawStrokeRect(canvas);
        //根据最后停留方向（left or right）绘制多一层直角矩形，覆盖圆角
        switch (mStayDirection) {
            default:
            case right:
                //覆盖右边圆角
                canvas.drawRect(mWidth / 2.f, 0, mWidth, mHeight, mBgPaint);
                //覆盖边线
                drawHorizontalStrokeRight(canvas);
                break;
            case left:
                //覆盖左边圆角
                canvas.drawRect(0, 0, mWidth / 2.f, mHeight, mBgPaint);
                //覆盖边线
                drawHorizontalStrokeLeft(canvas);
                break;
            case move:
                break;
        }
    }

    @Override
    public void show(int x, int y) {
        super.show(x, y);
        //启动时间更新任务
        mTimeUpdateRunnable = new TimeUpdateRunnable();
        ThreadManager.getThreadPool().execute(mTimeUpdateRunnable);
    }

    @Override
    public void dismiss() {
        ThreadManager.getThreadPool().cancel(mTimeUpdateRunnable);
        super.dismiss();
    }

    private void updateView() {
        MeetingInfo meetingInfo = mFloatingMeetingInfo.getMeetingInfo();
        //头像
        tvIcon.setText(String.valueOf(mFloatingMeetingInfo.getDemonstratorId()));
        tvIcon.setBackground(IconUtils.getIconRectArcDrawable(mFloatingMeetingInfo.getDemonstratorId()));

        if (meetingInfo.getMicStatus() == MeetingControlStatus.ON) {
            ivMicLeft.setImageResource(R.drawable.ic_vector_mic);
            ivMicLeft.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_on_color)));
            ivMicRight.setImageResource(R.drawable.ic_vector_mic);
            ivMicRight.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_on_color)));
        } else {
            ivMicLeft.setImageResource(R.drawable.ic_vector_mic_off);
            ivMicLeft.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_off_color)));
            ivMicRight.setImageResource(R.drawable.ic_vector_mic_off);
            ivMicRight.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_off_color)));
        }

        if (meetingInfo.getCameraStatus() == MeetingControlStatus.ON) {
            ivCameraLeft.setImageResource(R.drawable.ic_vector_video);
            ivCameraLeft.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_on_color)));
            ivCameraRight.setImageResource(R.drawable.ic_vector_video);
            ivCameraRight.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_on_color)));
        } else {
            ivCameraLeft.setImageResource(R.drawable.ic_vector_video_off);
            ivCameraLeft.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_off_color)));
            ivCameraRight.setImageResource(R.drawable.ic_vector_video_off);
            ivCameraRight.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                    R.color.meeting_floating_view_icon_off_color)));
        }
        updateMeetingTime();
    }

    @Override
    protected void moveOnLeft() {
        if (flLeft.getVisibility() == VISIBLE && ivArrowLeft.getVisibility() == VISIBLE) {
            return;
        }

        flRight.setVisibility(GONE);
        flLeft.setVisibility(VISIBLE);

        ivArrowLeft.setVisibility(VISIBLE);
        llStatusLeft.setVisibility(INVISIBLE);
    }

    @Override
    protected void moveOnRight() {
        if (flRight.getVisibility() == VISIBLE && ivArrowRight.getVisibility() == VISIBLE) {
            return;
        }

        flLeft.setVisibility(GONE);
        flRight.setVisibility(VISIBLE);

        ivArrowRight.setVisibility(VISIBLE);
        llStatusRight.setVisibility(INVISIBLE);
    }

    @Override
    protected void stayOnDirectionLeft() {
        flLeft.setVisibility(VISIBLE);
        flRight.setVisibility(GONE);

        ivArrowLeft.setVisibility(INVISIBLE);
        llStatusLeft.setVisibility(VISIBLE);
    }

    @Override
    protected void stayOnDirectionRight() {
        flLeft.setVisibility(GONE);
        flRight.setVisibility(VISIBLE);

        ivArrowRight.setVisibility(INVISIBLE);
        llStatusRight.setVisibility(VISIBLE);
    }

    /**
     * 更新会议时间
     */
    private void updateMeetingTime() {
        long time = System.currentTimeMillis() -
                mFloatingMeetingInfo.getMeetingInfo().getStartTime();
        tvTime.setText(timeMsFormatToString(time));
    }

    /**
     * 把毫秒转换成：1：20：30这样的形式
     *
     * @param timeMs 时间毫秒值
     * @return 转换后的字符串
     */
    @SuppressLint("DefaultLocale")
    public String timeMsFormatToString(long timeMs) {
        //转换成字符串的时间
        int totalSeconds = (int) (timeMs / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private TimeUpdateRunnable mTimeUpdateRunnable;

    private class TimeUpdateRunnable implements Runnable {
        @Override
        public void run() {
            while (isShowing) {
                try {
                    //1s刷新一次时间
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                post(new Runnable() {
                    @Override
                    public void run() {
                        updateMeetingTime();
                    }
                });
            }
        }
    }
}
