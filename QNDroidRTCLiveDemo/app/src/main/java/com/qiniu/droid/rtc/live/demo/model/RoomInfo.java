package com.qiniu.droid.rtc.live.demo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

public class RoomInfo implements Parcelable {
    String id;
    String name;
    UserInfo creator;

    @SerializedName("playURL")
    String playUrl;
    String status;
    int audienceNumber;
    UserInfo pkAnchor;
    List<AudioParticipant> joinedAudiences;

    public RoomInfo() {

    }

    protected RoomInfo(Parcel in) {
        id = in.readString();
        name = in.readString();
        creator = in.readParcelable(UserInfo.class.getClassLoader());
        playUrl = in.readString();
        status = in.readString();
        audienceNumber = in.readInt();
        pkAnchor = in.readParcelable(UserInfo.class.getClassLoader());
        joinedAudiences = in.createTypedArrayList(AudioParticipant.CREATOR);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserInfo getCreator() {
        return creator;
    }

    public void setCreator(UserInfo creator) {
        this.creator = creator;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAudienceNumber() {
        return audienceNumber;
    }

    public void setAudienceNumber(int audienceNumber) {
        this.audienceNumber = audienceNumber;
    }

    public UserInfo getPkAnchor() {
        return pkAnchor;
    }

    public void setPkAnchor(UserInfo pkAnchor) {
        this.pkAnchor = pkAnchor;
    }

    public List<AudioParticipant> getJoinedAudiences() {
        return joinedAudiences;
    }

    public void setJoinedAudiences(List<AudioParticipant> joinedAudiences) {
        this.joinedAudiences = joinedAudiences;
    }

    public static final Creator<RoomInfo> CREATOR = new Creator<RoomInfo>() {
        @Override
        public RoomInfo createFromParcel(Parcel in) {
            return new RoomInfo(in);
        }

        @Override
        public RoomInfo[] newArray(int size) {
            return new RoomInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(creator, flags);
        dest.writeString(playUrl);
        dest.writeString(status);
        dest.writeInt(audienceNumber);
        dest.writeParcelable(pkAnchor, flags);
        dest.writeTypedList(joinedAudiences);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomInfo)) return false;
        RoomInfo roomInfo = (RoomInfo) o;
        return getAudienceNumber() == roomInfo.getAudienceNumber() &&
                Objects.equals(getId(), roomInfo.getId()) &&
                Objects.equals(getName(), roomInfo.getName()) &&
                Objects.equals(getCreator(), roomInfo.getCreator()) &&
                Objects.equals(getPlayUrl(), roomInfo.getPlayUrl()) &&
                Objects.equals(getStatus(), roomInfo.getStatus()) &&
                Objects.equals(getPkAnchor(), roomInfo.getPkAnchor()) &&
                Objects.equals(getJoinedAudiences(), roomInfo.getJoinedAudiences());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getCreator(), getPlayUrl(), getStatus(), getAudienceNumber(), getPkAnchor(), getJoinedAudiences());
    }
}
