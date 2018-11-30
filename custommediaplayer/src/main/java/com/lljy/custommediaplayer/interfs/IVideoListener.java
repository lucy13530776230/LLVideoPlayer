package com.lljy.custommediaplayer.interfs;


import com.lljy.custommediaplayer.entity.VideoBean;

/**
 * @desc: 视频播放回调监听接口，随时监控视频的播放情况。
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 17:43
 */


public interface IVideoListener {
    /**
     * 播放准备完成
     */
    void onPrepared();

    /**
     * 实时返回播放进度
     *
     * @param progress 播放进度
     */
    void onProgress(int progress);

    /**
     * 缓冲进度
     *
     * @param progress 缓冲进度（即第二条进度条）
     */
    void onSecondProgress(int progress);

    /**
     * 总时间
     *
     * @param total
     */
    void onTotalTime(int total);

    /**
     * 播放完成
     */
    void onComplete();

    /**
     * 播放出错
     *
     * @param errorMsg 出错信息
     */
    void onError(String errorMsg);

    /**
     * 开始转菊花
     */
    void onLoadingStart();

    /**
     * 停止转菊花
     */
    void onLoadingFinished();

    /**
     * 打开全屏
     */
    void onStartFullScreen();

    /**
     * 退出全屏
     */
    void onExitFullScreen();
}
