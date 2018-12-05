package com.lljy.custommediaplayer.interfs;

public interface ControllerListener {
    /**
     * 拖拽到指定进度
     *
     * @param progress 指定进度
     */
    void onSeekTo(int progress);

    /**
     * 点击了开始播放/暂停
     */
    void onPlayOrPauseClick();
}
