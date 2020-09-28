package com.qiniu.droid.rtc.live.demo.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import com.qiniu.droid.rtc.live.demo.model.AccountInfo;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;

public class SharedPreferencesUtils {

    public static boolean resourceReady(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        boolean resourceReady = preferences.getBoolean("resource", false);
        int preVersioncode = preferences.getInt("versionCode", 0);
        return resourceReady && getVersionCode(context) == preVersioncode;
    }

    public static void setResourceReady(Context context, boolean isReady) {
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("resource", isReady);
        editor.putInt("versionCode", getVersionCode(context));
        editor.apply();
    }

    private static int getVersionCode(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Description: 设置和获取用户名
     */
    public static void setAccountInfo(Context context, AccountInfo accountInfo){
        SharedPreferences sp = context.getSharedPreferences(Config.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.KEY_USER_ID, accountInfo.getUserInfo().getUserId());
        editor.putString(Config.KEY_NICK_NAME, accountInfo.getUserInfo().getNickName());
        editor.putString(Config.KEY_GENDER, accountInfo.getUserInfo().getGender());
        editor.putString(Config.KEY_AUTH_TOKEN, accountInfo.getToken());
        editor.apply();
    }

    public static AccountInfo getAccountInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        String userId = sp.getString(Config.KEY_USER_ID, null);
        if (userId == null) {
            return null;
        }
        String nickName = sp.getString(Config.KEY_NICK_NAME, null);
        String gender = sp.getString(Config.KEY_GENDER, null);
        String token = sp.getString(Config.KEY_AUTH_TOKEN, null);
        return new AccountInfo(new UserInfo(userId, nickName, gender), token);
    }

    public static void updateUserInfoForAccount(Context context, UserInfo userInfo) {
        SharedPreferences sp = context.getSharedPreferences(Config.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Config.KEY_USER_ID, userInfo.getUserId());
        editor.putString(Config.KEY_NICK_NAME, userInfo.getNickName());
        editor.putString(Config.KEY_GENDER, userInfo.getGender());
        editor.apply();
    }

    public static UserInfo getUserInfo(Context context) {
        AccountInfo accountInfo = getAccountInfo(context);
        if (accountInfo != null) {
            return accountInfo.getUserInfo();
        }
        return null;
    }

    public static void clearAccountInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Config.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }
}
