package com.qiniu.droid.rtc.live.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.model.RoomInfo;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PKCandidatesAdapter extends RecyclerView.Adapter<PKCandidatesAdapter.PKCandidatesViewHolder> {
    private List<RoomInfo> mRoomInfos;
    private OnRequestPkClickListener mOnRequestPkClickListener;

    public interface OnRequestPkClickListener {
        void onRequestPkClicked(RoomInfo candidateRoomInfo);
    }

    public PKCandidatesAdapter(List<RoomInfo> candidateRooms) {
        mRoomInfos = candidateRooms;
    }

    public void setOnRequestPkClickListener(OnRequestPkClickListener listener) {
        mOnRequestPkClickListener = listener;
    }

    public void updateRoomInfos(List<RoomInfo> roomInfos) {
        if (mRoomInfos == null) {
            mRoomInfos = new ArrayList<RoomInfo>();
        }

        mRoomInfos.clear();
        if (roomInfos != null) {
            mRoomInfos.addAll(roomInfos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PKCandidatesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pk_candidate, parent, false);
        return new PKCandidatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PKCandidatesViewHolder holder, int position) {
        RoomInfo candidate = mRoomInfos.get(position);
        holder.candidateName.setText(candidate.getCreator().getNickName());
        holder.requestPkBtn.setOnClickListener(v -> {
            if (mOnRequestPkClickListener != null) {
                mOnRequestPkClickListener.onRequestPkClicked(mRoomInfos.get(position));
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mRoomInfos == null ? 0 : mRoomInfos.size();
    }

    static class PKCandidatesViewHolder extends RecyclerView.ViewHolder {
        TextView candidateName;
        TextView requestPkBtn;

        public PKCandidatesViewHolder(@NonNull View itemView) {
            super(itemView);
            candidateName = itemView.findViewById(R.id.candidate_nick_name);
            requestPkBtn = itemView.findViewById(R.id.request_pk_btn);
        }
    }
}
