package com.qiniu.droid.rtc.live.demo.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.adapter.PKParticipantsAdapter;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class PkParticipantsFragment extends BaseBottomSheetFragment {
    public static final String TAG = "PkParticipantsFragment";

    private RecyclerView mPkParticipantsRv;
    private PKParticipantsAdapter mPKParticipantsAdapter;
    private List<UserInfo> mPkParticipants;
    private OnPkParticipantClickListener mOnPkParticipantClickListener;

    public interface OnPkParticipantClickListener {
        void onLocalMuteAudioClicked();
        void onLocalMuteVideoClicked();
        void onEndPkClicked();
    }

    public PkParticipantsFragment(List<UserInfo> candidateRooms) {
        mPkParticipants = candidateRooms;
    }

    public void setOnPkParticipantClickListener(OnPkParticipantClickListener listener) {
        mOnPkParticipantClickListener = listener;
    }

    public void updatePkParticipants(List<UserInfo> pkParticipants) {
        if (mPKParticipantsAdapter != null) {
            mPKParticipantsAdapter.updateParticipants(pkParticipants);
        }
    }

    public void updateUserInfoState(UserInfo userInfo, boolean isAudioMuted, boolean isVideoMuted) {

    }

    @Override
    protected int getLayoutResId() {
        return R.layout.bottom_sheet_pk_participants;
    }

    @Override
    protected void initViewAndEvents(View view, Bundle savedInstanceState) {
        Button endPkBtn = view.findViewById(R.id.end_pk_btn);
        endPkBtn.setOnClickListener(v -> {
            if (mOnPkParticipantClickListener != null) {
                mOnPkParticipantClickListener.onEndPkClicked();
            }
        });

        mPkParticipantsRv = view.findViewById(R.id.pk_participants_list);
        mPkParticipantsRv.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mPkParticipantsRv.setLayoutManager(layoutManager);
        mPKParticipantsAdapter = new PKParticipantsAdapter(mPkParticipants);
        mPKParticipantsAdapter.setOnItemClickListener(new PKParticipantsAdapter.OnItemClickListener() {
            @Override
            public void onMuteAudioClicked() {

            }

            @Override
            public void onMuteVideoClicked() {

            }
        });
        mPkParticipantsRv.setAdapter(mPKParticipantsAdapter);
    }
}
