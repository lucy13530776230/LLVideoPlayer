package com.lljy.custommediaplayer.adapter;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.lljy.custommediaplayer.R;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.utils.VideoCoverUtils;

import java.util.List;

/**
 * @desc: 视频列表适配器
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/4 14:08
 */

public class VideoListAdapter extends BaseQuickAdapter<VideoBean, BaseViewHolder> {
    public VideoListAdapter(@Nullable List<VideoBean> data) {
        super(R.layout.item_video, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoBean item) {
        boolean isPlaying = item.isPlaying();
        Log.d("adapter", "notify:" + helper.getAdapterPosition());
        helper.setVisible(R.id.mongli_view, !isPlaying)
                .setVisible(R.id.center_name_tv, !isPlaying)
                .setVisible(R.id.bottom_name_tv, isPlaying).setText(R.id.center_name_tv, item.getVideoName())
                .setText(R.id.bottom_name_tv, item.getVideoName()).addOnClickListener(R.id.item_video_iv);
        helper.getView(R.id.child_root_rl).setSelected(isPlaying);
        VideoCoverUtils.load(mContext, helper.getView(R.id.item_video_iv), item);
    }

    public void setPlay(int position) {
        for (int i = 0; i < getData().size(); i++) {
            VideoBean videoBean = getData().get(i);
            if (videoBean != null) {
                videoBean.setPlaying(position == i);
            }
        }
        notifyDataSetChanged();
    }
}
