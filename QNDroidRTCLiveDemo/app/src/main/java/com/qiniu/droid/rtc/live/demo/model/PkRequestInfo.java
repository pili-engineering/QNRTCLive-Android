package com.qiniu.droid.rtc.live.demo.model;

import com.google.gson.annotations.SerializedName;

public class PkRequestInfo {
    @SerializedName("userID")
    String userId;

    @SerializedName("nickname")
    String nickName;

    @SerializedName("roomID")
    String roomId;

    String roomName;

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

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
}
