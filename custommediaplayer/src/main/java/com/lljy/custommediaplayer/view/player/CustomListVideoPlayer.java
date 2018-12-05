package com.lljy.custommediaplayer.view.player;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import com.lljy.custommediaplayer.constants.VideoStatus;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.interfs.ListControllerListener;
import com.lljy.custommediaplayer.view.controller.AbsController;
import com.lljy.custommediaplayer.view.controller.ListController;

import java.util.List;

/**
 * @desc: 带播放列表的播放器
 * 该列表播放器对应的控制器为：{@link ListController}，执行设置控制器{@link #setController(AbsController)}需传入{@link ListController}实例
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/3 10:20
 */

public class CustomListVideoPlayer extends AbsCustomVideoPlayer<ListController> implements ListControllerListener {
    public CustomListVideoPlayer(Context context) {
        super(context);
    }

    public CustomListVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化控制器监听
     *
     * @param controller
     */
    @Override
    protected void setControllerListener(ListController controller) {
        if (controller != null) {
            controller.setListener(this);
        }
    }

    /**
     * 点击了哪个要播放的视频
     *
     * @param videoBean 要播放的视频
     */
    @Override
    public void onVideoSelected(VideoBean videoBean) {
        super.setVideoSource(videoBean);
    }

    /**
     * 播放指定视频（供外界调用，调用时要更新正在播放的视频）
     *
     * @param videoBean 指定视频
     */
    public void playVideo(VideoBean videoBean) {
        if (mController != null) {

        }
        super.setVideoSource(videoBean);
    }

    /**
     * 视频重新排序
     *
     * @param videos 待排序的视频列表
     */
    public void orderVideo(List<VideoBean> videos) {
        if (mController != null) {
            mController.orderVideos(videos);
        }
    }

    /**
     * 初始化视频列表
     *
     * @param videos 视频列表
     */
    public void setVideos(List<VideoBean> videos) {
        if (mController != null) {
            mController.setVideos(videos);
        }
    }

    /**
     * 添加视频
     *
     * @param videos 待添加的视频列表
     */
    public void addVideos(List<VideoBean> videos) {
        if (mController != null) {
            mController.addVideos(videos);
        }
    }

    /**
     * 删除指定视频
     *
     * @param videos 删除指定视频
     */
    public void deleteVideos(List<VideoBean> videos) {
        if (mController != null) {
            mController.deleteVideos(videos);
        }
    }

    /**
     * 删除了指定视频
     *
     * @param videoBean 删除的视频
     */
    @Override
    public void onPlayedVideoDeleted(VideoBean videoBean) {
        if (mVideo != null && mVideo.equals(videoBean)) {
            //正在播放视频已被删除，播放下一个视频
            if (mController != null) {
                mVideo = null;
                Bundle params = new Bundle();
                params.putString(VideoStatus.Constants.PLAY_ERROR_MSG, "您观看的视频已被删除");
                mController.setVideoState(VideoStatus.MEDIA_STATE_ERROR, params);
            }
        }
    }

    /**
     * 点击了全屏
     */
    @Override
    public void onStartFullScreen() {
        super.startFullScreen();
    }

    /**
     * 退出全屏
     */
    @Override
    public void onExitFullScreen() {
        super.exitFullScreen();
    }

    /**
     * 拖拽到指定进度
     *
     * @param progress 指定进度
     */
    @Override
    public void onSeekTo(int progress) {
        super.seekTo(progress);
    }

    /**
     * 点击了开始播放/暂停
     */
    @Override
    public void onPlayOrPauseClick() {
        super.playOrPause();
    }
}
