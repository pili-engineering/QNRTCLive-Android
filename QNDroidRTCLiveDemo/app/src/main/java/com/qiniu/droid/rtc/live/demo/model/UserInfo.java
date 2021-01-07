package com.qiniu.droid.rtc.live.demo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class UserInfo implements Parcelable {
    /**
     * id nickName gender
     */
    @SerializedName(value = "id", alternate = "reqUserID")
    private String userId;

    @SerializedName(value = "name", alternate = "nickname")
    private String nickName;

    private String gender;

    private String avatar;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public UserInfo() {

    }

    public UserInfo(String userId, String nickName, String gender, String avatar) {
        this.userId = userId;
        this.nickName = nickName;
        this.gender = gender;
        this.avatar = avatar;
    }

    protected UserInfo(Parcel in) {
        userId = in.readString();
        nickName = in.readString();
        gender = in.readString();
        avatar = in.readString();
    }

    public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
        @Override
        public UserInfo createFromParcel(Parcel in) {
            return new UserInfo(in);
        }

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(nickName);
        dest.writeString(gender);
        dest.writeString(avatar);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserInfo)) return false;
        UserInfo userInfo = (UserInfo) o;
        return getUserId().equals(userInfo.getUserId()) &&
                getNickName().equals(userInfo.getNickName()) &&
                getGender().equals(userInfo.getGender()) &&
                getAvatar().equals(userInfo.getAvatar());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getNickName(), getGender(), getAvatar());
    }
}
