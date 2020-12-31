package com.qiniu.droid.rtc.live.demo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.qiniu.droid.rtc.live.demo.BuildConfig;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.activity.FeedbackActivity;
import com.qiniu.droid.rtc.live.demo.activity.LoginActivity;
import com.qiniu.droid.rtc.live.demo.activity.UserAgreementActivity;
import com.qiniu.droid.rtc.live.demo.activity.WebActivity;
import com.qiniu.droid.rtc.live.demo.im.DataInterface;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;
import com.qiniu.droid.rtc.live.demo.utils.AppUtils;
import com.qiniu.droid.rtc.live.demo.utils.PermissionChecker;
import com.qiniu.droid.rtc.live.demo.utils.QNAppServer;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class MineFragment extends Fragment implements View.OnClickListener {

    private static final String HOTLINE = "4008089176";

    private ImageView mIvAvater;
    private TextView mTvName;
    private TextView mTvVersionCode;
    private ImageView mIvEditName;
    private LinearLayout mLlQiniuRtc;
    private LinearLayout mLlUserAgreement;
    private LinearLayout mLlUserFeedback;
    private LinearLayout mLlCall;
    private LinearLayout mLlLogout;

    private UserInfo mUserInfo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        mIvAvater = view.findViewById(R.id.iv_mine_avatar);
        mTvName = view.findViewById(R.id.tv_mine_name);
        mTvVersionCode = view.findViewById(R.id.version_code_text);
        mIvEditName = view.findViewById(R.id.iv_mine_edit_name);
        mLlQiniuRtc = view.findViewById(R.id.ll_mine_qiniu_rtc);
        mLlUserAgreement = view.findViewById(R.id.ll_mine_user_agreement);
        mLlUserFeedback = view.findViewById(R.id.ll_mine_user_feedback);
        mLlCall = view.findViewById(R.id.ll_mine_call);
        mLlLogout = view.findViewById(R.id.ll_mine_logout);
        initEvent();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initEvent() {
        mIvEditName.setOnClickListener(this);
        mLlQiniuRtc.setOnClickListener(this);
        mLlUserAgreement.setOnClickListener(this);
        mLlUserFeedback.setOnClickListener(this);
        mLlCall.setOnClickListener(this);
        mLlLogout.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        initData();
    }

    private void initData() {
        // 加载用户数据
        mUserInfo = SharedPreferencesUtils.getUserInfo(AppUtils.getApp());
        if (mUserInfo != null && !"".equals(mUserInfo.getAvatar())) {
            Glide.with(this)
                    .load(mUserInfo.getAvatar())
                    .centerInside()
                    .into(mIvAvater);
        }
        mTvName.setText(mUserInfo == null ? "" : mUserInfo.getNickName());
        mTvVersionCode.setText(String.format(getString(R.string.version_code_text), BuildConfig.VERSION_NAME));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_mine_edit_name:
                // 修改名字
                View view = View.inflate(getContext(),R.layout.dialog_edit_profile,null);
                EditText editText = view.findViewById(R.id.et_edit_name);
                editText.setText(mUserInfo.getNickName());
                RadioGroup genderRadios = view.findViewById(R.id.gender_radio_group);
                RadioButton maleRadio = view.findViewById(R.id.gender_male);
                RadioButton femaleRadio = view.findViewById(R.id.gender_female);
                if ("male".equals(mUserInfo.getGender())) {
                    maleRadio.setChecked(true);
                } else {
                    femaleRadio.setChecked(true);
                }
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("修改账号信息")
                        .setView(view)
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确认", (dialog, which) -> {
                            String gender = genderRadios.getCheckedRadioButtonId() == R.id.gender_male ? "male" : "female";
                            UserInfo modifiedInfo = new UserInfo(mUserInfo.getUserId(), editText.getText().toString(), gender, mUserInfo.getAvatar());
                            new Thread(() -> QNAppServer.getInstance().updateProfile(modifiedInfo, new QNAppServer.OnRequestResultCallback() {
                                @Override
                                public void onRequestSuccess(String responseMsg) {
                                    UserInfo userInfo = new Gson().fromJson(responseMsg, UserInfo.class);
                                    mUserInfo = userInfo;
                                    SharedPreferencesUtils.updateUserInfoForAccount(AppUtils.getApp(), userInfo);
                                    getActivity().runOnUiThread(() -> mTvName.setText(userInfo.getNickName()));
                                }

                                @Override
                                public void onRequestFailed(int code, String reason) {
                                    ToastUtils.showShortToast(getString(R.string.toast_update_profile_failed));
                                }
                            })).start();
                        })
                        .create()
                        .show();
                break;
            case R.id.ll_mine_qiniu_rtc:
                // 七牛 RTC
                startActivity(new Intent(getContext(), WebActivity.class));
                break;
            case R.id.ll_mine_user_agreement:
                // 用户协议
                startActivity(new Intent(getContext(), UserAgreementActivity.class));
                break;
            case R.id.ll_mine_user_feedback:
                // 用户反馈
                if (isPermissionOK()) {
                    startActivity(new Intent(getContext(), FeedbackActivity.class));
                }
                break;
            case R.id.ll_mine_call:
                // 立即咨询
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + HOTLINE));
                startActivity(intent);
                break;
            case R.id.ll_mine_logout:
                // 登出
                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("确认")
                        .setMessage("确认退出登录")
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确认", (dialog, which) -> new Thread(() -> QNAppServer.getInstance().logout(new QNAppServer.OnRequestResultCallback() {
                            @Override
                            public void onRequestSuccess(String responseMsg) {
                                SharedPreferencesUtils.clearAccountInfo(AppUtils.getApp());
                                DataInterface.clearChatRoomConfig();
                                Intent loginIntent = new Intent(getContext(), LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(loginIntent);
                            }

                            @Override
                            public void onRequestFailed(int code, String reason) {

                            }
                        })).start())
                        .create()
                        .show();
                break;
            default:
                break;
        }
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(getActivity());
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.showShortToast("Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }
}
