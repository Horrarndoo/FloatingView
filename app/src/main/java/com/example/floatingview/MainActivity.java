package com.example.floatingview;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.example.floatingview.bean.FloatingMeetingInfo;
import com.example.floatingview.bean.MeetingInfo;
import com.example.floatingview.constants.MeetingControlStatus;
import com.example.floatingview.constants.MeetingType;
import com.example.floatingview.helper.FloatingWindowHelper;
import com.example.floatingview.utils.DisplayUtils;
import com.example.floatingview.widget.MeetingFloatingLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private MeetingFloatingLayout mMeetingFloatingLayout;
    private int mFloatingLastX;
    private int mFloatingLastY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //默认显示在右上
        mFloatingLastX = DisplayUtils.getScreenWidthPixels(this);
        mFloatingLastY = DisplayUtils.dp2px(60);

        findViewById(R.id.btn_show_floating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Android 6.0 以下无需获取权限，可直接展示悬浮窗
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        //判断是否拥有悬浮窗权限，无则跳转悬浮窗权限授权页面
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        //这里带一个requestCode，但是不需要处理，仅用于获得权限后能返回当前页面
                        startActivityForResult(intent, 100);
                        return;
                    }
                }

                showMeetingFloatingLayout();
            }
        });

        findViewById(R.id.btn_hide_floating).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissFloatingLayout();
            }
        });
    }

    private void showMeetingFloatingLayout() {
        MeetingInfo meetingInfo = new MeetingInfo();
        meetingInfo.setMeetingType(MeetingType.VOICE);
        meetingInfo.setInitiatorId(0);
        meetingInfo.setStartTime(System.currentTimeMillis());
        meetingInfo.setMicStatus(MeetingControlStatus.ON);
        meetingInfo.setCameraStatus(MeetingControlStatus.OFF);
        FloatingMeetingInfo floatingMeetingInfo = new FloatingMeetingInfo();
        floatingMeetingInfo.setDemonstratorId(0);
        floatingMeetingInfo.setMeetingInfo(meetingInfo);

        if (mMeetingFloatingLayout == null) {
            mMeetingFloatingLayout = new MeetingFloatingLayout(this, floatingMeetingInfo);
        }

        mMeetingFloatingLayout.show(mFloatingLastX, mFloatingLastY);
    }

    /**
     * 隐藏悬浮窗
     */
    private void dismissFloatingLayout() {
        if (mMeetingFloatingLayout == null) {
            return;
        }
        //更新最后一次的位置，用于下次显示
        mFloatingLastX = mMeetingFloatingLayout.getLayoutParamsX();
        mFloatingLastY = mMeetingFloatingLayout.getLayoutParamsY();
        mMeetingFloatingLayout.dismiss();
        //每次dismiss都是从window移除FloatingView，因此dismiss后需要将view置空，否则会以为失去父窗体而报内存泄露
        mMeetingFloatingLayout = null;
    }
}