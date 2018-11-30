package com.lljy.custommediaplayer.constants;

/**
 * @desc: 播放状态管理类
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/30 14:28
 */

public final class VideoStatus {
    /*播放状态*/
    public static final int MEDIA_STATE_RELEASE = 10;//释放状态
    public static final int MEDIA_STATE_RESUME = 11;//恢复播放（对应暂停）
    public static final int MEDIA_STATE_PAUSE = 12;//暂停播放
    public static final int MEDIA_STATE_PLAY_NEW = 13;//准备播放新视频
    public static final int MEDIA_STATE_START_PLAY = 14;//准备好即将开始播放
    public static final int MEDIA_STATE_ERROR = 15;//播放出错，顺带出错信息供显示
    public static final int MEDIA_STATE_BUFFER_START = 16;//缓冲开始
    public static final int MEDIA_STATE_BUFFER_END = 17;//缓冲完成
    public static final int MEDIA_STATE_COMPLETE = 18;//播放完成
    public static final int MEDIA_STATE_PLAY_PROGRESS = 19;//播放进度
    public static final int MEDIA_STATE_PLAY_SECOND_PROGRESS = 20;//缓冲进度
    public static final int MEDIA_STATE_PLAY_TOTAL_TIME = 21;//播放总时长
    public static final int MEDIA_STATE_START_FULL_SCREEN = 22;//开启全屏
    public static final int MEDIA_STATE_EXIT_FULL_SCREEN = 23;//关闭全屏

    public static class Constants {
        public static final String PLAY_ERROR_MSG = "errorMsg";
        public static final String PLAY_COVER_URL = "coverUrl";
        public static final String PLAY_PROGRESS = "playProgress";
        public static final String PLAY_SECOND_PROGRESS = "secondProgress";
        public static final String PLAY_TOTAL_TIME = "totoalTime";
    }
}
