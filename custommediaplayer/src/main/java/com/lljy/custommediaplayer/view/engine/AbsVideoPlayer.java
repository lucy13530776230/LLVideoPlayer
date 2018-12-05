package com.lljy.custommediaplayer.view.engine;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.interfs.IVideoPlayListener;

/**
 * @desc: 视频播放器统一抽象类，实现的子类目前有：
 * ①腾讯视频播放器{@link TencentVideoPlayer}
 * ②乐视视频播放器{@link LeVideoPlayer}
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 18:57
 */

public abstract class AbsVideoPlayer extends RelativeLayout {
    protected static final String TAG = "AbsVideoPlayer";
    protected Context mContext;//上下文
    protected VideoBean mVideo;//视频资源
    protected IVideoPlayListener mListener;//播放监听
    protected boolean hasPaused;//是否暂停了（指的是点击了暂停按钮之类的）

    public AbsVideoPlayer(Context context) {
        this(context, null);
    }

    public AbsVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    protected void init(Context context) {
        this.mContext = context;
        setBackgroundColor(Color.BLACK);
    }

    /**
     * 设置视频资源
     *
     * @param videoBean
     */
    public void setVideoSource(VideoBean videoBean) {
        this.mVideo = videoBean;
        initPlayer();
    }

    /**
     * 初始化播放器
     */
    protected abstract void initPlayer();

    /**
     * 播放
     */
    public abstract void play();

    /**
     * 暂停
     */
    public abstract void pause();

    /**
     * 释放播放器
     */
    public abstract void release();

    /**
     * 设置播放加你听
     *
     * @param listener 回调监听
     */
    public void setListener(IVideoPlayListener listener) {
        this.mListener = listener;
    }

    /**
     * 跳转到指定进度
     *
     * @param progress
     */
    public abstract void seekTo(int progress);

    /**
     * 是否正在播放
     *
     * @return true表示正在播放，false表示未播放
     */
    public abstract boolean isPlaying();


    /**
     * 获取视频总时长
     *
     * @return 返回视频总时长
     */
    public abstract int getDuration();

    /**
     * 获取当前进度
     *
     * @return 返回当前视频进度
     */
    public abstract int getCurrentProgress();
}
