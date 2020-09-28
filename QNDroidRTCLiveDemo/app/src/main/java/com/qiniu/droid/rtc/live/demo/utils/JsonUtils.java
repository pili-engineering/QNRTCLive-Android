package com.qiniu.droid.rtc.live.demo.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {

    private static final String TAG = "JsonUtils";

    public static void jsonPut(JSONObject json, String key, Object value) {
        try {
            json.put(key, value);
        } catch (JSONException e) {
            Log.e(TAG, "jsonPut error: " + e.getMessage());
        }
    }
}
