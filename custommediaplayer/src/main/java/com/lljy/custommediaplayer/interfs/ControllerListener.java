package com.lljy.custommediaplayer.interfs;

import android.content.Context;
import android.widget.ImageView;

import com.lljy.custommediaplayer.constants.ScreenStatus;

/**
 * @desc: 控制器（皮肤）基础接口
 * 其实现类需实现回调的方法有：
 * ①拖拽到了指定进度（手势拖拽/拖拽进度条后）{@link #onSeekTo(int)}
 * ②点击了开始播放/暂停按钮{@link #onPlayOrPausePressed()}
 * ③点击了打开或者退出全屏按钮{@link #onStartOrExitFullScreenPressed(ScreenStatus)}
 * ④点击了顶部返回按钮{@link #onTitleBackPressed(ScreenStatus)}
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/10 15:14
 */

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
    void onPlayOrPausePressed();

    /**
     * 点击了打开或者退出全屏
     *
     * @param currentScreenStatus 当前屏幕状态
     */
    void onStartOrExitFullScreenPressed(ScreenStatus currentScreenStatus);

    /**
     * 点击了标题栏的返回按钮
     *
     * @param currentScreenStatus 当前屏幕状态
     */
    void onTitleBackPressed(ScreenStatus currentScreenStatus);

    /**
     * 加载封面图片
     *
     * @param imageView 图片控件
     * @param cover     图片地址
     */
    void onCoverLoad(ImageView imageView, String cover);

    /**
     * 点击了重新加载
     */
    void onReloadClick();

    /**
     * 点击了继续播放
     */
    void onContinueClick();
}
