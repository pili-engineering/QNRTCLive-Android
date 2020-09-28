package com.qiniu.droid.rtc.live.demo.im.messageview;

import android.content.Context;
import android.view.LayoutInflater;

import com.qiniu.droid.rtc.live.demo.R;

import io.rong.imlib.model.MessageContent;

public class UnknownMsgView extends BaseMsgView {

    public UnknownMsgView(Context context) {
        super(context);
        LayoutInflater.from(getContext()).inflate(R.layout.msg_unknown_view, this);
    }

    @Override
    protected void onBindContent(MessageContent msgContent, String senderUserId) {
    }
}
