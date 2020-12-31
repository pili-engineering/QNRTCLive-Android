package com.qiniu.droid.rtc.live.demo.utils;

public class ViewClickUtils {
    private static final long DEFAULT_CLICK_DELTA_TIME_MS = 500;
    private static long mLastClickTimeMs;

    public static boolean isFastDoubleClick() {
        long currentTimeMs = System.currentTimeMillis();
        if ((currentTimeMs - mLastClickTimeMs) < DEFAULT_CLICK_DELTA_TIME_MS) {
            return true;
        } else {
            mLastClickTimeMs = currentTimeMs;
            return false;
        }
    }
}
