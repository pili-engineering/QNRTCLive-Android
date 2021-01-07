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
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.KEY_USER_ID, accountInfo.getUserInfo().getUserId());
        editor.putString(Constants.KEY_NICK_NAME, accountInfo.getUserInfo().getNickName());
        editor.putString(Constants.KEY_GENDER, accountInfo.getUserInfo().getGender());
        editor.putString(Constants.KEY_AVATAR, accountInfo.getUserInfo().getAvatar());
        editor.putString(Constants.KEY_AUTH_TOKEN, accountInfo.getToken());
        editor.apply();
    }

    public static AccountInfo getAccountInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        String userId = sp.getString(Constants.KEY_USER_ID, null);
        if (userId == null) {
            return null;
        }
        String nickName = sp.getString(Constants.KEY_NICK_NAME, null);
        String gender = sp.getString(Constants.KEY_GENDER, null);
        String avatar = sp.getString(Constants.KEY_AVATAR, "");
        String token = sp.getString(Constants.KEY_AUTH_TOKEN, null);
        return new AccountInfo(new UserInfo(userId, nickName, gender, avatar), token);
    }

    public static void updateUserInfoForAccount(Context context, UserInfo userInfo) {
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Constants.KEY_USER_ID, userInfo.getUserId());
        editor.putString(Constants.KEY_NICK_NAME, userInfo.getNickName());
        editor.putString(Constants.KEY_GENDER, userInfo.getGender());
        editor.putString(Constants.KEY_AVATAR, userInfo.getAvatar());
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
        SharedPreferences sp = context.getSharedPreferences(Constants.SP_USER_INFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.clear();
        editor.apply();
    }
}
