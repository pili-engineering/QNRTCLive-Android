package com.qiniu.droid.rtc.live.demo.activity;

import android.os.Handler;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.base.BaseActivity;
import com.qiniu.droid.rtc.live.demo.model.AccountInfo;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;
import com.qiniu.droid.rtc.live.demo.utils.AppUtils;
import com.qiniu.droid.rtc.live.demo.utils.BarUtils;
import com.qiniu.droid.rtc.live.demo.utils.KeyboardUtils;
import com.qiniu.droid.rtc.live.demo.utils.NetworkUtils;
import com.qiniu.droid.rtc.live.demo.utils.QNAppServer;
import com.qiniu.droid.rtc.live.demo.utils.RegexUtils;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ThreadUtils;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;

import androidx.annotation.NonNull;

public class LoginActivity extends BaseActivity {

    private static final String TAG = "LoginActivity";
    private static final int MESSAGE_VERIFICATION_CODE_COUNTDOWN = 1001;

    private EditText mEtPhone;
    private EditText mEtVerificationCode;
    private TextInputLayout mTextInputLayout;
    private Button mBtRequestVerificationCode;
    private Button mBtLogin;
    private RadioButton mRbUserAgreement;
    private TextView mTvUserAgreement;

    private Handler mHandler;

    private int mVerificationCodeCountdown = 60;
    private boolean mAgreeUserAgreement = true;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        initStatusBar();
        mEtPhone = findViewById(R.id.et_login_phone);
        mEtVerificationCode = findViewById(R.id.et_login_verification_code);
        mTextInputLayout = findViewById(R.id.layout_login_phone);
        mBtRequestVerificationCode = findViewById(R.id.bt_login_request_verification_code);
        mBtLogin = findViewById(R.id.bt_login_login);
        mRbUserAgreement = findViewById(R.id.rb_login_user_agreement);
        mTvUserAgreement = findViewById(R.id.tv_login_user_agreement);
        initUserAgreement();
    }

    private void initStatusBar() {
        BarUtils.transparentStatusBar(this, true);
        // 设置状态栏文字颜色及图标为深色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    @Override
    protected void initEvent() {
        mRbUserAgreement.setOnClickListener(this);
        mBtRequestVerificationCode.setOnClickListener(this);
        mBtLogin.setOnClickListener(this);
        mEtPhone.addTextChangedListener(new PhoneInputWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (RegexUtils.isMobilePhoneNumber(s)) {
                    mTextInputLayout.setError(null);
                    mTextInputLayout.setErrorEnabled(false);
                } else {
                    mTextInputLayout.setError("不正确的手机号码");
                    mTextInputLayout.setErrorEnabled(true);
                }
            }
        });
        mHandler = new Handler(msg -> {
            if (msg.what == MESSAGE_VERIFICATION_CODE_COUNTDOWN) {
                mVerificationCodeCountdown--;
                if (mVerificationCodeCountdown == 0) {
                    mBtRequestVerificationCode.setEnabled(true);
                    mVerificationCodeCountdown = 60;
                    mBtRequestVerificationCode.setText("获取验证码");
                } else {
                    mBtRequestVerificationCode.setEnabled(false);
                    mBtRequestVerificationCode.setText("剩余" + mVerificationCodeCountdown + "秒");
                    mHandler.sendEmptyMessageDelayed(MESSAGE_VERIFICATION_CODE_COUNTDOWN, 1000);
                }
            }
            return true;
        });
    }

    @Override
    protected void initData() {
        AccountInfo accountInfo = SharedPreferencesUtils.getAccountInfo(AppUtils.getApp());
        if (accountInfo != null) {
            QNAppServer.getInstance().setToken(accountInfo.getToken());
            startActivity(MainActivity.class);
            LoginActivity.this.finish();
        }
    }

    private void initUserAgreement() {
        SpannableString spannableString = new SpannableString("登录即表示同意用户协议");
        spannableString.setSpan(new UnderlineSpan(), 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(UserAgreementActivity.class);
            }
        };
        spannableString.setSpan(clickableSpan, 7, 11, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTvUserAgreement.setMovementMethod(LinkMovementMethod.getInstance());
        mTvUserAgreement.setText(spannableString);
    }

    public void requestVerificationCode(String phoneNumber) {
        ThreadUtils.getSingleThreadExecutor().execute(() ->
                QNAppServer.getInstance().sendSmsCode(phoneNumber, new QNAppServer.OnRequestResultCallback() {
            @Override
            public void onRequestSuccess(String responseMsg) {
                Log.i(TAG, "send sms code success");
                ToastUtils.showShortToast("验证码已发送");
            }

            @Override
            public void onRequestFailed(int code, String reason) {
                ToastUtils.showShortToast(reason);
            }
        }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_login_user_agreement:
                // 用户协议
                if (mAgreeUserAgreement) {
                    mRbUserAgreement.setChecked(false);
                    mAgreeUserAgreement = false;
                } else {
                    mRbUserAgreement.setChecked(true);
                    mAgreeUserAgreement = true;
                }
                break;
            case R.id.bt_login_login:
                // 登录
                if (!mRbUserAgreement.isChecked()) {
                    ToastUtils.showShortToast(getString(R.string.toast_user_agreement));
                    return;
                }
                ThreadUtils.getSingleThreadExecutor().execute(() -> QNAppServer.getInstance().login(
                        mEtPhone.getText().toString(), mEtVerificationCode.getText().toString(), new QNAppServer.OnRequestResultCallback() {
                            @Override
                            public void onRequestSuccess(String responseMsg) {
                                AccountInfo accountInfo = new Gson().fromJson(responseMsg, AccountInfo.class);
                                UserInfo userInfo = new Gson().fromJson(responseMsg, UserInfo.class);
                                accountInfo.setUserInfo(userInfo);
                                SharedPreferencesUtils.setAccountInfo(AppUtils.getApp(), accountInfo);
                                startActivity(MainActivity.class);
                                finish();
                            }

                            @Override
                            public void onRequestFailed(int code, String reason) {
                                ToastUtils.showShortToast(getString(R.string.toast_login_failed) + " : " + reason);
                            }
                        }));
                break;
            case R.id.bt_login_request_verification_code:
                // 请求验证码
                if (RegexUtils.isMobilePhoneNumber(mEtPhone.getText())) {
                    if (NetworkUtils.isConnected()) {
                        requestVerificationCode(mEtPhone.getText().toString());
                        mHandler.sendEmptyMessage(MESSAGE_VERIFICATION_CODE_COUNTDOWN);
                    } else {
                        ToastUtils.showShortToast("请检查网络");
                    }
                } else {
                    ToastUtils.showShortToast("手机号码格式不正确");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (KeyboardUtils.isShouldHideKeyboard(view, motionEvent)) {
                KeyboardUtils.hideSoftInput(this);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private abstract static class PhoneInputWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}