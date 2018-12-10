package com.lljy.custommediaplayer.view.engine;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import com.lljy.custommediaplayer.view.surfaceview.WrapContentSurfaceView;

import java.lang.ref.WeakReference;

/**
 * @desc: android原生的MediaPlayer
 * 添加原生MediaPlayer的目的是为了解决其他能解码的播放器播放url在班牌机器上丢帧的问题
 * 一发现存在这种问题的播放器有：腾讯视频播放器、乐视播放器、ijk播放器、EXO播放器、android官方的VideoView
 * 只有原生的{@link MediaPlayer}才能支持班牌url播放视频不卡顿丢帧（原因班牌机器质量太差）
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/7 9:57
 */

public class AndroidMediaPlayer extends AbsVideoPlayer {
    private static final String TAG = "AndroidMediaPlayer";
    private MediaPlayer mMediaPlayer;//本地多媒体播放器
    private WrapContentSurfaceView mSurfaceView;//播放器播放载体

    private static final int UPDATE_TIME_AND_PROGRESS = 1;
    private TimerHandler mTimerHandler;


    /**
     * 延时500ms重复获取进度和时间，达到实时更新进度条显示时间的目的
     */
    private static class TimerHandler extends Handler {
        private WeakReference<AndroidMediaPlayer> ref;

        public TimerHandler(AndroidMediaPlayer player) {
            this.ref = new WeakReference<>(player);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_TIME_AND_PROGRESS:
                    try {
                        if (ref != null) {
                            AndroidMediaPlayer player = ref.get();
                            if (player != null) {
                                int totalTime = player.getDuration();
                                int progress = player.getCurrentProgress();
                                if (player.mListener != null) {
                                    player.mListener.onProgress(progress);
                                    player.mListener.onTotalTime(totalTime);
                                    sendEmptyMessageDelayed(UPDATE_TIME_AND_PROGRESS, 500);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    public AndroidMediaPlayer(Context context) {
        super(context);
    }

    public AndroidMediaPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AndroidMediaPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
    }

    /**
     * SurfaceView监听
     */
    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                //init player
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.reset();
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setDataSource(TextUtils.isEmpty(mVideo.getNativeUrl()) ? mVideo.getNetUrl() : mVideo.getNativeUrl());
                //让MediaPlayer和SurfaceView进行视频画面的结合
                mMediaPlayer.setDisplay(holder);
                //设置监听
                mMediaPlayer.setOnCompletionListener(onCompletionListener);
                mMediaPlayer.setOnErrorListener(onErrorListener);
                mMediaPlayer.setOnPreparedListener(onPreparedListener);
                mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
                mMediaPlayer.setOnInfoListener(onInfoListener);
//                mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
                mMediaPlayer.setScreenOnWhilePlaying(true);//在视频播放的时候保持屏幕的高亮
                //异步准备
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    /**
     * 初始化播放器
     */
    @Override
    protected void initPlayer() {
        try {
            //release
            release();
            //check
            if (mVideo == null || (TextUtils.isEmpty(mVideo.getNativeUrl()) && TextUtils.isEmpty(mVideo.getNetUrl()))) {
                if (mListener != null) {
                    mListener.onError("无可用播放地址");
                }
                return;
            }
            //init handler
            mTimerHandler = new TimerHandler(this);
            //init surface
            mSurfaceView = new WrapContentSurfaceView(mContext);
            SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
            surfaceHolder.addCallback(mSurfaceHolderCallback);
            //add surface
            LayoutParams params = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            params.addRule(CENTER_IN_PARENT);
            addView(mSurfaceView, params);
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onError("视频初始化出错");
            }
        }
    }

    /**
     * 播放完成监听
     */
    private MediaPlayer.OnCompletionListener onCompletionListener = mp -> {
        Log.d(TAG,"android原生播放器播放完成");
        if (mListener != null) {
            mListener.onComplete();
        }
    };

    /**
     * 播放出错监听
     */
    private MediaPlayer.OnErrorListener onErrorListener = (mp, what, extra) -> {
        Log.e(TAG, "native media error:(" + what + "," + extra + ")");
        if (mListener != null) {
            mListener.onError("播放出错");
        }
        return true;
    };

    /**
     * 准备开始播放监听
     */
    private MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            if (mListener != null) {
                mListener.onPrepared();
            }
            if (mMediaPlayer != null) {
                mMediaPlayer.start();
            }
            if (mTimerHandler != null) {
                mTimerHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
                mTimerHandler.sendEmptyMessageDelayed(UPDATE_TIME_AND_PROGRESS, 500);
            }
        }
    };

    /**
     * 视频大小变化监听
     */
    private MediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            if (mSurfaceView != null) {
                mSurfaceView.adaptVideoSize(width, height);
            }
        }
    };

    /**
     * 播放信息监听（缓冲）
     */
    private MediaPlayer.OnInfoListener onInfoListener = (mp, what, extra) -> {
        switch (what) {
            //开始loading，显示菊花
            case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                if (mListener != null) {
                    mListener.onLoadingStart();
                }
                break;
            //loading完成，隐藏菊花
            case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                if (mListener != null) {
                    mListener.onLoadingFinished();
                }
                break;
        }
        return true;
    };

//    /**
//     * 缓冲进度条监听
//     */
//    private MediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = (mp, percent) -> {
//        if (mListener != null) {
//            mListener.onSecondProgress(percent);
//        }
//    };


    /**
     * 播放
     */
    @Override
    public void play() {
        try {
            if (mMediaPlayer != null && hasPaused) {
                mMediaPlayer.start();
                hasPaused = false;
            } else {
                initPlayer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停
     */
    @Override
    public void pause() {
        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                hasPaused = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放播放器
     */
    @Override
    public void release() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.release();
                mMediaPlayer.setSurface(null);
                mMediaPlayer = null;
            }
            if (mTimerHandler != null) {
                mTimerHandler.removeCallbacksAndMessages(null);
                mTimerHandler = null;
            }
            if (mSurfaceView != null) {
                removeView(mSurfaceView);
                mSurfaceView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hasPaused = false;
        }
    }

    /**
     * 跳转到指定进度
     *
     * @param progress
     */
    @Override
    public void seekTo(int progress) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.seekTo(progress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否正在播放
     *
     * @return true表示正在播放，false表示未播放
     */
    @Override
    public boolean isPlaying() {
        try {
            return mMediaPlayer != null && mMediaPlayer.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取视频总时长
     *
     * @return 返回视频总时长
     */
    @Override
    public int getDuration() {
        try {
            return mMediaPlayer == null ? 0 : mMediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前进度
     *
     * @return 返回当前视频进度
     */
    @Override
    public int getCurrentProgress() {
        try {
            return mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
