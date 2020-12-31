package com.qiniu.droid.rtc.live.demo.signal;

public interface ISignalClient {
    /**
     * 心跳 ping
     */
    void sendPing();

    /**
     * 心跳 pong
     */
    void sendPong();

    /**
     * 发起 PK 请求
     *
     * @param roomId 对方 PK roomId
     */
    void startPk(String roomId);

    /**
     * 结束 PK
     *
     * @param roomId PK 的 roomId
     */
    void endPk(String roomId);

    /**
     * 回复 PK 请求
     *
     * @param roomId 发起 PK 请求的 roomId
     * @param isAccepted 是否接受 PK
     */
    void answerPk(String roomId, boolean isAccepted);

    /**
     * 请求加入语音连麦
     *
     * @param roomId 房间 id
     * @param position 期望语音连麦 position
     */
    void startJoin(String roomId, int position);

    /**
     * 回复语音连麦请求
     *
     * @param roomId 房间 id
     * @param reqUserId 发起请求观众的 user id
     * @param isAccepted 是否同意连麦请求
     */
    void answerJoin(String roomId, String reqUserId, boolean isAccepted);

    /**
     * 结束语音连麦
     *
     * @param roomId 房间 id
     * @param reqUserId 发起请求观众的 user id
     */
    void endJoin(String roomId, String reqUserId);

    /**
     * 断开信令连接
     */
    void disconnect();
}
