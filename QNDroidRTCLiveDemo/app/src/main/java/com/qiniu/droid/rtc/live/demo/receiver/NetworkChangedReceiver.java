package com.qiniu.droid.rtc.live.demo.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import androidx.annotation.RequiresPermission;

import com.qiniu.droid.rtc.live.demo.utils.AppUtils;
import com.qiniu.droid.rtc.live.demo.utils.NetworkUtils;
import com.qiniu.droid.rtc.live.demo.utils.NetworkUtils.NetworkType;
import com.qiniu.droid.rtc.live.demo.utils.NetworkUtils.OnNetworkStatusChangedListener;
import com.qiniu.droid.rtc.live.demo.utils.ThreadUtils;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static com.qiniu.droid.rtc.live.demo.utils.NetworkUtils.getNetworkType;

public final class NetworkChangedReceiver extends BroadcastReceiver {

    public static NetworkChangedReceiver getInstance() {
        return LazyHolder.INSTANCE;
    }

    private NetworkType mType;
    private OnNetworkStatusChangedListener mListener;

    @RequiresPermission(ACCESS_NETWORK_STATE)
    public void registerListener(final OnNetworkStatusChangedListener listener) {
        if (listener == null) {
            return;
        }
        mListener = listener;
        ThreadUtils.runOnUiThread(() -> {
            mType = getNetworkType();
            IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            AppUtils.getApp().registerReceiver(NetworkChangedReceiver.getInstance(), intentFilter);
        });
    }

    public void unregisterListener() {
        ThreadUtils.runOnUiThread(() -> {
            AppUtils.getApp().unregisterReceiver(NetworkChangedReceiver.getInstance());
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            // 去除网络抖动
            ThreadUtils.runOnUiThreadDelayed(() -> {
                NetworkUtils.NetworkType networkType = getNetworkType();
                if (mType == networkType) {
                    return;
                }
                mType = networkType;

                if (networkType == NetworkUtils.NetworkType.NETWORK_NO) {
                    mListener.onDisconnected();
                } else {
                    mListener.onConnected(networkType);
                }
            }, 1000);
        }
    }

    private static class LazyHolder {
        private static final NetworkChangedReceiver INSTANCE = new NetworkChangedReceiver();
    }
}