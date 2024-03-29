package com.qiniu.droid.rtc.live.demo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.orzangleli.xdanmuku.DanmuContainerView;
import com.qiniu.droid.rtc.QNClientEventListener;
import com.qiniu.droid.rtc.QNConnectionDisconnectedInfo;
import com.qiniu.droid.rtc.QNConnectionState;
import com.qiniu.droid.rtc.QNCustomMessage;
import com.qiniu.droid.rtc.QNMediaRelayState;
import com.qiniu.droid.rtc.QNMicrophoneAudioTrack;
import com.qiniu.droid.rtc.QNPublishResultCallback;
import com.qiniu.droid.rtc.QNRTC;
import com.qiniu.droid.rtc.QNRTCClient;
import com.qiniu.droid.rtc.QNRTCEventListener;
import com.qiniu.droid.rtc.QNRemoteAudioTrack;
import com.qiniu.droid.rtc.QNRemoteTrack;
import com.qiniu.droid.rtc.QNRemoteVideoTrack;
import com.qiniu.droid.rtc.QNTrack;
import com.qiniu.droid.rtc.QNTrackInfoChangedListener;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.adapter.AudienceParticipantsAdapter;
import com.qiniu.droid.rtc.live.demo.fragment.AudioParticipantsFragment;
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
import com.qiniu.droid.rtc.live.demo.model.AudioParticipant;
import com.qiniu.droid.rtc.live.demo.model.PkRequestInfo;
import com.qiniu.droid.rtc.live.demo.model.RoomInfo;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;
import com.qiniu.droid.rtc.live.demo.signal.OnSignalClientListener;
import com.qiniu.droid.rtc.live.demo.signal.QNIMSignalClient;
import com.qiniu.droid.rtc.live.demo.utils.AppUtils;
import com.qiniu.droid.rtc.live.demo.utils.BarUtils;
import com.qiniu.droid.rtc.live.demo.utils.Constants;
import com.qiniu.droid.rtc.live.demo.utils.PermissionChecker;
import com.qiniu.droid.rtc.live.demo.utils.QNAppServer;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ThreadUtils;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;
import com.qiniu.droid.rtc.live.demo.utils.Utils;
import com.qiniu.droid.rtc.live.demo.utils.ViewClickUtils;
import com.qiniu.droid.rtc.live.demo.view.CircleImageView;
import com.qiniu.droid.rtc.live.demo.view.LoadingDialog;
import com.qiniu.droid.rtc.model.QNAudioDevice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.greenrobot.event.EventBus;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.POSITION_OCCUPIED;

/**
 * 语聊房实现方式
 *
 * 主要步骤如下：
 * 1. 初始化视图
 * 2. 初始化 RTC
 * 3. 创建 QNRTCClient 对象
 * 4. 创建本地麦克风音频采集 Track
 * 5. 加入房间
 * 6. 发布本地麦克风音频 Track
 * 7. 订阅远端音频 Track（sdk 默认自动订阅远端 Track）
 * 8. 离开房间
 * 9. 反初始化 RTC 释放资源
 */
public class AudioRoomActivity extends AppCompatActivity implements QNRTCEventListener, QNClientEventListener,
        AudienceParticipantsAdapter.OnItemClickListener, Handler.Callback {
    private static final String TAG = "AudioRoomActivity";

    private ConstraintLayout mAudioRoomLayout;
    // 主播相关
    private TextView mRoomNameText;
    private TextView mModifiedRoomNameText;
    private TextView mUserNumberText;
    private ImageView mModifyRoomNameBtn;
    private Button mStartCommunicationBtn;
    private CircleImageView mAnchorAvatarIv;
    private TextView mAnchorNameText;
    private ImageView mAnchorAudioStatusIv;
    private ImageButton mFinishBtn;

    private Group mTopBtns;
    private Group mBottomBtns;

    private ImageButton mIMBtn;
    private ImageButton mSpeakerMuteBtn;
    private ImageButton mMicrophoneMuteBtn;

    // 观众
    private ImageView mAudienceCloseBtn;

    private RecyclerView mAudioParticipantsView;
    private AudienceParticipantsAdapter mParticipantsAdapter;
    // 参与连麦的观众，不包括主播
    private List<AudioParticipant> mCommunicateAudiences;
    // 参与连麦的观众，包括主播
    private List<AudioParticipant> mAudioParticipants;

    private LoadingDialog mLoadingDialog;
    private AudioParticipantsFragment mAudioParticipantsFragment;

    private Toast mAudienceNumberToast;

    // 标记 RTC 的生命周期，确保 init/deinit 成对出现并执行
    static boolean mRTCInit = false;
    private QNRTCClient mClient;
    private QNMicrophoneAudioTrack mMicrophoneAudioTrack;

    private AudioParticipant mAnchorInfo;
    private AudioParticipant mSelfInfo;
    private RoomInfo mRoomInfo;
    private String mRoomToken;

    private QNConnectionState mCurrentConnectionState = QNConnectionState.DISCONNECTED;

    private Handler mMainHandler;
    private Handler mSubThreadHandler;

    private boolean mIsLocalAudioMute;
    private boolean mIsSpeakerMute;

    private boolean mIsAudioAnchor = false;
    private volatile boolean mIsCommunicateAudience = false;

    private ConcurrentHashMap<String, Dialog> mRequestDialogMap;

    private boolean mHasNavigationBar;

    // IM signal
    private QNIMSignalClient mSignalClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BarUtils.setStatusBarColor(this, R.color.audio_status_bar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_audio_room);

        if (mRTCInit) {
            showLifeCircleExceptionDialog();
        }

        mMainHandler = new Handler();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mSubThreadHandler = new Handler(handlerThread.getLooper());

        // 初始化相关数据
        initData();
        // 1. 初始化视图
        initViews();
        // 初始化连麦引擎
        initQNRTC();
        // 4. 创建本地麦克风音频采集 Track
        mMicrophoneAudioTrack = QNRTC.createMicrophoneAudioTrack();
        // 初始化 IM 控件
        initChatView();

        if (!mIsAudioAnchor) {
            mSubThreadHandler.post(() -> QNAppServer.getInstance().enterRoom(
                    mSelfInfo.getUserInfo().getUserId(), mRoomInfo.getId(), new QNAppServer.OnRequestResultCallback() {
                        @Override
                        public void onRequestSuccess(String responseMsg) {
                            joinRoomWithResponseInfo(responseMsg);
                        }

                        @Override
                        public void onRequestFailed(int code, String reason) {
                            Log.e(TAG, "code = " + code + " reason = " + reason);
                            ToastUtils.showShortToast("enter room failed : " + reason);
                            finish();
                        }
                    }));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRoomInfo != null) {
            ThreadUtils.getSingleThreadExecutor().execute(() -> QNAppServer.getInstance().leaveRoom(mSelfInfo.getUserInfo().getUserId(), mRoomInfo.getId(),
                    new QNAppServer.OnRequestResultCallback() {
                        @Override
                        public void onRequestSuccess(String responseMsg) {

                        }

                        @Override
                        public void onRequestFailed(int code, String reason) {

                        }
                    }));
        }

        if (mSignalClient != null) {
            mSignalClient.disconnect();
            mSignalClient.destroy();
            mSignalClient = null;
        }

        quitChatRoom();
        // 8. 离开房间
        if (mClient != null) {
            mClient.leave();
        }
        // 9. 反初始化 RTC 释放资源
        if (mRTCInit) {
            QNRTC.deinit();
            mRTCInit = false;
        }
        mClient = null;
    }

    public void onClickStartCommunication(View v) {
        if (ViewClickUtils.isFastDoubleClick()) {
            return;
        }
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog.Builder(this)
                    .setCancelable(true)
                    .setTipMessage("开启直播中...")
                    .create();
        }
        mLoadingDialog.show();
        startCommunication();
    }

    public void onClickMuteMicrophone(View v) {
        mIsLocalAudioMute = !mIsLocalAudioMute;
        mMicrophoneAudioTrack.setMuted(mIsLocalAudioMute);

        if (mIsCommunicateAudience) {
            mSelfInfo.setMute(mIsLocalAudioMute);
            updateAudienceMuteStatus(mSelfInfo.getPosition(), mIsLocalAudioMute);
        }
        if (mIsAudioAnchor) {
            mAnchorInfo.setMute(mIsLocalAudioMute);
            mAnchorAudioStatusIv.setImageResource(mIsLocalAudioMute ? R.drawable.ic_voice_off : R.drawable.ic_voice_on);
        }
        mMicrophoneMuteBtn.setImageResource(mIsLocalAudioMute ? R.drawable.ic_microphone_mute : R.drawable.ic_microphone_on);
        if (mAudioParticipantsFragment != null) {
            mAudioParticipantsFragment.notifyDataSetChanged();
        }
    }

    public void onClickMuteSpeaker(View v) {
        mIsSpeakerMute = !mIsSpeakerMute;
        QNRTC.setSpeakerphoneMuted(mIsSpeakerMute);
        mSpeakerMuteBtn.setImageResource(mIsSpeakerMute ? R.drawable.ic_speaker_mute : R.drawable.ic_speaker_on);
    }

    public void onClickModifyRoomName(View v) {
        if (ViewClickUtils.isFastDoubleClick()) {
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_modify_room_name, null);
        EditText editText = view.findViewById(R.id.et_edit_room_name);
        editText.setText(mModifiedRoomNameText.getText());
        new MaterialAlertDialogBuilder(this)
                .setTitle("修改房间名")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", (dialog, which) -> {
                    mMainHandler.post(() -> mModifiedRoomNameText.setText(editText.getText().toString()));
                })
                .create()
                .show();
    }

    public void onClickShowParticipantsList(View v) {
        showAudioParticipantsFragment();
    }

    public void onClickAudienceNum(View v) {
        ToastUtils.cancel();
        String message = "当前观众(包括上麦观众)人数：" + mUserNumberText.getText() + "人";
        if (mAudienceNumberToast == null) {
            mAudienceNumberToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
            TextView textView = new TextView(this);
            textView.setText(message);
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundResource(R.drawable.bg_transparent25_radius7);
            textView.setPadding(25, 10, 25, 10);
            mAudienceNumberToast.setView(textView);
            mAudienceNumberToast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            TextView textView = (TextView) mAudienceNumberToast.getView();
            textView.setText(message);
        }
        mAudienceNumberToast.show();
    }

    public void onClickFinish(View v) {
        finish();
    }

    private void initData() {
        UserInfo userInfo = SharedPreferencesUtils.getUserInfo(AppUtils.getApp());
        mRoomInfo = getIntent().getParcelableExtra(Constants.INTENT_ROOM_INFO);
        mIsAudioAnchor = getIntent().getBooleanExtra(Constants.KEY_IS_AUDIO_ANCHOR, false);
        if (userInfo == null || (!mIsAudioAnchor && mRoomInfo == null)) {
            finish();
        }

        mSelfInfo = new AudioParticipant();
        mSelfInfo.setUserInfo(userInfo);

        mAnchorInfo = new AudioParticipant();
        if (mIsAudioAnchor) {
            mAnchorInfo.setUserInfo(userInfo);
        } else {
            mAnchorInfo.setUserInfo(mRoomInfo.getCreator());
        }

        mAudioParticipants = new ArrayList<>();
        mAudioParticipants.add(mAnchorInfo);

        mCommunicateAudiences = new ArrayList<>(8);
        for (int i = 0; i < 8; i++) {
            mCommunicateAudiences.add(null);
        }

        mRequestDialogMap = new ConcurrentHashMap<>();
        mHasNavigationBar = BarUtils.checkDeviceHasNavigationBar(this);
    }

    private void initViews() {
        mAudioRoomLayout = findViewById(R.id.audio_communication_activity);
        mAnchorAvatarIv = findViewById(R.id.anchor_avatar_image);
        mAnchorAudioStatusIv = findViewById(R.id.anchor_audio_status);
        mAnchorNameText = findViewById(R.id.anchor_name_tv);
        mRoomNameText = findViewById(R.id.room_name_text);
        mUserNumberText = findViewById(R.id.audio_user_number_text);
        mIMBtn = findViewById(R.id.audio_chat_btn);
        mSpeakerMuteBtn = findViewById(R.id.speaker_operate_btn);
        mMicrophoneMuteBtn = findViewById(R.id.audio_operate_btn);
        mAudienceCloseBtn = findViewById(R.id.audience_close_btn);

        mBottomBtns = findViewById(R.id.bottom_operate_group);
        mTopBtns = findViewById(R.id.top_operate_group);

        mAudioParticipantsView = findViewById(R.id.audio_participants_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4, RecyclerView.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mAudioParticipantsView.setLayoutManager(layoutManager);
        mParticipantsAdapter = new AudienceParticipantsAdapter(this, mCommunicateAudiences);
        mParticipantsAdapter.setOnItemClickListener(this);
        mAudioParticipantsView.setAdapter(mParticipantsAdapter);

        mAudioRoomLayout.setOnClickListener(v -> {
            if (mChatBottomPanel != null && mChatBottomPanel.isPanelVisible()) {
                mChatBottomPanel.hidePanels();
            }
        });

        Glide.with(this)
                .load("".equals(mAnchorInfo.getUserInfo().getAvatar())
                        ? R.mipmap.default_avatar
                        : mAnchorInfo.getUserInfo().getAvatar())
                .centerInside()
                .into(mAnchorAvatarIv);
        mAnchorNameText.setText(mAnchorInfo.getUserInfo().getNickName());

        if (mIsAudioAnchor) {
            initViewsForAnchor();
        }
    }

    private void initViewsForAnchor() {
        mModifiedRoomNameText = findViewById(R.id.audio_room_name);
        mModifyRoomNameBtn = findViewById(R.id.modify_room_name_btn);
        mStartCommunicationBtn = findViewById(R.id.start_audio_communication_button);
        mFinishBtn = findViewById(R.id.finish_btn);
        mModifiedRoomNameText.setVisibility(View.VISIBLE);
        mModifyRoomNameBtn.setVisibility(View.VISIBLE);
        mStartCommunicationBtn.setVisibility(View.VISIBLE);
        mFinishBtn.setVisibility(View.VISIBLE);

        mModifiedRoomNameText.setText(String.format(getString(R.string.room_nick_name_edit_text), mAnchorInfo.getUserInfo().getNickName()));
    }

    private void initQNRTC() {
        // 2. 初始化 RTC
        QNRTC.init(getApplicationContext(), this);
        // 3. 创建 QNRTCClient 对象
        mClient = QNRTC.createClient(this);
        mRTCInit = true;
    }

    private void startCommunication() {
        ThreadUtils.getFixedThreadPool().execute(() ->
                QNAppServer.getInstance().getRoomInfoByCreator(mAnchorInfo.getUserInfo().getUserId(), new QNAppServer.OnRequestResultCallback() {
                    @Override
                    public void onRequestSuccess(String responseMsg) {
                        RoomInfo roomInfo = parseRoomInfo(responseMsg);
                        if (roomInfo == null) {
                            startCommunicationByCreateRoom();
                        } else {
                            QNAppServer.getInstance().closeRoom(mAnchorInfo.getUserInfo().getUserId(), roomInfo.getId(), new QNAppServer.OnRequestResultCallback() {
                                @Override
                                public void onRequestSuccess(String responseMsg) {
                                    startCommunicationByCreateRoom();
                                }

                                @Override
                                public void onRequestFailed(int code, String reason) {
                                    ToastUtils.showShortToast(getString(R.string.toast_close_room_failed) + " : " + reason);
                                }
                            });
                        }
                    }

                    @Override
                    public void onRequestFailed(int code, String reason) {
                        ToastUtils.showShortToast(getString(R.string.toast_get_live_rooms_failed) + " : " + reason);
                    }
                }));
    }

    private RoomInfo parseRoomInfo(String responseBody) {
        try {
            JSONObject jsonObject = new JSONObject(responseBody);
            String rooms = jsonObject.optString(Constants.KEY_ROOMS);
            if ("null".equals(rooms)) {
                return null;
            }
            JSONArray liveRoomArray = new JSONArray(rooms);
            return new Gson().fromJson(liveRoomArray.optString(0), RoomInfo.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startCommunicationByCreateRoom() {
        mSubThreadHandler.post(() -> QNAppServer.getInstance().createRoom(
                mAnchorInfo.getUserInfo().getUserId(), mModifiedRoomNameText.getText().toString().trim(),
                "voice", new QNAppServer.OnRequestResultCallback() {
                    @Override
                    public void onRequestSuccess(String responseMsg) {
                        joinRoomWithResponseInfo(responseMsg);
                    }

                    @Override
                    public void onRequestFailed(int code, String reason) {
                        mMainHandler.post(() -> {
                            ToastUtils.showShortToast(getString(R.string.toast_create_room_failed) + " : " + reason);
                            if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                                mLoadingDialog.dismiss();
                            }
                        });
                    }
                }));
    }

    private void joinRoomWithResponseInfo(String responseMsg) {
        try {
            JSONObject responseJson = new JSONObject(responseMsg);
            mRoomToken = responseJson.optString(Constants.KEY_ROOM_TOKEN);
            if (mRoomInfo == null) {
                mRoomInfo = new RoomInfo();
                mRoomInfo.setId(responseJson.optString(Constants.KEY_ROOM_ID));
                mRoomInfo.setName(responseJson.optString(Constants.KEY_ROOM_NAME));
            }

            String audienceInfo = responseJson.optString(Constants.KEY_JOINED_AUDIENCES);
            if (!"".equals(audienceInfo)) {
                parseAudienceInfo(audienceInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            ToastUtils.showShortToast("加入房间失败，请重新操作！");
            return;
        }
        // 加入 RTC 房间
        joinRoom(mRoomToken);

        connectIM();
        mMainHandler.post(() -> setChatViewVisible(View.VISIBLE));
    }

    private void joinRoom(String roomToken) {
        if (mClient != null) {
            // 5. 加入房间
            mClient.join(roomToken);
        }
    }

    private void showLifeCircleExceptionDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.toast_rtc_life_circle_exception)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.cancel();
                    finish();
                })
                .create()
                .show();
    }

    private void showRequestJoinDialog(int pos) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_handle_request, null);
        TextView content = view.findViewById(R.id.request_pk_info);
        Button acceptBtn = view.findViewById(R.id.accept_btn);
        Button refuseBtn = view.findViewById(R.id.refuse_btn);

        content.setText(R.string.confirm_communication_text);
        acceptBtn.setText(R.string.sure_to_communicate);
        refuseBtn.setText(R.string.not_sure_to_communicate);

        Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .setView(view)
                .create();

        acceptBtn.setOnClickListener(v1 -> {
            if (mSignalClient != null) {
                mSignalClient.startJoin(mRoomInfo.getId(), pos);
            }
            dialog.dismiss();
        });
        refuseBtn.setOnClickListener(arg0 -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showRequestLaunchedDialog(AudioParticipant participant) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_handle_request, null);
        TextView content = view.findViewById(R.id.request_pk_info);
        Button acceptBtn = view.findViewById(R.id.accept_btn);
        Button refuseBtn = view.findViewById(R.id.refuse_btn);

        content.setText(String.format(getString(R.string.request_communication_text), participant.getUserInfo().getNickName()));
        acceptBtn.setText(R.string.accept_communication);
        refuseBtn.setText(R.string.refuse_communication);

        Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .setView(view)
                .create();
        mRequestDialogMap.put(participant.getUserInfo().getUserId(), dialog);

        acceptBtn.setOnClickListener(v1 -> {
            if (mSignalClient != null) {
                mSignalClient.answerJoin(mRoomInfo.getId(), participant.getUserInfo().getUserId(), true);
            }
            dialog.dismiss();
        });
        refuseBtn.setOnClickListener(arg0 -> {
            if (mSignalClient != null) {
                mSignalClient.answerJoin(mRoomInfo.getId(), participant.getUserInfo().getUserId(), false);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showBeRefusedDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tips, null);
        TextView content = view.findViewById(R.id.dialog_content_text);
        Button sureBtn = view.findViewById(R.id.ok_btn);
        content.setText(getString(R.string.join_request_refused));
        sureBtn.setText(getString(R.string.confirm_be_refused));

        final Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.setContentView(view);

        sureBtn.setOnClickListener(v1 -> dialog.dismiss());
    }

    private void showJoinReqTimeoutDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tips, null);
        TextView content = view.findViewById(R.id.dialog_content_text);
        Button sureBtn = view.findViewById(R.id.ok_btn);
        content.setText(getString(R.string.join_request_timeout));
        sureBtn.setText(getString(R.string.confirm_text));

        final Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.setContentView(view);

        sureBtn.setOnClickListener(v1 -> dialog.dismiss());
    }

    private void showAudioParticipantsFragment() {
        if (mAudioParticipantsFragment == null) {
            mAudioParticipantsFragment = new AudioParticipantsFragment(mAudioParticipants, mIsCommunicateAudience);
            mAudioParticipantsFragment.setOnEndAudioClickListener(() -> {
                if (mSignalClient != null) {
                    mSignalClient.endJoin(mRoomInfo.getId(), mSelfInfo.getUserInfo().getUserId());
                }
                if (mClient != null) {
                    mClient.unpublish(mMicrophoneAudioTrack);
                }
                mIsCommunicateAudience = false;
                setBottomBtnsVisibility(View.VISIBLE);
                audioParticipantChanged(mSelfInfo, false);
                mIsLocalAudioMute = false;
                mSelfInfo.setMute(false);
                mMicrophoneMuteBtn.setImageResource(R.drawable.ic_microphone_on);
                mAudioParticipantsFragment.dismiss();
                mAudioParticipantsFragment.setEndBtnVisible(false);
            });
        }
        if (getSupportFragmentManager().findFragmentByTag(AudioParticipantsFragment.TAG) == null) {
            mAudioParticipantsFragment.show(getSupportFragmentManager(), AudioParticipantsFragment.TAG);
        }
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tips, null);
        TextView content = view.findViewById(R.id.dialog_content_text);
        Button sureBtn = view.findViewById(R.id.ok_btn);
        content.setText(errorMessage);
        sureBtn.setText(getString(R.string.confirm_text));

        final Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.setContentView(view);

        sureBtn.setOnClickListener(v1 -> finish());
    }

    private void parseAudienceInfo(String audienceInfo) {
        try {
            JSONArray audienceArray = new JSONArray(audienceInfo);
            for (int j = 0; j < audienceArray.length(); j++) {
                String joinedAudience = audienceArray.optString(j);
                UserInfo userInfo = new Gson().fromJson(joinedAudience, UserInfo.class);
                AudioParticipant participant = new Gson().fromJson(joinedAudience, AudioParticipant.class);
                participant.setRoomId(mRoomInfo.getId());
                participant.setUserInfo(userInfo);
                if (mSelfInfo.getUserInfo().getUserId().equals(participant.getUserInfo().getUserId())) {
                    Log.i(TAG, "self is communicator");
                    mSelfInfo.setMute(participant.isMute());
                    mSelfInfo.setPosition(participant.getPosition());
                    mIsLocalAudioMute = participant.isMute();
                    mIsCommunicateAudience = true;
                }
                mMainHandler.post(() -> audioParticipantChanged(
                        mSelfInfo.getUserInfo().getUserId().equals(participant.getUserInfo().getUserId()) ? mSelfInfo : participant, true));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String findNickNameById(String userId) {
        for (AudioParticipant participant : mAudioParticipants) {
            String participantId = participant.getUserInfo().getUserId();
            if (participantId.equals(userId)) {
                return participant.getUserInfo().getNickName();
            }
        }
        return null;
    }

    private void audioParticipantChanged(AudioParticipant participant, boolean isJoin) {
        mCommunicateAudiences.set(participant.getPosition(), isJoin ? participant : null);
        mMainHandler.post(() -> {
            mParticipantsAdapter.notifyItemChanged(participant.getPosition());
            if (isJoin) {
                mAudioParticipants.add(participant);
                if (mAudioParticipantsFragment != null) {
                    mAudioParticipantsFragment.notifyItemInserted(mAudioParticipants.size() - 1);
                }
            } else {
                int index = mAudioParticipants.indexOf(participant);
                mAudioParticipants.remove(participant);
                if (mAudioParticipantsFragment != null) {
                    mAudioParticipantsFragment.notifyItemRemoved(index);
                }
            }
        });
    }

    private void setBottomBtnsVisibility(int visibility) {
        if (visibility == View.GONE) {
            mBottomBtns.setVisibility(View.GONE);
            setChatBottomPanelVisibility(View.GONE);
        } else if (visibility == View.VISIBLE) {
            if (mIsAudioAnchor || mIsCommunicateAudience) {
                mBottomBtns.setVisibility(View.VISIBLE);
                setChatBottomPanelVisibility(View.GONE);
            } else {
                mBottomBtns.setVisibility(View.GONE);
                setChatBottomPanelVisibility(View.VISIBLE);
            }
        }
    }

    private void updateUIAfterConnected() {
        if (mIsAudioAnchor) {
            mModifiedRoomNameText.setVisibility(View.INVISIBLE);
            mModifyRoomNameBtn.setVisibility(View.INVISIBLE);
            mStartCommunicationBtn.setVisibility(View.GONE);
            mFinishBtn.setVisibility(View.GONE);
        }
        mRoomNameText.setText(mIsAudioAnchor ? mModifiedRoomNameText.getText() : mRoomInfo.getName());
        mTopBtns.setVisibility(View.VISIBLE);
        mUserNumberText.setText(String.valueOf(mClient.getRemoteUsers().size()));
        setBottomBtnsVisibility(View.VISIBLE);
    }

    private void updateAudienceMuteStatus(int pos, boolean isMute) {
        AudienceParticipantsAdapter.AudienceParticipantViewHolder viewHolder =
                ((AudienceParticipantsAdapter.AudienceParticipantViewHolder) mAudioParticipantsView.findViewHolderForAdapterPosition(pos));
        if (viewHolder != null) {
            viewHolder.setAudioStatus(isMute);
        }
    }

    private void userTrackInfoUpdated(String userId, List<QNTrack> trackList) {
        if (userId.equals(mAnchorInfo.getUserInfo().getUserId())) {
            for (QNTrack track : trackList) {
                if (track.isAudio()) {
                    mAnchorAudioStatusIv.setImageResource(track.isMuted() ? R.drawable.ic_voice_off : R.drawable.ic_voice_on);
                    mAnchorInfo.setMute(track.isMuted());
                }
            }
            return;
        }
        for (int i = 0; i < mCommunicateAudiences.size(); i++) {
            AudioParticipant participant = mCommunicateAudiences.get(i);
            if (participant == null) {
                continue;
            }
            if (userId.equals(participant.getUserInfo().getUserId())) {
                for (QNTrack track : trackList) {
                    if (track.isAudio()) {
                        participant.setMute(track.isMuted());
                        updateAudienceMuteStatus(participant.getPosition(), track.isMuted());
                    }
                }
            }
        }
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.showShortToast("Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    private OnSignalClientListener mOnSignalClientListener = new OnSignalClientListener() {
        @Override
        public void onPkRequestLaunched(PkRequestInfo requestInfo) {

        }

        @Override
        public void onReplyPkSuccess(String pkRoomId, String pkRoomToken) {

        }

        @Override
        public void onReplyPkFailed(int code, String reason) {

        }

        @Override
        public void onPkRequestHandled(boolean isAccepted, String pkRoomId, String pkRoomToken) {

        }

        @Override
        public void onPkRequestTimeout() {

        }

        @Override
        public void onPkEnd() {

        }

        @Override
        public void onRemoteEndPk() {

        }

        @Override
        public void onJoinRequestLaunched(AudioParticipant participant) {
            showRequestLaunchedDialog(participant);
        }

        @Override
        public void onJoinRequestHandled(String reqUserId, String roomId, boolean isAccepted, int position) {
            if (isAccepted) {
                // 6. 观众在上麦后发布本地麦克风音频 Track
                mClient.publish(new QNPublishResultCallback() {
                    @Override
                    public void onPublished() {
                        mIsCommunicateAudience = true;
                        mSelfInfo.setPosition(position);
                        audioParticipantChanged(mSelfInfo, true);
                        if (mAudioParticipantsFragment != null) {
                            mAudioParticipantsFragment.setEndBtnVisible(true);
                        }
                        mMainHandler.post(() -> setBottomBtnsVisibility(View.VISIBLE));
                    }

                    @Override
                    public void onError(int errorCode, String errorMessage) {

                    }
                }, mMicrophoneAudioTrack);
            } else {
                showBeRefusedDialog();
            }
        }

        @Override
        public void onAudienceJoin(AudioParticipant participant) {
            if (!participant.getUserInfo().getUserId().equals(mSelfInfo.getUserInfo().getUserId())) {
                audioParticipantChanged(participant, true);
            }
        }

        @Override
        public void onAudienceLeft(AudioParticipant participant) {
            audioParticipantChanged(participant, false);
        }

        @Override
        public void onJoinRequestTimeout(String reqUserId) {
            if (mRequestDialogMap.containsKey(reqUserId)) {
                mRequestDialogMap.remove(reqUserId).dismiss();
            }
            showJoinReqTimeoutDialog();
        }

        @Override
        public void onRoomClosed() {
            ToastUtils.showLongToast(getString(mIsCommunicateAudience
                    ? R.string.toast_room_closed_communicate_audience
                    : R.string.toast_room_closed_audience));
            finish();
        }

        @Override
        public void onError(int errorCode, String errorMessage) {
            Log.e(TAG, "Error happened : {" + errorCode + ", " + errorMessage + "}");
            switch (errorCode) {
                case POSITION_OCCUPIED:
                    ToastUtils.showShortToast(getString(R.string.toast_position_occupied));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onItemClicked(int pos) {
        if (mChatBottomPanel != null && mChatBottomPanel.isPanelVisible()) {
            mChatBottomPanel.hidePanels();
            return;
        }
        if (mIsAudioAnchor && mCurrentConnectionState != QNConnectionState.DISCONNECTED) {
            ToastUtils.showShortToast(getString(R.string.toast_join_room));
            return;
        }
        if (mIsAudioAnchor || mIsCommunicateAudience) {
            ToastUtils.showShortToast(getString(R.string.toast_already_communicate));
            return;
        }
        for (AudioParticipant participant : mCommunicateAudiences) {
            if (participant != null && pos == participant.getPosition()) {
                return;
            }
        }
        if (isPermissionOK()) {
            showRequestJoinDialog(pos);
        }
    }

    @Override
    public void onAudioRouteChanged(QNAudioDevice routing) {

    }

    @Override
    public void onConnectionStateChanged(QNConnectionState state, @Nullable QNConnectionDisconnectedInfo info) {
        Log.i(TAG, "onConnectionStateChanged : " + state.name());
        mCurrentConnectionState = state;
        switch (state) {
            case CONNECTED:
                if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
                if (mIsAudioAnchor || mIsCommunicateAudience) {
                    // 6. 主播发布本地麦克风音频 Track
                    mClient.publish(new QNPublishResultCallback() {
                        @Override
                        public void onPublished() {
                            if (mIsCommunicateAudience) {
                                if (mSelfInfo.isMute()) {
                                    mMicrophoneAudioTrack.setMuted(mIsLocalAudioMute);
                                    mMicrophoneMuteBtn.setImageResource(mIsLocalAudioMute ? R.drawable.ic_microphone_mute : R.drawable.ic_microphone_on);
                                }
                                if (mAudioParticipantsFragment != null) {
                                    mAudioParticipantsFragment.setEndBtnVisible(true);
                                }
                                setBottomBtnsVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onError(int errorCode, String errorMessage) {

                        }
                    }, mMicrophoneAudioTrack);
                }
                updateUIAfterConnected();
                ToastUtils.showShortToast(mSelfInfo.getUserInfo().getUserId() + " 成功加入房间！");
                break;
            case RECONNECTING:
                ToastUtils.showShortToast(getString(R.string.toast_reconnecting));
                break;
            case RECONNECTED:
                ToastUtils.showShortToast(getString(R.string.toast_reconnected));
                break;
        }
    }

    @Override
    public void onUserJoined(String remoteUserID, String userData) {
        mUserNumberText.setText(String.valueOf(mClient.getRemoteUsers().size()));
    }

    @Override
    public void onUserReconnecting(String remoteUserID) {
        String nickName = findNickNameById(remoteUserID);
        if (nickName != null) {
            ToastUtils.showShortToast(
                    String.format(getString(R.string.toast_remote_user_reconnecting), nickName));
        }
    }

    @Override
    public void onUserReconnected(String remoteUserID) {
        String nickName = findNickNameById(remoteUserID);
        if (nickName != null) {
            ToastUtils.showShortToast(
                    String.format(getString(R.string.toast_remote_user_reconnected), nickName));
        }
    }

    @Override
    public void onUserLeft(String remoteUserID) {
        mUserNumberText.setText(String.valueOf(mClient.getRemoteUsers().size()));
    }

    @Override
    public void onUserPublished(String remoteUserID, List<QNRemoteTrack> trackList) {

    }

    @Override
    public void onUserUnpublished(String remoteUserID, List<QNRemoteTrack> trackList) {

    }

    @Override
    public void onSubscribed(String remoteUserID, List<QNRemoteAudioTrack> remoteAudioTracks, List<QNRemoteVideoTrack> remoteVideoTracks) {
        for (QNRemoteAudioTrack remoteAudioTrack : remoteAudioTracks) {
            remoteAudioTrack.setTrackInfoChangedListener(new QNTrackInfoChangedListener() {
                @Override
                public void onMuteStateChanged(boolean isMuted) {
                    ArrayList<QNTrack> muteTracks = new ArrayList<>(1);
                    muteTracks.add(remoteAudioTrack);
                    userTrackInfoUpdated(remoteUserID, muteTracks);
                }
            });
        }
        userTrackInfoUpdated(remoteUserID, new ArrayList<>(remoteAudioTracks));
    }

    @Override
    public void onMessageReceived(QNCustomMessage message) {

    }

    @Override
    public void onMediaRelayStateChanged(String relayRoom, QNMediaRelayState state) {

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
    private LinearLayout mBottomBarLayout;

    private void initChatView() {
        mChatListView = (ListView) findViewById(R.id.chat_list_view);
        mChatListAdapter = new ChatListAdapter(this);
        mChatListView.setAdapter(mChatListAdapter);
        mChatBottomPanel = (BottomPanelFragment) getSupportFragmentManager().findFragmentById(R.id.bottom_bar);
        mChatBottomPanel.setIgnoreAboveKeyboard(true);
        mBottomBarLayout = findViewById(R.id.audience_bottom_bar);

        mHeartLayout = (HeartLayout) findViewById(R.id.heart_layout);
        mBtnHeart = (ImageView) mChatBottomPanel.getView().findViewById(R.id.btn_heart);

        mDanmuContainerView = (DanmuContainerView) findViewById(R.id.danmuContainerView);
        mDanmuContainerView.setAdapter(new DanmuAdapter(this));

        mGiftView = (GiftView) findViewById(R.id.giftView);
        mGiftView.setViewCount(2);
        mGiftView.init();

        setChatViewVisible(View.INVISIBLE);

        mIMBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChatBottomPanel.showInputPanel();
            }
        });

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
            resetBottomBarLayout(true);
            setBottomBtnsVisibility(View.VISIBLE);
        });

        mChatBottomPanel.setGiftPanelListener(new BottomPanelFragment.GiftPanelListener() {
            @Override
            public void onPanelOpen() {
                resetBottomBarLayout(false);
                setBottomBtnsVisibility(View.GONE);
            }

            @Override
            public void onPanelClose() {
                resetBottomBarLayout(true);
                setBottomBtnsVisibility(View.VISIBLE);
            }
        });

        mChatBottomPanel.setBtnsVisible(View.INVISIBLE);

        // 添加软键盘弹出监听，记录软键盘高度
        addOnSoftKeyBoardVisibleListener(findViewById(R.id.audio_communication_activity), (visible, softInputHeight) -> {
            if (visible) {
                resetBottomBarLayout(false);
                setBottomBtnsVisibility(View.GONE);
            } else {
                if (!mChatBottomPanel.isSelectingEmoji() && !mChatBottomPanel.isGiftViewVisible()) {
                    resetBottomBarLayout(true);
                    mChatBottomPanel.hidePanels();
                }
            }
        });

        View chatTouchView = findViewById(R.id.chat_list_touch_view);
        chatTouchView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mCurrentConnectionState == QNConnectionState.DISCONNECTED) {
                            return false;
                        }
                        if (mChatBottomPanel.isSelectingEmoji() || mChatBottomPanel.isGiftViewVisible()) {
                            resetBottomBarLayout(true);
                        }
                        mChatBottomPanel.hidePanels();
                        setBottomBtnsVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
                return false;
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
        mGiftView.setVisibility(visible);
        mChatListView.setVisibility(visible);
        mHeartLayout.setVisibility(visible);
        mDanmuContainerView.setVisibility(visible);
    }

    private void setChatBottomPanelVisibility(int visibility) {
        mChatBottomPanel.setBtnsVisible(visibility);
        mAudienceCloseBtn.setVisibility(visibility);
    }

    private void resetBottomBarLayout(boolean needMargin) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mBottomBarLayout.getLayoutParams();
        int margin = needMargin ? Utils.dp2px(this, 16) : 0;
        lp.setMargins(margin, margin, margin, margin);
        mBottomBarLayout.setLayoutParams(lp);
    }

    private void connectIM() {
        UserInfo userInfo = SharedPreferencesUtils.getUserInfo(this);
        String userName = userInfo.getNickName().isEmpty() ? "路人" : userInfo.getNickName();
        String roomName;
        if (mIsAudioAnchor) {
            roomName = mModifiedRoomNameText.getText().toString();
        } else {
            roomName = mRoomInfo.getName();
        }

        DataInterface.connectIM(new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                Log.e(TAG, "connect IM failed : token incorrect !");

                DataInterface.connectIM(this);
            }

            @Override
            public void onSuccess(String s) {
                Log.i(TAG, "connect IM Success : " + s);

                DataInterface.setLogin(userName, Uri.parse(mSelfInfo.getUserInfo().getAvatar()));
                mChatRoomInfo = new ChatRoomInfo(mRoomInfo.getId(), roomName, null, DataInterface.getUserId(), 0);

                initChatRoom();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                Log.e(TAG, "connect IM error : " + errorCode);
            }
        });
    }

    private void initChatRoom() {
        ChatroomKit.addEventHandler(handler);
        DataInterface.setBanStatus(false);
        joinChatRoom();
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

    private void quitChatRoom() {
        ChatroomKit.quitChatRoom(new RongIMClient.OperationCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "quitChatRoom success");
                ChatroomKit.removeEventHandler(handler);
                if (DataInterface.isLogin()) {
                    ChatroomUserQuit userQuit = new ChatroomUserQuit();
                    userQuit.setId(ChatroomKit.getCurrentUser().getUserId());
                    ChatroomKit.sendMessage(userQuit, new IRongCallback.ISendMessageCallback() {
                        @Override
                        public void onAttached(io.rong.imlib.model.Message message) {
                        }

                        @Override
                        public void onSuccess(io.rong.imlib.model.Message message) {
                            DataInterface.logout();
                        }

                        @Override
                        public void onError(io.rong.imlib.model.Message message, RongIMClient.ErrorCode errorCode) {
                            DataInterface.logout();
                        }
                    });
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {
                ChatroomKit.removeEventHandler(handler);
                DataInterface.logout();
                Log.i(TAG, "quitChatRoom failed errorCode = " + errorCode);
            }
        });
    }

    protected void onJoinChatRoom() {
        if (ChatroomKit.getCurrentUser() == null) {
            return;
        }

        // IM signal 初始化
        mSignalClient = new QNIMSignalClient();
        mSignalClient.setOnSignalClientListener(mOnSignalClientListener);

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
                // 信令消息无需更新 UI
                if (messageContent instanceof ChatroomSignal) {
                    return false;
                }
                String sendUserId = ((io.rong.imlib.model.Message) msg.obj).getSenderUserId();
                if (messageContent instanceof ChatroomBarrage) {
                    ChatroomBarrage barrage = (ChatroomBarrage) messageContent;
                    DanmuEntity danmuEntity = new DanmuEntity();
                    danmuEntity.setContent(barrage.getContent());
                    String name = messageContent.getUserInfo().getName();
                    Uri uri = DataInterface.getAvatarUri(messageContent.getUserInfo().getPortraitUri());
                    danmuEntity.setPortrait(uri);
                    danmuEntity.setName(name);
                    danmuEntity.setType(barrage.getType());
                    mDanmuContainerView.addDanmu(danmuEntity);
                } else if (messageContent instanceof ChatroomGift) {
                    ChatroomGift gift = (ChatroomGift) messageContent;
                    if (gift.getNumber() > 0) {
                        GiftSendModel model = new GiftSendModel(gift.getNumber());
                        model.setGiftRes(DataInterface.getGiftInfo(gift.getId()).getGiftRes());
                        String name = messageContent.getUserInfo().getName();
                        Uri uri = DataInterface.getAvatarUri(messageContent.getUserInfo().getPortraitUri());
                        model.setSig("送出" + DataInterface.getGiftNameById(gift.getId()));
                        model.setNickname(name);
                        model.setUserAvatarRes(uri.toString());
                        mGiftView.addGift(model);
                    }
                } else if (((io.rong.imlib.model.Message) msg.obj).getConversationType() == Conversation.ConversationType.CHATROOM) {
                    io.rong.imlib.model.Message msgObj = (io.rong.imlib.model.Message) msg.obj;
                    mChatListAdapter.addMessage(msgObj);

                    if (messageContent instanceof ChatroomLike) {
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
                                                 final SoftInputStatusListener listener) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isVisibleForLast = false;
            private int mViewHeight;
            private int mNavigationBarHeight;

            @Override
            public void onGlobalLayout() {
                // 屏幕整体的高度
                int height = root.getHeight();
                if (mViewHeight == 0) {
                    mViewHeight = height;
                }
                if (mNavigationBarHeight == 0 && mHasNavigationBar) {
                    mNavigationBarHeight = BarUtils.getNavigationBarHeight();
                }
                boolean visible = Math.abs(mViewHeight - height) > mNavigationBarHeight;
                // 键盘高度
                int keyboardHeight = mViewHeight - height;
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
        setBottomBtnsVisibility(View.VISIBLE);
    }
}
