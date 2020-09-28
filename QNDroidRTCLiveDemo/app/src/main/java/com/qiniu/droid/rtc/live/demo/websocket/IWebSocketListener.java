package com.qiniu.droid.rtc.live.demo.websocket;

import okhttp3.Response;
import okio.ByteString;

public interface IWebSocketListener {
    /**
     * 当与远端成功建立 websocket 连接并可以开始传输信息时触发
     */
    void onOpen(Response response);

    /**
     * 收到远端发来的 String 类型信息
     */
    void onMessage(String msg);

    /**
     * 收到远端发来的二进制信息
     */
    void onMessage(ByteString bytes);

    /**
     * 收到远端发来的 CLOSE 信息，准备关闭连接
     */
    void onClosing(int code, String reason);

    /**
     * 当连接成功释放时触发
     */
    void onClosed(int code, String reason);

    /**
     * 当由于 IO 错误导致连接关闭时触发，listener 将会失效，传入、传出的消息可能会丢失
     */
    void onFailure(Throwable t, Response response);
}
