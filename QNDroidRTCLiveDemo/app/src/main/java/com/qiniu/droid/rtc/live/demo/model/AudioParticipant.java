package com.qiniu.droid.rtc.live.demo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class AudioParticipant implements Parcelable {
    @SerializedName(value = "roomID")
    private String roomId;
    private int position;
    private UserInfo userInfo;
    private boolean mute;

    public AudioParticipant() {

    }

    protected AudioParticipant(Parcel in) {
        roomId = in.readString();
        position = in.readInt();
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());
        mute = in.readByte() != 0;
    }

    public static final Creator<AudioParticipant> CREATOR = new Creator<AudioParticipant>() {
        @Override
        public AudioParticipant createFromParcel(Parcel in) {
            return new AudioParticipant(in);
        }

        @Override
        public AudioParticipant[] newArray(int size) {
            return new AudioParticipant[size];
        }
    };

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomId);
        dest.writeInt(position);
        dest.writeParcelable(userInfo, flags);
        dest.writeByte((byte) (mute ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioParticipant)) return false;
        AudioParticipant that = (AudioParticipant) o;
        return getPosition() == that.getPosition() &&
                Objects.equals(getRoomId(), that.getRoomId()) &&
                Objects.equals(getUserInfo().getUserId(), that.getUserInfo().getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoomId(), getPosition(), getUserInfo().getUserId());
    }
}
