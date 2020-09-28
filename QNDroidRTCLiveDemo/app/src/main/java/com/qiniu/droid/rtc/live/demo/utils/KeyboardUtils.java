package com.qiniu.droid.rtc.live.demo.utils;

import android.app.Activity;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class KeyboardUtils {

    /**
     * 根据触摸落点和视图位置判断是否应该收起软键盘
     *
     * @param view  要判断的视图
     * @param event 触摸事件
     * @return 是否应该收起软键盘
     */
    public static boolean isShouldHideKeyboard(View view, MotionEvent event) {
        if ((view instanceof EditText)) {
            int[] location = {0, 0};
            view.getLocationOnScreen(location);
            int left = location[0];
            int top = location[1];
            int bottom = top + view.getHeight();
            int right = left + view.getWidth();
            return !(event.getRawX() > left && event.getRawX() < right && event.getRawY() > top && event.getRawY() < bottom);
        }
        return false;
    }

    public static void hideSoftInput(@NonNull final Activity activity) {
        hideSoftInput(activity.getWindow());
    }

    /**
     * 添加一个 EditText 来抢夺焦点
     */
    public static void hideSoftInput(@NonNull final Window window) {
        View view = window.getCurrentFocus();
        if (view == null) {
            View decorView = window.getDecorView();
            View focusView = decorView.findViewWithTag("keyboardTagView");
            if (focusView == null) {
                view = new EditText(window.getContext());
                view.setTag("keyboardTagView");
                ((ViewGroup) decorView).addView(view, 0, 0);
            } else {
                view = focusView;
            }
            view.requestFocus();
        }
        hideSoftInput(view);
    }

    /**
     * 利用输入法管理器来隐藏软键盘
     */
    public static void hideSoftInput(@NonNull final View view) {
        InputMethodManager imm = (InputMethodManager) AppUtils.getApp().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
