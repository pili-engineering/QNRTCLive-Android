package com.qiniu.droid.rtc.live.demo.model;

public enum RoomType {
    /**
     * 普通场景
     */
    SINGLE("single"),
    /**
     * 连麦 PK 场景
     */
    PK("pk");

    private final String mValue;

    RoomType(String value) {
        mValue = value;
    }

    public static RoomType safeValueOf(String string){
        if (PK.toString().equalsIgnoreCase(string)){
            return PK;
        }
        return SINGLE;
    }

    public String getValue() {
        return mValue;
    }
}
