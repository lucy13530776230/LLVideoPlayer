package com.lljy.custommediaplayer.interfs;

import com.lljy.custommediaplayer.entity.VideoBean;

/**
 * @desc: 视频播放列表回调接口
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/3 10:26
 */

public interface ListControllerListener extends ControllerListener {
    /**
     * 点击了哪个要播放的视频
     *
     * @param videoBean 要播放的视频
     */
    void onVideoSelected(VideoBean videoBean);

    /**
     * 删除了正在播放的视频
     *
     * @param videoBean 删除的视频
     */
    void onPlayedVideoDeleted(VideoBean videoBean);

    /**
     * 点击了全屏
     */
    void onStartFullScreen();

    /**
     * 退出了全屏
     */
    void onExitFullScreen();
}
