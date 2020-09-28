package com.qiniu.droid.rtc.live.demo.fragment;

import com.qiniu.droid.rtc.live.demo.base.BaseFragment;
import com.qiniu.droid.rtc.live.demo.base.IPresenter;

/**
 * 每个功能 fragemnt 的基类
 *
 * @param <T>
 */
public abstract class BaseFeatureFragment<T extends IPresenter, Callback> extends BaseFragment<T> {
    private Callback mCallback;

    public BaseFeatureFragment setCallback(Callback t) {
        this.mCallback = t;
        return this;
    }

    public Callback getCallback() {
        return mCallback;
    }
}
