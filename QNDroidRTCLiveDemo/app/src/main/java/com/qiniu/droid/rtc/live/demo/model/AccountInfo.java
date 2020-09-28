package com.qiniu.droid.rtc.live.demo.model;

import android.os.Parcel;
import android.os.Parcelable;

public class AccountInfo implements Parcelable {

    private UserInfo userInfo;

    private String token;

    public AccountInfo(UserInfo userInfo, String token) {
        this.userInfo = userInfo;
        this.token = token;
    }

    protected AccountInfo(Parcel in) {
        userInfo = in.readParcelable(UserInfo.class.getClassLoader());
        token = in.readString();
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static final Creator<AccountInfo> CREATOR = new Creator<AccountInfo>() {
        @Override
        public AccountInfo createFromParcel(Parcel in) {
            return new AccountInfo(in);
        }

        @Override
        public AccountInfo[] newArray(int size) {
            return new AccountInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(userInfo, flags);
        dest.writeString(token);
    }
}
