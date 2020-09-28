package com.qiniu.droid.rtc.live.demo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.orzangleli.xdanmuku.DanmuContainerView;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.widget.PLVideoView;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.RTCLiveApplication;
import com.qiniu.droid.rtc.live.demo.base.BaseActivity;
import com.qiniu.droid.rtc.live.demo.common.ErrorCode;
import com.qiniu.droid.rtc.live.demo.im.ChatroomKit;
import com.qiniu.droid.rtc.live.demo.im.DataInterface;
import com.qiniu.droid.rtc.live.demo.im.adapter.ChatListAdapter;
import com.qiniu.droid.rtc.live.demo.im.danmu.DanmuAdapter;
import com.qiniu.droid.rtc.live.demo.im.danmu.DanmuEntity;
import com.qiniu.droid.rtc.live.demo.im.gift.GiftSendModel;
import com.qiniu.droid.rtc.live.demo.im.gift.GiftView;
import com.qiniu.droid.rtc.live.demo.im.like.HeartLayout;
import com.qiniu.droid.rtc.live.demo.im.message.ChatroomBarrage;
import com.qiniu.droid.rtc.live.demo.im.message.ChatroomGift;
import com.qiniu.droid.rtc.live.demo.im.message.ChatroomLike;
import com.qiniu.droid.rtc.live.demo.im.message.ChatroomSignal;
import com.qiniu.droid.rtc.live.demo.im.message.ChatroomUserQuit;
import com.qiniu.droid.rtc.live.demo.im.message.ChatroomWelcome;
import com.qiniu.droid.rtc.live.demo.im.model.ChatRoomInfo;
import com.qiniu.droid.rtc.live.demo.im.model.NeedLoginEvent;
import com.qiniu.droid.rtc.live.demo.im.panel.BottomPanelFragment;
import com.qiniu.droid.rtc.live.demo.im.panel.InputPanel;
import com.qiniu.droid.rtc.live.demo.model.RoomInfo;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;
import com.qiniu.droid.rtc.live.demo.utils.AppUtils;
import com.qiniu.droid.rtc.live.demo.utils.BarUtils;
import com.qiniu.droid.rtc.live.demo.utils.Config;
import com.qiniu.droid.rtc.live.demo.utils.Constants;
import com.qiniu.droid.rtc.live.demo.utils.NetworkUtils;
import com.qiniu.droid.rtc.live.demo.utils.QNAppServer;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;
import com.qiniu.droid.rtc.live.demo.view.LoadingDialog;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

import static com.qiniu.droid.rtc.live.demo.im.DataInterface.DEFAULT_AVATAR;
import static com.qiniu.droid.rtc.live.demo.im.DataInterface.getUri;
import static com.qiniu.droid.rtc.live.demo.im.message.ChatroomSignal.SIGNAL_STREAMER_BACK_TO_LIVING;
import static com.qiniu.droid.rtc.live.demo.im.message.ChatroomSignal.SIGNAL_STREAMER_SWITCH_TO_BACKSTAGE;

public class PlayingActivity extends BaseActivity implements Handler.Callback {
    private static final String TAG = "PlayingActivity";

    private TextView mTvRoomName;
    private TextView mTvRoomAudience;
    private ImageView mIvClose;
    private View mAnchorLeftView;

    private PLVideoView mVideoView;
    private String mPlayUrl;

    private LoadingDialog mLoadingDialog;

    private UserInfo mUserInfo;
    private RoomInfo mRoomInfo;

    private ScheduledExecutorService mExecutor;
    private ScheduledExecutorService mReconnectExecutor;

    private boolean mStreamerIsBackground;

    private final Runnable mAudienceNumGetter = new Runnable() {
        @Override
        public void run() {
            if (mRoomInfo != null && mRoomInfo.getId() != null && NetworkUtils.isConnected()) {
                QNAppServer.getInstance().getRoomInfo(mRoomInfo.getId(), new QNAppServer.OnRequestResultCallback() {
                    @Override
                    public void onRequestSuccess(String responseMsg) {
                        Log.i(TAG, "get audience num success : " + responseMsg);
                        RoomInfo roomInfo = new Gson().fromJson(responseMsg, RoomInfo.class);
                        runOnUiThread(() -> {
                            if (mTvRoomAudience != null && roomInfo != null) {
                                mTvRoomAudience.setText(String.valueOf(roomInfo.getAudienceNumber()));
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed(int code, String reason) {
                        Log.i(TAG, "get audience num failed : code = " + code + " reason = " + reason);
                        if (code == ErrorCode.NO_SUCH_ROOM) {
                            ToastUtils.showLongToast("本次直播结束，感谢观看！");
                            leaveRoom();
                        }
                    }
                });
            }
        }
    };

    private final Runnable mReconnectRunnable = new Runnable() {
        @Override
        public void run() {
            runOnUiThread(() -> {
                if (mVideoView != null && mPlayUrl != null && NetworkUtils.isConnected()) {
                    mVideoView.stopPlayback();
                    initPlayer(mPlayUrl);
                    mVideoView.start();
                    if (mReconnectExecutor != null && !mReconnectExecutor.isShutdown()) {
                        mReconnectExecutor.shutdown();
                    }
                }
            });

        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_playing;
    }

    @Override
    protected void initView() {
        initStatusBar();
        mAnchorLeftView = findViewById(R.id.view_playing_anchor_left);
        mTvRoomName = findViewById(R.id.tv_playing_room_name);
        mTvRoomAudience = findViewById(R.id.tv_playing_room_audience);
        mIvClose = findViewById(R.id.iv_playing_close);
        mVideoView = findViewById(R.id.playing_player_view);

        mLoadingDialog = new LoadingDialog.Builder(this)
                .setCancelable(true)
                .create();
        mLoadingDialog.show();

        // 初始化 IM 控件
        initChatView();
    }

    private void initStatusBar() {
        BarUtils.transparentStatusBar(this, false);
        RelativeLayout bar = findViewById(R.id.rl_playing_bar);
        RelativeLayout.LayoutParams newLayoutParams = new RelativeLayout.LayoutParams(bar.getLayoutParams());
        newLayoutParams.setMargins(0, BarUtils.getStatusBarHeight(), 0, 0);
        bar.setLayoutParams(newLayoutParams);
    }

    @Override
    protected void initEvent() {
        mIvClose.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        mUserInfo = SharedPreferencesUtils.getUserInfo(AppUtils.getApp());
        mRoomInfo = intent.getParcelableExtra(Constants.INTENT_ROOM_INFO);
        if (mUserInfo == null || mRoomInfo == null) {
            finish();
        }
        if (mRoomInfo.getPlayUrl().isEmpty()) {
            ToastUtils.showShortToast("获取播放地址失败");
            return;
        }

        mTvRoomName.setText(mRoomInfo.getName());
        mTvRoomAudience.setText(String.valueOf(mRoomInfo.getAudienceNumber()));

        new Thread(() -> QNAppServer.getInstance().enterRoom(mUserInfo.getUserId(), mRoomInfo.getId(), new QNAppServer.OnRequestResultCallback() {
            @Override
            public void onRequestSuccess(String responseMsg) {
                runOnUiThread(() -> {
                    connectIM();
                    Log.i(TAG, "response : " + responseMsg);

                    mPlayUrl = mRoomInfo.getPlayUrl();
                    initPlayer(mPlayUrl);
                    mVideoView.start();
                });
            }

            @Override
            public void onRequestFailed(int code, String reason) {
                runOnUiThread(() -> {
                    Log.e(TAG, "code = " + code + " reason = " + reason);
                    ToastUtils.showShortToast("enter room failed : " + reason);
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    finish();
                });
            }
        })).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_playing_close:
                // 退出
                finish();
                leaveRoom();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null && mPlayUrl != null) {
            mVideoView.start();
            if (mLoadingDialog != null) {
                mLoadingDialog.show();
            }
        }
        if (mExecutor == null || mExecutor.isShutdown()) {
            mExecutor = Executors.newSingleThreadScheduledExecutor();
            mExecutor.scheduleAtFixedRate(mAudienceNumGetter, 0, Config.GET_AUDIENCE_NUM_PERIOD, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReconnectExecutor != null) {
            mReconnectExecutor.shutdown();
            mReconnectExecutor = null;
        }
        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }
        new Thread(() -> QNAppServer.getInstance().leaveRoom(mUserInfo.getUserId(), mRoomInfo.getId(),
                new QNAppServer.OnRequestResultCallback() {
            @Override
            public void onRequestSuccess(String responseMsg) {

            }

            @Override
            public void onRequestFailed(int code, String reason) {

            }
        })).start();
        ChatroomKit.quitChatRoom(new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "quitChatRoom success");
                ChatroomKit.removeEventHandler(handler);
                if (DataInterface.isLogin()) {
                    ChatroomUserQuit userQuit = new ChatroomUserQuit();
                    userQuit.setId(ChatroomKit.getCurrentUser().getUserId());
                    ChatroomKit.sendMessage(userQuit);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                ChatroomKit.removeEventHandler(handler);
                Log.i(TAG, "quitChatRoom failed errorCode = " + errorCode);
            }
        });
    }

    private void initPlayer(String playUrl) {
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, AVOptions.MEDIA_CODEC_SW_DECODE);
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        mVideoView.setAVOptions(options);

        // Set some listeners
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnErrorListener(mOnErrorListener);

        mVideoView.setDisplayAspectRatio(PLVideoView.ASPECT_RATIO_PAVED_PARENT);
        mVideoView.setVideoPath(playUrl);
    }

    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    if (!mStreamerIsBackground && mLoadingDialog != null) {
                        mLoadingDialog.show();
                    }
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.i(TAG, "Response: " + mVideoView.getResponseInfo());
                    if (mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                    if (mReconnectExecutor != null && !mReconnectExecutor.isShutdown()) {
                        mReconnectExecutor.shutdown();
                        mReconnectExecutor = null;
                    }
                    if (!mChatBottomPanel.isPanelVisible()) {
                        mIvClose.setVisibility(View.VISIBLE);
                    }
                    setChatViewVisible(View.VISIBLE);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    Log.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    Log.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    Log.i(TAG, mVideoView.getMetadata().toString());
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    Log.i(TAG, "Connected !");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    Log.i(TAG, "Rotation changed: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_LOOP_DONE:
                    Log.i(TAG, "Loop done");
                    break;
                case PLOnInfoListener.MEDIA_INFO_CACHE_DOWN:
                    Log.i(TAG, "Cache done");
                    break;
                default:
                    break;
            }
        }
    };

    private PLOnErrorListener mOnErrorListener = errorCode -> {
        Log.e(TAG, "Error happened, errorCode = " + errorCode);
        switch (errorCode) {
            case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                if (mReconnectExecutor == null || mReconnectExecutor.isShutdown()) {
                    mReconnectExecutor = Executors.newSingleThreadScheduledExecutor();
                    mReconnectExecutor.scheduleAtFixedRate(mReconnectRunnable, 2, Config.PLAYER_RECONNECT_PERIOD, TimeUnit.SECONDS);
                }
                return false;
            case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                ToastUtils.showShortToast(getResources().getString(R.string.toast_player_open_failed));
                break;
            default:
                ToastUtils.showShortToast("位置错误!");
                break;
        }
        if (mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        return true;
    };

    private void leaveRoom() {
        new Thread(() -> QNAppServer.getInstance().leaveRoom(mUserInfo.getUserId(), mRoomInfo.getId(),
                new QNAppServer.OnRequestResultCallback() {
                    @Override
                    public void onRequestSuccess(String responseMsg) {
                        finish();
                    }

                    @Override
                    public void onRequestFailed(int code, String reason) {
                    }
                })).start();
    }

    /**
     * ============================================== IM 相关 ==============================================
     **/

    private ListView mChatListView;
    private ChatListAdapter mChatListAdapter;
    private ChatRoomInfo mChatRoomInfo;
    protected BottomPanelFragment mChatBottomPanel;
    private Handler handler = new Handler(this);
    private HeartLayout mHeartLayout;
    private Random mRandom = new Random();
    private ImageView mBtnHeart;
    int mClickCount = 0;
    long mCurrentTime = 0;
    private DanmuContainerView mDanmuContainerView;
    private GiftView mGiftView;

    private void initChatView() {
        mChatListView = (ListView) findViewById(R.id.chat_list_view);
        mChatListAdapter = new ChatListAdapter(this);
        mChatListView.setAdapter(mChatListAdapter);
        mChatBottomPanel = (BottomPanelFragment) getSupportFragmentManager().findFragmentById(R.id.bottom_bar);

        mHeartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        mBtnHeart = (ImageView) mChatBottomPanel.getView().findViewById(R.id.btn_heart);

        mDanmuContainerView = (DanmuContainerView) findViewById(R.id.danmuContainerView);
        mDanmuContainerView.setAdapter(new DanmuAdapter(this));

        mGiftView = (GiftView) findViewById(R.id.giftView);
        mGiftView.setViewCount(2);
        mGiftView.init();

        setChatViewVisible(View.INVISIBLE);

        mBtnHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataInterface.isLogin()) {
                    mHeartLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            int rgb = Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
                            mHeartLayout.addHeart(rgb);
                        }
                    });
                    mClickCount++;
                    mCurrentTime = System.currentTimeMillis();
                    checkAfter(mCurrentTime);
                } else {
                    EventBus.getDefault().post(new NeedLoginEvent(true));
                }
            }
        });

        mChatBottomPanel.setInputPanelListener((text, type) -> {
            if (type == InputPanel.TYPE_TEXTMESSAGE) {
                final TextMessage content = TextMessage.obtain(text);
                ChatroomKit.sendMessage(content);
            } else if (type == InputPanel.TYPE_BARRAGE) {
                ChatroomBarrage barrage = new ChatroomBarrage();
                barrage.setContent(text);
                ChatroomKit.sendMessage(barrage);
            }
            mIvClose.setVisibility(View.VISIBLE);
        });

        mChatBottomPanel.setGiftPanelListener(new BottomPanelFragment.GiftPanelListener() {
            @Override
            public void onPanelOpen() {
                mIvClose.setVisibility(View.GONE);
            }

            @Override
            public void onPanelClose() {
                mIvClose.setVisibility(View.VISIBLE);
            }
        });

        // 添加软键盘弹出监听，记录软键盘高度
        addOnSoftKeyBoardVisibleListener(findViewById(R.id.playing_layout), (visible, softInputHeight) -> {
            if (visible) {
                mChatBottomPanel.setSoftInputHeight(softInputHeight);
                mChatBottomPanel.isShowInputAboveKeyboard(true);
                mIvClose.setVisibility(View.GONE);
            } else {
                mChatBottomPanel.isShowInputAboveKeyboard(false);
            }
        });
    }

    //500毫秒后做检查，如果没有继续点击了，发消息
    public void checkAfter(final long lastTime) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (lastTime == mCurrentTime) {
                    ChatroomLike likeMessage = new ChatroomLike();
                    likeMessage.setCounts(mClickCount);
                    ChatroomKit.sendMessage(likeMessage);

                    mClickCount = 0;
                }
            }
        }, 500);
    }

    private void setChatViewVisible(int visible) {
        mChatBottomPanel.setBtnsVisible(visible);
        mGiftView.setVisibility(visible);
        mChatListView.setVisibility(visible);
        mHeartLayout.setVisibility(visible);
        mDanmuContainerView.setVisibility(visible);
    }

    private void connectIM() {
        UserInfo userInfo = SharedPreferencesUtils.getUserInfo(this);
        String userName = userInfo.getNickName().isEmpty() ? "路人" : userInfo.getNickName();

        DataInterface.connectIM(new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                Log.e(TAG, "connect IM failed : token incorrect !");

                DataInterface.connectIM(this);
            }

            @Override
            public void onSuccess(String s) {
                Log.i(TAG, "connect IM Success : " + s);

                DataInterface.setLogin(userName);
                mChatRoomInfo = new ChatRoomInfo(mRoomInfo.getId(), mRoomInfo.getName(), null, mRoomInfo.getCreator().getUserId(), 0);

                initChatRoom();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Log.e(TAG, "connect IM error : " + errorCode);

                ToastUtils.showLongToast(getString(R.string.im_connect_error));
            }
        });
    }

    private void initChatRoom() {
        ChatroomKit.addEventHandler(handler);
        DataInterface.setBanStatus(false);
        joinChatRoom();
    }

    private void onStreamerSwitchToBackstage() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
        mStreamerIsBackground = true;
        Log.e(TAG, "onStreamerSwitchToBackstage");
        ToastUtils.showLongToast("主播暂时离开！！！");
        mAnchorLeftView.setVisibility(View.VISIBLE);
    }

    private void onStreamerBackToLiving() {
        mStreamerIsBackground = false;
        Log.e(TAG, "onStreamerBackToLiving");
        ToastUtils.showLongToast("主播已经回来！！！");
        mAnchorLeftView.setVisibility(View.GONE);
    }

    private void joinChatRoom() {
        ChatroomKit.joinChatRoom(mChatRoomInfo.getRoomId(), -1, new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "加入聊天室成功！");
                onJoinChatRoom();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                ToastUtils.showLongToast("聊天室加入失败! errorCode = " + errorCode);
            }
        });
    }

    protected void onJoinChatRoom() {
        if (ChatroomKit.getCurrentUser() == null) {
            return;
        }
        //发送欢迎信令
        ChatroomWelcome welcomeMessage = new ChatroomWelcome();
        welcomeMessage.setId(ChatroomKit.getCurrentUser().getUserId());
        ChatroomKit.sendMessage(welcomeMessage);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case ChatroomKit.MESSAGE_ARRIVED:
            case ChatroomKit.MESSAGE_SENT: {
                MessageContent messageContent = ((io.rong.imlib.model.Message) msg.obj).getContent();
                String sendUserId = ((io.rong.imlib.model.Message) msg.obj).getSenderUserId();
                if (messageContent instanceof ChatroomBarrage) {
                    ChatroomBarrage barrage = (ChatroomBarrage) messageContent;
                    DanmuEntity danmuEntity = new DanmuEntity();
                    danmuEntity.setContent(barrage.getContent());
                    String name = sendUserId;
                    Uri uri = getUri(PlayingActivity.this, DEFAULT_AVATAR);
                    if (messageContent != null) {
                        name = messageContent.getUserInfo().getName();
                        uri = DataInterface.getAvatarUri(messageContent.getUserInfo().getPortraitUri());
                    }
                    danmuEntity.setPortrait(uri);
                    danmuEntity.setName(name);
                    danmuEntity.setType(barrage.getType());
                    mDanmuContainerView.addDanmu(danmuEntity);
                } else if (messageContent instanceof ChatroomGift) {
                    ChatroomGift gift = (ChatroomGift) messageContent;
                    if (gift.getNumber() > 0) {
                        GiftSendModel model = new GiftSendModel(gift.getNumber());
                        model.setGiftRes(DataInterface.getGiftInfo(gift.getId()).getGiftRes());
                        String name = sendUserId;
                        Uri uri = getUri(RTCLiveApplication.getContext(), R.drawable.avatar_1);
                        if (messageContent != null) {
                            name = messageContent.getUserInfo().getName();
                            uri = DataInterface.getAvatarUri(messageContent.getUserInfo().getPortraitUri());
                        }
                        model.setSig("送出" + DataInterface.getGiftNameById(gift.getId()));
                        model.setNickname(name);
                        model.setUserAvatarRes(uri.toString());
                        mGiftView.addGift(model);
                    }
                } else if (messageContent instanceof ChatroomSignal) {
                    ChatroomSignal signalMessage = (ChatroomSignal) messageContent;
                    String signal = signalMessage.getSignal();
                    if (TextUtils.equals(signal, SIGNAL_STREAMER_SWITCH_TO_BACKSTAGE)) {
                        onStreamerSwitchToBackstage();
                    } else if (TextUtils.equals(signal, SIGNAL_STREAMER_BACK_TO_LIVING)) {
                        onStreamerBackToLiving();
                    }
                } else if (((io.rong.imlib.model.Message) msg.obj).getConversationType() == Conversation.ConversationType.CHATROOM) {
                    io.rong.imlib.model.Message msgObj = (io.rong.imlib.model.Message) msg.obj;
                    mChatListAdapter.addMessage(msgObj);

                    if (messageContent instanceof ChatroomUserQuit) {
                        String senderUserId = msgObj.getSenderUserId();
                        if (TextUtils.equals(senderUserId, mChatRoomInfo.getPubUserId())) {
                            ToastUtils.showLongToast("本次直播结束，感谢观看！");
                            leaveRoom();
                        }
                    } else if (messageContent instanceof ChatroomLike) {
                        //出点赞的心
                        for (int i = 0; i < ((ChatroomLike) messageContent).getCounts(); i++) {
                            mHeartLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    int rgb = Color.rgb(mRandom.nextInt(255), mRandom.nextInt(255), mRandom.nextInt(255));
                                    mHeartLayout.addHeart(rgb);
                                }
                            });
                        }
                    }
                }
                break;
            }
            case ChatroomKit.MESSAGE_SEND_ERROR: {
                Log.e(TAG, "handleMessage Error: " + msg.arg1 + ", " + msg.obj);
                break;
            }
            default:
        }
        mChatListAdapter.notifyDataSetChanged();
        return false;
    }

    public void addOnSoftKeyBoardVisibleListener(final View root,
                                                 final PlayingActivity.SoftInputStatusListener listener) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isVisibleForLast = false;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                root.getWindowVisibleDisplayFrame(rect);
                // 可见屏幕的高度
                int displayHeight = rect.bottom - rect.top;
                // 屏幕整体的高度
                int height = root.getHeight();
                // 键盘高度
                int keyboardHeight = height - displayHeight;
                boolean visible = (double) displayHeight / height < 0.8;
                if (visible != isVisibleForLast) {
                    listener.onSoftInputStatusChanged(visible, keyboardHeight);
                }
                isVisibleForLast = visible;
            }
        });
    }

    interface SoftInputStatusListener {
        void onSoftInputStatusChanged(boolean toVisible, int keyboardHeight);
    }

    @Override
    public void onBackPressed() {
        if (!mChatBottomPanel.onBackAction()) {
            super.onBackPressed();
        }
    }
}
