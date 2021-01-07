package com.qiniu.droid.rtc.live.demo.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.adapter.AudioParticipantsAdapter;
import com.qiniu.droid.rtc.live.demo.model.AudioParticipant;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AudioParticipantsFragment extends BaseBottomSheetFragment {
    public static final String TAG = "AudioParticipantsFragment";

    private Button mEndBtn;
    private RecyclerView mAudioParticipantsRv;
    private LinearLayoutManager mLinearLayoutManager;
    private AudioParticipantsAdapter mAudioParticipantsAdapter;
    private List<AudioParticipant> mAudioParticipantList;
    private boolean mIsCommunicateAudience;

    private OnEndAudioClickListener mOnEndAudioClickListener;

    public interface OnEndAudioClickListener {
        void onEndAudioClicked();
    }

    public AudioParticipantsFragment(List<AudioParticipant> audioParticipantList, boolean isComminicateAudience) {
        mAudioParticipantList = audioParticipantList;
        mIsCommunicateAudience = isComminicateAudience;
    }

    public void setOnEndAudioClickListener(OnEndAudioClickListener listener) {
        mOnEndAudioClickListener = listener;
    }

    public void notifyDataSetChanged() {
        if (mAudioParticipantsAdapter != null) {
            mAudioParticipantsAdapter.notifyDataSetChanged();
        }
    }

    public void notifyItemInserted(int position) {
        if (mAudioParticipantsAdapter != null) {
            mAudioParticipantsAdapter.notifyItemInserted(position);
        }
    }

    public void notifyItemRemoved(int position) {
        if (mAudioParticipantsAdapter != null) {
            mAudioParticipantsAdapter.notifyItemRemoved(position);
        }
    }

    public void setEndBtnVisible(boolean visible) {
        mEndBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void updateMuteStatus(int pos, boolean isMuted) {
        AudioParticipantsAdapter.AudioParticipantViewHolder viewHolder =
                ((AudioParticipantsAdapter.AudioParticipantViewHolder) mAudioParticipantsRv.findViewHolderForAdapterPosition(pos));
        if (viewHolder != null) {
            viewHolder.setMuteStatus(isMuted);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.bottom_sheet_communicate_participants;
    }

    @Override
    protected void initViewAndEvents(View view, Bundle savedInstanceState) {
        mEndBtn = view.findViewById(R.id.end_communicate_btn);
        mEndBtn.setText(R.string.quit_audio_communication);
        mEndBtn.setOnClickListener(v -> {
            if (mOnEndAudioClickListener != null) {
                mOnEndAudioClickListener.onEndAudioClicked();
            }
        });
        if (!mIsCommunicateAudience) {
            mEndBtn.setVisibility(View.GONE);
        }

        mAudioParticipantsRv = view.findViewById(R.id.communicate_participants_list);
        mAudioParticipantsRv.setNestedScrollingEnabled(false);
        mLinearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        mAudioParticipantsRv.setLayoutManager(mLinearLayoutManager);
        mAudioParticipantsAdapter = new AudioParticipantsAdapter(mAudioParticipantList);
        mAudioParticipantsRv.setAdapter(mAudioParticipantsAdapter);
    }
}
