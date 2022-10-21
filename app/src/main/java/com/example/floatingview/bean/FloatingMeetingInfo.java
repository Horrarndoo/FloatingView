package com.example.floatingview.bean;

/**
 * Created by Horrarndoo on 2022/10/18.
 * <p>
 * 会议悬浮窗信息
 */
public class FloatingMeetingInfo {
    /**
     * 会议信息
     */
    private MeetingInfo meetingInfo;
    /**
     * 演示成员id
     */
    private int demonstratorId;

    public MeetingInfo getMeetingInfo() {
        return meetingInfo;
    }

    public void setMeetingInfo(MeetingInfo meetingInfo) {
        this.meetingInfo = meetingInfo;
    }

    public int getDemonstratorId() {
        return demonstratorId;
    }

    public void setDemonstratorId(int demonstratorId) {
        this.demonstratorId = demonstratorId;
    }
}
