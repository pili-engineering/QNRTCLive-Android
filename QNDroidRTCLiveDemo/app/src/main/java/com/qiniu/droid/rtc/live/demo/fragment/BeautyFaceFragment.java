package com.qiniu.droid.rtc.live.demo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.adapter.ButtonViewRVAdapter;
import com.qiniu.droid.rtc.live.demo.fragment.contract.ItemGetContract;
import com.qiniu.droid.rtc.live.demo.fragment.contract.OnCloseListener;
import com.qiniu.droid.rtc.live.demo.fragment.contract.presenter.ItemGetPresenter;
import com.qiniu.droid.rtc.live.demo.model.ButtonItem;

import java.util.List;

public class BeautyFaceFragment extends BaseFeatureFragment<ItemGetContract.Presenter, BeautyFaceFragment.IBeautyCallBack>
        implements EffectFragment.IProgressCallback, OnCloseListener, ButtonViewRVAdapter.OnItemClickListener, ItemGetContract.View {
    private RecyclerView rv;
    private int mType;

    public interface IBeautyCallBack {
        void onBeautySelect(ButtonItem item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_beauty, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPresenter(new ItemGetPresenter());

        rv = view.findViewById(R.id.rv_beauty);
        List<ButtonItem> items = mPresenter.getItems(mType);
        ButtonViewRVAdapter adapter = new ButtonViewRVAdapter(items, this);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv.setAdapter(adapter);
    }

    public BeautyFaceFragment setType(final int type) {
        mType = type;
        return this;
    }

    @Override
    public void onItemClick(ButtonItem item) {
        getCallback().onBeautySelect(item);
    }

    @Override
    public void onProgress(float progress, int id) {
        if (rv == null || rv.getAdapter() == null) {
            return;
        }
        ((ButtonViewRVAdapter)rv.getAdapter()).onProgress(progress, id);
    }

    @Override
    public int getSelect() {
        return ((ButtonViewRVAdapter)rv.getAdapter()).getSelect();
    }

    @Override
    public void setSelect(int select) {
        ((ButtonViewRVAdapter)rv.getAdapter()).setSelect(select);
    }

    @Override
    public void setSelectItem(int id) {
        ((ButtonViewRVAdapter)rv.getAdapter()).setSelectItem(id);
    }

    @Override
    public void onClose() {
        ((ButtonViewRVAdapter)rv.getAdapter()).onClose();
    }
}
