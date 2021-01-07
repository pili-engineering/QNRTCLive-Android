package com.qiniu.droid.rtc.live.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.model.AudioParticipant;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AudioParticipantsAdapter extends RecyclerView.Adapter<AudioParticipantsAdapter.AudioParticipantViewHolder> {
    private List<AudioParticipant> mAudioParticipants;

    public AudioParticipantsAdapter(List<AudioParticipant> audioParticipants) {
        mAudioParticipants = audioParticipants;
    }

    @NonNull
    @Override
    public AudioParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_communication_participant, parent, false);
        return new AudioParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioParticipantViewHolder holder, int position) {
        AudioParticipant participant = mAudioParticipants.get(position);
        holder.itemView.setVisibility(View.VISIBLE);
        holder.audienceName.setText(participant.getUserInfo().getNickName());
        holder.roleTag.setText(position == 0 ? R.string.role_tag_anchor : R.string.role_tag_audience);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mAudioParticipants == null ? 0 : mAudioParticipants.size();
    }

    public static class AudioParticipantViewHolder extends RecyclerView.ViewHolder {
        TextView audienceName;
        TextView roleTag;
        ImageView audioStatus;

        public AudioParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            audienceName = itemView.findViewById(R.id.participant_nick_name);
            roleTag = itemView.findViewById(R.id.role_flag);
            audioStatus = itemView.findViewById(R.id.mute_audio_btn);
        }

        public void setMuteStatus(boolean isMute) {
            audioStatus.setImageResource(isMute ? R.drawable.ic_operate_voice_off : R.drawable.ic_operate_voice_on);
        }
    }
}
