package com.qiniu.droid.rtc.live.demo.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class AppStateTracker {
    public static final int STATE_FOREGROUND = 0;

    public static final int STATE_BACKGROUND = 1;

    private static int currentState;

    public static int getCurrentState() {
        return currentState;
    }

    public interface AppStateChangeListener {
        void appTurnIntoForeground();
        void appTurnIntoBackGround();
        void appDestroyed();
    }

    public static void track(Application application, final AppStateChangeListener appStateChangeListener) {

        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

            private int resumeActivityCount = 0;
            private int createActivityCount = 0;

            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                createActivityCount++;
            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (resumeActivityCount == 0){
                    currentState = STATE_FOREGROUND;
                    appStateChangeListener.appTurnIntoForeground();
                }

                resumeActivityCount++;
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                resumeActivityCount--;

                if (resumeActivityCount==0){
                    currentState = STATE_BACKGROUND;
                    appStateChangeListener.appTurnIntoBackGround();
                }
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                createActivityCount--;

                if (createActivityCount == 0) {
                    appStateChangeListener.appDestroyed();
                }
            }
        });
    }
}
