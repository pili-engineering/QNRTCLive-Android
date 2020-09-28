package com.qiniu.droid.rtc.live.demo.websocket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManagerImpl implements IWebSocketManager {
    private static final int NORMAL_CLOSE = 1000;

    private OkHttpClient mOkHttpClient;
    private WebSocket mWebSocket;
    private IWebSocketListener mWsListener;
    private String mWsUrl;

    public WebSocketManagerImpl(String wsUrl) {
        mOkHttpClient = new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(false)
                .build();
        mWsUrl = wsUrl;
    }

    public void setWebSocketListener(IWebSocketListener listener) {
        mWsListener = listener;
    }

    @Override
    public void startConnect() {
        Request request = new Request.Builder()
                .url(mWsUrl)
                .build();
        mOkHttpClient.dispatcher().cancelAll();
        mOkHttpClient.newWebSocket(request, mWebSocketListener);
    }

    @Override
    public void stopConnect() {
        if (mWebSocket != null) {
            mWebSocket.close(NORMAL_CLOSE, null);
        }
    }

    @Override
    public boolean sendMessage(String msg) {
        if (mWebSocket != null) {
            return mWebSocket.send(msg);
        }
        return false;
    }

    @Override
    public boolean sendMessage(ByteString msg) {
        if (mWebSocket != null) {
            return mWebSocket.send(msg);
        }
        return false;
    }

    private WebSocketListener mWebSocketListener = new WebSocketListener() {
        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);
            if (mOkHttpClient != null) {
                mOkHttpClient.dispatcher().cancelAll();
            }
            if (mWsListener != null) {
                mWsListener.onClosed(code, reason);
            }
        }

        @Override
        public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosing(webSocket, code, reason);
            if (mWsListener != null) {
                mWsListener.onClosing(code, reason);
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);
            if (mOkHttpClient != null) {
                mOkHttpClient.dispatcher().cancelAll();
            }
            if (mWsListener != null) {
                mWsListener.onFailure(t, response);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            if (mWsListener != null) {
                mWsListener.onMessage(text);
            }
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);
            if (mWsListener != null) {
                mWsListener.onMessage(bytes);
            }
        }

        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);
            mWebSocket = webSocket;
            if (mWsListener != null) {
                mWsListener.onOpen(response);
            }
        }
    };
}
