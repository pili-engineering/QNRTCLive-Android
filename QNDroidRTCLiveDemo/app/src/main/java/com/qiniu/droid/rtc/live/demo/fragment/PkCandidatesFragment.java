package com.qiniu.droid.rtc.live.demo.fragment;

import android.os.Bundle;
import android.view.View;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.adapter.PKCandidatesAdapter;
import com.qiniu.droid.rtc.live.demo.model.RoomInfo;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PkCandidatesFragment extends BaseBottomSheetFragment {
    public static final String TAG = "PkCandidatesFragment";

    private RecyclerView mPKCandidateRoomsRv;
    private PKCandidatesAdapter mPKCandidateRoomsAdapter;
    private List<RoomInfo> mPkCandidateRooms;
    private OnPkCandidateRoomClickListener mOnPkCandidateRoomClickListener;

    public interface OnPkCandidateRoomClickListener {
        void onPkCandidateClicked(RoomInfo roomInfo);
    }

    public PkCandidatesFragment(List<RoomInfo> candidateRooms) {
        mPkCandidateRooms = candidateRooms;
    }

    public void setOnPkCandidateRoomClickListener(OnPkCandidateRoomClickListener listener) {
        mOnPkCandidateRoomClickListener = listener;
    }

    public void updateCandidateRooms(List<RoomInfo> candidateRooms) {
        if (mPKCandidateRoomsAdapter != null) {
            mPKCandidateRoomsAdapter.updateRoomInfos(candidateRooms);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.bottom_sheet_pk_candidates;
    }

    @Override
    protected void initViewAndEvents(View view, Bundle savedInstanceState) {
        mPKCandidateRoomsRv = view.findViewById(R.id.pk_candidates_list);
        mPKCandidateRoomsRv.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mPKCandidateRoomsRv.setLayoutManager(layoutManager);
        mPKCandidateRoomsAdapter = new PKCandidatesAdapter(mPkCandidateRooms);
        mPKCandidateRoomsAdapter.setOnRequestPkClickListener(candidateRoomInfo -> {
            if (mOnPkCandidateRoomClickListener != null) {
                mOnPkCandidateRoomClickListener.onPkCandidateClicked(candidateRoomInfo);
            }
        });
        mPKCandidateRoomsRv.setAdapter(mPKCandidateRoomsAdapter);
    }
}
