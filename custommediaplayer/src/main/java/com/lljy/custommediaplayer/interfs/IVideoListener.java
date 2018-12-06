package com.lljy.custommediaplayer.interfs;

/**
 * @desc: 视频播放器回调监听（给调用者使用，如activity、fragment）
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/3 15:08
 */

public interface IVideoListener {
    /**
     * 通知调用者打开全屏
     */
    void onStartFullScreen();

    /**
     * 通知调用者关闭全屏
     */
    void onExitFullScreen();

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
}
