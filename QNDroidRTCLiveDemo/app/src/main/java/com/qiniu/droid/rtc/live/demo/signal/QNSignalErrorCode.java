package com.qiniu.droid.rtc.live.demo.signal;

public class QNSignalErrorCode {
    /**
     * 成功
     */
    public static final int SUCCESS = 0;

    /**
     * 内部错误
     */
    public static final int INTERNAL_ERROR = 1;

    /**
     * 消息不属于已知类型，无法解析
     */
    public static final int UNKNOWN_MESSAGE = 10001;

    /**
     * 认证用的 token 错误
     */
    public static final int TOKEN_ERROR = 10002;

    /**
     * 没有权限（观众发起请求等情况）
     */
    public static final int NO_PERMISSION = 10003;

    /**
     * 房间不存在
     */
    public static final int ROOM_NOT_EXIST = 10011;

    /**
     * 房间正在 PK 连麦直播中，不能发起 PK
     */
    public static final int ROOM_IN_PK = 10012;

    /**
     * 房间未在 PK 中，不能结束 PK
     */
    public static final int ROOM_NOT_IN_PK = 10013;
}
