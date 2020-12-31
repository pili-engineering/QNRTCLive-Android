package com.qiniu.droid.rtc.live.demo.utils;

import android.util.Log;

import com.qiniu.droid.rtc.live.demo.common.ErrorCode;
import com.qiniu.droid.rtc.live.demo.common.MessageEvent;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.greenrobot.event.EventBus;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.qiniu.droid.rtc.live.demo.common.ErrorCode.INTERNAL_ERROR;
import static com.qiniu.droid.rtc.live.demo.common.ErrorCode.NETWORK_UNREACHABLE;
import static com.qiniu.droid.rtc.live.demo.common.ErrorCode.REQUEST_TIMEOUT;

public class QNAppServer {
    private static final String TAG = "QNAppServer";

    public static final String LIVE_SERVER_ADDR = "https://qlive-api.qnsdk.com";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private String mAuthorization;
    private String mAuthToken;

    public interface OnRequestResultCallback {
        void onRequestSuccess(String responseMsg);
        void onRequestFailed(int code, String reason);
    }

    private static class QNAppServerHolder {
        private static final QNAppServer instance = new QNAppServer();
    }

    private QNAppServer(){}

    public static QNAppServer getInstance() {
        return QNAppServerHolder.instance;
    }

    public void setToken(String token) {
        mAuthToken = token;
        mAuthorization = "Bearer " + token;
    }

    public void sendSmsCode(String phoneNumber, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/send_sms_code?phone_number=" + phoneNumber;
        doPostRequest(url, null, null, callback);
    }

    public void login(String phoneNumber, String smsCode, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/login?logintype=smscode";
        String requestBody = "{\"phoneNumber\":\"" + phoneNumber + "\",\"smsCode\":\"" + smsCode + "\"}";
        doPostRequest(url, requestBody, null, callback);
    }

    public void logout(OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/logout";
        doPostRequest(url, null, mAuthorization, callback);
    }

    public String getAuthorization() {
        return mAuthorization;
    }

    public void updateProfile(UserInfo userInfo, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/profile";
        String requestBody = "{\"id\":\"" + userInfo.getUserId()
                + "\",\"nickname\":\"" + userInfo.getNickName()
                + "\",\"gender\":\"" + userInfo.getGender() + "\"}";
        doPostRequest(url, requestBody, mAuthorization, callback);
    }

    public void getLivingRooms(OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/rooms";
        doGetRequest(url, mAuthorization, callback);
    }

    public void getChatToken(OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/im_user_token";
        doPostRequest(url, "", mAuthorization, callback);
    }


    public void enterRoom(String userId, String roomId, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/enter_room";
        String requestBody = "{\"userID\":\"" + userId + "\",\"roomID\":\"" + roomId + "\"}";
        doPostRequest(url, requestBody, mAuthorization, callback);
    }

    public void leaveRoom(String userId, String roomId, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/leave_room";
        String requestBody = "{\"userID\":\"" + userId + "\",\"roomID\":\"" + roomId + "\"}";
        doPostRequest(url, requestBody, mAuthorization, callback); }

    public void createRoom(String userId, String roomName, String roomType, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/rooms";
        String requestBody = "{\"userID\":\"" + userId + "\",\"roomName\":\"" + roomName + "\",\"roomType\":\"" + roomType + "\"}";
        doPostRequest(url, requestBody, mAuthorization, callback);
    }

    public void updateRoomName(String roomId, String roomName, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/rooms/" + roomId;
        String requestBody = "{\"roomName\":\"" + roomName + "\"}";
        doPutRequest(url, requestBody, mAuthorization, callback);
    }

    public void getRoomInfo(String roomId, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/rooms/" + roomId;
        doGetRequest(url, mAuthorization, callback);
    }

    public void refreshRoom(String roomId, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/refresh_room";
        String requestBody = "{\"roomID\":\"" + roomId + "\"}";
        doPostRequest(url, requestBody, mAuthorization, callback);
    }

    public void getRoomInfoByCreator(String userId, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/rooms?creator=" + userId;
        doGetRequest(url, mAuthorization, callback);
    }

    public void getLiveRoomsCanPk(OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/rooms?can_pk=true";
        doGetRequest(url, mAuthorization, callback);
    }

    public void closeRoom(String userId, String roomId, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/close_room";
        String requestBody = "{\"userID\":\"" + userId + "\",\"roomID\":\"" + roomId + "\"}";
        doPostRequest(url, requestBody, mAuthorization, callback);
    }

    public String getSignalAuthToken() {
        return mAuthToken;
    }

    public void getUploadToken(String key, int expireTimeInSecond, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/upload/token";
        String requestBody = "{\"filename\":\"" + key + "\",\"expireSeconds\":" + expireTimeInSecond + "}";
        doPostRequest(url, requestBody, mAuthorization, callback);
    }

    public void sendFeedbacks(String content, String attachment, OnRequestResultCallback callback) {
        String url = LIVE_SERVER_ADDR + "/v1/feedbacks";
        String requestBody = "{\"content\":\"" + content + "\",\"attachment\":\"" + attachment + "\"}";
        doPostRequest(url, requestBody, mAuthorization, callback);
    }

    private static X509TrustManager getTrustManager() {
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore)null);
            for (TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    return (X509TrustManager) tm;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // This shall not happen
        return null;
    }

    private void doPostRequest(String url, String requestBody, String authorization, OnRequestResultCallback callback) {
        if (!NetworkUtils.isConnected()) {
            if (callback != null) {
                callback.onRequestFailed(NETWORK_UNREACHABLE, "network is unreachable");
            }
            return;
        }
        try {
            Log.i(TAG, "doPostRequest url = " + url);
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody body = RequestBody.create(requestBody == null ? "" : requestBody, JSON);
            Request request = new Request.Builder()
                    .header("Authorization", authorization == null ? "" : authorization)
                    .url(url)
                    .post(body)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.body() != null) {
                String responseMsg = response.body().string();
                Log.i(TAG, "doPostRequest responseMsg = " + responseMsg);
                if (response.isSuccessful()) {
                    if (authorization == null && !"\"\"".equals(responseMsg)) {
                        JSONObject responseJson = new JSONObject(responseMsg);
                        if (responseJson.has("token")) {
                            mAuthToken = responseJson.optString("token");
                            mAuthorization = "Bearer " + mAuthToken;
                            Log.i(TAG, "token = " + mAuthorization);
                        }
                    }
                    if (callback != null) {
                        callback.onRequestSuccess(responseMsg);
                    }
                } else {
                    if (response.body() != null && callback != null) {
                        JSONObject responseJson = new JSONObject(responseMsg);
                        int code = responseJson.optInt("code");
                        String errorMsg = responseJson.optString("summary");
                        if (code == ErrorCode.BAD_TOKEN) {
                            EventBus.getDefault().post(new MessageEvent("bad token"));
                        }
                        callback.onRequestFailed(code, errorMsg);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException && callback != null) {
                callback.onRequestFailed(REQUEST_TIMEOUT, "request timeout");
            } else if (callback != null) {
                callback.onRequestFailed(INTERNAL_ERROR, "internal error");
            }
        }
    }

    private void doGetRequest(String url, String authorization, OnRequestResultCallback callback) {
        if (!NetworkUtils.isConnected()) {
            if (callback != null) {
                callback.onRequestFailed(NETWORK_UNREACHABLE, "network is unreachable");
            }
            return;
        }
        try {
            Log.i(TAG, "doGetRequest url = " + url);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(2, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .header("Authorization", authorization == null ? "" : authorization)
                    .url(url)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.body() != null) {
                String responseMsg = response.body().string();
                Log.i(TAG, "doGetRequest responseMsg = " + responseMsg);
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.onRequestSuccess(responseMsg);
                    }
                } else {
                    if (response.body() != null && callback != null) {
                        JSONObject responseJson = new JSONObject(responseMsg);
                        int code = responseJson.optInt("code");
                        String errorMsg = responseJson.optString("summary");
                        if (code == ErrorCode.BAD_TOKEN) {
                            EventBus.getDefault().post(new MessageEvent("bad token"));
                        }
                        callback.onRequestFailed(code, errorMsg);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException && callback != null) {
                callback.onRequestFailed(REQUEST_TIMEOUT, "request timeout");
            } else if (callback != null) {
                callback.onRequestFailed(INTERNAL_ERROR, "internal error");
            }
        }
    }

    public void doPutRequest(String url, String requestBody, String authorization, OnRequestResultCallback callback){
        if (!NetworkUtils.isConnected()) {
            if (callback != null) {
                callback.onRequestFailed(NETWORK_UNREACHABLE, "network is unreachable");
            }
            return;
        }
        try {
            Log.i(TAG, "doPutRequest url = " + url);
            OkHttpClient okHttpClient = new OkHttpClient();
            RequestBody body = RequestBody.create(requestBody == null ? "" : requestBody, JSON);
            Request request = new Request.Builder()
                    .header("Authorization", authorization == null ? "" : authorization)
                    .url(url)
                    .put(body)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.body() != null) {
                String responseMsg = response.body().string();
                Log.i(TAG, "doPutRequest : " + responseMsg);
                if (response.isSuccessful()) {
                    if (callback != null) {
                        callback.onRequestSuccess(responseMsg);
                    }
                } else {
                    if (response.body() != null && callback != null) {
                        JSONObject responseJson = new JSONObject(responseMsg);
                        int code = responseJson.optInt("code");
                        String errorMsg = responseJson.optString("summary");
                        if (code == ErrorCode.BAD_TOKEN) {
                            EventBus.getDefault().post(new MessageEvent("bad token"));
                        }
                        callback.onRequestFailed(code, errorMsg);
                    }
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (e instanceof SocketTimeoutException && callback != null) {
                callback.onRequestFailed(REQUEST_TIMEOUT, "request timeout");
            } else if (callback != null) {
                callback.onRequestFailed(INTERNAL_ERROR, "internal error");
            }
        }
    }
}
