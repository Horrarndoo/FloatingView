package com.example.floatingview.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.floatingview.constants.MeetingControlStatus;
import com.example.floatingview.constants.MeetingType;

import java.util.ArrayList;

/**
 * Created by Horrarndoo on 2022/10/12.
 * <p>
 * 会议信息
 */
public class MeetingInfo implements Parcelable {
    /**
     * 会议类型
     */
    private int meetingType = MeetingType.NONE;
    /**
     * 会议发起人id
     */
    private int initiatorId = -1;
    /**
     * 会议内成员id集合（包含发起人）
     */
    private ArrayList<Integer> meetingMemberIds = new ArrayList<>();
    /**
     * 会议开始时间
     */
    private long startTime;
    /**
     * 麦克风状态（成员本机）
     */
    private int micStatus = MeetingControlStatus.ON;
    /**
     * 扬声器状态（成员本机）
     */
    private int speakerStatus = MeetingControlStatus.ON;
    /**
     * 摄像头状态（成员本机）
     */
    private int cameraStatus = MeetingControlStatus.ON;

    public MeetingInfo() {
    }

    protected MeetingInfo(Parcel in) {
        meetingType = in.readInt();
        initiatorId = in.readInt();
        micStatus = in.readInt();
        speakerStatus = in.readInt();
        cameraStatus = in.readInt();
        in.readList(meetingMemberIds, Integer.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(meetingType);
        dest.writeInt(initiatorId);
        dest.writeInt(micStatus);
        dest.writeInt(speakerStatus);
        dest.writeInt(cameraStatus);
        dest.writeList(meetingMemberIds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MeetingInfo> CREATOR = new Creator<MeetingInfo>() {
        @Override
        public MeetingInfo createFromParcel(Parcel in) {
            return new MeetingInfo(in);
        }

        @Override
        public MeetingInfo[] newArray(int size) {
            return new MeetingInfo[size];
        }
    };

    public int getInitiatorId() {
        return initiatorId;
    }

    public void setInitiatorId(int initiatorId) {
        this.initiatorId = initiatorId;
    }

    public int getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(int meetingType) {
        this.meetingType = meetingType;
    }

    public ArrayList<Integer> getMeetingMemberIds() {
        if (meetingMemberIds == null) {
            meetingMemberIds = new ArrayList<>();
        }
        return meetingMemberIds;
    }

    public void setMeetingMemberIds(ArrayList<Integer> meetingMemberIds) {
        this.meetingMemberIds = meetingMemberIds;
    }

    public int getMicStatus() {
        return micStatus;
    }

    public void setMicStatus(int micStatus) {
        this.micStatus = micStatus;
    }

    public int getSpeakerStatus() {
        return speakerStatus;
    }

    public void setSpeakerStatus(int speakerStatus) {
        this.speakerStatus = speakerStatus;
    }

    public int getCameraStatus() {
        return cameraStatus;
    }

    public void setCameraStatus(int cameraStatus) {
        this.cameraStatus = cameraStatus;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * 移除参会人员iD
     *
     * @param memberId 成员id
     */
    public void removeMemberId(int memberId) {
        getMeetingMemberIds().remove(Integer.valueOf(memberId));
    }

    /**
     * 会议控件状态是否相同
     *
     * @param info MeetingInfo
     * @return 会议控件状态是否相同
     */
    public boolean equalWithMeetingControl(MeetingInfo info) {
        return micStatus == info.getMicStatus()
                && speakerStatus == info.getSpeakerStatus()
                && cameraStatus == info.getCameraStatus();
    }
}
