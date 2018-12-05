package com.lljy.custommediaplayer.view.player;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.lljy.custommediaplayer.constants.ScreenStatus;
import com.lljy.custommediaplayer.constants.VideoStatus;
import com.lljy.custommediaplayer.download.VideoDownloadManager;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.interfs.IVideoListener;
import com.lljy.custommediaplayer.interfs.IVideoPlayListener;
import com.lljy.custommediaplayer.view.controller.AbsController;
import com.lljy.custommediaplayer.view.engine.AbsVideoPlayer;
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
 * ③设置播放资源{@link #setVideoSource(VideoBean)}
 * ④onPause释放资源{@link #onPause()}，onResume恢复播放{@link #onResume()}
 * ⑤跳转到指定进度播放{@link #seekTo(int)}
 * ⑥开始/暂停{@link #playOrPause()}
 * ⑦播放进度实时监听并把状态设置给控制器和返回给调用者：
 * ●播放准备完成{@link #onPrepared()}时设置控制器{@link AbsController}播放状态为准备完成状态;
 * ●播放进度{@link #onProgress(int)}监听当前播放进度，并设置控制器{@link AbsController}进度条进度；
 * ●缓冲进度{@link #onSecondProgress(int)}监听缓冲进度，并设置控制器{@link AbsController}进度条第二条进度；
 * ●总时间{@link #onTotalTime(int)}监听，设置控制器{@link AbsController}总时间显示；
 * ●监听视频加载{@link #onLoadingStart()}和停止加载{@link #onLoadingFinished()}，并设置控制器{@link AbsController}加载进度框状态；
 * ●监听打开全屏{@link #startFullScreen()} ()}和退出全屏{@link #exitFullScreen()} ()}，并将屏幕状态设置方式返回给调用者控制全屏,还可以设置控制器全屏按钮{@link AbsController}状态;
 * 抽象类实现了这些基本功能，如需拓展，可在子类进行拓展。
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 20:21
 */

public abstract class AbsCustomVideoPlayer<T extends AbsController> extends RelativeLayout implements IVideoPlayListener {
    private static final String TAG = "AbsCustomVideoPlayer";
    private AbsVideoPlayer mPlayer;
    private Context mContext;
    protected T mController;

    protected VideoBean mVideo;
    private boolean isFirstEnter;//是否是第一次进入

    private int currentProgress;//当前播放进度

    private IVideoListener mListener;

    public AbsCustomVideoPlayer(Context context) {
        this(context, null);
    }

    public AbsCustomVideoPlayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsCustomVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
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
    private void init(Context context) {
        currentProgress = 0;
        setBackgroundColor(Color.BLACK);
        mContext = context;
        isFirstEnter = true;
    }

    protected void setVideoSource(VideoBean videoBean) {
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
        if (videoBean == null || videoBean.getInfo() == null || TextUtils.isEmpty(videoBean.getVideo_id()) || TextUtils.isEmpty(videoBean.getSource())) {
            onError("该视频资源未找到");
            return;
        }
        String netUrl = videoBean.getSrc();
        String uuid = videoBean.getInfo().getUu();
        String vuid = videoBean.getInfo().getVu();
        String uniqueKey;
        //腾讯播放器
        if (!TextUtils.isEmpty(netUrl) && (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(vuid))) {
            uniqueKey = videoBean.getSource() + videoBean.getVideo_id();
            mPlayer = new TencentVideoPlayer(mContext);
            Log.d(TAG, "使用腾讯视频播放引擎");
        } else if (!TextUtils.isEmpty(uuid) && !TextUtils.isEmpty(vuid)) {
            uniqueKey = uuid + vuid;
            mPlayer = new LeVideoPlayer(mContext);
            Log.d(TAG, "使用乐视视频播放引擎");
        } else {
            onError("该视频资源未找到");
            return;
        }
        String nativeUrl = VideoDownloadManager.getInstance().getNativeUrl(uniqueKey);
        if (!VideoDownloadManager.getInstance().isVideoExits(uniqueKey)) {
            nativeUrl = null;
        }
        if (TextUtils.isEmpty(nativeUrl)) {
            VideoDownloadManager.getInstance().addDownloadVideo(videoBean);
        }
        mVideo.setNativeSrc(nativeUrl);
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
            initPlayer(mVideo);
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
        release();
        if (mController != null) {
            mController.setVideoState(VideoStatus.MEDIA_STATE_COMPLETE);
        }
        currentProgress = 0;
    }

    @Override
    public void onError(String errorMsg) {
        try {
            if (mController != null) {
                Bundle params = new Bundle();
                params.putString(VideoStatus.Constants.PLAY_ERROR_MSG, errorMsg);
                boolean isPlayCache = mVideo != null && !TextUtils.isEmpty(mVideo.getNativeSrc());
                params.putBoolean(VideoStatus.Constants.IS_PLAY_CACHE, mVideo != null && !TextUtils.isEmpty(mVideo.getNativeSrc()) && mVideo.getInfo() != null);
                mController.setVideoState(VideoStatus.MEDIA_STATE_ERROR, params);
                if (isPlayCache) {
                    String uu = mVideo.getInfo().getUu();
                    String vu = mVideo.getInfo().getVu();
                    String source = mVideo.getSource();
                    String videoId = mVideo.getVideo_id();
                    String uniqueKey;
                    if (!TextUtils.isEmpty(uu) && !TextUtils.isEmpty(vu)) {
                        uniqueKey = uu + vu;
                    } else {
                        uniqueKey = source + videoId;
                    }
                    String nativeUrl = mVideo.getNativeSrc();
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
     * 打开全屏
     */
    public void startFullScreen() {
        if (mListener != null) {
            mListener.onStartFullScreen();
        }
    }

    /**
     * 退出全屏
     */
    public void exitFullScreen() {
        if (mListener != null) {
            mListener.onExitFullScreen();
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
