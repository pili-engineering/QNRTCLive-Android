package com.qiniu.droid.rtc.live.demo.signal;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.gson.Gson;
import com.qiniu.droid.rtc.live.demo.model.PkRequestInfo;
import com.qiniu.droid.rtc.live.demo.utils.Config;
import com.qiniu.droid.rtc.live.demo.utils.JsonUtils;
import com.qiniu.droid.rtc.live.demo.utils.Utils;
import com.qiniu.droid.rtc.live.demo.websocket.IWebSocketListener;
import com.qiniu.droid.rtc.live.demo.websocket.WebSocketManagerImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import androidx.annotation.WorkerThread;
import okhttp3.Response;
import okio.ByteString;

public class QNSignalClient implements IWebSocketListener {
    private static final String TAG = "QNSignalClient";

    private WebSocketManagerImpl mWebSocketManager;
    private Handler mHandler;
    private OnSignalClientListener mOnSignalClientListener;
    private volatile SignalConnectionState mSignalConnectionState = SignalConnectionState.NEW;

    private final LinkedList<String> mSendQueue;
    private long mLastReadTime;
    private long mReconnectTimeoutMs;
    private long mFirstReconnectTime = -1;

    public interface OnSignalClientListener {
        /**
         * 信令连接成功
         */
        void onConnected();

        /**
         * 信令通道认证通过
         */
        void onAuthorized();

        /**
         * 处理重连
         *
         * @return 是否处理
         */
        boolean onReconnectHandled();

        /**
         * 重连失败，重连失败后，房间信息会被销毁，需要重新创建并加入房间
         */
        void onReconnectFailed();

        /**
         * 远端 PK 请求到达
         *
         * @param requestInfo PK 请求者的信息
         */
        void onPkRequestLaunched(PkRequestInfo requestInfo);

        /**
         * 处理 PK 请求成功回调
         */
        void onReplyPkSuccess();

        /**
         * 处理 PK 请求失败回调
         *
         * @param code 错误码
         * @param reason 错误信息
         */
        void onReplyPkFailed(int code, String reason);

        /**
         * 远端 PK 请求处理回调通知
         *
         * @param isAccepted 是否接受
         * @param roomToken 远端房间 roomToken
         */
        void onPkRequestHandled(boolean isAccepted, String pkRoomId, String roomToken);

        /**
         * 本地结束 PK 成功信令返回
         */
        void onPkEnd();

        /**
         * 远端结束 PK 信令通知
         */
        void onRemoteEndPk();

        /**
         * 信令通道关闭
         */
        void onClosed(int code, String reason);

        /**
         * 信令通道异常
         */
        void onError(int errorCode, String errorMessage);
    }

    public enum SignalConnectionState {
        NEW, CONNECTED, AUTHORIZED, CLOSED, ERROR
    }

    public QNSignalClient(String wsUrl) {
        mWebSocketManager = new WebSocketManagerImpl(wsUrl);
        mWebSocketManager.setWebSocketListener(this);

        mSendQueue = new LinkedList<>();

        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public void setOnSignalClientListener(OnSignalClientListener listener) {
        mOnSignalClientListener = listener;
    }

    public void connect() {
        Log.e(TAG, "connect : " + mSignalConnectionState.name());
        if (mSignalConnectionState != SignalConnectionState.NEW
                && mSignalConnectionState != SignalConnectionState.CLOSED
                && mSignalConnectionState != SignalConnectionState.ERROR) {
            Log.e(TAG, "signal connection is already connected.");
            return;
        }
        mWebSocketManager.startConnect();
    }

    public void disconnect() {
        if (mSignalConnectionState == SignalConnectionState.CLOSED) {
            Log.e(TAG, "signal connection is already closed.");
            return;
        }
        sendDisconnect();
        mSignalConnectionState = SignalConnectionState.CLOSED;
    }

    public void destroy() {
        mHandler.post(() -> mHandler.getLooper().quit());
    }

    public void sendAuthMessage(String token) {
        if (token == null) {
            mOnSignalClientListener.onError(QNSignalErrorCode.TOKEN_ERROR, "auth token can not be null");
            return;
        }
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Config.KEY_TOKEN, token);
        String msg = generateMsg(QNSignalMethod.AUTH.getValue(), json);
        sendMessageInternal(QNSignalMethod.AUTH, msg);
    }

    public void sendPing() {
        if (mSignalConnectionState != SignalConnectionState.AUTHORIZED) {
            Log.e(TAG, "signal connection is not authorized.");
            return;
        }
        String pingMsg = generateMsg(QNSignalMethod.PING.getValue(), new JSONObject());
        sendMessageInternal(QNSignalMethod.PING, pingMsg);
    }

    public void sendPong() {
        String pongMsg = generateMsg(QNSignalMethod.PONG.getValue(), new JSONObject());
        sendMessageInternal(QNSignalMethod.PONG, pongMsg);
    }

    public void startPk(String roomId) {
        if (mSignalConnectionState != SignalConnectionState.AUTHORIZED) {
            Log.e(TAG, "signal connection is not authorized.");
            return;
        }
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Config.KEY_PK_ROOM_ID, roomId);
        String msg = generateMsg(QNSignalMethod.START_PK.getValue(), json);
        sendMessageInternal(QNSignalMethod.START_PK, msg);
    }

    public void replyPk(String roomId, boolean isAccepted) {
        if (mSignalConnectionState != SignalConnectionState.AUTHORIZED) {
            Log.e(TAG, "signal connection is not authorized.");
            return;
        }
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Config.KEY_PK_ROOM_ID, roomId);
        JsonUtils.jsonPut(json, Config.KEY_ACCEPT_PK, isAccepted);
        String msg = generateMsg(QNSignalMethod.ANSWER_PK.getValue(), json);
        sendMessageInternal(QNSignalMethod.ANSWER_PK, msg);
    }

    public void endPk(String roomId) {
        if (mSignalConnectionState != SignalConnectionState.AUTHORIZED) {
            Log.e(TAG, "signal connection is not authorized.");
            return;
        }
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Config.KEY_PK_ROOM_ID, roomId);
        String msg = generateMsg(QNSignalMethod.END_PK.getValue(), json);
        sendMessageInternal(QNSignalMethod.END_PK, msg);
    }

    private void sendDisconnect() {
        if (mSignalConnectionState != SignalConnectionState.AUTHORIZED) {
            Log.e(TAG, "signal connection is not authorized.");
            return;
        }
        String msg = generateMsg(QNSignalMethod.DISCONNECT.getValue(), new JSONObject());
        sendMessageInternal(QNSignalMethod.DISCONNECT, msg);
    }

    public boolean isConnected() {
        return mSignalConnectionState == SignalConnectionState.CONNECTED || mSignalConnectionState == SignalConnectionState.AUTHORIZED;
    }

    private String generateMsg(String type, JSONObject message) {
        JsonUtils.jsonPut(message, Config.KEY_RPC_ID, "android-" + Utils.randomNumberGenerator());
        return type + "=" + message.toString();
    }

    private double getTimeSinceLastRead() {
        return (System.currentTimeMillis() - mLastReadTime) / 1000.0;
    }

    private void sendMessageInternal(QNSignalMethod method, String message) {
        Log.e(TAG, "sendMessageInternal : " + message);
        if (method == QNSignalMethod.DISCONNECT) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHandler.post(() -> {
            if (method == QNSignalMethod.AUTH || method == QNSignalMethod.DISCONNECT) {
                sendMessage(message);
            } else {
                send(message);
            }
        });
    }

    private void sendMessage(String message) {
        mWebSocketManager.sendMessage(message);
    }

    private void send(String message) {
        switch (mSignalConnectionState) {
            case NEW:
            case CONNECTED:
                // Store outgoing messages and send them after client is authorized.
                mSendQueue.add(message);
                return;
            case ERROR:
            case CLOSED:
                Log.e(TAG, "WebSocket send() in error or closed. state : " + message);
                return;
            case AUTHORIZED:
                sendMessage(message);
                break;
        }
    }

    private boolean handleReconnect() {
        if (mOnSignalClientListener == null) {
            return false;
        }
        if (mFirstReconnectTime == -1) {
            mFirstReconnectTime = System.currentTimeMillis();
        }
        if (mFirstReconnectTime != -1) {
            if ((System.currentTimeMillis() - mFirstReconnectTime) < mReconnectTimeoutMs) {
                return mOnSignalClientListener.onReconnectHandled();
            } else {
                mFirstReconnectTime = -1;
                mOnSignalClientListener.onReconnectFailed();
            }
        }
        return false;
    }

    @WorkerThread
    private void processMessage(String msg) {
        if (mOnSignalClientListener == null) {
            return;
        }

        Log.e(TAG, "processMessage : " + msg);

        int index = msg.indexOf("=");
        if (index <= 0) {
            Log.e(TAG, "msg is invalid!");
            return;
        }

        String msgType = msg.substring(0, index);
        String msgValue = msg.substring(index + 1);
        QNSignalMethod method = QNSignalMethod.get(msgType);

        if (method == null) {
            Log.i(TAG, "unknown msg type: " + msgType);
            return;
        }

        if (method == QNSignalMethod.PING) {
            sendPong();
            return;
        }

        try {
            JSONObject json = new JSONObject(msgValue);
            int errorCode = json.optInt("code");

            if (method == QNSignalMethod.AUTH_RES) {
                if (errorCode == QNSignalErrorCode.SUCCESS) {
                    Log.e(TAG, "auth success!");
                    mSignalConnectionState = SignalConnectionState.AUTHORIZED;
                    mOnSignalClientListener.onAuthorized();
                    for (String sendMessage : mSendQueue) {
                        sendMessage(sendMessage);
                    }
                    mSendQueue.clear();
                    JSONObject authRes = new JSONObject(msgValue);
                    mReconnectTimeoutMs = authRes.optInt(Config.KEY_PONG_TIMEOUT) * 1000L;
                    Log.i(TAG, "mReconnectTimeoutMs = " + mReconnectTimeoutMs);
                } else {
                    Log.e(TAG, "auth failed : " + json.optString(Config.KEY_ERROR));
                    mOnSignalClientListener.onError(errorCode, json.optString(Config.KEY_ERROR));
                }
            } else if (method == QNSignalMethod.START_PK_RES) {
                if (errorCode != QNSignalErrorCode.SUCCESS) {
                    Log.e(TAG, "start pk failed : " + json.optString(Config.KEY_ERROR));
                    mOnSignalClientListener.onError(errorCode, json.optString(Config.KEY_ERROR));
                }
            } else if (method == QNSignalMethod.ON_PK_OFFER) {
                PkRequestInfo requestInfo = new Gson().fromJson(msgValue, PkRequestInfo.class);
                mOnSignalClientListener.onPkRequestLaunched(requestInfo);
            } else if (method == QNSignalMethod.ANSWER_PK_RES) {
                if (errorCode == QNSignalErrorCode.SUCCESS) {
                    mOnSignalClientListener.onReplyPkSuccess();
                } else {
                    Log.e(TAG, "reply pk failed : " + json.optString(Config.KEY_ERROR));
                    mOnSignalClientListener.onReplyPkFailed(errorCode, json.optString(Config.KEY_ERROR));
                }
            } else if (method == QNSignalMethod.ON_PK_ANSWER) {
                boolean isAccepted = json.optBoolean("accepted");
                String roomToken = isAccepted ? json.optString("rtcRoomToken") : null;
                String rtcRoom = json.optString("rtcRoom");
                mOnSignalClientListener.onPkRequestHandled(isAccepted, rtcRoom, roomToken);
            } else if (method == QNSignalMethod.END_PK_RES) {
                if (errorCode == QNSignalErrorCode.SUCCESS) {
                    mOnSignalClientListener.onPkEnd();
                } else {
                    Log.e(TAG, "end pk failed : " + json.optString(Config.KEY_ERROR));
                    mOnSignalClientListener.onError(errorCode, json.optString(Config.KEY_ERROR));
                }
            } else if (method == QNSignalMethod.ON_PK_END) {
                mOnSignalClientListener.onRemoteEndPk();
            }
        } catch (JSONException e) {
            Log.e(TAG, "processMessage error: " + e.getMessage());
        }
    }

    @Override
    public void onOpen(Response response) {
        Log.e(TAG, "signal connection open : " + response.toString());
        mSignalConnectionState = SignalConnectionState.CONNECTED;
        if (mOnSignalClientListener != null) {
            mOnSignalClientListener.onConnected();
        }
    }

    @Override
    public void onMessage(String msg) {
        mLastReadTime = System.currentTimeMillis();
        if (mSignalConnectionState == SignalConnectionState.CONNECTED
                || mSignalConnectionState == SignalConnectionState.AUTHORIZED) {
            mHandler.post(() -> processMessage(msg));
        }
    }

    @Override
    public void onMessage(ByteString bytes) {
        mLastReadTime = System.currentTimeMillis();
    }

    @Override
    public void onClosing(int code, String reason) {
        Log.e(TAG, "signal connection onClosing");
    }

    @Override
    public void onClosed(int code, String reason) {
        Log.e(TAG, "signal connection closed : " + reason);
        // 处理异常断开
        if (mSignalConnectionState != SignalConnectionState.CLOSED) {
            boolean reconnectHandled = handleReconnect();
            if (reconnectHandled) {
                return;
            }
        }
        mSignalConnectionState = SignalConnectionState.CLOSED;
        if (mOnSignalClientListener != null) {
            mOnSignalClientListener.onClosed(code, reason);
        }
    }

    @Override
    public void onFailure(Throwable t, Response response) {
        if (mSignalConnectionState == SignalConnectionState.CLOSED) {
            if (mOnSignalClientListener != null) {
                mOnSignalClientListener.onClosed(QNSignalErrorCode.SUCCESS, "");
            }
            return;
        }
        mSignalConnectionState = SignalConnectionState.ERROR;
        Log.e(TAG, "signal connection error : " + t.getMessage());
        // 异常断开
        if (mSignalConnectionState != SignalConnectionState.CLOSED) {
            boolean isReconnectHandled = handleReconnect();
            if (isReconnectHandled) {
                return;
            }
        }
        if (mOnSignalClientListener != null) {
            mOnSignalClientListener.onError(QNSignalErrorCode.INTERNAL_ERROR, t.getMessage());
        }
    }
}
