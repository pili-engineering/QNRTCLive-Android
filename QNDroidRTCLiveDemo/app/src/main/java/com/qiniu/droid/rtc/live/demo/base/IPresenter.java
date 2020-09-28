package com.qiniu.droid.rtc.live.demo.base;

public interface IPresenter {
    void attachView(IView view);
    void detachView();
}
