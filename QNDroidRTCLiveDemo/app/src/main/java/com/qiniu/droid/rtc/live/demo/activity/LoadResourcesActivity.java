package com.qiniu.droid.rtc.live.demo.activity;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.base.BaseActivity;
import com.qiniu.droid.rtc.live.demo.utils.LoadResourcesTask;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ThreadUtils;
import com.qiniu.droid.rtc.live.demo.view.TipProgressBar;

public class LoadResourcesActivity extends BaseActivity implements LoadResourcesTask.ILoadResourcesCallback {

    private static final String DST_FOLDER = "resource";

    private TipProgressBar mTipProgressBar;

    private LoadResourcesTask mLoadResourcesTask;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_load_resources;
    }

    @Override
    protected void initView() {
        mTipProgressBar = findViewById(R.id.progressbar);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {
        mLoadResourcesTask = new LoadResourcesTask(this);
        mLoadResourcesTask.execute(DST_FOLDER);
    }

    @Override
    public void onStartTask() {

    }

    @Override
    public void onProgress(float progress, String currentFileName) {
        ThreadUtils.runOnUiThread(() -> {
            mTipProgressBar.setCurrentProgress(progress);
            mTipProgressBar.setTipText(currentFileName.substring(currentFileName.lastIndexOf("/") + 1));
        });
    }

    @Override
    public void onEndTask(boolean result) {
        if (result) {
            SharedPreferencesUtils.setResourceReady(this, result);
            Toast.makeText(LoadResourcesActivity.this,"资源准备就绪", Toast.LENGTH_SHORT).show();
            startActivity(LiveRoomActivity.class);
            finish();
        } else {
            Toast.makeText(LoadResourcesActivity.this,"资源处理失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadResourcesTask != null) {
            mLoadResourcesTask.cancel(true);
        }
    }
}
