package com.qiniu.droid.rtc.live.demo.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.adapter.FeedbackPictureAdapter;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;
import com.qiniu.droid.rtc.live.demo.utils.BarUtils;
import com.qiniu.droid.rtc.live.demo.utils.FileUtils;
import com.qiniu.droid.rtc.live.demo.utils.QNAppServer;
import com.qiniu.droid.rtc.live.demo.utils.QNUploadManager;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ThreadUtils;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;
import com.qiniu.droid.rtc.live.demo.utils.ZipManager;
import com.qiniu.droid.rtc.live.demo.view.LoadingDialog;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FeedbackActivity";
    private static final int REQUEST_CODE_CHOOSE = 23;
    private static final int MAX_CONTENT_COUNT = 500;

    private RecyclerView mFeedbackPictureRv;
    private EditText mFeedbackEt;
    private TextView mFeedbackContentCountTv;
    private Button mSubmitBtn;
    private ImageButton mBackBtn;

    private FeedbackPictureAdapter mFeedbackPictureAdapter;
    private List<Uri> mFeedbackPictures;

    private QNUploadManager mUploadManager;
    private LoadingDialog mLoadingDialog;

    private UserInfo mUserInfo;
    private String mFeedbackFileDirectory;
    private String mFeedbackAttachmentPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feedback);
        BarUtils.setStatusBarColor(this, R.color.dark_blue);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        mFeedbackContentCountTv = findViewById(R.id.feedback_text_count);
        mFeedbackContentCountTv.setText(String.valueOf(MAX_CONTENT_COUNT));
        mFeedbackEt = findViewById(R.id.feedback_text);
        InputFilter[] filters = {new InputFilter.LengthFilter(MAX_CONTENT_COUNT)};
        mFeedbackEt.setFilters(filters);
        mFeedbackEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mFeedbackContentCountTv.setText(String.valueOf(MAX_CONTENT_COUNT - s.length()));
            }
        });
        mFeedbackPictureRv = findViewById(R.id.feedback_pictures);
        mSubmitBtn = findViewById(R.id.submit_feedback_btn);
        mBackBtn = findViewById(R.id.back_btn);
        mBackBtn.setOnClickListener(this);
        mSubmitBtn.setOnClickListener(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        mFeedbackPictureRv.setLayoutManager(layoutManager);
        mFeedbackPictures = new ArrayList<>();
        mFeedbackPictures.add(getUriByResource(R.mipmap.ic_picture_selection));
        mFeedbackPictureAdapter = new FeedbackPictureAdapter(this, mFeedbackPictures);
        mFeedbackPictureAdapter.setOnPictureOperateListener(() -> {
            if (mFeedbackPictureAdapter.getItemCount() == 4) {
                ToastUtils.showShortToast(getString(R.string.toast_picture_upload_limit));
                return;
            }
            choosePictures();
        });
        mFeedbackPictureRv.setAdapter(mFeedbackPictureAdapter);

        mUploadManager = new QNUploadManager(getApplicationContext());

        mUserInfo = SharedPreferencesUtils.getUserInfo(this);
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && getExternalFilesDir(null) != null) {
            mFeedbackFileDirectory = getExternalFilesDir(null).getPath() + File.separator + "Feedback";
        } else {
            mFeedbackFileDirectory = getFilesDir().getAbsolutePath() + File.separator + "Feedback";
        }
        File feedbackFile = new File(mFeedbackFileDirectory);
        if (!feedbackFile.exists()) {
            feedbackFile.mkdir();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ZipManager.cancel();
        mUploadManager.cancelUpload();
        dismissLoadingDialog();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                finish();
                break;
            case R.id.submit_feedback_btn:
                showLoadingDialog();
                new Thread(() -> {
                    // 压缩反馈附件信息
                    boolean res = zipFeedbackAttachments();
                    if (!res) {
                        dismissLoadingDialog();
                        return;
                    }

                    // 上传附件到云存储
                    res = uploadFeedbackAttachment();
                    if (!res) {
                        dismissLoadingDialog();
                        return;
                    }

                    // 发送反馈信息到服务器
                    File attachment = new File(mFeedbackAttachmentPath);
                    QNAppServer.getInstance().sendFeedbacks(mFeedbackEt.getText().toString().replace("\n", ""), attachment.getName(), new QNAppServer.OnRequestResultCallback() {
                        @Override
                        public void onRequestSuccess(String responseMsg) {
                            attachment.delete();
                            dismissLoadingDialog();
                            ToastUtils.showShortToast("反馈成功！");
                            finish();
                        }

                        @Override
                        public void onRequestFailed(int code, String reason) {
                            attachment.delete();
                            dismissLoadingDialog();
                            ToastUtils.showShortToast("反馈失败！");
                        }
                    });
                }).start();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK && mFeedbackPictureAdapter != null) {
            mFeedbackPictureAdapter.addPictureUri(Matisse.obtainResult(data).get(0));
        }
    }

    private void choosePictures() {
        Matisse.from(FeedbackActivity.this)
                .choose(MimeType.ofImage())
                .showSingleMediaType(true)
                .countable(true)
                .maxSelectable(1)
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(new GlideEngine())
                .showPreview(false) // Default is `true`
                .forResult(REQUEST_CODE_CHOOSE);
    }

    private Uri getUriByResource(int resId) {
        Resources r = getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(resId) + "/"
                + r.getResourceTypeName(resId) + "/"
                + r.getResourceEntryName(resId));
    }

    private String getLogFilePath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && getExternalFilesDir(null) != null) {
            return getExternalFilesDir(null).getPath() + File.separator + "QNRTCLog";
        } else {
            return getFilesDir().getAbsolutePath() + File.separator + "QNRTCLog";
        }
    }

    private boolean zipFeedbackAttachments() {
        ArrayList<File> attachments = new ArrayList<>();
        File logFileDirectory = new File(getLogFilePath());
        File[] files = logFileDirectory.listFiles();
        if (files != null && files.length != 0) {
            attachments.addAll(Arrays.asList(files));
        }
        List<Uri> feedbackPics = mFeedbackPictureAdapter.getFeedbackPictures();
        for (Uri uri : feedbackPics) {
            String filePath = FileUtils.getFilePathByUri(this, uri);
            if (filePath != null) {
                attachments.add(new File(filePath));
            }
        }

        boolean[] res = new boolean[1];
        CountDownLatch countDownLatch = new CountDownLatch(1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        mFeedbackAttachmentPath = mFeedbackFileDirectory + File.separator + mUserInfo.getUserId() + "_" + df.format(new Date()) + ".zip";
        ZipManager.zip(attachments, mFeedbackAttachmentPath, new ZipManager.ZipCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(int percent) {
                Log.i(TAG, "zip progress : " + percent);
            }

            @Override
            public void onFinish(boolean success) {
                Log.i(TAG, "zip result : " + success);
                countDownLatch.countDown();
                res[0] = success;
            }
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    private boolean uploadFeedbackAttachment() {
        boolean[] res = new boolean[1];
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ThreadUtils.getSingleThreadExecutor().execute(() -> QNAppServer.getInstance().getUploadToken(new File(mFeedbackAttachmentPath).getName(), 3600, new QNAppServer.OnRequestResultCallback() {
            @Override
            public void onRequestSuccess(String responseMsg) {
                try {
                    Log.i(TAG, "get upload token success");
                    String token = new JSONObject(responseMsg).optString("token");
                    File attachment = new File(mFeedbackAttachmentPath);
                    if (attachment.exists()) {
                        mUploadManager.uploadFeedbackAttachment(attachment, token, new QNUploadManager.OnUploadFeedbackCallback() {
                            @Override
                            public void onUploadProgress(double percent) {

                            }

                            @Override
                            public void onUploadSuccess() {
                                Log.i(TAG, "upload attachment success");
                                res[0] = true;
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onUploadFailed(String reason) {
                                String errorInfo = "upload attachment failed : " + reason;
                                Log.e(TAG, errorInfo);
                                ToastUtils.showShortToast(errorInfo);
                                res[0] = false;
                                countDownLatch.countDown();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onRequestFailed(int code, String reason) {
                String errorInfo = "get upload token failed : (" + code + " : " + reason + ")";
                Log.e(TAG, errorInfo);
                ToastUtils.showShortToast(errorInfo);
                res[0] = false;
                countDownLatch.countDown();
            }
        }));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return res[0];
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog.Builder(this)
                    .setCancelable(true)
                    .setTipMessage("反馈信息提交中...")
                    .create();
        }
        mLoadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
