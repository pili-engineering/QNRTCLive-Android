package com.qiniu.droid.rtc.live.demo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.qiniu.droid.rtc.QNRTCEnv;
import com.qiniu.droid.rtc.live.demo.activity.LoginActivity;
import com.qiniu.droid.rtc.live.demo.common.MessageEvent;
import com.qiniu.droid.rtc.live.demo.im.ChatroomKit;
import com.qiniu.droid.rtc.live.demo.im.DataInterface;
import com.qiniu.droid.rtc.live.demo.utils.AppStateTracker;
import com.qiniu.droid.rtc.live.demo.utils.AppUtils;
import com.qiniu.droid.rtc.live.demo.utils.SharedPreferencesUtils;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;
import com.qiniu.droid.rtc.live.demo.utils.Utils;

import de.greenrobot.event.EventBus;

public class RTCLiveApplication extends Application {
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        /*
          init must be called before any other func
         */

        QNRTCEnv.init(getApplicationContext());
        QNRTCEnv.setDnsManager(Utils.getDefaultDnsManager(getApplicationContext()));

        DataInterface.init(this);
        ChatroomKit.init(this, DataInterface.APP_KEY);
        AppUtils.init(this);
        EventBus.getDefault().register(this);

        AppStateTracker.track(this, new AppStateTracker.AppStateChangeListener() {
            @Override
            public void appTurnIntoForeground() {
            }

            @Override
            public void appTurnIntoBackGround() {
            }

            @Override
            public void appDestroyed() {
                EventBus.getDefault().unregister(this);
            }
        });

        sContext = getApplicationContext();
    }

    public void onEventMainThread(MessageEvent messageEvent) {
        ToastUtils.showShortToast(getString(R.string.toast_bad_token));
        SharedPreferencesUtils.clearAccountInfo(AppUtils.getApp());
        Intent loginIntent = new Intent(getContext(), LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(loginIntent);
    }

    public static Context getContext() {
        return sContext;
    }
}
