package com.qiniu.droid.rtc.live.demo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.model.AudioParticipant;
import com.qiniu.droid.rtc.live.demo.view.CircleImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AudienceParticipantsAdapter extends RecyclerView.Adapter<AudienceParticipantsAdapter.AudienceParticipantViewHolder> {

    private Context mContext;
    private List<AudioParticipant> mAudioParticipants;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClicked(int pos);
    }

    public AudienceParticipantsAdapter(Context context, List<AudioParticipant> list) {
        mContext = context;
        mAudioParticipants = list;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public AudienceParticipantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_participant, parent, false);
        return new AudienceParticipantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudienceParticipantViewHolder holder, int position) {
        AudioParticipant participant = mAudioParticipants.get(position);
        if (participant == null) {
            holder.avatarImage.setImageResource(R.drawable.ic_waiting_participant);
            holder.audioStatus.setVisibility(View.GONE);
            holder.setAudioStatus(false);
            holder.name.setText(mContext.getString(R.string.audio_waiting_participant));
            holder.name.setTextColor(Color.parseColor("#838383"));
            holder.itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(position);
                }
            });
            return;
        }
        if ("".equals(participant.getUserInfo().getAvatar())) {
            holder.avatarImage.setImageResource(R.mipmap.default_avatar);
        } else {
            Glide.with(mContext)
                    .load(participant.getUserInfo().getAvatar())
                    .centerInside()
                    .into(holder.avatarImage);
        }
        holder.name.setText(participant.getUserInfo().getNickName());
        holder.name.setTextColor(Color.WHITE);
        holder.audioStatus.setVisibility(View.VISIBLE);
        holder.setAudioStatus(participant.isMute());
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mAudioParticipants == null ? 0 : mAudioParticipants.size();
    }

    public static class AudienceParticipantViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarImage;
        ImageView audioStatus;
        TextView name;

        public AudienceParticipantViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.audio_avatar);
            audioStatus = itemView.findViewById(R.id.audio_status);
            name = itemView.findViewById(R.id.audio_participant_name);
        }

        public void setAudioStatus(boolean isMuted) {
            audioStatus.setImageResource(isMuted ? R.drawable.ic_voice_off : R.drawable.ic_voice_on);
        }
    }
}
