package com.qiniu.droid.rtc.live.demo.signal;

import com.qiniu.droid.rtc.live.demo.model.AudioParticipant;
import com.qiniu.droid.rtc.live.demo.model.PkRequestInfo;

public interface OnSignalClientListener {

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
     * PK 请求超时未处理
     */
    void onPkRequestTimeout();

    /**
     * 本地结束 PK 成功信令返回
     */
    void onPkEnd();

    /**
     * 远端结束 PK 信令通知
     */
    void onRemoteEndPk();

    /**
     * 远端观众请求语音连麦
     *
     * @param info 请求信息
     */
    void onJoinRequestLaunched(AudioParticipant info);

    /**
     * 房主处理了语音连麦请求
     *  @param reqUserId 发起请求 userId
     * @param roomId 请求的房间 id
     * @param isAccepted 房主是否同意连麦请求
     * @param position 上麦位置
     */
    void onJoinRequestHandled(String reqUserId, String roomId, boolean isAccepted, int position);

    /**
     * 远端观众加入了语音连麦
     *
     * @param participant 连麦观众信息
     */
    void onAudienceJoin(AudioParticipant participant);

    /**
     * 远端观众结束连麦
     *
     * @param participant 连麦观众信息
     */
    void onAudienceLeft(AudioParticipant participant);

    /**
     * 连麦请求超时
     *
     * @param reqUserId 超时请求发起者的 id
     */
    void onJoinRequestTimeout(String reqUserId);

    /**
     * 房间关闭
     */
    void onRoomClosed();

    /**
     * 操作发生错误
     *
     * @param errorCode 错误码
     * @param errorMessage 错误信息
     */
    void onError(int errorCode, String errorMessage);
}
