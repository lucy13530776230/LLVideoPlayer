package com.lljy.custommediaplayer.adapter;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lljy.custommediaplayer.R;
import com.lljy.custommediaplayer.entity.VideoEntity;

import java.util.List;

/**
 * @desc: 视频列表适配器
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/4 14:08
 */

public class VideoListAdapter extends BaseQuickAdapter<VideoEntity, BaseViewHolder> {
    private OnCoverLoadListener mListener;

    public VideoListAdapter(@Nullable List<VideoEntity> data, OnCoverLoadListener listener) {
        super(R.layout.item_video, data);
        this.mListener = listener;
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoEntity item) {
        boolean isPlaying = item.isPlaying();
        Log.d("adapter", "notify:" + helper.getAdapterPosition());
        helper.setVisible(R.id.mongli_view, !isPlaying)
                .setVisible(R.id.center_name_tv, !isPlaying)
                .setVisible(R.id.bottom_name_tv, isPlaying).setText(R.id.center_name_tv, item.getVideoName())
                .setText(R.id.bottom_name_tv, item.getVideoName()).addOnClickListener(R.id.item_video_iv);
        helper.getView(R.id.child_root_rl).setSelected(isPlaying);
        if (mListener != null) {
            mListener.onCoverLoad(helper.getView(R.id.item_video_iv), item.getCoverUrl());
        }
    }

    public void setPlay(int position) {
        for (int i = 0; i < getData().size(); i++) {
            VideoEntity videoEntity = getData().get(i);
            if (videoEntity != null) {
                videoEntity.setPlaying(position == i);
            }
        }
        notifyDataSetChanged();
    }

    public interface OnCoverLoadListener {
        void onCoverLoad(ImageView imageView, String cover);
    }
}
