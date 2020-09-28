package com.qiniu.droid.rtc.live.demo.websocket;

import okio.ByteString;

public interface IWebSocketManager {
    void startConnect();
    void stopConnect();
    boolean sendMessage(String msg);
    boolean sendMessage(ByteString msg);
}
