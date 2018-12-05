package com.lljy.custommediaplayer.download;

import com.lljy.custommediaplayer.entity.VideoBean;

/**
 * @desc: 下载回调监听
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/4 16:08
 */

public interface VideoDownloadListener {
    /**
     * 下载开始
     *
     * @param videoBean 下载的视频
     */
    void onDownloadStart(VideoBean videoBean);

    /**
     * 下载出错
     *
     * @param errorMsg 错误信息
     */
    void onDownloadFailed(String errorMsg);

    /**
     * 下载完成
     *
     * @param videoBean 下载完成的视频
     */
    void onDownloadFinished(VideoBean videoBean);
}
