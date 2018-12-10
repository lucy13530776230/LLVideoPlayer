package com.lljy.custommediaplayer.view.player;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.lljy.custommediaplayer.R;
import com.lljy.custommediaplayer.constants.ScreenStatus;
import com.lljy.custommediaplayer.constants.VideoEngineType;
import com.lljy.custommediaplayer.constants.VideoStatus;
import com.lljy.custommediaplayer.download.VideoDownloadManager;
import com.lljy.custommediaplayer.entity.VideoEntity;
import com.lljy.custommediaplayer.interfs.IVideoListener;
import com.lljy.custommediaplayer.interfs.IVideoPlayListener;
import com.lljy.custommediaplayer.utils.VideoManager;
import com.lljy.custommediaplayer.view.controller.AbsController;
import com.lljy.custommediaplayer.view.engine.AbsVideoPlayer;
import com.lljy.custommediaplayer.view.engine.AndroidMediaPlayer;
import com.lljy.custommediaplayer.view.engine.LeVideoPlayer;
import com.lljy.custommediaplayer.view.engine.TencentVideoPlayer;

import java.io.File;

/**
 * @desc: 自定义视频播放器抽象类
 * 对乐视视频播放器、腾讯视频播放器进行统一管理
 * 子类可继承后，实现定制皮肤
 * 控制器抽象类{@link AbsController}自带功能有暂停/开始按钮、进度条、触摸快进/倒退、触摸调节音量大小，子类设置的控制器{@link #setController(AbsController)}也许继承自该控制器抽象类
 * 抽象类实现的方法和功能：
 * ①设置控制器（即设置皮肤）{@link #setController(AbsController)}
 * ②设置背景颜色为黑色
 * ③设置播放资源{@link #setVideoSource(VideoEntity)}
 * ④onPause释放资源{@link #onPause()}，onResume恢复播放{@link #onResume()}
 * ⑤跳转到指定进度播放{@link #seekTo(int)}
 * ⑥开始/暂停{@link #playOrPause()}
 * ⑦播放进度实时监听并把状态设置给控制器和返回给调用者：
 * ●播放准备完成{@link #onPrepared()}时设置控制器{@link AbsController}播放状态为准备完成状态;
 * ●播放进度{@link #onProgress(int)}监听当前播放进度，并设置控制器{@link AbsController}进度条进度；
 * ●缓冲进度{@link #onSecondProgress(int)}监听缓冲进度，并设置控制器{@link AbsController}进度条第二条进度；
 * ●总时间{@link #onTotalTime(int)}监听，设置控制器{@link AbsController}总时间显示；
 * ●监听视频加载{@link #onLoadingStart()}和停止加载{@link #onLoadingFinished()}，并设置控制器{@link AbsController}加载进度框状态；
 * ●监听打开全屏和退出全屏点击{@link #pressStartOrExitFullscreen(ScreenStatus)} ()} ()}，让调用者自己控制全屏状态;
 * ●监听返回按钮点击{@link #pressedTitleBack(ScreenStatus)}，让调用者自己控制返回后的操作（是退出全屏还是退出应用等）
 * 抽象类实现了这些基本功能，如需拓展，可在子类进行拓展。
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 20:21
 */

public abstract class AbsCustomVideoPlayer<T extends AbsController> extends RelativeLayout implements IVideoPlayListener {
    protected static final String TAG = "AbsCustomVideoPlayer";
    private AbsVideoPlayer mPlayer;
    protected Context mContext;
    protected T mController;

    protected VideoEntity mVideo;
    protected boolean mIsFirstEnter;//是否是第一次进入

    protected int mCurrentProgress;//当前播放进度

    protected IVideoListener mListener;

    protected boolean mNeedTouchControlVol;//是否需要手指滑动控制音量
    protected boolean mNeedTouchControlProgress;//是否需要手指滑动控制进度
    protected boolean mNeedStartOrExitFullScreenButton;//是否需要退出或者打开全屏的按钮
    protected boolean mNeedTopTitleAndBackLayout;//是否需要顶部布局
    protected boolean mNeedBackButtonOnNormalScreenStatus;//是否在正常屏幕状态下需要返回按钮
    protected boolean mNeedBackButtonOnFullScreenStatus;//是否在全屏状态小需要返回按钮
    protected boolean mNeedTitle;//是否需要标题


    public AbsCustomVideoPlayer(Context context) {
        this(context, null);
    }

    public AbsCustomVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsCustomVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    /**
     * 设置播放器回调监听（给调用者使用）
     *
     * @param listener
     */
    public void setListener(IVideoListener listener) {
        this.mListener = listener;
    }

    /**
     * 设置控制器
     *
     * @param controller 控制器
     */
    public void setController(T controller) {
        this.mController = controller;
        addView(mController);
        if (mController != null) {
            mController.setNeedTouchControlProgress(mNeedTouchControlProgress);
            mController.setNeedTouchControlVol(mNeedTouchControlVol);
            mController.setNeedStartOrExitFullScreenButton(mNeedStartOrExitFullScreenButton);
            mController.setNeedTopTitleAndBackLayout(mNeedTopTitleAndBackLayout);
            mController.setNeedBackButtonOnNormalScreenStatus(mNeedBackButtonOnNormalScreenStatus);
            mController.setNeedBackButtonOnFullScreenStatus(mNeedBackButtonOnFullScreenStatus);
            mController.setNeedTitle(mNeedTitle);
            //初始化屏幕状态
            setScreenStatus(ScreenStatus.SCREEN_STATUS_NORMAL);
        }
        setControllerListener(controller);
    }

    /**
     * 初始化控制器监听
     *
     * @param controller
     */
    protected abstract void setControllerListener(T controller);

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AbsCustomVideoPlayer);
        mNeedTouchControlProgress = typedArray.getBoolean(R.styleable.AbsCustomVideoPlayer_needTouchControlProgress, true);//是否需要手指滑动控制进度，默认true
        mNeedTouchControlVol = typedArray.getBoolean(R.styleable.AbsCustomVideoPlayer_needTouchControlVol, true);//是否需要手指滑动控制音量，默认true
        mNeedStartOrExitFullScreenButton = typedArray.getBoolean(R.styleable.AbsCustomVideoPlayer_needStartOrExitFullScreenButton, true);//是否需要全屏/退出全屏按钮，默认true
        mNeedTopTitleAndBackLayout = typedArray.getBoolean(R.styleable.AbsCustomVideoPlayer_needTopTitleAndBackLayout, true);//是否需要顶部布局，默认true
        mNeedBackButtonOnNormalScreenStatus = typedArray.getBoolean(R.styleable.AbsCustomVideoPlayer_needBackButtonOnNormalScreenStatus, false);//是否需要在正常屏幕状态下显示返回按钮，默认false
        mNeedBackButtonOnFullScreenStatus = typedArray.getBoolean(R.styleable.AbsCustomVideoPlayer_needBackButtonOnFullScreenStatus, true);//是否需要在全屏模式下显示返回按钮，默认true
        mNeedTitle = typedArray.getBoolean(R.styleable.AbsCustomVideoPlayer_needTitle, true);
        typedArray.recycle();
        mCurrentProgress = 0;
        setBackgroundColor(Color.BLACK);
        mContext = context;
        mIsFirstEnter = true;
    }

    protected void setVideoSource(VideoEntity videoEntity) {
        mCurrentProgress = 0;
        if (mController != null) {
            Bundle params = new Bundle();
            if (videoEntity != null) {
                String cover = videoEntity.getCoverUrl();
                params.putString(VideoStatus.Constants.PLAY_COVER_URL, cover);
                params.putString(VideoStatus.Constants.PLAY_TITLE, videoEntity.getVideoName());
            }
            mController.setVideoState(VideoStatus.MEDIA_STATE_PLAY_NEW, params);
        }
        if (videoEntity != null) {
            this.mVideo = videoEntity.clone();
        } else {
            mVideo = null;
        }
        initPlayer(mVideo);
    }

    /**
     * 初始化播放器
     */
    private void initPlayer(VideoEntity videoEntity) {
        release();
        if (videoEntity == null || TextUtils.isEmpty(videoEntity.getId()) || TextUtils.isEmpty(videoEntity.getVideoEngineType())) {
            onError("该视频资源未找到");
            return;
        }
        String uuid = videoEntity.getUu();
        String vuid = videoEntity.getVu();
        String engineType = videoEntity.getVideoEngineType();
        String uniqueKey;
        if (VideoEngineType.TYPE_ANDROID_MEDIA.equals(engineType)) {
            //原生播放器
            uniqueKey = videoEntity.getVideoEngineType() + videoEntity.getId();
            mPlayer = new AndroidMediaPlayer(mContext);
            Log.d(TAG, "使用原生视频播放引擎");
        } else if (VideoEngineType.TYPE_TENCENT.equals(engineType)) {
            //腾讯播放器
            uniqueKey = videoEntity.getVideoEngineType() + videoEntity.getId();
            mPlayer = new TencentVideoPlayer(mContext);
            Log.d(TAG, "使用腾讯视频播放引擎");
        } else if (VideoEngineType.TYPE_LETV.equals(engineType)) {
            //乐视播放器
            uniqueKey = uuid + vuid;
            mPlayer = new LeVideoPlayer(mContext);
            Log.d(TAG, "使用乐视视频播放引擎");
        } else {
            onError("该视频资源未找到");
            return;
        }
        String nativeUrl = null;
        if (VideoManager.getInstance().isEnableDownloadEngine()) {
            nativeUrl = VideoDownloadManager.getInstance().getNativeUrl(uniqueKey);
            if (!VideoDownloadManager.getInstance().isVideoExits(uniqueKey)) {
                nativeUrl = null;
            }
            if (TextUtils.isEmpty(nativeUrl)) {
                VideoDownloadManager.getInstance().addDownloadVideo(videoEntity);
            }
        }
        mVideo.setNativeUrl(nativeUrl);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(CENTER_IN_PARENT);
        addView(mPlayer, 0, params);
        mPlayer.setListener(this);
        mPlayer.setVideoSource(videoEntity);
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
        if (!mIsFirstEnter) {
            initPlayer(mVideo);
        }
        mIsFirstEnter = false;
    }

    /**
     * 播放指定进度
     *
     * @param progress
     */
    public void seekTo(int progress) {
        if (mPlayer != null) {
            mPlayer.seekTo(progress);
        }
    }


    /**
     * 开始播放/暂停
     */
    public void playOrPause() {
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                if (mController != null) {
                    mController.setVideoState(VideoStatus.MEDIA_STATE_PAUSE);
                }
            } else {
                mPlayer.play();
                if (mController != null) {
                    mController.setVideoState(VideoStatus.MEDIA_STATE_RESUME);
                }
            }
        } else {
            setVideoSource(mVideo);
        }
    }

    @Override
    public void onPrepared() {
        if (mCurrentProgress > 0) {
            seekTo(mCurrentProgress);
        }
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_START_PLAY);
        }
    }

    @Override
    public void onProgress(int progress) {
        this.mCurrentProgress = progress;
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
        release();
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_COMPLETE);
        }
        mCurrentProgress = 0;
    }

    @Override
    public void onError(String errorMsg) {
        try {
            if (mController != null) {
                Bundle params = new Bundle();
                params.putString(VideoStatus.Constants.PLAY_ERROR_MSG, errorMsg);
                boolean isPlayCache = mVideo != null && !TextUtils.isEmpty(mVideo.getNativeUrl());
                params.putBoolean(VideoStatus.Constants.IS_PLAY_CACHE, mVideo != null && !TextUtils.isEmpty(mVideo.getNativeUrl()));
                mController.setVideoState(VideoStatus.MEDIA_STATE_ERROR, params);
                if (isPlayCache) {
                    String uu = mVideo.getUu();
                    String vu = mVideo.getVu();
                    String videoEngineType = mVideo.getVideoEngineType();
                    String videoId = mVideo.getId();
                    String uniqueKey;
                    if (VideoEngineType.TYPE_LETV.equals(videoEngineType)) {
                        uniqueKey = uu + vu;
                    } else {
                        uniqueKey = videoEngineType + videoId;
                    }
                    String nativeUrl = mVideo.getNativeUrl();
                    File file = new File(nativeUrl);
                    if (file.exists()) {
                        file.delete();
                    }
                    VideoDownloadManager.getInstance().removeDownloadRecord(uniqueKey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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


    /**
     * 点击了全屏按钮
     */
    protected void pressStartOrExitFullscreen(ScreenStatus currentScreenStatus) {
        if (mListener != null) {
            mListener.onStartOrExitFullScreenPressed(currentScreenStatus);
        }
    }

    /**
     * 点击标题返回按钮后回调给调用者
     */
    protected void pressedTitleBack(ScreenStatus currentScreenStatus) {
        if (mListener != null) {
            mListener.onTitleBackPressed(currentScreenStatus);
        }
    }

    /**
     * 设置屏幕状态
     *
     * @param screenStatus 屏幕状态
     */
    public void setScreenStatus(ScreenStatus screenStatus) {
        if (mController != null) {
            mController.setScreenStatus(screenStatus);
        }
    }

    /**
     * 当前屏幕状态
     *
     * @return 返回NONE说明出问题了
     */
    public ScreenStatus getScreenStatus() {
        return mController != null ? mController.getScreenStatus() : ScreenStatus.NONE;
    }
}
