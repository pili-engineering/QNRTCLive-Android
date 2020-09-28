package com.qiniu.droid.rtc.live.demo.signal;

import java.util.HashMap;
import java.util.Map;

public enum QNSignalMethod {

    /**
     * 认证
     */
    AUTH("auth"),

    /**
     * 认证结果
     */
    AUTH_RES("auth-res"),

    /**
     * 心跳
     */
    PING("ping"),

    /**
     * 心跳回应
     */
    PONG("pong"),

    /**
     * 发起 PK
     */
    START_PK("start-pk"),

    /**
     * 发起 PK 请求的回应
     */
    START_PK_RES("start-pk-res"),

    /**
     * 远端发来 PK 请求
     */
    ON_PK_OFFER("on-pk-offer"),

    /**
     * 接受/拒绝 PK 请求
     */
    ANSWER_PK("answer-pk"),

    /**
     * 回复 PK 请求处理结果
     */
    ANSWER_PK_RES("answer-pk-res"),

    /**
     * PK 请求被回应的推送
     */
    ON_PK_ANSWER("on-pk-answer"),

    /**
     * 结束 PK
     */
    END_PK("end-pk"),

    /**
     * 结束 PK 返回
     */
    END_PK_RES("end-pk-res"),

    /**
     * 结束 PK 通知
     */
    ON_PK_END("on-pk-end"),

    /**
     * 主动断开连接
     */
    DISCONNECT("disconnect");

    private final String mValue;

    QNSignalMethod(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    //Lookup table
    private static final Map<String, QNSignalMethod> lookup = new HashMap<>();

    //Populate the lookup table on loading time
    static {
        for (QNSignalMethod method : QNSignalMethod.values()) {
            lookup.put(method.getValue(), method);
        }
    }

    public static QNSignalMethod get(String url) {
        return lookup.get(url);
    }
}
