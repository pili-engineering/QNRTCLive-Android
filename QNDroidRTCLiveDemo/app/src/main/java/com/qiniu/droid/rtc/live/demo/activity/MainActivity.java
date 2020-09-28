package com.qiniu.droid.rtc.live.demo.activity;

import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.base.BaseActivity;
import com.qiniu.droid.rtc.live.demo.fragment.HomeFragment;
import com.qiniu.droid.rtc.live.demo.fragment.MineFragment;
import com.qiniu.droid.rtc.live.demo.utils.BarUtils;
import com.qiniu.droid.rtc.live.demo.utils.PermissionChecker;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;

public class MainActivity extends BaseActivity {

    private static final String TAG_HOME = "HOME";
    private static final String TAG_MINE = "MINE";

    private TextView mTvHome;
    private ImageView mIvStartLive;
    private TextView mTvMine;

    private HomeFragment mHomeFragment;
    private MineFragment mMineFragment;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        BarUtils.setStatusBarColor(this, R.color.dark_blue);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        mTvHome = findViewById(R.id.tv_main_home);
        mIvStartLive = findViewById(R.id.iv_main_start_live);
        mTvMine = findViewById(R.id.tv_main_mine);

        showFragment(TAG_HOME);
    }

    @Override
    protected void initEvent() {
        mTvHome.setOnClickListener(this);
        mIvStartLive.setOnClickListener(this);
        mTvMine.setOnClickListener(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_main_home:
                mTvHome.setTextColor(getResources().getColor(R.color.blue));
                mTvMine.setTextColor(Color.BLACK);
                showFragment(TAG_HOME);
                break;
            case R.id.iv_main_start_live:
                if (isPermissionOK()) {
                    startActivity(LiveRoomActivity.class);
                }
                break;
            case R.id.tv_main_mine:
                mTvHome.setTextColor(Color.BLACK);
                mTvMine.setTextColor(getResources().getColor(R.color.blue));
                showFragment(TAG_MINE);
                break;
            default:
                break;
        }
    }

    private Fragment showingFragment() {
        if (mHomeFragment != null && !mHomeFragment.isHidden()) {
            return mHomeFragment;
        } else if (mMineFragment != null && !mMineFragment.isHidden()) {
            return mMineFragment;
        }
        return null;
    }

    private void showFragment(String tag) {
        if (showingFragment() != null) {
            getSupportFragmentManager().beginTransaction().hide(showingFragment()).commit();
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment == null) {
            if (mHomeFragment == null) {
                mHomeFragment = new HomeFragment();
                fragment = mHomeFragment;
            } else if (mMineFragment == null) {
                mMineFragment = new MineFragment();
                fragment = mMineFragment;
            }
            fragmentTransaction.add(R.id.fragment_container, fragment, tag).commit();
        } else {
            fragmentTransaction.show(fragment).commit();
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

}
