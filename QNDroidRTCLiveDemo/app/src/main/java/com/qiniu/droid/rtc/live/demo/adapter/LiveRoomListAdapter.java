package com.qiniu.droid.rtc.live.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.model.RoomInfo;
import com.qiniu.droid.rtc.live.demo.model.RoomType;

import java.util.List;

public class LiveRoomListAdapter extends RecyclerView.Adapter<LiveRoomListAdapter.LiveRoomListHolder> {

    private Context mContext;
    private List<RoomInfo> mRoomInfos;

    private OnSelectLiveRoomListener mListener;

    public LiveRoomListAdapter(Context context) {
        mContext = context;
    }

    public void setOnSelectLiveRoomListener(OnSelectLiveRoomListener onSelectLiveRoomListener) {
        mListener = onSelectLiveRoomListener;
    }

    public void setData(List<RoomInfo> roomInfoList){
        mRoomInfos = roomInfoList;
    }

    @NonNull
    @Override
    public LiveRoomListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_home_rv, parent, false);
        return new LiveRoomListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LiveRoomListHolder holder, int position) {
        RoomInfo roomInfo = mRoomInfos.get(position);
        if (RoomType.SINGLE.getValue().equalsIgnoreCase(roomInfo.getStatus())) {
            holder.mRoomType.setText("单主播直播");
        } else if (RoomType.PK.getValue().equalsIgnoreCase(roomInfo.getStatus())) {
            holder.mRoomType.setText("连麦 PK");
        } else if (RoomType.VOICE_LIVE.getValue().equalsIgnoreCase(roomInfo.getStatus())) {
            holder.mRoomType.setText("语音聊天室");
        }
        holder.mRoomName.setText(roomInfo.getName());
        holder.mRoomAudience.setText("观众人数：" + roomInfo.getAudienceNumber());
        holder.mCardItem.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSelectedLiveRoom(roomInfo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRoomInfos.size();
    }

    class LiveRoomListHolder extends RecyclerView.ViewHolder {

        TextView mRoomType;
        TextView mRoomName;
        TextView mRoomAudience;
        ImageView mCoverImage;
        MaterialCardView mCardItem;

        public LiveRoomListHolder(@NonNull View itemView) {
            super(itemView);
            mRoomType = itemView.findViewById(R.id.tv_room_type);
            mRoomName = itemView.findViewById(R.id.tv_room_name);
            mRoomAudience = itemView.findViewById(R.id.tv_audience);
            mCoverImage = itemView.findViewById(R.id.iv_cover_image);
            mCardItem = itemView.findViewById(R.id.card_item);
        }
    }

    public interface OnSelectLiveRoomListener {
        void onSelectedLiveRoom(RoomInfo roomInfo);
    }
}
