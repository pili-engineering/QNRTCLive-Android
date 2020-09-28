package com.qiniu.droid.rtc.live.demo.fragment.contract.presenter;

import com.qiniu.bytedanceplugin.ByteDancePlugin;
import com.qiniu.bytedanceplugin.model.StickerModel;
import com.qiniu.droid.rtc.live.demo.fragment.contract.StickerContract;

import java.util.ArrayList;
import java.util.List;

public class StickerPresenter extends StickerContract.Presenter {
    private List<StickerModel> mStickerModels = new ArrayList<>();
    @Override
    public List<StickerModel> getStickersItems() {
        if (mStickerModels.size() == 0) {
            mStickerModels.add(0,new StickerModel());
            mStickerModels.addAll(ByteDancePlugin.getStickerList());
        }
        return mStickerModels;
    }

}
