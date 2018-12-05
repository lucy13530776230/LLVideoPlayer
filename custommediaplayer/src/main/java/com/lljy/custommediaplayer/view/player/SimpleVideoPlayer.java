package com.lljy.custommediaplayer.view.player;

import android.content.Context;
import android.util.AttributeSet;

import com.lljy.custommediaplayer.interfs.ControllerListener;
import com.lljy.custommediaplayer.view.controller.SimpleController;

/**
 * @desc: 简单的视频播放器，只有{@link AbsCustomVideoPlayer}的基本功能
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/3 16:18
 */

public class SimpleVideoPlayer extends AbsCustomVideoPlayer<SimpleController> implements ControllerListener {
    public SimpleVideoPlayer(Context context) {
        super(context);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 初始化控制器监听
     *
     * @param controller
     */
    @Override
    protected void setControllerListener(SimpleController controller) {
        if (controller != null) {
            controller.setListener(this);
        }
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
