package com.lljy.custommediaplayer.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.lljy.custommediaplayer.constants.VideoStatus;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.gesture.ControllerListener;
import com.lljy.custommediaplayer.interfs.IVideoListener;

/**
 * @desc: 自定义视频播放器
 * 对乐视视频播放器、腾讯视频播放器进行统一管理
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 20:21
 */

public class CustomVideoView extends RelativeLayout implements IVideoListener, ControllerListener {
    private static final String TAG = "xgw_video";
    private AbsVideoPlayer mPlayer;
    private Context mContext;
    private AbsController mController;

    private VideoBean mVideo;
    private boolean isFirstEnter;//是否是第一次进入

    private int currentProgress;//当前播放进度

    public CustomVideoView(Context context) {
        this(context, null);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 设置控制器
     *
     * @param controller 控制器
     */
    public void setController(AbsController controller) {
        this.mController = controller;
        if (mController != null) {
            mController.setControllerListener(this);
        }
        addView(mController);
    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        currentProgress = 0;
        setBackgroundColor(Color.BLACK);
        mContext = context;
        isFirstEnter = true;
    }

    public void setVideoSource(VideoBean videoBean) {
        currentProgress = 0;
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_PLAY_NEW);
        }
        this.mVideo = videoBean;
        initPlayer(videoBean);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer(VideoBean videoBean) {
        release();
        if (videoBean == null || videoBean.getInfo() == null) {
            onError("该视频资源未找到");
            return;
        }
        String nativeUrl = videoBean.getNativeSrc();
        String netUrl = videoBean.getSrc();
        String uuid = videoBean.getInfo().getUu();
        String vuid = videoBean.getInfo().getVu();
        //腾讯播放器
        if (!TextUtils.isEmpty(nativeUrl) || (!TextUtils.isEmpty(netUrl) && (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(vuid)))) {
            mPlayer = new TencentVideoPlayer(mContext);
            Log.d(TAG, "使用腾讯视频播放引擎");
        } else if (!TextUtils.isEmpty(uuid) && !TextUtils.isEmpty(vuid)) {
            mPlayer = new LeVideoPlayer(mContext);
            Log.d(TAG, "使用乐视视频播放引擎");
        } else {
            onError("该视频资源未找到");
            return;
        }
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mPlayer, 0, params);
        mPlayer.setListener(this);
        mPlayer.setVideoSource(videoBean);
    }

    /**
     * 释放播放器
     */
    private void release() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer.setListener(null);
            removeView(mPlayer);
            mPlayer = null;
        }
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_RELEASE);
        }
    }

    /**
     * 对应activity/fragment的onPause()方法
     */
    public void onPause() {
        release();
    }

    /**
     * 对应activity/fragment的onResume()方法
     */
    public void onResume() {
        if (!isFirstEnter) {
            initPlayer(mVideo);
        }
        isFirstEnter = false;
    }

    /**
     * 播放指定进度
     *
     * @param progress
     */
    private void seekTo(int progress) {
        if (mPlayer != null) {
            mPlayer.seekTo(progress);
        }
    }

    @Override
    public void onPrepared() {
        Log.d(TAG, "onPrepared current progress:" + currentProgress);
        if (currentProgress > 0) {
            seekTo(currentProgress);
        }
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_START_PLAY);
        }
    }

    @Override
    public void onProgress(int progress) {
        this.currentProgress = progress;
        if (mController != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(VideoStatus.Constants.PLAY_PROGRESS, progress);
            mController.setVideoState(VideoStatus.MEDIA_STATE_PLAY_PROGRESS, bundle);
        }
    }

    @Override
    public void onSecondProgress(int progress) {
        if (mController != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(VideoStatus.Constants.PLAY_SECOND_PROGRESS, progress);
            mController.setVideoState(VideoStatus.MEDIA_STATE_PLAY_SECOND_PROGRESS, bundle);
        }
    }

    @Override
    public void onTotalTime(int total) {
        if (mController != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(VideoStatus.Constants.PLAY_TOTAL_TIME, total);
            mController.setVideoState(VideoStatus.MEDIA_STATE_PLAY_TOTAL_TIME, bundle);
        }
    }

    @Override
    public void onComplete() {
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_COMPLETE);
        }
        currentProgress = 0;
    }

    @Override
    public void onError(String errorMsg) {
        if (mController != null) {
            Bundle bundle = new Bundle();
            bundle.putString(VideoStatus.Constants.PLAY_ERROR_MSG, errorMsg);
            mController.setVideoState(VideoStatus.MEDIA_STATE_ERROR, bundle);
        }
    }

    @Override
    public void onLoadingStart() {
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_BUFFER_START);
        }
    }

    @Override
    public void onLoadingFinished() {
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_BUFFER_END);
        }
    }

    @Override
    public void onStartFullScreen() {
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_START_FULL_SCREEN);
        }
    }

    @Override
    public void onExitFullScreen() {
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_EXIT_FULL_SCREEN);
        }

    }

    /**
     * 拖拽到指定进度
     *
     * @param progress 指定进度
     */
    @Override
    public void onSeekTo(int progress) {
        seekTo(progress);
    }

    /**
     * 点击了开始播放/暂停
     */
    @Override
    public void onPlayOrPauseClick() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                if (mController != null) {
                    mController.setVideoState(VideoStatus.MEDIA_STATE_PAUSE);
                }
            } else {
                mPlayer.play();
            }
        } else {
            initPlayer(mVideo);
        }
    }
}
