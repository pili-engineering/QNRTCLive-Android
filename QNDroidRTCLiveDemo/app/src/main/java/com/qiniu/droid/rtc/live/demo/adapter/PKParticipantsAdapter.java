package com.qiniu.droid.rtc.live.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.model.UserInfo;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PKParticipantsAdapter extends RecyclerView.Adapter<PKParticipantsAdapter.PKParticipantsViewHolder> {
    private List<UserInfo> mPkParticipants;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onMuteAudioClicked();
        void onMuteVideoClicked();
    }

    public PKParticipantsAdapter(List<UserInfo> participants) {
        mPkParticipants = participants;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void updateParticipants(List<UserInfo> pkParticipants) {
        mPkParticipants = pkParticipants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PKParticipantsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pk_participant, parent, false);
        return new PKParticipantsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PKParticipantsViewHolder holder, int position) {
        UserInfo participant = mPkParticipants.get(position);
        holder.participantName.setText(participant.getNickName());
        holder.muteAudioBtn.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onMuteAudioClicked();
            }
        });
        holder.muteVideoBtn.setOnClickListener(v -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onMuteVideoClicked();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mPkParticipants.size();
    }

    static class PKParticipantsViewHolder extends RecyclerView.ViewHolder {
        TextView participantName;
        ImageButton muteAudioBtn;
        ImageButton muteVideoBtn;

        public PKParticipantsViewHolder(@NonNull View itemView) {
            super(itemView);
            participantName = itemView.findViewById(R.id.participant_nick_name);
            muteAudioBtn = itemView.findViewById(R.id.mute_audio_btn);
            muteVideoBtn = itemView.findViewById(R.id.mute_video_btn);
        }
    }
}
