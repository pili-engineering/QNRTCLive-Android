package com.qiniu.droid.rtc.live.demo.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.utils.LoadResourcesTask;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;

public class LoadResourcesActivity extends AppCompatActivity implements LoadResourcesTask.ILoadResourcesCallback {

    private static final String DST_FOLDER = "resource";
    private Button mBtStart;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_load_resources);
        initView();
        startTask();
    }

    private void initView() {
        mBtStart = findViewById(R.id.bt_start);
    }

    public void startTask() {
        LoadResourcesTask mTask = new LoadResourcesTask(this);
        mTask.execute(DST_FOLDER);
    }

    @Override
    public void onStartTask() {
        mBtStart.setEnabled(false);
        mBtStart.setText("资源准备中");
    }

    @Override
    public void onEndTask(boolean result) {
        if (result) {
            SharedPreferencesUtils.setResourceReady(this, result);
            Toast.makeText(LoadResourcesActivity.this,"资源准备就绪", Toast.LENGTH_SHORT).show();
            mBtStart.setText("开始");
            mBtStart.setEnabled(true);
            finish();
        } else {
            Toast.makeText(LoadResourcesActivity.this,"资源处理失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

}
