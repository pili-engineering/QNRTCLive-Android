package com.qiniu.droid.rtc.live.demo.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.Group;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.orzangleli.xdanmuku.DanmuContainerView;
import com.qiniu.bytedanceplugin.ByteDancePlugin;
import com.qiniu.bytedanceplugin.model.ProcessType;
import com.qiniu.droid.rtc.QNCameraEventListener;
import com.qiniu.droid.rtc.QNCameraSwitchResultCallback;
import com.qiniu.droid.rtc.QNCameraVideoTrack;
import com.qiniu.droid.rtc.QNCameraVideoTrackConfig;
import com.qiniu.droid.rtc.QNClientEventListener;
import com.qiniu.droid.rtc.QNClientMode;
import com.qiniu.droid.rtc.QNClientRole;
import com.qiniu.droid.rtc.QNConnectionDisconnectedInfo;
import com.qiniu.droid.rtc.QNConnectionState;
import com.qiniu.droid.rtc.QNCustomMessage;
import com.qiniu.droid.rtc.QNDirectLiveStreamingConfig;
import com.qiniu.droid.rtc.QNLiveStreamingErrorInfo;
import com.qiniu.droid.rtc.QNLiveStreamingListener;
import com.qiniu.droid.rtc.QNLocalAudioTrackStats;
import com.qiniu.droid.rtc.QNLocalTrack;
import com.qiniu.droid.rtc.QNLocalVideoTrackStats;
import com.qiniu.droid.rtc.QNMediaRelayConfiguration;
import com.qiniu.droid.rtc.QNMediaRelayInfo;
import com.qiniu.droid.rtc.QNMediaRelayResultCallback;
import com.qiniu.droid.rtc.QNMediaRelayState;
import com.qiniu.droid.rtc.QNMicrophoneAudioTrack;
import com.qiniu.droid.rtc.QNNetworkQuality;
import com.qiniu.droid.rtc.QNPublishResultCallback;
import com.qiniu.droid.rtc.QNRTC;
import com.qiniu.droid.rtc.QNRTCClient;
import com.qiniu.droid.rtc.QNRTCClientConfig;
import com.qiniu.droid.rtc.QNRTCEventListener;
import com.qiniu.droid.rtc.QNRTCSetting;
import com.qiniu.droid.rtc.QNRemoteAudioTrack;
import com.qiniu.droid.rtc.QNRemoteAudioTrackStats;
import com.qiniu.droid.rtc.QNRemoteTrack;
import com.qiniu.droid.rtc.QNRemoteVideoTrack;
import com.qiniu.droid.rtc.QNRemoteVideoTrackStats;
import com.qiniu.droid.rtc.QNRenderMode;
import com.qiniu.droid.rtc.QNSurfaceView;
import com.qiniu.droid.rtc.QNTrack;
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingConfig;
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingImage;
import com.qiniu.droid.rtc.QNTranscodingLiveStreamingTrack;
import com.qiniu.droid.rtc.QNVideoCaptureConfigPreset;
import com.qiniu.droid.rtc.QNVideoEncoderConfig;
import com.qiniu.droid.rtc.QNVideoFrameListener;
import com.qiniu.droid.rtc.QNVideoFrameType;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.fragment.EffectFragment;
import com.qiniu.droid.rtc.live.demo.fragment.LiveSettingFragment;
import com.qiniu.droid.rtc.live.demo.fragment.PkCandidatesFragment;
import com.qiniu.droid.rtc.live.demo.fragment.PkParticipantsFragment;
import com.qiniu.droid.rtc.live.demo.fragment.StickerFragment;
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
import com.qiniu.droid.rtc.live.demo.utils.Config;
import com.qiniu.droid.rtc.live.demo.utils.Constants;
import com.qiniu.droid.rtc.live.demo.utils.NetworkUtils;
import com.qiniu.droid.rtc.live.demo.utils.QNAppServer;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ThreadUtils;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;
import com.qiniu.droid.rtc.live.demo.utils.ViewClickUtils;
import com.qiniu.droid.rtc.live.demo.view.LoadingDialog;
import com.qiniu.droid.rtc.model.QNAudioDevice;
import com.qiniu.droid.rtc.model.QNImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Size;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.INVALID_PARAMETER;
import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.ROOM_IN_PK;
import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.ROOM_NOT_EXIST;
import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.ROOM_NOT_IN_PK;

/**
 * 直播 PK 场景实现方式
 *
 * 主要步骤如下：
 * 1. 初始化视图
 * 2. 初始化 RTC
 * 3. 创建 QNRTCClient 对象
 * 4. 创建本地音视频 Track
 * 5. 加入房间
 * 6. 发布本地音视频 Track
 * 7. 创建单路转推直播流，进行直播
 * 8. 通过业务逻辑完成 PK 请求的交互 (这里我们假设双方同意 PK)
 * 9. 双方将本地音视频跨房媒体转发到对端房间
 * 10. 订阅远端音视频 Track
 * 11. 创建合流转推任务，并配置合流布局，注意需要使用和单路转推任务相同的推流地址，并实现抢流逻辑，创建成功后停止单路转推任务
 * 12. 结束 PK 时停止跨房媒体转发
 * 13. 重新开启单路转推任务，同样需要使用和合流转推任务相同的推流地址，并实现抢流逻辑，创建成功后停止合流转推任务
 * 14. 离开房间
 * 15. 反初始化 RTC 释放资源
 */
public class LiveRoomActivity extends AppCompatActivity implements QNRTCEventListener, QNClientEventListener, Handler.Callback {
    private static final String TAG = "LiveRoomActivity";

    private QNSurfaceView mLocalVideoSurfaceView;
    private QNSurfaceView mRemoteVideoSurfaceView;
    private Group mControlBtnsAfterLiving;
    private Group mControlBtnsBeforeLiving;
    private ConstraintLayout mConstraintLayout;
    private ImageButton mIMBtn;
    private ImageButton mBeautyBtn;
    private ImageButton mSwitchCameraBtn;
    private ImageButton mSettingBtn;
    private ImageButton mCloseBtn;
    private Button mStartStreaming;
    private TextView mModifiedRoomNameText;
    private TextView mRoomNameText;
    private ImageView mAudienceNumberImage;
    private TextView mAudienceNumberText;
    private LoadingDialog mLoadingDialog;
    private Dialog mPkRequestDialog;

    // 标记 RTC 的生命周期，确保 init/deinit 成对出现并执行
    static boolean mRTCInit = false;
    private QNRTCClient mClient;

    private QNCameraVideoTrack mCameraVideoTrack;
    private QNMicrophoneAudioTrack mMicrophoneAudioTrack;
    private List<QNLocalTrack> mLocalTracks;

    private LiveSettingFragment mLiveSettingFragment;
    private PkCandidatesFragment mPkCandidatesFragment;
    private PkParticipantsFragment mPkParticipantsFragment;
    private Handler mMainHandler;
    private Handler mSubThreadHandler;

    private String mRoomId;
    private String mRoomToken;
    private volatile boolean mIsDirectStreamingStarted;
    private volatile boolean mIsTranscodingStreamingStarted;
    private volatile boolean mNeedResetFlashlight = false;
    private volatile boolean mIsMicrophoneOn = true;
    private volatile boolean mIsSpeakerOn = true;
    private volatile boolean mIsPkEnd = true;

    // 抢流逻辑的参数，从 1 开始递增，数值越大，优先级越高
    private int mSerialNum = 1;

    private QNConnectionState mCurrentConnectionState = QNConnectionState.DISCONNECTED;
    private UserInfo mUserInfo;

    // 获取媒体质量统计信息
    private Timer mStatsTimer;

    // PK 相关
    private QNDirectLiveStreamingConfig mDirectLiveStreamingConfig; // 单路转推配置类
    private QNTranscodingLiveStreamingConfig mTranscodingLiveStreamingConfig; // 合流转推配置类
    private List<QNTranscodingLiveStreamingTrack> mTranscodingLiveStreamingTracks; // 合流转推布局配置列表

    private List<UserInfo> mPkUserList;
    private UserInfo mPkRequesterInfo;
    private String mRemoteRoomID;
    private volatile boolean mIsPkAccepted;
    private List<RoomInfo> mLiveRoomsCanPk;
    private RoomInfo mTargetPkRoomInfo;

    // IM signal
    private QNIMSignalClient mSignalClient;

    private ScheduledExecutorService mExecutor;
    private final Semaphore captureStoppedSem = new Semaphore(1);
    private boolean mStreamingStopped;
    private Toast mAudienceNumberToast;

    private final Runnable mAudienceNumGetter = new Runnable() {
        @Override
        public void run() {
            if (mRoomId != null && (mIsDirectStreamingStarted || mIsTranscodingStreamingStarted) && NetworkUtils.isConnected()) {
                QNAppServer.getInstance().getRoomInfo(mRoomId, new QNAppServer.OnRequestResultCallback() {
                    @Override
                    public void onRequestSuccess(String responseMsg) {
                        RoomInfo roomInfo = new Gson().fromJson(responseMsg, RoomInfo.class);
                        mMainHandler.post(() -> {
                            if (mAudienceNumberText != null && roomInfo != null) {
                                mAudienceNumberText.setText(String.valueOf(roomInfo.getAudienceNumber()));
                            }
                        });
                    }

                    @Override
                    public void onRequestFailed(int code, String reason) {

                    }
                });
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.avtivity_live_room);

        if (mRTCInit) {
            showLifeCircleExceptionDialog();
        }

        mUserInfo = SharedPreferencesUtils.getUserInfo(AppUtils.getApp());
        mMainHandler = new Handler();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mSubThreadHandler = new Handler(handlerThread.getLooper());

        initViews();
        initStatusBar();
        initNavigationBar();
        // 初始化 QNRTCEngine
        initQNRTC();
        // 初始化本地发布 track
        initLocalTrackList();
        // 初始化字节跳动特效
        initByteDanceEffect();
        // 初始化 IM 控件
        initChatView();

        startStatsTimer();

        mPkUserList = new ArrayList<>();
        mPkUserList.add(mUserInfo);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCaptureAfterAcquire();
        streamerBackToLiving();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraVideoTrack.stopCapture();
        if (!mStreamingStopped) {
            streamerSwitchToBackstage();
        }
        if (mAudienceNumberToast != null) {
            mAudienceNumberToast.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mStatsTimer.cancel();

        if (mSignalClient != null) {
            mSignalClient.disconnect();
            mSignalClient.destroy();
            mSignalClient = null;
        }

        quitChatRoom();

        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }

        if (mDirectLiveStreamingConfig != null) {
            mClient.stopLiveStreaming(mDirectLiveStreamingConfig);
            mDirectLiveStreamingConfig = null;
        }
        if (mTranscodingLiveStreamingConfig != null) {
            mClient.stopLiveStreaming(mTranscodingLiveStreamingConfig);
            mTranscodingLiveStreamingConfig = null;
        }
        // 14. 离开房间
        if (mClient != null) {
            mClient.leave();
        }
        // 15. 反初始化 RTC 释放资源
        if (mRTCInit) {
            QNRTC.deinit();
            mRTCInit = false;
        }
        mClient = null;
    }

    public void onClickStartLiveStreaming(View v) {
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
        startStreaming();
    }

    public void onClickPk(View v) {
        if (mIsDirectStreamingStarted) {
            showLiveRoomsCanPk();
        } else {
            showPkParticipantsFragment();
        }
    }

    public void onClickCloseLiving(View v) {
        mStreamingStopped = true;
        finish();
    }

    public void onClickSwitchCamera(View v) {
        mCameraVideoTrack.switchCamera(new QNCameraSwitchResultCallback() {
            @Override
            public void onSwitched(boolean isFrontCamera) {
                mIsFrontCamera = isFrontCamera;
                mNeedResetFlashlight = true;
                updateProcessTypes();
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
    }

    public void onClickLiveSetting(View v) {
        showLiveSettingFragment();
    }

    public void onClickModifyRoomName(View v) {
        View view = getLayoutInflater().inflate(R.layout.dialog_modify_room_name, null);
        EditText editText = view.findViewById(R.id.et_edit_room_name);
        editText.setText(mModifiedRoomNameText.getText());
        new MaterialAlertDialogBuilder(this)
                .setTitle("修改房间名")
                .setView(view)
                .setNegativeButton("取消", null)
                .setPositiveButton("确认", (dialog, which) -> {
                    final String roomName = editText.getText().toString();
                    mModifiedRoomNameText.setText(roomName);
                })
                .create()
                .show();
    }

    private void initStatusBar() {
        BarUtils.transparentStatusBar(this, false);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mConstraintLayout);
        constraintSet.setMargin(mRoomNameText.getId(), ConstraintSet.TOP, BarUtils.getStatusBarHeight() + 32);
        constraintSet.applyTo(mConstraintLayout);
    }

    private void initNavigationBar() {
        mBeautyBtn.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mBeautyBtn.removeOnLayoutChangeListener(this);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    WindowInsets windowInsets = getWindow().getDecorView().getRootWindowInsets();
                    ConstraintSet constraintSet = new ConstraintSet();
                    constraintSet.clone(mConstraintLayout);
                    int bottomMargin = constraintSet.getParameters(mBeautyBtn.getId()).layout.bottomMargin;
                    constraintSet.setMargin(mBeautyBtn.getId(), ConstraintSet.BOTTOM, bottomMargin + windowInsets.getSystemWindowInsetBottom());
                    constraintSet.applyTo(mConstraintLayout);
                }
            }
        });
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

    /**
     * 1. 初始化视图
     */
    private void initViews() {
        mConstraintLayout = findViewById(R.id.live_room_layout);
        mLocalVideoSurfaceView = findViewById(R.id.local_surface_view);
        mRemoteVideoSurfaceView = findViewById(R.id.remote_surface_view);
        mControlBtnsBeforeLiving = findViewById(R.id.group_control_buttons_before_living);
        mControlBtnsAfterLiving = findViewById(R.id.group_living_btns);
        mIMBtn = findViewById(R.id.im_button);
        mBeautyBtn = findViewById(R.id.face_beauty_button);
        mSwitchCameraBtn = findViewById(R.id.switch_camera_button);
        mSettingBtn = findViewById(R.id.setting_button);
        mCloseBtn = findViewById(R.id.close_button);
        mStartStreaming = findViewById(R.id.start_live_streaming_button);
        mRoomNameText = findViewById(R.id.room_nick_name_text);
        mModifiedRoomNameText = findViewById(R.id.room_nick_name);
        mAudienceNumberImage = findViewById(R.id.audience_image);
        mAudienceNumberText = findViewById(R.id.audience_number_text);

        mModifiedRoomNameText.setText(String.format(getString(R.string.room_nick_name_edit_text), mUserInfo.getNickName()));

        mAudienceNumberImage.setOnClickListener(v -> showAudienceNumberToast());
        mAudienceNumberText.setOnClickListener(v -> showAudienceNumberToast());
        mBeautyBtn.setOnClickListener(v -> showEffectPanelSelectPopupWindow());
    }

    /**
     * 初始化 QNRTC
     */
    private void initQNRTC() {
        // 2. 初始化 RTC
        QNRTCSetting setting = new QNRTCSetting()
                .setHWCodecEnabled(false)
                .setMaintainResolution(true);
        QNRTC.init(this, setting, this);
        QNRTC.setSpeakerphoneMuted(!mIsSpeakerOn);

        // 3. 创建 QNRTCClient 对象
        // 使用 LIVE 场景创建 QNRTCClient 实例
        QNRTCClientConfig clientConfig = new QNRTCClientConfig(QNClientMode.LIVE, QNClientRole.BROADCASTER);
        mClient = QNRTC.createClient(clientConfig, this);
        mClient.setLiveStreamingListener(mLiveStreamingListener); // 设置 CDN 转推事件监听器
        mRTCInit = true;
    }

    private void startCaptureAfterAcquire() {
        try {
            captureStoppedSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mCameraVideoTrack.startCapture();
    }

    /**
     * 4. 创建本地音视频 track
     * 关于 Track 的概念介绍 https://developer.qiniu.com/rtc/8767/audio-and-video-collection-android
     */
    private void initLocalTrackList() {
        mLocalTracks = new ArrayList<>();
        // 创建本地麦克风采集的音频 track
        mMicrophoneAudioTrack = QNRTC.createMicrophoneAudioTrack();
        mMicrophoneAudioTrack.setMuted(!mIsMicrophoneOn);
        mLocalTracks.add(mMicrophoneAudioTrack);

        // 创建 Camera 采集的视频 Track
        QNCameraVideoTrackConfig cameraVideoTrackConfig = new QNCameraVideoTrackConfig()
                .setVideoCaptureConfig(QNVideoCaptureConfigPreset.CAPTURE_1280x720)
                .setVideoEncoderConfig(new QNVideoEncoderConfig(
                        Config.STREAMING_HEIGHT, Config.STREAMING_WIDTH,
                        Config.STREAMING_FPS, Config.STREAMING_BITRATE));
        mCameraVideoTrack = QNRTC.createCameraVideoTrack(cameraVideoTrackConfig);
        mCameraVideoTrack.play(mLocalVideoSurfaceView);
        mCameraVideoTrack.setVideoFrameListener(mVideoFrameListener);
        mCameraVideoTrack.setCameraEventListener(mCameraEventListener);
        mLocalTracks.add(mCameraVideoTrack);
    }

    private void startStatsTimer() {
        mStatsTimer = new Timer();
        mStatsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // local video track
                Map<String, List<QNLocalVideoTrackStats>> localVideoTrackStats = mClient.getLocalVideoTrackStats();
                for (Map.Entry<String, List<QNLocalVideoTrackStats>> entry : localVideoTrackStats.entrySet()) {
                    for (QNLocalVideoTrackStats stats : entry.getValue()) {
                        Log.i(TAG, "local: trackID : " + entry.getKey() + ", " + stats.toString());
                    }
                }
                // local audio track
                Map<String, QNLocalAudioTrackStats> localAudioTrackStats = mClient.getLocalAudioTrackStats();
                for (Map.Entry<String, QNLocalAudioTrackStats> entry : localAudioTrackStats.entrySet()) {
                    Log.i(TAG, "local: trackID : " + entry.getKey() + ", " + entry.getValue().toString());
                }
                // remote video track
                Map<String, QNRemoteVideoTrackStats> remoteVideoTrackStats = mClient.getRemoteVideoTrackStats();
                for (Map.Entry<String, QNRemoteVideoTrackStats> entry : remoteVideoTrackStats.entrySet()) {
                    Log.i(TAG, "remote: trackID : " + entry.getKey() + ", " + entry.getValue().toString());
                }
                // remote audio track
                Map<String, QNRemoteAudioTrackStats> remoteAudioTrackStats = mClient.getRemoteAudioTrackStats();
                for (Map.Entry<String, QNRemoteAudioTrackStats> entry : remoteAudioTrackStats.entrySet()) {
                    Log.i(TAG, "remote: trackID : " + entry.getKey() + ", " + entry.getValue().toString());
                }
                // network
                Map<String, QNNetworkQuality> userNetworkQuality = mClient.getUserNetworkQuality();
                for (Map.Entry<String, QNNetworkQuality> entry : userNetworkQuality.entrySet()) {
                    Log.i(TAG, "remote: network quality: userID : " + entry.getKey() + ", " + entry.getValue().toString());
                }
            }
        }, 0, 10000);
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

    private void parseJoinRoomInfo(String response) {
        try {
            JSONObject responseJson = new JSONObject(response);
            mRoomId = responseJson.optString(Constants.KEY_ROOM_ID);
            mRoomToken = responseJson.optString(Constants.KEY_ROOM_TOKEN);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<RoomInfo> getLiveRoomsCanPk() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final String[] response = {null};
        mSubThreadHandler.post(() -> QNAppServer.getInstance().getLiveRoomsCanPk(new QNAppServer.OnRequestResultCallback() {
            @Override
            public void onRequestSuccess(String responseMsg) {
                response[0] = responseMsg;
                countDownLatch.countDown();
            }

            @Override
            public void onRequestFailed(int code, String reason) {
                Log.e(TAG, "get live only rooms failed : " + reason);
                countDownLatch.countDown();
            }
        }));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<RoomInfo> liveRooms = null;
        try {
            JSONObject responseJson = new JSONObject(response[0]);
            JSONArray liveRoomArray = responseJson.optJSONArray(Constants.KEY_ROOMS);
            if (liveRoomArray == null) {
                Log.e(TAG, "get live only rooms failed !");
                return null;
            }
            liveRooms = new ArrayList<>();
            for (int i = 0; i < liveRoomArray.length(); i++) {
                RoomInfo liveRoom = new Gson().fromJson(liveRoomArray.optString(i), RoomInfo.class);
                liveRooms.add(liveRoom);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return liveRooms;
    }

    private void showLiveRoomsCanPk() {
        mLiveRoomsCanPk = getLiveRoomsCanPk();
        if (mPkCandidatesFragment == null) {
            mPkCandidatesFragment = new PkCandidatesFragment(mLiveRoomsCanPk);
            mPkCandidatesFragment.setOnPkCandidateRoomClickListener(roomInfo -> {
                if (mSignalClient != null) {
                    // 8. 通过业务逻辑完成 PK 请求的交互，此处仅作示例演示
                    mSignalClient.startPk(roomInfo.getId());
                } else {
                    ToastUtils.showShortToast(getString(R.string.toast_can_not_start_pk));
                }
                mPkCandidatesFragment.dismiss();
            });
        } else {
            mPkCandidatesFragment.updateCandidateRooms(mLiveRoomsCanPk);
        }
        if (getSupportFragmentManager().findFragmentByTag(PkCandidatesFragment.TAG) == null) {
            mPkCandidatesFragment.show(getSupportFragmentManager(), PkCandidatesFragment.TAG);
        }
    }

    private void showPkParticipantsFragment() {
        if (mPkParticipantsFragment == null) {
            mPkParticipantsFragment = new PkParticipantsFragment(mPkUserList);
            mPkParticipantsFragment.setOnPkParticipantClickListener(new PkParticipantsFragment.OnPkParticipantClickListener() {
                @Override
                public void onLocalMuteAudioClicked() {

                }

                @Override
                public void onLocalMuteVideoClicked() {

                }

                @Override
                public void onEndPkClicked() {
                    // 退出 PK
                    if (mClient == null) {
                        return;
                    }
                    if (mSignalClient != null) {
                        mSignalClient.endPk(mRemoteRoomID);
                    }
                    mPkParticipantsFragment.dismiss();
                }
            });
        }
        if (getSupportFragmentManager().findFragmentByTag(PkParticipantsFragment.TAG) == null) {
            mPkParticipantsFragment.show(getSupportFragmentManager(), PkParticipantsFragment.TAG);
        }
    }

    private void showAudienceNumberToast() {
        ToastUtils.cancel();
        String message = "当前观看人数：" + mAudienceNumberText.getText() + "人";
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

    private void updateUIAfterLiving() {
        mRoomNameText.setText(mModifiedRoomNameText.getText());
        mControlBtnsBeforeLiving.setVisibility(View.GONE);
        mControlBtnsAfterLiving.setVisibility(View.VISIBLE);
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.channel_error_title))
                .setMessage(errorMessage)
                .setCancelable(false)
                .setNeutralButton(R.string.ok,
                        (dialog, id) -> {
                            dialog.cancel();
                            finish();
                        })
                .create()
                .show();
    }

    private void startDirectLiveStreaming() {
        if (mDirectLiveStreamingConfig == null) {
            mDirectLiveStreamingConfig = new QNDirectLiveStreamingConfig();
            mDirectLiveStreamingConfig.setStreamID(String.format(getString(R.string.forward_job_id), mUserInfo.getUserId()));
            // 设置单路转推任务的推流地址，该地址需和合流转推时保持一致，并使得 SerialNum 自增以提高直播流优先级
            mDirectLiveStreamingConfig.setUrl(String.format(getString(R.string.publish_url), mUserInfo.getUserId(), mSerialNum++));
            mDirectLiveStreamingConfig.setAudioTrack(mMicrophoneAudioTrack);
            mDirectLiveStreamingConfig.setVideoTrack(mCameraVideoTrack);
        }
        Log.i(TAG, "create direct live streaming job : " + mDirectLiveStreamingConfig.getStreamID());
        mClient.startLiveStreaming(mDirectLiveStreamingConfig);
    }

    private void startTranscodingLiveStreaming() {
        // 创建合流任务对象
        if (mTranscodingLiveStreamingConfig == null) {
            mTranscodingLiveStreamingConfig = new QNTranscodingLiveStreamingConfig();

            // 设置合流任务 id，该 id 为合流任务的唯一标识符
            mTranscodingLiveStreamingConfig.setStreamID(String.format(getString(R.string.merge_job_id), mUserInfo.getUserId()));

            mTranscodingLiveStreamingConfig.setWidth(Config.STREAMING_WIDTH);
            mTranscodingLiveStreamingConfig.setHeight(Config.STREAMING_HEIGHT);
            // QNTranscodingLiveStreamingConfig 中码率单位为 kbps，所以，若期望码率为 1200kbps，则实际传入的参数值应为 1200
            mTranscodingLiveStreamingConfig.setBitrate(Config.STREAMING_BITRATE);
            mTranscodingLiveStreamingConfig.setVideoFrameRate(Config.STREAMING_FPS);
            mTranscodingLiveStreamingConfig.setRenderMode(QNRenderMode.ASPECT_FILL);

            QNTranscodingLiveStreamingImage background = new QNTranscodingLiveStreamingImage();
            background.setUrl(Config.STREAMING_BACKGROUND);
            background.setX(0);
            background.setY(0);
            background.setHeight(Config.STREAMING_HEIGHT);
            background.setWidth(Config.STREAMING_WIDTH);
            mTranscodingLiveStreamingConfig.setBackground(background);
        }
        // 设置合流任务的推流地址，该地址需和单路转推时保持一致，并使得 SerialNum 自增以提高直播流优先级
        mTranscodingLiveStreamingConfig.setUrl(String.format(getString(R.string.publish_url), mUserInfo.getUserId(), mSerialNum++));

        // 创建合流任务
        mClient.startLiveStreaming(mTranscodingLiveStreamingConfig);
    }

    private void setTranscodingLiveStreamingTracks(List<QNTrack> trackList, boolean isRemote) {
        for (QNTrack track : trackList) {
            QNTranscodingLiveStreamingTrack liveStreamingTrack = new QNTranscodingLiveStreamingTrack();
            liveStreamingTrack.setTrackID(track.getTrackID());
            if (track.isVideo()) {
                // 设置视频画面在合流布局中的位置，需要根据您期望视频在合流画布中的位置自行定义 x、y 的值
                liveStreamingTrack.setX(isRemote ? Config.STREAMING_WIDTH / 2 : 0); // 设置 Track 在合流布局中位置的左上角 x 坐标，此处仅做示例
                liveStreamingTrack.setY(Config.STREAMING_HEIGHT * 4 / 23); // 设置 Track 在合流布局中位置的左上角 y 坐标，此处仅做示例
                liveStreamingTrack.setZOrder(0); // 设置合流层级，值越大，画面层级越高
                liveStreamingTrack.setWidth(Config.STREAMING_WIDTH / 2); // 设置 Track 在合流布局中的宽度
                liveStreamingTrack.setHeight(Config.STREAMING_HEIGHT / 2); // 设置 Track 在合流布局中的高度，此处仅做示例
            }
            if (mTranscodingLiveStreamingTracks == null) {
                mTranscodingLiveStreamingTracks = new ArrayList<>();
            }
            mTranscodingLiveStreamingTracks.add(liveStreamingTrack);
        }
    }

    private void relayoutLocalSurfaceView(boolean isPkMode) {
        mRemoteVideoSurfaceView.setVisibility(isPkMode ? View.VISIBLE : View.GONE);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mConstraintLayout);
        if (isPkMode) {
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.BOTTOM, R.id.pk_bottom_divider, ConstraintSet.TOP);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.TOP, R.id.pk_top_divider, ConstraintSet.BOTTOM);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.END, R.id.background_divider, ConstraintSet.START);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        } else {
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        }
        constraintSet.applyTo(mConstraintLayout);
    }

    private void updateBtnsSources(boolean isPkMode) {
        if (isPkMode) {
            mIMBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_chat_pk));
            mSwitchCameraBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_switch_camera_pk));
            mBeautyBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_beauty_pk));
            mSettingBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_settings_pk));
            mCloseBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_small_close_pk));
        } else {
            mIMBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_chat));
            mSwitchCameraBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_switch_camera));
            mBeautyBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_beauty));
            mSettingBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_settings));
            mCloseBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_small_close));
        }
    }

    private void showPkRequestDialog(PkRequestInfo info) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_handle_request, null);
        TextView content = view.findViewById(R.id.request_pk_info);
        Button acceptBtn = view.findViewById(R.id.accept_btn);
        Button refuseBtn = view.findViewById(R.id.refuse_btn);

        content.setText(String.format(getString(R.string.request_pk_text), info.getNickName()));
        acceptBtn.setText(R.string.accept_pk);
        refuseBtn.setText(R.string.refuse_pk);

        if (mPkRequestDialog == null) {
            mPkRequestDialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                    .setCancelable(false)
                    .create();
        }
        mPkRequestDialog.show();
        mPkRequestDialog.setContentView(view);

        // 接收远端的 PK 请求处理
        acceptBtn.setOnClickListener(v1 -> {
            if (mClient == null || mSignalClient == null) {
                return;
            }
            if (mPkCandidatesFragment != null && !mPkCandidatesFragment.isHidden()) {
                mPkCandidatesFragment.dismiss();
            }
            mIsPkAccepted = true;
            // 回复 PK 请求
            mSignalClient.answerPk(info.getRoomId(), mIsPkAccepted);
            mPkRequestDialog.dismiss();
        });

        // 拒绝远端的 PK 请求处理
        refuseBtn.setOnClickListener(arg0 -> {
            if (mSignalClient == null) {
                return;
            }
            mIsPkAccepted = false;
            // 回复 PK 请求
            mSignalClient.answerPk(info.getRoomId(), mIsPkAccepted);
            mPkRequestDialog.dismiss();
        });
    }

    private void showBeRefusedDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tips, null);
        TextView content = view.findViewById(R.id.dialog_content_text);
        Button sureBtn = view.findViewById(R.id.ok_btn);
        content.setText(getString(R.string.remote_refused_pk_request));
        sureBtn.setText(getString(R.string.confirm_be_refused));

        final Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.setContentView(view);

        sureBtn.setOnClickListener(v1 -> dialog.dismiss());
    }

    private void showPkReqTimeoutDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tips, null);
        TextView content = view.findViewById(R.id.dialog_content_text);
        Button sureBtn = view.findViewById(R.id.ok_btn);
        content.setText(getString(R.string.pk_timeout_content));
        sureBtn.setText(getString(R.string.confirm_text));

        final Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.setContentView(view);

        sureBtn.setOnClickListener(v1 -> dialog.dismiss());
    }

    private void showReconnectFailedDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tips, null);
        TextView content = view.findViewById(R.id.dialog_content_text);
        Button sureBtn = view.findViewById(R.id.ok_btn);
        content.setText(getString(R.string.tips_reconnect_failed));
        sureBtn.setText(getString(R.string.confirm_text));

        final Dialog dialog = new AlertDialog.Builder(this, R.style.DialogTheme)
                .setCancelable(false)
                .create();
        dialog.show();
        dialog.setContentView(view);

        sureBtn.setOnClickListener(v1 -> finish());
    }

    private void joinRoomByCreate() {
        QNAppServer.getInstance().createRoom(
                mUserInfo.getUserId(), mModifiedRoomNameText.getText().toString().trim(), "pk", new QNAppServer.OnRequestResultCallback() {
                    @Override
                    public void onRequestSuccess(String responseMsg) {
                        joinRoomWithResponseInfo(responseMsg);
                    }

                    @Override
                    public void onRequestFailed(int code, String reason) {
                        ToastUtils.showShortToast(getString(R.string.toast_create_room_failed) + " : " + reason);
                        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                            mLoadingDialog.dismiss();
                        }
                    }
                });
    }

    private void startStreaming() {
        ThreadUtils.getFixedThreadPool().execute(() ->
                QNAppServer.getInstance().getRoomInfoByCreator(mUserInfo.getUserId(), new QNAppServer.OnRequestResultCallback() {
                    @Override
                    public void onRequestSuccess(String responseMsg) {
                        RoomInfo roomInfo = parseRoomInfo(responseMsg);
                        if (roomInfo == null) {
                            joinRoomByCreate();
                        } else {
                            QNAppServer.getInstance().closeRoom(mUserInfo.getUserId(), roomInfo.getId(), new QNAppServer.OnRequestResultCallback() {
                                @Override
                                public void onRequestSuccess(String responseMsg) {
                                    joinRoomByCreate();
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

    private void joinRoom(String roomToken) {
        if (mClient != null) {
            // 5. 加入房间
            mClient.join(roomToken);
        }
    }

    private void joinRoomWithResponseInfo(String responseMsg) {
        parseJoinRoomInfo(responseMsg);
        // 加入 RTC 房间
        joinRoom(mRoomToken);

        connectIM();
        mMainHandler.post(() -> setChatViewVisible(View.VISIBLE));
    }

    /**
     * 结束 PK
     */
    private void handleEndPk() {
        if (mIsPkEnd) {
            return;
        }
        mIsPkEnd = true;
        // 12. 停止跨房媒体转发
        mClient.stopMediaRelay(new QNMediaRelayResultCallback() {
            @Override
            public void onResult(Map<String, QNMediaRelayState> stateMap) {
                if (stateMap.containsKey(mRemoteRoomID) && stateMap.get(mRemoteRoomID) == QNMediaRelayState.STOPPED) {
                    //13. 创建单路转推任务
                    startDirectLiveStreaming();
                    if (mPkRequesterInfo != null) {
                        mPkUserList.remove(mPkRequesterInfo);
                        mPkRequesterInfo = null;
                    }
                    if (mTargetPkRoomInfo != null) {
                        mPkUserList.remove(mTargetPkRoomInfo.getCreator());
                        mTargetPkRoomInfo = null;
                    }
                    mMainHandler.post(() -> {
                        updateBtnsSources(false);
                        relayoutLocalSurfaceView(false);
                        if (mPkParticipantsFragment != null && mPkParticipantsFragment.isVisible()) {
                            mPkParticipantsFragment.dismiss();
                        }
                    });
                    mRemoteRoomID = null;
                }
            }

            @Override
            public void onError(int errorCode, String description) {

            }
        });
    }

    private void showLiveSettingFragment() {
        if (mLiveSettingFragment == null) {
            mLiveSettingFragment = new LiveSettingFragment();
            mLiveSettingFragment.setOnLiveSettingClickListener(new LiveSettingFragment.OnLiveSettingClickListener() {
                @Override
                public void onFragmentResumed() {
                    if (mNeedResetFlashlight) {
                        mLiveSettingFragment.resetFlashlightSetting();
                        mNeedResetFlashlight = false;
                    }
                    mLiveSettingFragment.setFlashlightSettingEnabled(!mIsFrontCamera);
                }

                @Override
                public void onMicrophoneSettingChanged(boolean isOn) {
                    if (mMicrophoneAudioTrack != null) {
                        mIsMicrophoneOn = isOn;
                        mMicrophoneAudioTrack.setMuted(!mIsMicrophoneOn);
                    }
                }

                @Override
                public void onSpeakerSettingChanged(boolean isOn) {
                    mIsSpeakerOn = isOn;
                    QNRTC.setSpeakerphoneMuted(!mIsSpeakerOn);
                }

                @Override
                public void onFlashlightSettingChanged(boolean isOn) {
                    if (mCameraVideoTrack == null) {
                        return;
                    }
                    if (isOn) {
                        mCameraVideoTrack.turnLightOn();
                    } else {
                        mCameraVideoTrack.turnLightOff();
                    }
                }
            });
        }
        if (getSupportFragmentManager().findFragmentByTag(LiveSettingFragment.TAG) == null) {
            mLiveSettingFragment.show(getSupportFragmentManager(), LiveSettingFragment.TAG);
        }
    }

    @Override
    public void onAudioRouteChanged(QNAudioDevice qnAudioDevice) {

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
                if (mExecutor == null || mExecutor.isShutdown()) {
                    mExecutor = Executors.newSingleThreadScheduledExecutor();
                    mExecutor.scheduleAtFixedRate(mAudienceNumGetter, 0, Config.GET_AUDIENCE_NUM_PERIOD, TimeUnit.SECONDS);
                }
                // 6. 发布本地音视频 Track
                mClient.publish(new QNPublishResultCallback() {
                    @Override
                    public void onPublished() {
                        Log.i(TAG, "onLocalPublished");
                        // 预先对本地合流布局进行配置，以应对需要 PK 的场景
                        setTranscodingLiveStreamingTracks(new ArrayList<>(mLocalTracks), false);
                        // 7. 创建单路转推直播流，进行直播
                        startDirectLiveStreaming();
                    }

                    @Override
                    public void onError(int errorCode, String errorMessage) {

                    }
                }, mCameraVideoTrack, mMicrophoneAudioTrack);
                updateUIAfterLiving();
                break;
            case RECONNECTING:
                ToastUtils.showShortToast(getString(R.string.toast_reconnecting));
                break;
            case RECONNECTED:
                ToastUtils.showShortToast(getString(R.string.toast_reconnected));
                if (mIsTranscodingStreamingStarted) {
                    startTranscodingLiveStreaming();
                } else if (mIsDirectStreamingStarted) {
                    startDirectLiveStreaming();
                }
                break;
            case DISCONNECTED:
                if (info != null && info.getReason() == QNConnectionDisconnectedInfo.Reason.LEAVE) {
                    Log.i(TAG, "已离开房间");
                    mTranscodingLiveStreamingTracks.clear();
                } else {
                    ToastUtils.showShortToast(
                            String.format(getString(R.string.toast_connection_disconnected),
                                    info.getReason().name(), info.getErrorCode(), info.getErrorMessage()));
                }
        }
    }

    @Override
    public void onUserJoined(String remoteUserID, String userData) {
        Log.i(TAG, "onUserJoined : " + remoteUserID);
    }

    @Override
    public void onUserReconnecting(String remoteUserID) {
        ToastUtils.showShortToast("远端用户正在重连");
    }

    @Override
    public void onUserReconnected(String remoteUserID) {
        ToastUtils.showShortToast("远端用户已重新连接");
    }

    @Override
    public void onUserLeft(String remoteUserID) {
        Log.i(TAG, "onRemoteUserLeft : " + remoteUserID);
        ToastUtils.showShortToast("远端用户已下线，PK 结束");
        handleEndPk();
    }

    @Override
    public void onUserPublished(String remoteUserID, List<QNRemoteTrack> trackList) {
        Log.i(TAG, "onUserPublished : " + remoteUserID);
    }

    @Override
    public void onUserUnpublished(String remoteUserID, List<QNRemoteTrack> trackList) {
        Log.i(TAG, "onRemoteUnpublished : " + remoteUserID);
    }

    @Override
    public void onSubscribed(String remoteUserID, List<QNRemoteAudioTrack> remoteAudioTracks, List<QNRemoteVideoTrack> remoteVideoTracks) {
        Log.i(TAG, "onSubscribed : " + remoteUserID);
        // 11. 成功订阅到远端音视频 Track，创建合流转推任务，并在创建成功后，配置合流布局
        List<QNRemoteTrack> remoteTracks = new ArrayList<>();
        remoteTracks.addAll(remoteAudioTracks);
        remoteTracks.addAll(remoteVideoTracks);
        setTranscodingLiveStreamingTracks(new ArrayList<>(remoteTracks), true); // 更新合流布局
        startTranscodingLiveStreaming();

        for (QNRemoteVideoTrack remoteVideoTrack : remoteVideoTracks) {
            remoteVideoTrack.play(mRemoteVideoSurfaceView); // 渲染远端视频画面
        }
        relayoutLocalSurfaceView(true);
        updateBtnsSources(true);
    }

    @Override
    public void onMessageReceived(QNCustomMessage message) {

    }

    @Override
    public void onMediaRelayStateChanged(String relayRoom, QNMediaRelayState state) {
        Log.i(TAG, "跨房媒体转发状态改变：" + relayRoom + " " + state.name());
    }

    private final QNLiveStreamingListener mLiveStreamingListener = new QNLiveStreamingListener() {
        /**
         * 转推任务成功创建时触发此回调
         *
         * @param streamID 转推成功的 streamID
         */
        @Override
        public void onStarted(String streamID) {
            if (mDirectLiveStreamingConfig != null && streamID.equals(mDirectLiveStreamingConfig.getStreamID())) {
                ToastUtils.showShortToast("转推任务创建成功：" + streamID);
                Log.i(TAG, "转推任务创建成功：" + streamID + " url = " + mDirectLiveStreamingConfig.getUrl());
                mIsDirectStreamingStarted = true;
                // 如果正在合流，需停止合流转推
                if (mIsTranscodingStreamingStarted) {
                    Log.i(TAG, "停止合流任务：" + mTranscodingLiveStreamingConfig.getStreamID());
                    mClient.stopLiveStreaming(mTranscodingLiveStreamingConfig);
                }
            }
            if (mTranscodingLiveStreamingConfig != null && streamID.equals(mTranscodingLiveStreamingConfig.getStreamID())) {
                Log.i(TAG, "合流任务创建成功：" + streamID + " url = " + mTranscodingLiveStreamingConfig.getUrl());
                // 停止单路转推
                if (mIsDirectStreamingStarted) {
                    Log.i(TAG, "停止转推任务：" + mDirectLiveStreamingConfig.getStreamID());
                    mClient.stopLiveStreaming(mDirectLiveStreamingConfig);
                }
                // 11. 配置合流布局
                mClient.setTranscodingLiveStreamingTracks(streamID, mTranscodingLiveStreamingTracks);
                mIsTranscodingStreamingStarted = true;
            }
        }

        /**
         * 转推任务成功停止时触发此回调
         *
         * @param streamID 停止转推的 streamID
         */
        @Override
        public void onStopped(String streamID) {
            if (mDirectLiveStreamingConfig != null && streamID.equals(mDirectLiveStreamingConfig.getStreamID())) {
                mIsDirectStreamingStarted = false;
            }
            if (mTranscodingLiveStreamingConfig != null && streamID.equals(mTranscodingLiveStreamingConfig.getStreamID())) {
                mIsTranscodingStreamingStarted = false;
            }
        }

        /**
         * 转推任务配置更新时触发此回调
         *
         * @param streamID 配置更新的 streamID
         */
        @Override
        public void onTranscodingTracksUpdated(String streamID) {
            Log.i(TAG, "合流布局更新成功：" + streamID);
        }

        /**
         * 转推任务出错时触发此回调
         *  @param streamID 出现错误的 streamID
         * @param errorInfo 详细错误原因
         */
        @Override
        public void onError(String streamID, QNLiveStreamingErrorInfo errorInfo) {
            Log.i(TAG, "CDN 转推出错：" + streamID + ", " + errorInfo);
        }
    };

    /**
     * 8. 通过业务逻辑完成 PK 请求的交互，此处仅作示例演示
     */
    private final OnSignalClientListener mOnSignalClientListener = new OnSignalClientListener() {
        @Override
        public void onPkRequestLaunched(PkRequestInfo requestInfo) {
            // 收到远端的 PK 请求
            mPkRequesterInfo = new UserInfo(requestInfo.getUserId(), requestInfo.getNickName(), "", "");
            showPkRequestDialog(requestInfo);
        }

        @Override
        public void onReplyPkSuccess(String pkRoomId, String pkRoomToken) {
            if (!mIsPkAccepted || mClient == null) {
                mPkRequesterInfo = null;
                return;
            }
            mPkUserList.add(mPkRequesterInfo);
            // 处理 PK 请求成功后的回调
            mRemoteRoomID = pkRoomId;
            mIsPkAccepted = false;
            mIsPkEnd = false;
            // 开始跨房媒体转发
            QNMediaRelayInfo srcRoomInfo = new QNMediaRelayInfo(mRoomId, mRoomToken);
            QNMediaRelayConfiguration mediaRelayConfiguration = new QNMediaRelayConfiguration(srcRoomInfo); // 初始化并设置源房间信息
            QNMediaRelayInfo destRelayRoomInfo = new QNMediaRelayInfo(mRemoteRoomID, pkRoomToken);
            mediaRelayConfiguration.addDestRoomInfo(destRelayRoomInfo); // 设置目标房间信息
            Log.i(TAG, "onReplyPkSuccess media relay : " + destRelayRoomInfo.getRoomName() + " " + destRelayRoomInfo.getRelayToken());
            // 9. PK 请求处理方将本地音视频跨房媒体转发到对端房间
            mClient.startMediaRelay(mediaRelayConfiguration, new QNMediaRelayResultCallback() {
                @Override
                public void onResult(Map<String, QNMediaRelayState> map) {
                    if (map.containsKey(mRemoteRoomID) && map.get(mRemoteRoomID) == QNMediaRelayState.SUCCESS) {
                        ToastUtils.showShortToast(getString(R.string.toast_start_media_relay_success));
                    }
                }

                @Override
                public void onError(int errorCode, String description) {
                    Log.i(TAG, "start media relay error : " + errorCode + ", " + description);
                }
            });
        }

        @Override
        public void onReplyPkFailed(int code, String reason) {
            // 处理 PK 请求失败，失败后，无论同意还是拒绝 PK 请求，都将视为无效
            switch (code) {
                case ROOM_NOT_EXIST:
                    ToastUtils.showShortToast(getString(R.string.toast_room_not_exist));
                    break;
                case ROOM_IN_PK:
                    ToastUtils.showShortToast(getString(R.string.toast_room_in_pk));
                    break;
            }
        }

        @Override
        public void onPkRequestHandled(boolean isAccepted, String pkRoomId, String pkRoomToken) {
            if (isAccepted) {
                // PK 请求成功被远端接受后回调处理
                if (mClient == null) {
                    return;
                }
                for (RoomInfo roomInfo : mLiveRoomsCanPk) {
                    if (roomInfo.getId().equals(pkRoomId)) {
                        mTargetPkRoomInfo = roomInfo;
                        break;
                    }
                }
                if (mTargetPkRoomInfo == null) {
                    ToastUtils.showLongToast(getString(R.string.toast_remote_room_exception));
                    return;
                }
                mPkUserList.add(mTargetPkRoomInfo.getCreator());
                mRemoteRoomID = pkRoomId;
                mIsPkEnd = false;

                // 开始跨房媒体转发
                QNMediaRelayInfo srcRoomInfo = new QNMediaRelayInfo(mRoomId, mRoomToken);
                QNMediaRelayConfiguration mediaRelayConfiguration = new QNMediaRelayConfiguration(srcRoomInfo); // 初始化并设置源房间信息
                QNMediaRelayInfo destRelayRoomInfo = new QNMediaRelayInfo(mRemoteRoomID, pkRoomToken);
                mediaRelayConfiguration.addDestRoomInfo(destRelayRoomInfo); // 设置目标房间信息
                Log.i(TAG, "onPkRequestHandled media relay : " + destRelayRoomInfo.getRoomName() + " " + destRelayRoomInfo.getRelayToken());
                // 9. PK 请求发起方将本地音视频跨房媒体转发到对端房间
                mClient.startMediaRelay(mediaRelayConfiguration, new QNMediaRelayResultCallback() {
                    @Override
                    public void onResult(Map<String, QNMediaRelayState> stateMap) {
                        if (stateMap.containsKey(mRemoteRoomID) && stateMap.get(mRemoteRoomID) == QNMediaRelayState.SUCCESS) {
                            ToastUtils.showShortToast(getString(R.string.toast_start_media_relay_success));
                        }
                    }

                    @Override
                    public void onError(int errorCode, String description) {
                        Log.i(TAG, "pk requester start media relay error : " + errorCode + ", " + description);
                    }
                });
            } else {
                mTargetPkRoomInfo = null;
                showBeRefusedDialog();
            }
        }

        @Override
        public void onPkRequestTimeout() {
            if (mPkRequestDialog != null && mPkRequestDialog.isShowing()) {
                mPkRequestDialog.dismiss();
            }
            showPkReqTimeoutDialog();
        }

        @Override
        public void onPkEnd() {
            handleEndPk();
        }

        @Override
        public void onRemoteEndPk() {
            handleEndPk();
        }

        @Override
        public void onJoinRequestLaunched(AudioParticipant info) {

        }

        @Override
        public void onJoinRequestHandled(String reqUserId, String roomId, boolean isAccepted, int position) {

        }

        @Override
        public void onAudienceJoin(AudioParticipant info) {

        }

        @Override
        public void onAudienceLeft(AudioParticipant info) {

        }

        @Override
        public void onJoinRequestTimeout(String reqUserId) {

        }

        @Override
        public void onRoomClosed() {

        }

        @Override
        public void onError(int errorCode, String errorMessage) {
            switch (errorCode) {
                case ROOM_NOT_EXIST:
                    ToastUtils.showShortToast(getString(R.string.toast_room_not_exist));
                    break;
                case ROOM_IN_PK:
                    ToastUtils.showShortToast(getString(R.string.toast_room_in_pk));
                    break;
                case ROOM_NOT_IN_PK:
                    ToastUtils.showShortToast(getString(R.string.toast_room_not_in_pk));
                    break;
                case INVALID_PARAMETER:
                    ToastUtils.showShortToast(getString(R.string.toast_invalid_parameter));
                    break;
            }
        }
    };

    /**
     * ============================================== 字节跳动特效相关 ==============================================
     **/
    private static final String TAG_EFFECT = "effect";
    private static final String TAG_STICKER = "sticker";

    private EffectFragment mEffectFragment;
    private StickerFragment mStickerFragment;
    private PopupWindow mEffectPanelSelectPopupWindow;
    private Handler mGLHandler;
    // 特效相关
    private ByteDancePlugin mByteDancePlugin;
    private String mEffectResourcePath;
    // 特效处理列表，其中存储的是将纹理、YUV转正所需要的处理类型
    private volatile CopyOnWriteArrayList<ProcessType> mProcessTypes;
    private volatile boolean mIsFrontCamera = true;
    private volatile int mTextureRotation;

    private void initByteDanceEffect() {
        //此路径为之前拷贝资源的地址
        mEffectResourcePath = getExternalFilesDir("assets") + File.separator + "resource";
        mByteDancePlugin = new ByteDancePlugin(this, ByteDancePlugin.PluginType.record);
        mProcessTypes = new CopyOnWriteArrayList<>();
    }

    private void showBeautyPanel() {
        hideBottomBtns();
        showPanel(TAG_EFFECT);
    }

    private void showStickerPanel() {
        hideBottomBtns();
        showPanel(TAG_STICKER);
    }

    /**
     * 关闭正在显示的面板
     *
     * @return 是否成功关闭
     */
    private void hideEffectPanel() {
        Fragment showingFragment = showingFragment();
        if (showingFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.push_down_in, R.anim.push_down_out);
            ft.hide(showingFragment).commit();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            // 隐藏特效类型选择的弹窗
            hideEffectPanelSelectPopupWindow();
            if (!super.dispatchTouchEvent(motionEvent)) {
                // 如果当前抬起事件无人消费，说明点击在了空处，隐藏特效面板
                hideEffectPanel();

                // 隐藏聊天输入框并显示底部按钮
                mChatBottomPanel.hidePanels();
                showBottomBtns();

                return false;
            }
            return true;
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    private void hideBottomBtns() {
        mIMBtn.setVisibility(View.INVISIBLE);
        mBeautyBtn.setVisibility(View.INVISIBLE);
        mSwitchCameraBtn.setVisibility(View.INVISIBLE);
        mSettingBtn.setVisibility(View.INVISIBLE);
        mCloseBtn.setVisibility(View.INVISIBLE);
        mStartStreaming.setVisibility(View.INVISIBLE);
    }

    private void showBottomBtns() {
        if (mCurrentConnectionState != QNConnectionState.DISCONNECTED) {
            setBottomBtnsVisible(View.VISIBLE);
        } else {
            mBeautyBtn.setVisibility(View.VISIBLE);
            mSwitchCameraBtn.setVisibility(View.VISIBLE);
            mSettingBtn.setVisibility(View.VISIBLE);
            mStartStreaming.setVisibility(View.VISIBLE);
        }
    }

    private void setBottomBtnsVisible(int visible) {
        mIMBtn.setVisibility(visible);
        mBeautyBtn.setVisibility(visible);
        mSwitchCameraBtn.setVisibility(visible);
        mSettingBtn.setVisibility(visible);
        mCloseBtn.setVisibility(visible);
    }

    /**
     * 展示指定的 fragemnt
     *
     * @param tag effect 代表特效面板，sticker 代表贴纸面板
     */
    private void showPanel(String tag) {
        if (showingFragment() != null) {
            getSupportFragmentManager().beginTransaction().hide(showingFragment()).commit();
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.push_down_in, R.anim.push_down_out);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment == null) {
            fragment = generateFragment(tag);
            ft.add(R.id.effect_panel_container, fragment, tag).commit();
        } else {
            ft.show(fragment).commit();
        }
    }

    /**
     * 根据 tag 创建指定的 fragment
     *
     * @param tag effect 代表特效面板，sticker 代表贴纸面板
     * @return 创建好的 fragment
     */
    private Fragment generateFragment(String tag) {
        switch (tag) {
            case TAG_EFFECT:
                if (mEffectFragment != null) {
                    return mEffectFragment;
                }

                final EffectFragment effectFragment = new EffectFragment();
                effectFragment.setCallback(new EffectFragment.IEffectCallback() {
                    @Override
                    public void updateComposeNodes(final String[] nodes) {
                        mGLHandler.post(() -> mByteDancePlugin.setComposerNodes(nodes));
                    }

                    @Override
                    public void updateComposeNodeIntensity(final String path, final String key, final float value) {
                        mGLHandler.post(() -> mByteDancePlugin.updateComposerNode(path, key, value));
                    }

                    @Override
                    public void onFilterSelected(final String fileName) {
                        mGLHandler.post(() -> mByteDancePlugin.setFilter(fileName));
                    }

                    @Override
                    public void onFilterValueChanged(final float value) {
                        mGLHandler.post(() -> mByteDancePlugin.updateFilterIntensity(value));
                    }

                    @Override
                    public void setEffectOn(final boolean isOn) {
                        mGLHandler.post(() -> mByteDancePlugin.setEffectOn(isOn));
                    }

                    @Override
                    public void onDefaultClick() {
                    }
                });
                mEffectFragment = effectFragment;
                return effectFragment;
            case TAG_STICKER:
                if (mStickerFragment != null) {
                    return mStickerFragment;
                }
                StickerFragment stickerFragment = new StickerFragment();
                stickerFragment.setCallback(fileName -> mGLHandler.post(() -> mByteDancePlugin.setSticker(fileName)));
                mStickerFragment = stickerFragment;
                return stickerFragment;
            default:
                return null;
        }
    }

    /**
     * 取得正在显示的 fragment
     *
     * @return 正在显示的 fragment
     */
    private Fragment showingFragment() {
        if (mEffectFragment != null && !mEffectFragment.isHidden()) {
            return mEffectFragment;
        } else if (mStickerFragment != null && !mStickerFragment.isHidden()) {
            return mStickerFragment;
        }
        return null;
    }

    private void showEffectPanelSelectPopupWindow() {
        if (mEffectPanelSelectPopupWindow == null) {
            mEffectPanelSelectPopupWindow = new PopupWindow(this);
            mEffectPanelSelectPopupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            mEffectPanelSelectPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            View view = LayoutInflater.from(this).inflate(R.layout.popup_effect_panel_select, null, false);
            mEffectPanelSelectPopupWindow.setContentView(view);
            LinearLayout beautyLayout = view.findViewById(R.id.ll_popup_effect_panel_beauty);
            LinearLayout stickerLayout = view.findViewById(R.id.ll_popup_effect_panel_sticker);
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            beautyLayout.setOnClickListener(v -> {
                showBeautyPanel();
                mEffectPanelSelectPopupWindow.dismiss();
            });
            stickerLayout.setOnClickListener(v -> {
                showStickerPanel();
                mEffectPanelSelectPopupWindow.dismiss();
            });
            mEffectPanelSelectPopupWindow.setBackgroundDrawable(null);
        }
        int[] location = new int[2];
        mBeautyBtn.getLocationOnScreen(location);
        View contentView = mEffectPanelSelectPopupWindow.getContentView();
        mEffectPanelSelectPopupWindow.showAtLocation(mBeautyBtn, Gravity.NO_GRAVITY,
                (location[0] + mBeautyBtn.getMeasuredWidth() / 2) - contentView.getMeasuredWidth() / 2,
                location[1] - contentView.getMeasuredHeight());
    }

    private void hideEffectPanelSelectPopupWindow() {
        if (mEffectPanelSelectPopupWindow != null && mEffectPanelSelectPopupWindow.isShowing()) {
            mEffectPanelSelectPopupWindow.dismiss();
        }
    }

    private final QNCameraEventListener mCameraEventListener = new QNCameraEventListener() {
        /**
         * 当打开采集设备时触发
         * <p>可以用于根据返回的设备能力选择采集参数<p/>
         *
         * @param sizes        采集设备支持的分辨率列表
         * @param fpsAscending 采集设备支持的帧率
         * @return 选择的分辨率和帧率在 {@code sizes} 和 {@code fpsAscending} 中的下标
         */
        @Override
        public int[] onCameraOpened(List<Size> sizes, List<Integer> fpsAscending) {
            return new int[]{-1, -1};
        }

        /**
         * 当开始采集时触发
         */
        @Override
        public void onCaptureStarted() {
            mByteDancePlugin.init(mEffectResourcePath);
            // 由于切换到后台时会 destroy mByteDancePlugin，所以从后台切回来的时候需要先恢复特效
            mByteDancePlugin.recoverEffects();
            updateProcessTypes();
        }

        /**
         * 当采集停止时触发
         */
        @Override
        public void onCaptureStopped() {
            //停止连麦或者切到后台时候会调用
            mByteDancePlugin.destroy();
            mTextureRotation = 0;
            mIsFrontCamera = true;
            captureStoppedSem.release();
        }

        /**
         * 当 Camera 错误发生时触发此回调
         *
         * @param errorCode   错误码
         * @param description 错误原因
         */
        @Override
        public void onError(int errorCode, String description) {

        }
    };

    private final QNVideoFrameListener mVideoFrameListener = new QNVideoFrameListener() {
        /**
         * YUV 视频数据回调
         *
         * @param data                  视频数据
         * @param type                  数据类型
         * @param width                 宽
         * @param height                高
         * @param rotation              旋转角度
         * @param timestampNs           时间戳
         */
        @Override
        public void onYUVFrameAvailable(byte[] data, QNVideoFrameType type, int width, int height, int rotation, long timestampNs) {
            if (mTextureRotation != rotation) {
                mTextureRotation = rotation;
                updateProcessTypes();
            }
        }

        /**
         * 纹理视频数据回调，只有 QNCameraVideoTrack 的回调才会触发此方法
         *
         * @param textureID             纹理 ID
         * @param type                  数据类型
         * @param width                 宽
         * @param height                高
         * @param rotation              旋转角度
         * @param timestampNs           时间戳
         * @param transformMatrix       纹理变换矩阵
         *
         * @return int textureID
         */
        @Override
        public int onTextureFrameAvailable(int textureID, QNVideoFrameType type, int width, int height, int rotation, long timestampNs, float[] transformMatrix) {
            if (mGLHandler == null) {
                mGLHandler = new Handler();
            }

            if (mByteDancePlugin.isUsingEffect()) {
                return mByteDancePlugin.drawFrame(
                        textureID, width, height, timestampNs, mProcessTypes,
                        type == QNVideoFrameType.TEXTURE_OES);
            }
            return textureID;
        }
    };

    private void updateProcessTypes() {
        mProcessTypes.clear();
        switch (mTextureRotation) {
            case 90:
                mProcessTypes.add(ProcessType.ROTATE_90);
                break;
            case 180:
                mProcessTypes.add(ProcessType.ROTATE_180);
                break;
            case 270:
                mProcessTypes.add(ProcessType.ROTATE_270);
                break;
            default:
                mProcessTypes.add(ProcessType.ROTATE_0);
                break;
        }
        if (mIsFrontCamera) {
            mProcessTypes.add(ProcessType.FLIPPED_HORIZONTAL);
        }
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
    private DanmuContainerView mDanmuContainerView;
    private GiftView mGiftView;

    private void initChatView() {
        mChatListView = (ListView) findViewById(R.id.chat_list_view);
        mChatListAdapter = new ChatListAdapter(this);
        mChatListView.setAdapter(mChatListAdapter);
        mChatBottomPanel = (BottomPanelFragment) getSupportFragmentManager().findFragmentById(R.id.bottom_bar);

        mHeartLayout = (HeartLayout) findViewById(R.id.heart_layout);

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

        mChatBottomPanel.setInputPanelListener(new InputPanel.InputPanelListener() {
            @Override
            public void onSendClick(String text, int type) {
                final TextMessage content = TextMessage.obtain(text);
                ChatroomKit.sendMessage(content);
                showBottomBtns();
            }
        });
        mChatBottomPanel.setBtnsVisible(View.INVISIBLE);

        // 添加软键盘弹出监听，记录软键盘高度
        addOnSoftKeyBoardVisibleListener(findViewById(R.id.live_room_layout), new SoftInputStatusListener() {
            @Override
            public void onSoftInputStatusChanged(boolean visible, int softInputHeight) {
                if (visible || mChatBottomPanel.isSelectingEmoji()) {
                    mChatBottomPanel.setSoftInputHeight(softInputHeight);
                    mChatBottomPanel.isShowInputAboveKeyboard(true);
                } else {
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
                        mChatBottomPanel.hidePanels();
                        showBottomBtns();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void setChatViewVisible(int visible) {
        mGiftView.setVisibility(visible);
        mChatListView.setVisibility(visible);
        mHeartLayout.setVisibility(visible);
        mDanmuContainerView.setVisibility(visible);
    }

    private void connectIM() {
        UserInfo userInfo = SharedPreferencesUtils.getUserInfo(this);
        String userName = userInfo.getNickName().isEmpty() ? "路人" : userInfo.getNickName();
        String roomName = mModifiedRoomNameText.getText().toString();

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
                mChatRoomInfo = new ChatRoomInfo(mRoomId, roomName, null, DataInterface.getUserId(), 0);

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

    private void streamerSwitchToBackstage() {
        QNImage image = new QNImage(getApplicationContext());
        image.setResourceID(R.drawable.pause_publish);
        mCameraVideoTrack.pushImage(image);
    }

    private void streamerBackToLiving() {
        mCameraVideoTrack.pushImage(null);
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

                    if (messageContent instanceof ChatroomUserQuit) {
                        String senderUserId = msgObj.getSenderUserId();
                        if (TextUtils.equals(senderUserId, mChatRoomInfo.getPubUserId())) {
                            ToastUtils.showLongToast("本次直播结束，感谢观看！");
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
                                                 final SoftInputStatusListener listener) {
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean isVisibleForLast = false;
            private int statusBarHeight = 0;

            @Override
            public void onGlobalLayout() {
                if (statusBarHeight == 0) {
                    int resourceId = getApplicationContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
                    if (resourceId > 0) {
                        statusBarHeight = getApplicationContext().getResources().getDimensionPixelSize(resourceId);
                    }
                }
                Rect rect = new Rect();
                root.getWindowVisibleDisplayFrame(rect);
                // 可见屏幕的高度
                int displayHeight = rect.bottom - rect.top;
                // 屏幕整体的高度
                int height = root.getHeight() - statusBarHeight;
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
            mStreamingStopped = true;
            super.onBackPressed();
        }
        showBottomBtns();
    }
}
