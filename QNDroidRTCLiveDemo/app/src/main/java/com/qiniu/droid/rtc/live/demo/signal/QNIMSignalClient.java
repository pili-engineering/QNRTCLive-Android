package com.qiniu.droid.rtc.live.demo.signal;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.qiniu.droid.rtc.live.demo.im.ChatroomKit;
import com.qiniu.droid.rtc.live.demo.model.AudioParticipant;
import com.qiniu.droid.rtc.live.demo.model.PkRequestInfo;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;
import com.qiniu.droid.rtc.live.demo.utils.Constants;
import com.qiniu.droid.rtc.live.demo.utils.JsonUtils;
import com.qiniu.droid.rtc.live.demo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

public class QNIMSignalClient implements ISignalClient, Handler.Callback {
    private static final String TAG = "QNIMSignalClient";

    private static final String SIGNAL_TARGET_ID = "qlive-system";

    private Handler mSignalHandler;
    private OnSignalClientListener mOnSignalClientListener;

    public QNIMSignalClient() {
        mSignalHandler = new Handler(this);
        ChatroomKit.addEventHandler(mSignalHandler);
    }

    public void setOnSignalClientListener(OnSignalClientListener listener) {
        mOnSignalClientListener = listener;
    }

    public void destroy() {
        ChatroomKit.removeEventHandler(mSignalHandler);
        mSignalHandler.removeCallbacksAndMessages(null);
        mSignalHandler = null;
    }

    @Override
    public void sendPing() {
        String pingMsg = generateMsg(QNSignalMethod.PING.getValue(), new JSONObject());
        TextMessage content = TextMessage.obtain(pingMsg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void sendPong() {
        String pingMsg = generateMsg(QNSignalMethod.PONG.getValue(), new JSONObject());
        TextMessage content = TextMessage.obtain(pingMsg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void startPk(String roomId) {
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Constants.KEY_PK_ROOM_ID, roomId);
        String msg = generateMsg(QNSignalMethod.START_PK.getValue(), json);
        TextMessage content = TextMessage.obtain(msg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void endPk(String roomId) {
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Constants.KEY_PK_ROOM_ID, roomId);
        String msg = generateMsg(QNSignalMethod.END_PK.getValue(), json);
        TextMessage content = TextMessage.obtain(msg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void answerPk(String roomId, boolean isAccepted) {
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Constants.KEY_REQUEST_ROOM_ID, roomId);
        JsonUtils.jsonPut(json, Constants.KEY_ACCEPT, isAccepted);
        String msg = generateMsg(QNSignalMethod.ANSWER_PK.getValue(), json);
        TextMessage content = TextMessage.obtain(msg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void startJoin(String roomId, int position) {
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Constants.KEY_ROOM_ID, roomId);
        JsonUtils.jsonPut(json, Constants.KEY_POSITION, position);
        String msg = generateMsg(QNSignalMethod.START_JOIN.getValue(), json);
        TextMessage content = TextMessage.obtain(msg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void answerJoin(String roomId, String reqUserId, boolean isAccepted) {
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Constants.KEY_ROOM_ID, roomId);
        JsonUtils.jsonPut(json, Constants.KEY_REQUEST_USER_ID, reqUserId);
        JsonUtils.jsonPut(json, Constants.KEY_ACCEPT, isAccepted);
        String msg = generateMsg(QNSignalMethod.ANSWER_JOIN.getValue(), json);
        TextMessage content = TextMessage.obtain(msg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void endJoin(String roomId, String reqUserId) {
        JSONObject json = new JSONObject();
        JsonUtils.jsonPut(json, Constants.KEY_ROOM_ID, roomId);
        JsonUtils.jsonPut(json, Constants.KEY_REQUEST_USER_ID, reqUserId);
        String msg = generateMsg(QNSignalMethod.END_JOIN.getValue(), json);
        TextMessage content = TextMessage.obtain(msg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public void disconnect() {
        String msg = generateMsg(QNSignalMethod.DISCONNECT.getValue(), new JSONObject());
        TextMessage content = TextMessage.obtain(msg);
        ChatroomKit.sendSignalMessage(SIGNAL_TARGET_ID, content);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case ChatroomKit.MESSAGE_ARRIVED:
                MessageContent messageContent = ((io.rong.imlib.model.Message) msg.obj).getContent();
                if (!(messageContent instanceof TextMessage)) {
                    return false;
                }
                String content = ((TextMessage) messageContent).getContent();

                int index = content.indexOf("=");
                if (index <= 0) {
                    Log.e(TAG, "msg is invalid!");
                    return true;
                }

                String msgType = content.substring(0, index);
                String msgValue = content.substring(index + 1);
                QNSignalMethod method = QNSignalMethod.get(msgType);

                if (method == null) {
                    Log.i(TAG, "unknown msg type: " + msgType);
                    return true;
                }

                if (method == QNSignalMethod.PING) {
                    sendPong();
                    return true;
                }

                Log.i(TAG, "msgType = " + msgType + " msgValue = " + msgValue);

                try {
                    JSONObject json = new JSONObject(msgValue);
                    int errorCode = json.optInt("code");

                    if (method == QNSignalMethod.START_PK_RES) {
                        if (errorCode != QNSignalErrorCode.SUCCESS && mOnSignalClientListener != null) {
                            Log.e(TAG, "start pk failed : " + json.optString(Constants.KEY_ERROR));
                            mOnSignalClientListener.onError(errorCode, json.optString(Constants.KEY_ERROR));
                        }
                    } else if (method == QNSignalMethod.ON_PK_OFFER) {
                        PkRequestInfo requestInfo = new Gson().fromJson(msgValue, PkRequestInfo.class);
                        if (mOnSignalClientListener != null) {
                            mOnSignalClientListener.onPkRequestLaunched(requestInfo);
                        }
                    } else if (method == QNSignalMethod.ANSWER_PK_RES) {
                        if (mOnSignalClientListener == null) {
                            return true;
                        }
                        if (errorCode == QNSignalErrorCode.SUCCESS) {
                            String relayRtcRoom = json.optString(Constants.KEY_RELAY_ROOM);
                            String relayRoomToken = json.optString(Constants.KEY_RELAY_ROOM_TOKEN);
                            mOnSignalClientListener.onReplyPkSuccess(relayRtcRoom, relayRoomToken);
                        } else {
                            Log.e(TAG, "reply pk failed : " + json.optString(Constants.KEY_ERROR));
                            mOnSignalClientListener.onReplyPkFailed(errorCode, json.optString(Constants.KEY_ERROR));
                        }
                    } else if (method == QNSignalMethod.ON_PK_ANSWER) {
                        boolean isAccepted = json.optBoolean("accepted");
                        String relayRoom = json.optString(Constants.KEY_RELAY_ROOM);
                        String relayRoomToken = json.optString(Constants.KEY_RELAY_ROOM_TOKEN);
                        if (mOnSignalClientListener != null) {
                            mOnSignalClientListener.onPkRequestHandled(isAccepted, relayRoom, relayRoomToken);
                        }
                    } else if (method == QNSignalMethod.END_PK_RES) {
                        if (mOnSignalClientListener == null) {
                            return true;
                        }
                        if (errorCode == QNSignalErrorCode.SUCCESS) {
                            mOnSignalClientListener.onPkEnd();
                        } else {
                            Log.e(TAG, "end pk failed : " + json.optString(Constants.KEY_ERROR));
                            mOnSignalClientListener.onError(errorCode, json.optString(Constants.KEY_ERROR));
                        }
                    } else if (method == QNSignalMethod.ON_PK_END) {
                        if (mOnSignalClientListener != null) {
                            mOnSignalClientListener.onRemoteEndPk();
                        }
                    } else if (method == QNSignalMethod.ON_PK_TIMEOUT) {
                        if (mOnSignalClientListener != null) {
                            mOnSignalClientListener.onPkRequestTimeout();
                        }
                    } else if (method == QNSignalMethod.START_JOIN_RES) {
                        if (errorCode != QNSignalErrorCode.SUCCESS && mOnSignalClientListener != null) {
                            mOnSignalClientListener.onError(errorCode, json.optString(Constants.KEY_ERROR));
                        }
                    } else if (method == QNSignalMethod.ON_JOIN_REQUEST) {
                        if (mOnSignalClientListener != null) {
                            UserInfo userInfo = new Gson().fromJson(msgValue, UserInfo.class);
                            AudioParticipant audioParticipant = new Gson().fromJson(msgValue, AudioParticipant.class);
                            audioParticipant.setUserInfo(userInfo);
                            mOnSignalClientListener.onJoinRequestLaunched(audioParticipant);
                        }
                    } else if (method == QNSignalMethod.ON_JOIN_ANSWER) {
                        if (mOnSignalClientListener != null) {
                            boolean isAccepted = json.optBoolean(Constants.KEY_ACCEPT);
                            String roomId = json.optString(Constants.KEY_ROOM_ID);
                            String reqUserId = json.optString(Constants.KEY_REQUEST_USER_ID);
                            int position = json.optInt(Constants.KEY_POSITION);
                            mOnSignalClientListener.onJoinRequestHandled(reqUserId, roomId, isAccepted, position);
                        }
                    } else if (method == QNSignalMethod.ON_AUDIENCE_JOIN) {
                        if (mOnSignalClientListener != null) {
                            UserInfo userInfo = new Gson().fromJson(msgValue, UserInfo.class);
                            AudioParticipant audioParticipant = new Gson().fromJson(msgValue, AudioParticipant.class);
                            audioParticipant.setUserInfo(userInfo);
                            mOnSignalClientListener.onAudienceJoin(audioParticipant);
                        }
                    } else if (method == QNSignalMethod.END_JOIN_RES) {
                        if (mOnSignalClientListener == null) {
                            return true;
                        }
                        if (errorCode != QNSignalErrorCode.SUCCESS) {
                            Log.e(TAG, "end join failed : " + json.optString(Constants.KEY_ERROR));
                            mOnSignalClientListener.onError(errorCode, json.optString(Constants.KEY_ERROR));
                        }
                    } else if (method == QNSignalMethod.ON_JOIN_END) {
                        if (mOnSignalClientListener != null) {
                            UserInfo userInfo = new Gson().fromJson(msgValue, UserInfo.class);
                            AudioParticipant audioParticipant = new Gson().fromJson(msgValue, AudioParticipant.class);
                            audioParticipant.setUserInfo(userInfo);
                            mOnSignalClientListener.onAudienceLeft(audioParticipant);
                        }
                    } else if (method == QNSignalMethod.ON_JOIN_TIMEOUT) {
                        if (mOnSignalClientListener != null) {
                            String reqUserId = json.optString(Constants.KEY_REQUEST_USER_ID);
                            mOnSignalClientListener.onJoinRequestTimeout(reqUserId);
                        }
                    } else if (method == QNSignalMethod.ON_ROOM_CLOSE) {
                        if (mOnSignalClientListener != null) {
                            mOnSignalClientListener.onRoomClosed();
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "processMessage error: " + e.getMessage());
                }
                break;
        }
        return true;
    }

    private String generateMsg(String type, JSONObject message) {
        JsonUtils.jsonPut(message, Constants.KEY_RPC_ID, "android-" + Utils.randomNumberGenerator());
        String msg = type + "=" + message.toString();
        Log.i(TAG, "msg = " + msg);
        return msg;
    }
}
