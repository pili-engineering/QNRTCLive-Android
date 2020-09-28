package com.qiniu.droid.rtc.live.demo.utils;

import android.annotation.SuppressLint;
import android.content.Context;

public class AppUtils {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static void init(Context context){
        sContext = context;
        ToastUtils.init(context);
        NetworkUtils.registerNetworkStatusChangedListener();
    }

    public static void release(){
        NetworkUtils.unregisterNetworkStatusChangedListener();
    }

    public static Context getApp(){
        return sContext;
    }
}
