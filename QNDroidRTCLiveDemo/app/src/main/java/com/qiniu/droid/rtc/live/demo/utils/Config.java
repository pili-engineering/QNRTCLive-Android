package com.qiniu.droid.rtc.live.demo.utils;

public class Config {
    /**
     * 设置推流画面尺寸，仅用于 Demo 测试，用户可以在创建七牛 APP 时设置该参数
     */
    public static final int STREAMING_WIDTH = 720;
    public static final int STREAMING_HEIGHT = 1280;
    public static final int STREAMING_FPS = 30;
    public static final int STREAMING_BITRATE = 2000 * 1000;
    public static final String STREAMING_BACKGROUND = "http://pili-playback.qnsdk.com/streaming_black_background.png";

    public static final long GET_AUDIENCE_NUM_PERIOD = 5;
    public static final long PLAYER_RECONNECT_PERIOD = 3;
}
