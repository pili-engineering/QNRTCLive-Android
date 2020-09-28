package com.qiniu.droid.rtc.live.demo.activity;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.base.BaseActivity;
import com.qiniu.droid.rtc.live.demo.utils.BarUtils;

public class UserAgreementActivity extends BaseActivity {

    private TextView mTvBarTitle;
    private LinearLayout mRlBarBack;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_agreement;
    }

    @Override
    protected void initView() {
        initStatusBar();
        mRlBarBack = findViewById(R.id.rl_bar_back);
        mTvBarTitle = findViewById(R.id.tv_bar_title);
        mTvBarTitle.setText("用户协议");
    }

    private void initStatusBar() {
        BarUtils.setStatusBarColor(this, R.color.blue);
        // 设置状态栏文字颜色及图标为浅色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    protected void initEvent() {
        mRlBarBack.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_bar_back:
                finish();
                break;
            default:
                break;
        }
    }

}
