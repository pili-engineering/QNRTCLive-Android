package com.qiniu.droid.rtc.live.demo.im.model;

import java.util.List;

/**
 * Created by wangw on 2019-09-04.
 */
public class BaseResponse {

    public int code;
    public String desc;
    public List<ChatRoomInfo> roomList;

    public BaseResponse() {
    }

    public boolean isSuccess(){
        return code == 0;
    }

}
