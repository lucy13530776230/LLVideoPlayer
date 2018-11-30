package com.lljy.custommediaplayer.adapter;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lljy.custommediaplayer.R;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.utils.VideoCoverUtils;

import java.util.List;

/**
 * 视频列表适配器
 */
public class VideoListAdapter extends BaseQuickAdapter<VideoBean, BaseViewHolder> {
    private int playingPos;//正在播放的视频

    public VideoListAdapter(int layoutResId, @Nullable List<VideoBean> data) {
        super(R.layout.item_video, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoBean item) {
        int pos = helper.getAdapterPosition();
        boolean isPlaying = isPlaying(pos);
        helper.setVisible(R.id.mongli_view, !isPlaying)
                .setVisible(R.id.center_name_tv, !isPlaying)
                .setVisible(R.id.bottom_name_tv, isPlaying).setText(R.id.center_name_tv, item.getVideoName())
                .setText(R.id.bottom_name_tv, item.getVideoName()).addOnClickListener(R.id.item_video_iv);
        helper.getView(R.id.child_root_rl).setSelected(isPlaying);
        VideoCoverUtils.load(mContext, (ImageView) helper.getView(R.id.item_video_iv), item);
    }

    private boolean isPlaying(int position) {
        return playingPos == position;
    }

    public void setPlay(int position) {
        if (position != playingPos) {
            int size = getData().size();
            int tempPos = this.playingPos;
            playingPos = position;
            if (tempPos <= getData().size() - 1) {
                notifyItemChanged(tempPos);
            }
            if (playingPos <= getData().size() - 1) {
                notifyItemChanged(playingPos);
            }
        }
    }
}
