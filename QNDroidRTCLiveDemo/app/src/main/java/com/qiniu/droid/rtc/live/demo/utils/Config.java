package com.qiniu.droid.rtc.live.demo.utils;

public class Config {
    /**
     * 设置推流画面尺寸，仅用于 Demo 测试，用户可以在创建七牛 APP 时设置该参数
     */
    public static final int STREAMING_WIDTH = 720;
    public static final int STREAMING_HEIGHT =1280;
    public static final String STREAMING_BACKGROUND = "http://pili-playback.qnsdk.com/streaming_background.png";

    public static final long GET_AUDIENCE_NUM_PERIOD = 5;
    public static final long PLAYER_RECONNECT_PERIOD = 3;
    public static final long REFRESH_LIVE_ROOMS_PERIOD = 30;
    public static final long REFRESH_LIVE_ROOMS_INITIAL_DELAY = 2;

    public static final String KEY_ROOM_ID = "roomID";
    public static final String KEY_ROOM_NAME = "roomName";
    public static final String KEY_ROOM_TOKEN = "rtcRoomToken";
    public static final String KEY_WS_URL = "wsURL";
    public static final String KEY_ROOMS = "rooms";

    public static final String KEY_RPC_ID = "rpcID";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_PK_ROOM_ID = "pkRoomID";
    public static final String KEY_ACCEPT_PK = "accept";
    public static final String KEY_ERROR = "error";
    public static final String KEY_PONG_TIMEOUT = "pongTimeout";

    public static final String SP_USER_INFO_NAME = "userInfo";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_NICK_NAME = "nickName";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_AUTH_TOKEN = "authToken";

    public static final String SP_ROOM_INFO_NAME = "roomInfo";

    public static final String KEY_CHAT_TOKEN = "token";
    public static final String KEY_CHAT_USER_ID = "userID";
}
