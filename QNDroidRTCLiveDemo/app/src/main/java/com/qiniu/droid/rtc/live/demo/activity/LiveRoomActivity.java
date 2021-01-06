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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.orzangleli.xdanmuku.DanmuContainerView;
import com.qiniu.bytedanceplugin.ByteDancePlugin;
import com.qiniu.bytedanceplugin.model.ProcessType;
import com.qiniu.droid.rtc.QNCameraSwitchResultCallback;
import com.qiniu.droid.rtc.QNCaptureVideoCallback;
import com.qiniu.droid.rtc.QNCustomMessage;
import com.qiniu.droid.rtc.QNErrorCode;
import com.qiniu.droid.rtc.QNLocalAudioPacketCallback;
import com.qiniu.droid.rtc.QNRTCEngine;
import com.qiniu.droid.rtc.QNRTCEngineEventListener;
import com.qiniu.droid.rtc.QNRTCSetting;
import com.qiniu.droid.rtc.QNRoomState;
import com.qiniu.droid.rtc.QNSourceType;
import com.qiniu.droid.rtc.QNStatisticsReport;
import com.qiniu.droid.rtc.QNSurfaceView;
import com.qiniu.droid.rtc.QNTrackInfo;
import com.qiniu.droid.rtc.QNTrackKind;
import com.qiniu.droid.rtc.QNVideoFormat;
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
import com.qiniu.droid.rtc.model.QNBackGround;
import com.qiniu.droid.rtc.model.QNForwardJob;
import com.qiniu.droid.rtc.model.QNImage;
import com.qiniu.droid.rtc.model.QNMergeJob;
import com.qiniu.droid.rtc.model.QNMergeTrackOption;
import com.qiniu.droid.rtc.model.QNStretchMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.VideoFrame;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.MessageContent;
import io.rong.message.TextMessage;

import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.INVALID_PARAMETER;
import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.ROOM_IN_PK;
import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.ROOM_NOT_EXIST;
import static com.qiniu.droid.rtc.live.demo.signal.QNSignalErrorCode.ROOM_NOT_IN_PK;

public class LiveRoomActivity extends AppCompatActivity implements QNRTCEngineEventListener, Handler.Callback {
    private static final String TAG = "LiveRoomActivity";
    private static final int JOB_STOP_DELAY_TIME = 5000;

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

    private QNRTCEngine mEngine;
    private QNForwardJob mForwardJob;

    private List<QNTrackInfo> mLocalTrackList = null;
    private List<QNTrackInfo> mRemoteTrackList = null;

    private QNTrackInfo mLocalVideoTrack;
    private QNTrackInfo mLocalAudioTrack;

    private LiveSettingFragment mLiveSettingFragment;
    private PkCandidatesFragment mPkCandidatesFragment;
    private PkParticipantsFragment mPkParticipantsFragment;
    private Handler mMainHandler;
    private Handler mSubThreadHandler;

    private String mRoomId;
    private String mRoomToken;
    private volatile boolean mIsForwardJobStreaming;
    private volatile boolean mIsMergeJobStreaming;
    private volatile boolean mIsLocalTracksMerged;
    private volatile boolean mIsRemoteTracksMerged;
    private boolean mReturnOriginalRoom;
    private volatile boolean mNeedResetFlashlight = false;
    private volatile boolean mIsMicrophoneOn = true;
    private volatile boolean mIsSpeakerOn = true;

    private int mSerialNum;

    private QNRoomState mCurrentRoomState = QNRoomState.IDLE;
    private UserInfo mUserInfo;

    // PK 相关
    private QNMergeJob mQNMergeJob;
    private List<QNMergeTrackOption> mMergeTrackOptions;
    private String mMergeJobId = null;
    private List<UserInfo> mPkUserList;
    private UserInfo mPkRequesterInfo;
    private String mPkRoomId;
    private String mTargetPkRoomToken;
    private volatile boolean mIsPkAccepted;
    private volatile boolean mIsPkMode;
    private boolean mIsPkRequester = false;
    private RoomInfo mTargetPkRoomInfo;

    // IM signal
    private QNIMSignalClient mSignalClient;

    private ScheduledExecutorService mExecutor;
    private Semaphore captureStoppedSem = new Semaphore(1);
    private boolean mStreamingStopped;
    private Toast mAudienceNumberToast;

    private final Runnable mAudienceNumGetter = new Runnable() {
        @Override
        public void run() {
            if (mRoomId != null && (mIsForwardJobStreaming || mIsMergeJobStreaming) && NetworkUtils.isConnected()) {
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

        mUserInfo = SharedPreferencesUtils.getUserInfo(AppUtils.getApp());
        mMainHandler = new Handler();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mSubThreadHandler = new Handler(handlerThread.getLooper());

        initViews();
        initStatusBar();
        initNavigationBar();
        // 初始化 QNRTCEngine
        initQNRTCEngine();
        // 初始化本地发布 track
        initLocalTrackInfoList();
        // 初始化字节跳动特效
        initByteDanceEffect();
        // 初始化 IM 控件
        initChatView();

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
        mEngine.stopCapture();
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

        if (mIsForwardJobStreaming && mForwardJob != null) {
            mEngine.stopForwardJob(mForwardJob.getForwardJobId());
            mForwardJob = null;
            mIsForwardJobStreaming = false;
        }
        if (mIsMergeJobStreaming) {
            Log.i(TAG, "stop merge stream : " + mMergeJobId);
            mEngine.stopMergeStream(mMergeJobId);
            mIsMergeJobStreaming = false;
        }
        mEngine.leaveRoom();
        mEngine.destroy();
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
        if (!mIsPkMode) {
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
        if (mEngine != null) {
            mEngine.switchCamera(new QNCameraSwitchResultCallback() {
                @Override
                public void onCameraSwitchDone(boolean isFrontCamera) {
                    mIsFrontCamera = isFrontCamera;
                    mNeedResetFlashlight = true;
                    updateProcessTypes();
                }

                @Override
                public void onCameraSwitchError(String errorMessage) {
                }
            });
        }
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

    /**
     * 初始化视图
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
     * 初始化 QNRTCEngine
     */
    private void initQNRTCEngine() {
        // 1. VideoPreviewFormat 和 VideoEncodeFormat 建议保持一致
        // 2. 如果远端连麦出现回声的现象，可以通过配置 setLowAudioSampleRateEnabled(true) 或者 setAEC3Enabled(true) 后再做进一步测试，并将设备信息反馈给七牛技术支持
        QNVideoFormat format = new QNVideoFormat(Config.STREAMING_HEIGHT, Config.STREAMING_WIDTH, Config.STREAMING_FPS);
        QNRTCSetting setting = new QNRTCSetting();
        setting.setCameraID(QNRTCSetting.CAMERA_FACING_ID.FRONT)
                .setHWCodecEnabled(false)
                .setMaintainResolution(true)
                .setVideoBitrate(Config.STREAMING_BITRATE)
                .setVideoEncodeFormat(format)
                .setVideoPreviewFormat(format);
        mEngine = QNRTCEngine.createEngine(getApplicationContext(), setting, this);
        mEngine.setCapturePreviewWindow(mLocalVideoSurfaceView);
        mEngine.setCaptureVideoCallBack(mCaptureVideoCallback);
        mEngine.muteLocalAudio(!mIsMicrophoneOn);
        mEngine.muteRemoteAudio(!mIsSpeakerOn);
    }

    private void startCaptureAfterAcquire() {
        try {
            captureStoppedSem.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mEngine.startCapture();
    }

    /**
     * 初始化本地音视频 track
     * 关于 Track 的概念介绍 https://doc.qnsdk.com/rtn/android/docs/preparation#5
     */
    private void initLocalTrackInfoList() {
        mLocalTrackList = new ArrayList<>();
        // 创建本地音频 track
        mLocalAudioTrack = mEngine.createTrackInfoBuilder()
                .setSourceType(QNSourceType.AUDIO)
                .setMaster(true)
                .create();
        mEngine.setLocalAudioPacketCallback(mLocalAudioTrack, new QNLocalAudioPacketCallback() {
            @Override
            public int onPutExtraData(ByteBuffer extraData, int extraDataMaxSize) {
                return 0;
            }

            @Override
            public int onSetMaxEncryptSize(int frameSize) {
                return 0;
            }

            @Override
            public int onEncrypt(ByteBuffer frame, int frameSize, ByteBuffer encryptedFrame) {
                return 0;
            }
        });
        mLocalTrackList.add(mLocalAudioTrack);

        // 创建 Camera 采集的视频 Track
        mLocalVideoTrack = mEngine.createTrackInfoBuilder()
                .setSourceType(QNSourceType.VIDEO_CAMERA)
                .setMaster(true)
                .create();
        mLocalTrackList.add(mLocalVideoTrack);
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
        List<RoomInfo> liveRoomsCanPk = getLiveRoomsCanPk();
        if (mPkCandidatesFragment == null) {
            mPkCandidatesFragment = new PkCandidatesFragment(liveRoomsCanPk);
            mPkCandidatesFragment.setOnPkCandidateRoomClickListener(roomInfo -> {
                mTargetPkRoomInfo = roomInfo;
                if (mSignalClient != null) {
                    mSignalClient.startPk(roomInfo.getId());
                } else {
                    ToastUtils.showShortToast(getString(R.string.toast_can_not_start_pk));
                }
                mPkCandidatesFragment.dismiss();
            });
        } else {
            mPkCandidatesFragment.updateCandidateRooms(liveRoomsCanPk);
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
                    if (mEngine == null) {
                        return;
                    }
                    if (mSignalClient != null) {
                        mSignalClient.endPk(mPkRoomId);
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

    private void createForwardJob() {
        if (mForwardJob == null) {
            mForwardJob = new QNForwardJob();
            mForwardJob.setForwardJobId(String.format(getString(R.string.forward_job_id), mUserInfo.getUserId()));
            mForwardJob.setPublishUrl(String.format(getString(R.string.publish_url), mUserInfo.getUserId(), mSerialNum++));
            mForwardJob.setAudioTrack(mLocalAudioTrack);
            mForwardJob.setVideoTrack(mLocalVideoTrack);
            mForwardJob.setInternalForward(true);
        }
        Log.i(TAG, "create forward job : " + mForwardJob.getForwardJobId());
        mEngine.createForwardJob(mForwardJob);
    }

    private void createMergeJob() {
        // 创建合流任务对象
        if (mQNMergeJob == null) {
            mQNMergeJob = new QNMergeJob();
        }
        // 设置合流任务 id，该 id 为合流任务的唯一标识符
        mQNMergeJob.setMergeJobId(String.format(getString(R.string.merge_job_id), mUserInfo.getUserId()));
        // 设置合流任务的推流地址，该场景下需保持一致
        mQNMergeJob.setPublishUrl(String.format(getString(R.string.publish_url), mUserInfo.getUserId(), mSerialNum++));
        mQNMergeJob.setWidth(Config.STREAMING_WIDTH);
        mQNMergeJob.setHeight(Config.STREAMING_HEIGHT);
        // QNMergeJob 中码率单位为 bps，所以，若期望码率为 1200kbps，则实际传入的参数值应为 1200 * 1000
        mQNMergeJob.setBitrate(Config.STREAMING_BITRATE);
        mQNMergeJob.setFps(Config.STREAMING_FPS);
        mQNMergeJob.setStretchMode(QNStretchMode.ASPECT_FILL);

        QNBackGround qnBackGround = new QNBackGround();
        qnBackGround.setFile(Config.STREAMING_BACKGROUND);
        qnBackGround.setX(0);
        qnBackGround.setY(0);
        qnBackGround.setH(Config.STREAMING_HEIGHT);
        qnBackGround.setW(Config.STREAMING_WIDTH);
        mQNMergeJob.setBackground(qnBackGround);
        // 创建合流任务
        mEngine.createMergeJob(mQNMergeJob);
    }

    private void setMergeOptions(List<QNTrackInfo> trackInfoList, boolean isRemote) {
        for (QNTrackInfo info : trackInfoList) {
            QNMergeTrackOption option = new QNMergeTrackOption();
            option.setTrackId(info.getTrackId());
            if (info.isVideo()) {
                option.setX(isRemote ? Config.STREAMING_WIDTH / 2 : 0);
                option.setY(Config.STREAMING_HEIGHT * 4 / 23);
                option.setZ(0);
                option.setWidth(Config.STREAMING_WIDTH / 2);
                option.setHeight(Config.STREAMING_HEIGHT / 2);
            }
            if (mMergeTrackOptions == null) {
                mMergeTrackOptions = new ArrayList<>();
            }
            mMergeTrackOptions.add(option);
        }
    }

    private void relayoutLocalSurfaceView(boolean isPkMode) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(mConstraintLayout);
        if (isPkMode) {
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.BOTTOM, R.id.pk_bottom_divider, ConstraintSet.TOP);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.TOP, R.id.pk_top_divider, ConstraintSet.BOTTOM);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.END, R.id.background_divider, ConstraintSet.START);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            if (mRemoteVideoSurfaceView != null && mRemoteVideoSurfaceView.getVisibility() == View.GONE) {
                mRemoteVideoSurfaceView.setVisibility(View.VISIBLE);
            }
        } else {
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(mLocalVideoSurfaceView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
            if (mRemoteVideoSurfaceView != null && mRemoteVideoSurfaceView.getVisibility() == View.VISIBLE) {
                mRemoteVideoSurfaceView.setVisibility(View.GONE);
            }
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
        if (isFinishing()) {
            return;
        }
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

        acceptBtn.setOnClickListener(v1 -> {
            if (mEngine == null || mSignalClient == null) {
                return;
            }
            if (mPkCandidatesFragment != null && !mPkCandidatesFragment.isHidden()) {
                mPkCandidatesFragment.dismiss();
            }
            mIsPkAccepted = true;
            mSignalClient.answerPk(info.getRoomId(), mIsPkAccepted);
            mPkRequestDialog.dismiss();
        });
        refuseBtn.setOnClickListener(arg0 -> {
            if (mSignalClient == null) {
                return;
            }
            mIsPkAccepted = false;
            mSignalClient.answerPk(info.getRoomId(), mIsPkAccepted);
            mPkRequestDialog.dismiss();
        });
    }

    private void showBeRefusedDialog() {
        if (isFinishing()) {
            return;
        }
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
        if (isFinishing()) {
            return;
        }
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
        if (isFinishing()) {
            return;
        }
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
        if (mEngine != null) {
            mEngine.joinRoom(roomToken);
        }
    }

    private void rejoinSelfRoom() {
        mSubThreadHandler.post(() -> {
            if (mEngine == null) {
                return;
            }
            QNAppServer.getInstance().refreshRoom(mRoomId, new QNAppServer.OnRequestResultCallback() {
                @Override
                public void onRequestSuccess(String responseMsg) {
                    try {
                        JSONObject jsonObject = new JSONObject(responseMsg);
                        mRoomId = jsonObject.optString(Constants.KEY_ROOM_ID);
                        mRoomToken = jsonObject.optString(Constants.KEY_ROOM_TOKEN);
                        if (mEngine != null) {
                            mEngine.joinRoom(mRoomToken);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onRequestFailed(int code, String reason) {

                }
            });
        });
    }

    private void joinRoomWithResponseInfo(String responseMsg) {
        parseJoinRoomInfo(responseMsg);
        // 加入 RTC 房间
        joinRoom(mRoomToken);

        connectIM();
        mMainHandler.post(() -> setChatViewVisible(View.VISIBLE));
    }

    private void handleEndPk() {
        if (!mIsPkMode) {
            return;
        }
        mIsPkMode = false;
        if (mIsPkRequester) {
            // 1. PK 发起者首先要停止合流转推
            if (mQNMergeJob != null) {
                Log.i(TAG, "停止合流任务：" + mMergeJobId);
                mEngine.stopMergeStream(mMergeJobId, JOB_STOP_DELAY_TIME);
                mMergeJobId = null;
                mQNMergeJob = null;
            }
            // 2. PK 发起者要离开房间
            mEngine.leaveRoom();
            // 3. 回到自己房间
            mReturnOriginalRoom = true;
        } else {
            // 创建单路转推任务
            createForwardJob();
            mPkUserList.remove(mPkRequesterInfo);
        }
        mMainHandler.post(() -> {
            if (mPkParticipantsFragment != null && mPkParticipantsFragment.isVisible()) {
                mPkParticipantsFragment.dismiss();
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
                    if (mEngine != null) {
                        mIsMicrophoneOn = isOn;
                        mEngine.muteLocalAudio(!mIsMicrophoneOn);
                    }
                }

                @Override
                public void onSpeakerSettingChanged(boolean isOn) {
                    if (mEngine != null) {
                        mIsSpeakerOn = isOn;
                        mEngine.muteRemoteAudio(!mIsSpeakerOn);
                    }
                }

                @Override
                public void onFlashlightSettingChanged(boolean isOn) {
                    if (mEngine == null) {
                        return;
                    }
                    if (isOn) {
                        mEngine.turnLightOn();
                    } else {
                        mEngine.turnLightOff();
                    }
                }
            });
        }
        if (getSupportFragmentManager().findFragmentByTag(LiveSettingFragment.TAG) == null) {
            mLiveSettingFragment.show(getSupportFragmentManager(), LiveSettingFragment.TAG);
        }
    }

    @Override
    public void onRoomStateChanged(QNRoomState qnRoomState) {
        Log.i(TAG, "onRoomStateChanged : " + qnRoomState.name());
        mCurrentRoomState = qnRoomState;
        switch (qnRoomState) {
            case CONNECTED:
                if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                    mLoadingDialog.dismiss();
                }
                if (mExecutor == null || mExecutor.isShutdown()) {
                    mExecutor = Executors.newSingleThreadScheduledExecutor();
                    mExecutor.scheduleAtFixedRate(mAudienceNumGetter, 0, Config.GET_AUDIENCE_NUM_PERIOD, TimeUnit.SECONDS);
                }
                mEngine.publishTracks(mLocalTrackList);
                updateUIAfterLiving();
                if (mIsPkMode) {
                    // PK 场景下需要创建合流任务并重新进行推流
                    createMergeJob();
                    mPkUserList.add(mTargetPkRoomInfo.getCreator());
                }
                break;
            case RECONNECTING:
                ToastUtils.showShortToast(getString(R.string.toast_reconnecting));
                if (mForwardJob != null) {
                    mForwardJob = null;
                }
                if (mQNMergeJob != null) {
                    mQNMergeJob = null;
                }
                break;
            case RECONNECTED:
                ToastUtils.showShortToast(getString(R.string.toast_reconnected));
                if (mIsPkMode) {
                    createMergeJob();
                } else {
                    createForwardJob();
                }
                break;
        }
    }

    @Override
    public void onRoomLeft() {
        Log.i(TAG, "onRoomLeft");
        mMergeTrackOptions.clear();
        if (mIsPkMode) {
            joinRoom(mTargetPkRoomToken);
        }
        if (mReturnOriginalRoom) {
            mReturnOriginalRoom = false;
            mPkUserList.remove(mTargetPkRoomInfo.getCreator());
            relayoutLocalSurfaceView(false);
            updateBtnsSources(false);
            rejoinSelfRoom();
        }
    }

    @Override
    public void onRemoteUserJoined(String remoteUserId, String userData) {
        Log.i(TAG, "onRemoteUserJoined : " + remoteUserId);
    }

    @Override
    public void onRemoteUserReconnecting(String remoteUserId) {
        ToastUtils.showShortToast("远端用户正在重连");
    }

    @Override
    public void onRemoteUserReconnected(String remoteUserId) {
        ToastUtils.showShortToast("远端用户已重新连接");
    }

    @Override
    public void onRemoteUserLeft(String remoteUserId) {
        Log.i(TAG, "onRemoteUserLeft : " + remoteUserId);
        updateBtnsSources(false);
        relayoutLocalSurfaceView(false);
    }

    @Override
    public void onLocalPublished(List<QNTrackInfo> trackInfoList) {
        Log.i(TAG, "onLocalPublished");
        if (mEngine != null) {
            mEngine.enableStatistics();
            // 预先对本地合流布局进行配置，以应对需要 PK 的场景
            setMergeOptions(trackInfoList, false);
            if (mIsPkMode) {
                if (mMergeJobId != null) {
                    mEngine.setMergeStreamLayouts(mMergeTrackOptions, mMergeJobId);
                    mIsLocalTracksMerged = true;
                }
            } else {
                createForwardJob();
            }
        }
    }

    @Override
    public void onRemotePublished(String remoteUserId, List<QNTrackInfo> list) {
        Log.i(TAG, "onRemotePublished : " + remoteUserId);
        if (mEngine != null) {
            mEngine.subscribeTracks(list);
            mRemoteTrackList = new ArrayList<>(list);
            setMergeOptions(list, true);
            if (mMergeJobId != null) {
                mEngine.setMergeStreamLayouts(mMergeTrackOptions, mMergeJobId);
                mIsRemoteTracksMerged = true;
            }
        }
    }

    @Override
    public void onRemoteUnpublished(String remoteUserId, List<QNTrackInfo> list) {
        Log.i(TAG, "onRemoteUnpublished : " + remoteUserId);
        if (mRemoteTrackList != null) {
            mRemoteTrackList.removeAll(list);
        }
    }

    @Override
    public void onRemoteUserMuted(String remoteUserId, List<QNTrackInfo> list) {
        Log.i(TAG, "onRemoteUserMuted : " + remoteUserId);
    }

    @Override
    public void onSubscribed(String remoteUserId, List<QNTrackInfo> list) {
        Log.i(TAG, "onSubscribed : " + remoteUserId);
        for (QNTrackInfo trackInfo : list) {
            if (trackInfo.isVideo()) {
                mEngine.setRenderWindow(trackInfo, mRemoteVideoSurfaceView);
            }
        }
        relayoutLocalSurfaceView(mIsPkMode);
        updateBtnsSources(mIsPkMode);
    }

    @Override
    public void onSubscribedProfileChanged(String remoteUserId, List<QNTrackInfo> list) {

    }

    @Override
    public void onKickedOut(String userId) {
        Log.i(TAG, "onKickedOut : " + userId);
    }

    @Override
    public void onStatisticsUpdated(QNStatisticsReport report) {
        if (report.userId == null || report.userId.equals(mUserInfo.getUserId())) {
            if (QNTrackKind.AUDIO.equals(report.trackKind)) {
                Log.i(TAG, "\n音频码率:" + report.audioBitrate / 1000 + "kbps \n" +
                        "音频丢包率:" + report.audioPacketLostRate);
            } else if (QNTrackKind.VIDEO.equals(report.trackKind)) {
                Log.i(TAG, "\n视频码率:" + report.videoBitrate / 1000 + "kbps \n" +
                        "视频丢包率:" + report.videoPacketLostRate + " \n" +
                        "视频的宽:" + report.width + " \n" +
                        "视频的高:" + report.height + " \n" +
                        "视频的帧率:" + report.frameRate);
            }
        }
    }

    @Override
    public void onRemoteStatisticsUpdated(List<QNStatisticsReport> reports) {
        for (QNStatisticsReport report : reports) {
            int lost = report.trackKind.equals(QNTrackKind.VIDEO) ? report.videoPacketLostRate : report.audioPacketLostRate;
            Log.i(TAG, "remote user " + report.userId
                    + " rtt " + report.rtt
                    + " grade " + report.networkGrade
                    + " track " + report.trackId
                    + " kind " + (report.trackKind.name())
                    + " lostRate " + lost);
        }
    }

    @Override
    public void onAudioRouteChanged(QNAudioDevice qnAudioDevice) {

    }

    @Override
    public void onCreateMergeJobSuccess(String mergeJobId) {
        Log.i(TAG, "合流任务创建成功：" + mergeJobId + " url = " + mQNMergeJob.getPublishUrl());
        mMergeJobId = mergeJobId;
        // PK 请求接受方停止单路转推
        if (!mIsPkRequester && mForwardJob != null) {
            Log.i(TAG, "停止转推任务：" + mForwardJob.getForwardJobId());
            mEngine.stopForwardJob(mForwardJob.getForwardJobId(), JOB_STOP_DELAY_TIME);
            mIsForwardJobStreaming = false;
            mForwardJob = null;
        }

        // 发布配置本地合流布局
        if (!mIsLocalTracksMerged && mLocalTrackList != null) {
            mEngine.setMergeStreamLayouts(mMergeTrackOptions, mMergeJobId);
            mIsLocalTracksMerged = true;
        }
        // 发布配置远端合流布局
        if (!mIsRemoteTracksMerged && mRemoteTrackList != null) {
            mEngine.setMergeStreamLayouts(mMergeTrackOptions, mMergeJobId);
            mIsRemoteTracksMerged = true;
        }
        mIsMergeJobStreaming = true;
    }

    @Override
    public void onCreateForwardJobSuccess(String forwardJobId) {
        ToastUtils.showShortToast("转推任务创建成功：" + forwardJobId);
        Log.i(TAG, "转推任务创建成功：" + forwardJobId + " url = " + mForwardJob.getPublishUrl());
        mIsForwardJobStreaming = true;
        // PK 请求接受方停止合流转推
        if (!mIsPkRequester && mQNMergeJob != null) {
            Log.i(TAG, "停止合流任务：" + mMergeJobId);
            mEngine.stopMergeStream(mMergeJobId, JOB_STOP_DELAY_TIME);
            mIsPkMode = false;
            mMergeJobId = null;
            mQNMergeJob = null;
        }
        mIsPkRequester = false;
    }

    @Override
    public void onError(int errorCode, String description) {
        /**
         * 关于错误异常的相关处理，都应在该回调中完成; 需要处理的错误码及建议处理逻辑如下:
         *
         *【TOKEN 相关】
         * 1. QNErrorCode.ERROR_TOKEN_INVALID 和 QNErrorCode.ERROR_TOKEN_ERROR 表示您提供的房间 token 不符合七牛 token 签算规则,
         *    详情请参考【服务端开发说明.RoomToken 签发服务】https://doc.qnsdk.com/rtn/docs/server_overview#1
         * 2. QNErrorCode.ERROR_TOKEN_EXPIRED 表示您的房间 token 过期, 需要重新生成 token 再加入；
         *
         *【房间设置相关】以下情况可以与您的业务服务开发确认具体设置
         * 1. QNErrorCode.ERROR_ROOM_FULL 当房间已加入人数超过每个房间的人数限制触发；请确认后台服务的设置；
         * 2. QNErrorCode.ERROR_PLAYER_ALREADY_EXIST 后台如果配置为开启【禁止自动踢人】,则同一用户重复加入/未正常退出再加入会触发此错误，您的业务可根据实际情况选择配置；
         * 3. QNErrorCode.ERROR_NO_PERMISSION 用户对于特定操作，如合流需要配置权限，禁止出现未授权的用户操作；
         * 4. QNErrorCode.ERROR_ROOM_CLOSED 房间已被管理员关闭；
         *
         *【其他错误】
         * 1. QNErrorCode.ERROR_AUTH_FAIL 服务验证时出错，可能为服务网络异常。建议重新尝试加入房间；
         * 2. QNErrorCode.ERROR_PUBLISH_FAIL 发布失败, 会有如下3种情况:
         * 1 ）请确认成功加入房间后，再执行发布操作
         * 2 ）请确定对于音频/视频 Track，分别最多只能有一路为 master
         * 3 ）请确认您的网络状况是否正常
         * 3. QNErrorCode.ERROR_RECONNECT_TOKEN_ERROR 内部重连后出错，一般出现在网络非常不稳定时出现，建议提示用户并尝试重新加入房间；
         * 4. QNErrorCode.ERROR_INVALID_PARAMETER 服务交互参数错误，请在开发时注意合流、踢人动作等参数的设置。
         * 5. QNErrorCode.ERROR_DEVICE_CAMERA 系统摄像头错误, 建议提醒用户检查
         */
        switch (errorCode) {
            case QNErrorCode.ERROR_TOKEN_INVALID:
            case QNErrorCode.ERROR_TOKEN_ERROR:
                ToastUtils.showShortToast("roomToken 错误，请检查后重新生成，再加入房间");
                break;
            case QNErrorCode.ERROR_TOKEN_EXPIRED:
                ToastUtils.showShortToast("roomToken 过期");
                rejoinSelfRoom();
                break;
            case QNErrorCode.ERROR_ROOM_FULL:
                ToastUtils.showShortToast("房间人数已满!");
                break;
            case QNErrorCode.ERROR_PLAYER_ALREADY_EXIST:
                ToastUtils.showShortToast("不允许同一用户重复加入");
                break;
            case QNErrorCode.ERROR_NO_PERMISSION:
                ToastUtils.showShortToast("请检查用户权限:" + description);
                break;
            case QNErrorCode.ERROR_INVALID_PARAMETER:
                ToastUtils.showShortToast("请检查参数设置:" + description);
                break;
            case QNErrorCode.ERROR_PUBLISH_FAIL: {
                if (mEngine.getRoomState() != QNRoomState.CONNECTED
                        && mEngine.getRoomState() != QNRoomState.RECONNECTED) {
                    ToastUtils.showShortToast("发布失败，请加入房间发布: " + description);
                    joinRoom(mRoomToken);
                } else {
                    ToastUtils.showShortToast("发布失败: " + description);
                    mEngine.publishTracks(mLocalTrackList);
                }
            }
            break;
            case QNErrorCode.ERROR_RECONNECT_TOKEN_ERROR:
                showReconnectFailedDialog();
                break;
            case QNErrorCode.ERROR_ROOM_CLOSED:
                disconnectWithErrorMessage("房间被关闭");
                break;
            case QNErrorCode.ERROR_DEVICE_CAMERA:
                ToastUtils.showShortToast("请检查摄像头权限，或者被占用");
                break;
            default:
                ToastUtils.showShortToast("errorCode:" + errorCode + " description:" + description);
                break;
        }
    }

    @Override
    public void onMessageReceived(QNCustomMessage qnCustomMessage) {

    }

    private OnSignalClientListener mOnSignalClientListener = new OnSignalClientListener() {
        @Override
        public void onPkRequestLaunched(PkRequestInfo requestInfo) {
            mPkRequesterInfo = new UserInfo(requestInfo.getUserId(), requestInfo.getNickName(), "", "");
            showPkRequestDialog(requestInfo);
        }

        @Override
        public void onReplyPkSuccess() {
            if (mIsPkAccepted) {
                // 处理 PK 请求成功后的回调
                // 作为 PK 请求的接受者，pkRoomId 即为自己的 roomId
                mPkRoomId = mRoomId;
                mPkUserList.add(mPkRequesterInfo);
                mIsPkMode = true;
                mIsPkRequester = false;
                createMergeJob();
            }
            mIsPkAccepted = false;
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
        public void onPkRequestHandled(boolean isAccepted, String pkRoomId, String roomToken) {
            if (isAccepted) {
                // PK 请求成功被远端接受后回调处理
                if (mEngine == null) {
                    return;
                }
                mTargetPkRoomToken = roomToken;
                mPkRoomId = pkRoomId;
                // 停止当前房间的单路转推任务
                if (mForwardJob != null) {
                    mEngine.stopForwardJob(mForwardJob.getForwardJobId(), JOB_STOP_DELAY_TIME);
                    mForwardJob = null;
                    mIsForwardJobStreaming = false;
                }
                // 离开当前房间，并在 onRoomLeft() 回调中加入到 PK 主播的房间
                mEngine.leaveRoom();
                mIsPkRequester = true;
                mIsPkMode = true;
            } else {
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
        if (mCurrentRoomState != QNRoomState.IDLE) {
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

    private QNCaptureVideoCallback mCaptureVideoCallback = new QNCaptureVideoCallback() {
        @Override
        public void onCaptureStarted() {
            mByteDancePlugin.init(mEffectResourcePath);
            // 由于切换到后台时会 destroy mByteDancePlugin，所以从后台切回来的时候需要先恢复特效
            mByteDancePlugin.recoverEffects();
            updateProcessTypes();
        }

        @Override
        public void onRenderingFrame(VideoFrame.TextureBuffer textureBuffer, long timestampNs) {
            if (mGLHandler == null) {
                mGLHandler = new Handler();
            }

            if (mByteDancePlugin.isUsingEffect()) {
                boolean isOES = textureBuffer.getType() == VideoFrame.TextureBuffer.Type.OES;
                int newTexture = mByteDancePlugin.drawFrame(textureBuffer.getTextureId(), textureBuffer.getWidth(), textureBuffer.getHeight(), timestampNs, mProcessTypes, isOES);
                if (newTexture != textureBuffer.getTextureId()) {
                    textureBuffer.setType(VideoFrame.TextureBuffer.Type.RGB);
                    textureBuffer.setTextureId(newTexture);
                }
            }
        }

        @Override
        public void onPreviewFrame(byte[] data, int width, int height, int rotation, int fmt, long timestampNs) {
            if (mTextureRotation != rotation) {
                mTextureRotation = rotation;
                updateProcessTypes();
            }
        }

        @Override
        public void onCaptureStopped() {
            //停止连麦或者切到后台时候会调用
            mByteDancePlugin.destroy();
            mTextureRotation = 0;
            mIsFrontCamera = true;
            captureStoppedSem.release();
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
        image.setResourceId(R.drawable.pause_publish);
        mEngine.pushCameraTrackWithImage(image);
    }

    private void streamerBackToLiving() {
        mEngine.pushCameraTrackWithImage(null);
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
