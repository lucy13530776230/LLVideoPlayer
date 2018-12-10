package com.lljy.custommediaplayer.view.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.lljy.custommediaplayer.constants.ScreenStatus;
import com.lljy.custommediaplayer.entity.VideoEntity;
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
    public void onPlayOrPausePressed() {
        super.playOrPause();
    }

    /**
     * 点击了打开或者退出全屏
     *
     * @param currentScreenStatus 当前屏幕状态
     */
    @Override
    public void onStartOrExitFullScreenPressed(ScreenStatus currentScreenStatus) {
        super.pressStartOrExitFullscreen(currentScreenStatus);
    }

    /**
     * 点击了标题栏的返回按钮
     *
     * @param currentScreenStatus 当前屏幕状态
     */
    @Override
    public void onTitleBackPressed(ScreenStatus currentScreenStatus) {
        super.pressedTitleBack(currentScreenStatus);
    }

    /**
     * 加载封面图片
     *
     * @param imageView 图片控件
     * @param cover     图片地址
     */
    @Override
    public void onCoverLoad(ImageView imageView, String cover) {
        super.loadCover(imageView, cover);
    }


    @Override
    public void onReloadClick() {
        super.reload();
    }

    /**
     * 设置视频播放资源
     *
     * @param videoEntity 视频
     */
    public void setVideo(VideoEntity videoEntity) {
        setVideoSource(videoEntity);
    }
}
