package com.qiniu.droid.rtc.live.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.bytedanceplugin.model.StickerModel;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.adapter.StickerRVAdapter;
import com.qiniu.droid.rtc.live.demo.fragment.contract.OnCloseListener;
import com.qiniu.droid.rtc.live.demo.fragment.contract.StickerContract;
import com.qiniu.droid.rtc.live.demo.fragment.contract.presenter.StickerPresenter;
import com.qiniu.droid.rtc.live.demo.utils.ToastUtils;

public class StickerFragment extends BaseFeatureFragment<StickerContract.Presenter, StickerFragment.IStickerCallback>
        implements StickerRVAdapter.OnItemClickListener, OnCloseListener, StickerContract.View {
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rv = (RecyclerView) inflater.inflate(R.layout.fragment_sticker, container, false);
        return rv;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPresenter(new StickerPresenter());

        StickerRVAdapter adapter = new StickerRVAdapter(mPresenter.getStickersItems(), this);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        rv.setAdapter(adapter);
    }

    @Override
    public void onItemClick(StickerModel item) {
        if (item.getTip()!=null) {
            ToastUtils.showShortToast(item.getTip());
        }
        if (getCallback() == null) {
            return;
        }
        getCallback().onStickerSelected(item.getFilePath());
    }

    @Override
    public void onClose() {
        getCallback().onStickerSelected(null);
        ((StickerRVAdapter)rv.getAdapter()).setSelect(0);
    }

    public interface IStickerCallback {
        void onStickerSelected(String path);
    }
}
