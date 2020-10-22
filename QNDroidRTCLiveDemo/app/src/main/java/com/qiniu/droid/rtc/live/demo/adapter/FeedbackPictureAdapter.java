package com.qiniu.droid.rtc.live.demo.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qiniu.droid.rtc.live.demo.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FeedbackPictureAdapter extends RecyclerView.Adapter<FeedbackPictureAdapter.PictureViewHolder> {
    private Context mContext;
    private List<Uri> mPicturesList;

    private OnPictureOperateListener mOnPictureOperateListener;

    public FeedbackPictureAdapter(Context context, List<Uri> picturesList) {
        mContext = context;
        mPicturesList = picturesList;
    }

    public interface OnPictureOperateListener {
        void onChoosePictureClicked();
    }

    public void setOnPictureOperateListener(OnPictureOperateListener listener) {
        mOnPictureOperateListener = listener;
    }

    public void addPictureUri(Uri uri) {
        int insertPos = getItemCount() - 1;
        mPicturesList.add(insertPos, uri);
        notifyItemInserted(insertPos);
    }

    public List<Uri> getFeedbackPictures() {
        return mPicturesList == null ? null : mPicturesList.subList(0, mPicturesList.size() - 1);
    }

    @NonNull
    @Override
    public PictureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback_picture, parent, false);
        return new PictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PictureViewHolder holder, int position) {
        Glide.with(mContext)
                .load(mPicturesList.get(position))
                .centerInside()
                .into(holder.pictureView);
        if (position == mPicturesList.size() - 1) {
            holder.closeBtn.setVisibility(View.GONE);
            holder.pictureView.setScaleType(ImageView.ScaleType.CENTER);
            holder.pictureView.setOnClickListener(v -> {
                if (mOnPictureOperateListener != null) {
                    mOnPictureOperateListener.onChoosePictureClicked();
                }
            });
        } else {
            holder.pictureView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.closeBtn.setVisibility(View.VISIBLE);
            holder.closeBtn.setOnClickListener(v -> {
                mPicturesList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount() - position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return mPicturesList == null ? 0 : mPicturesList.size();
    }

    static class PictureViewHolder extends RecyclerView.ViewHolder {
        private ImageView pictureView;
        private ImageButton closeBtn;

        public PictureViewHolder(@NonNull View itemView) {
            super(itemView);
            pictureView = itemView.findViewById(R.id.feedback_picture_display);
            closeBtn = itemView.findViewById(R.id.delete_picture_btn);
        }
    }
}
