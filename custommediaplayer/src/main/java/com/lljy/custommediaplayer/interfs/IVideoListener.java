package com.lljy.custommediaplayer.interfs;

import android.content.Context;
import android.widget.ImageView;

import com.lljy.custommediaplayer.constants.ScreenStatus;

/**
 * @desc: 视频播放器回调监听（给调用者使用，如activity、fragment）
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/3 15:08
 */

public interface IVideoListener {
    /**
     * 点击了打开或者退出全屏
     *
     * @param currentScreenStatus 当前屏幕状态
     */
    void onStartOrExitFullScreenPressed(ScreenStatus currentScreenStatus);

    /**
     * 返回
     *
     * @param currentScreenStatus 当前屏幕状态
     */
    void onTitleBackPressed(ScreenStatus currentScreenStatus);

    /**
     * 播放出错
     *
     * @param msg 错误信息
     */
    void onError(String msg);

    /**
     * 播放完成
     */
    void onComplete();


    /**
     * 加载封面图片
     *
     * @param imageView 图片控件
     * @param cover     图片地址
     */
    void onCoverLoad(ImageView imageView, String cover);
}
