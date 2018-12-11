package com.lljy.custommediaplayer.view.controller;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lljy.custommediaplayer.R;
import com.lljy.custommediaplayer.constants.ScreenStatus;
import com.lljy.custommediaplayer.constants.VideoStatus;
import com.lljy.custommediaplayer.gesture.ControllerSimpleGestureListener;
import com.lljy.custommediaplayer.gesture.GestureResultListener;
import com.lljy.custommediaplayer.interfs.ControllerListener;
import com.lljy.custommediaplayer.utils.VideoTimeUtils;

import java.lang.ref.WeakReference;

/**
 * @desc: 控制布局抽象类，用来进行播放进度、开始/暂停、快进倒退的管理
 * 基本部件有：
 * ①进度条{@link #mSeekBar}
 * ②开始时间{@link #mStartTimeTv}
 * ③总时间{@link #mTotalTimeTv}
 * ④封面{@link #mCoverIv}
 * ⑤开始/暂停图标{@link #mPlayOrPauseIv}
 * ⑥loading框{@link #mLoadingRl}
 * ⑦播放出错布局{@link #mErrorRl}
 * ⑧错误内容{@link #mErrorTv}
 * ⑨提示框{@link #mHintView}
 * 注意：这些基本控件在子类是不需要重新初始化的
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/30 11:04
 */

public abstract class AbsController<T extends ControllerListener> extends RelativeLayout implements View.OnTouchListener, GestureResultListener {
    protected static final String TAG = "AbsController";
    protected GestureDetector mGestureDetector;//手势器
    protected ControllerSimpleGestureListener mControllerSimpleGestureListener;//手势器回调接口

    protected SeekBar mSeekBar;//进度条
    protected TextView mStartTimeTv;//开始时间
    protected TextView mTotalTimeTv;//总时间
    protected ImageView mCoverIv;//封面
    protected ImageView mPlayOrPauseIv;//开始/暂停图标
    protected RelativeLayout mLoadingRl;//进度框
    protected RelativeLayout mErrorRl;//播放出错布局
    protected TextView mErrorTv;//错误内容
    protected ShowControlView mHintView;//提示框


    private ImageView mFullScreenIv;//全屏按钮

    private LinearLayout mPlayProgressLl;//底部进度条父布局

    protected T mListener;//控制页面回调接口

    protected AudioManager mAudioManager;
    protected int maxVolume = 0;//最大音量
    protected int oldVolume = 0;//历史音量

    protected int oldFF_REWProgress = 0;//手指按下时老的播放进度
    protected float newPlaybackTime;//拖动后新的播放进度

    protected int mTotalTime;//总时间
    protected int mCurrentProgress;//当前播放进度

    protected boolean mIsPlayError;//是否播放错误
    protected boolean mIsComplete;//是否播放完毕
    protected boolean mIsPause;//是否暂停
    protected boolean mIsLoading;//是否正在加载

    protected ScreenStatus mScreenStatus = ScreenStatus.SCREEN_STATUS_NORMAL;//默认状态为退出全屏

    protected HideControlRunnable mHideRunnable;//延时隐藏线程
    protected Handler mHandler;
    protected static final long delayHideMillis = 2000;

    protected boolean isControlVisible = false;

    protected LinearLayout mTopLl;
    protected RelativeLayout mBackRl;
    protected TextView mTitleTv;

    protected TextView mReloadTv;

    protected boolean mNeedTopTitleAndBackLayout;//是否需要顶部布局
    protected boolean mNeedBackButtonOnNormalStatus;//是否在正常屏幕状态下需要返回按钮
    protected boolean mNeedBackButtonOnFullScreenStatus;//是否在全屏状态下需要返回按钮

    /**
     * 隐藏control布局的Runnable
     */
    private static class HideControlRunnable implements Runnable {
        private WeakReference<AbsController> ref;

        public HideControlRunnable(AbsController controller) {
            this.ref = new WeakReference<>(controller);
        }

        @Override
        public void run() {
            try {
                //延时关闭控制布局
                if (ref != null && ref.get() != null) {
                    ref.get().delayControlVisibility(GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public AbsController(Context context) {
        this(context, null);
    }

    public AbsController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    /**
     * 初始化
     *
     * @param context
     */
    protected void init(Context context) {
        try {
            mHandler = new Handler();
            mHideRunnable = new HideControlRunnable(this);
            //初始化获取音量属性
            mAudioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
            maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            View contentView = LayoutInflater.from(context).inflate(getLayoutId(), this, true);
            //顶部
            mTopLl = contentView.findViewById(R.id.top_ll);
            mBackRl = contentView.findViewById(R.id.back_rl);
            if (mBackRl != null) {
                mBackRl.setOnClickListener(v -> clickBack());
            }
            mTitleTv = contentView.findViewById(R.id.title_tv);
            //底部
            mTotalTimeTv = contentView.findViewById(R.id.duration_tv);
            mStartTimeTv = contentView.findViewById(R.id.play_start_tv);
            mPlayProgressLl = contentView.findViewById(R.id.play_progress);
            //进度条
            mSeekBar = contentView.findViewById(R.id.seekbar);
            if (mSeekBar != null) {
                mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        //停止拖动，获取总进度
                        int totalTime = mSeekBar.getProgress();
                        //跳转到当前位置
                        if (mListener != null) {
                            mListener.onSeekTo(totalTime);
                        }
                    }
                });
            }
            //封面
            mCoverIv = contentView.findViewById(R.id.cover_iv);
            //开始/暂停
            mPlayOrPauseIv = contentView.findViewById(R.id.play_iv);
            if (mPlayOrPauseIv != null) {
                mPlayOrPauseIv.setOnClickListener(v -> {
                    if (mListener != null && !mIsPlayError) {
                        mListener.onPlayOrPausePressed();
                    }
                });
            }

            //加载
            mLoadingRl = contentView.findViewById(R.id.loading_rl);
            //错误布局
            mErrorRl = contentView.findViewById(R.id.error_rl);
            mErrorTv = contentView.findViewById(R.id.error_tv);
            mReloadTv = contentView.findViewById(R.id.click_reload_tv);
            mReloadTv.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onReloadClick();
                }
            });
            //手势相关
            mControllerSimpleGestureListener = new ControllerSimpleGestureListener(this);
            mGestureDetector = new GestureDetector(context, mControllerSimpleGestureListener);
            //取消长按，不然会影响滑动
            mGestureDetector.setIsLongpressEnabled(false);
            //提示窗
            mHintView = contentView.findViewById(R.id.scl);
            //设置触摸监听
            setOnTouchListener(this);

            //手势器监听设置
            if (mControllerSimpleGestureListener != null) {
                mControllerSimpleGestureListener.setVideoGestureListener(this);
            }

            //全屏按钮
            mFullScreenIv = contentView.findViewById(R.id.full_screen_iv);
            mFullScreenIv.setOnClickListener(v -> startOrExitFullScreen());

            initExtensionViews(contentView, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 打开或者退出全屏
     */
    private void startOrExitFullScreen() {
        if (mListener != null) {
            mListener.onStartOrExitFullScreenPressed(mScreenStatus);
        }
    }

    /**
     * 点击了返回按钮
     */
    protected void clickBack() {
        if (mListener != null) {
            mListener.onTitleBackPressed(mScreenStatus);
        }
    }


    /**
     * 设置全屏状态 设置全屏状态后会更改全屏按钮样式、返回按钮可见/不可见
     *
     * @param screenStatus 全屏状态
     */
    public void setScreenStatus(ScreenStatus screenStatus) {
        this.mScreenStatus = screenStatus;
        if (mNeedTopTitleAndBackLayout && mBackRl != null) {
            if (mScreenStatus == ScreenStatus.SCREEN_STATUS_FULL) {
                if (mNeedBackButtonOnFullScreenStatus) {
                    if (mBackRl.getVisibility() != VISIBLE) {
                        mBackRl.setVisibility(VISIBLE);
                    }
                } else {
                    if (mBackRl.getVisibility() != GONE) {
                        mBackRl.setVisibility(GONE);
                    }
                }
                if (mFullScreenIv != null) {
                    //图标改为退出全屏
                    mFullScreenIv.setImageResource(R.drawable.video_exit);
                }
            } else {
                if (mNeedBackButtonOnNormalStatus) {
                    if (mBackRl.getVisibility() != VISIBLE) {
                        mBackRl.setVisibility(VISIBLE);
                    }
                } else {
                    if (mBackRl.getVisibility() != GONE) {
                        mBackRl.setVisibility(GONE);
                    }
                }
                if (mFullScreenIv != null) {
                    //图标改为打开全屏
                    mFullScreenIv.setImageResource(R.drawable.video_full_screen);
                }
            }
        }
    }

    /**
     * 设置是否可触摸控制视频播放进度
     *
     * @param needTouchControlProgress 是否可触摸控制播放进度
     */
    public void setNeedTouchControlProgress(boolean needTouchControlProgress) {
        if (mControllerSimpleGestureListener != null) {
            mControllerSimpleGestureListener.setNeedTouchControlProgress(needTouchControlProgress);
        }
    }

    /**
     * 设置是否可触摸控制视频音量
     *
     * @param needTouchControlVol 是否可触摸可触摸控制音量
     */
    public void setNeedTouchControlVol(boolean needTouchControlVol) {
        if (mControllerSimpleGestureListener != null) {
            mControllerSimpleGestureListener.setNeedTouchControlVol(needTouchControlVol);
        }
    }


    /**
     * 设置是否需要全屏/退出全屏按钮
     *
     * @param needStartOrExitFullScreenButton 是否需要
     */
    public void setNeedStartOrExitFullScreenButton(boolean needStartOrExitFullScreenButton) {
        if (mFullScreenIv != null) {
            mFullScreenIv.setVisibility(needStartOrExitFullScreenButton ? VISIBLE : GONE);
        }
    }

    /**
     * 设置是否需要顶部的标题和返回按钮
     *
     * @param needTopTitleAndBackLayout 是否
     */
    public void setNeedTopTitleAndBackLayout(boolean needTopTitleAndBackLayout) {
        this.mNeedTopTitleAndBackLayout = needTopTitleAndBackLayout;
    }

    /**
     * 正常屏幕状态下是否需要返回按钮
     *
     * @param needBackButtonOnNormalScreenStatus 是否在非全屏状态下需要返回按钮
     */
    public void setNeedBackButtonOnNormalScreenStatus(boolean needBackButtonOnNormalScreenStatus) {
        this.mNeedBackButtonOnNormalStatus = needBackButtonOnNormalScreenStatus;
    }

    /**
     * 是否需要返回按钮
     *
     * @param needBackButtonOnFullScreenStatus 是否需要
     */
    public void setNeedBackButtonOnFullScreenStatus(boolean needBackButtonOnFullScreenStatus) {
        this.mNeedBackButtonOnFullScreenStatus = needBackButtonOnFullScreenStatus;
    }

    /**
     * 设置是否需要标题
     *
     * @param needTitle 是否需要
     */
    public void setNeedTitle(boolean needTitle) {
        if (mTitleTv != null) {
            mTitleTv.setVisibility(needTitle ? VISIBLE : GONE);
        }
    }

    /**
     * 是否需要重试按钮
     *
     * @param needReloadButton 是否需要
     */
    public void setNeedReloadButton(boolean needReloadButton) {
        if (mReloadTv != null) {
            mReloadTv.setVisibility(needReloadButton ? VISIBLE : GONE);
        }
    }

    /**
     * 设置标题
     *
     * @param title
     */
    private void setTitle(String title) {
        if (mTitleTv != null) {
            mTitleTv.setText(title);
        }
    }

    /**
     * 设置顶部可见
     */
    protected void showTitleAndBack() {
        if (mTopLl != null && mTopLl.getVisibility() != VISIBLE) {
            mTopLl.setVisibility(VISIBLE);
        }
    }

    /**
     * 设置顶部不可见
     */
    protected void dismissTitleAndBack() {
        if (mTopLl != null && mTopLl.getVisibility() != GONE) {
            mTopLl.setVisibility(GONE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            if (mHandler != null) {
                mHandler.removeCallbacks(mHideRunnable);
            }
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            if (mHandler != null) {
                mHandler.postDelayed(mHideRunnable, delayHideMillis);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 设置控制页面回调接口
     *
     * @param listener 控制页回调给播放器
     */
    public void setListener(T listener) {
        this.mListener = listener;
    }

    /**
     * 给子类初始化拓展控件用的
     *
     * @param contentView 内容view
     * @param context     上下文
     */
    protected abstract void initExtensionViews(View contentView, Context context);

    /**
     * 获取子控件布局id
     *
     * @return 返回布局id
     */
    protected abstract int getLayoutId();


    /**
     * 播放状态设置
     *
     * @param state 设置播放状态
     */
    public void setVideoState(int state) {
        setVideoState(state, null);
    }

    /**
     * 播放状态设置
     *
     * @param state  设置播放状态
     * @param params 额外参数
     */
    public void setVideoState(int state, Bundle params) {
        try {
            switch (state) {
                case VideoStatus.MEDIA_STATE_RELEASE:
                    onReleaseHandle();
                    break;
                case VideoStatus.MEDIA_STATE_PAUSE://暂停
                    onPauseHandle();
                    break;
                case VideoStatus.MEDIA_STATE_RESUME://恢复播放
                    onResumeHandle();
                    break;
                case VideoStatus.MEDIA_STATE_PLAY_NEW://播放新视频
                    onPlayNewHandle(params);
                    break;
                case VideoStatus.MEDIA_STATE_START_PLAY://播放器准备好，即将播放
                    onStartPlayHandle();
                    break;
                case VideoStatus.MEDIA_STATE_ERROR:///播放出错，显示错误信息
                    onErrorHandle(params);
                    break;
                case VideoStatus.MEDIA_STATE_BUFFER_START://loading开始
                    onBufferStartHandle();
                    break;
                case VideoStatus.MEDIA_STATE_BUFFER_END://loading结束
                    onBufferEndHandle();
                    break;
                case VideoStatus.MEDIA_STATE_COMPLETE://播放完成
                    onCompleteHandle();
                    break;
                case VideoStatus.MEDIA_STATE_PLAY_PROGRESS://播放进度
                    onPlayProgressHandle(params);
                    break;
                case VideoStatus.MEDIA_STATE_PLAY_SECOND_PROGRESS://缓冲进度
                    onSecondPlayProgressHandle(params);
                    break;
                case VideoStatus.MEDIA_STATE_PLAY_TOTAL_TIME://总时长
                    onTotalTimeHandle(params);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置封面
     *
     * @param cover 封面url
     */
    protected void setCover(String cover) {
        try {
            if (mCoverIv != null && mListener != null) {
                mListener.onCoverLoad(mCoverIv, cover);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示封面
     */
    protected void showCover() {
        if (mCoverIv != null && mCoverIv.getVisibility() != VISIBLE) {
            mCoverIv.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏封面
     */
    protected void dismissCover() {
        if (mCoverIv != null && mCoverIv.getVisibility() != GONE) {
            mCoverIv.setVisibility(GONE);
        }
    }

    /**
     * 加载loading
     */
    protected void startLoading() {
        mIsLoading = true;
        if (mLoadingRl != null && mLoadingRl.getVisibility() != VISIBLE) {
            mLoadingRl.setVisibility(VISIBLE);
        }
        delayControlVisibility(GONE);
        dismissErrorLayout();
        if (mNeedTopTitleAndBackLayout) {
            showTitleAndBack();
        }
    }

    /**
     * 停止加载
     */
    protected void stopLoading() {
        mIsLoading = false;
        if (mLoadingRl != null && mLoadingRl.getVisibility() != GONE) {
            mLoadingRl.setVisibility(GONE);
        }
        dismissTitleAndBack();
    }

    /**
     * 显示暂停、播放按钮
     */
    protected void showPlayOrPauseIv() {
        if (mPlayOrPauseIv != null && mPlayOrPauseIv.getVisibility() != VISIBLE) {
            mPlayOrPauseIv.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏暂停、播放按钮
     */
    protected void dismissPlayOrPauseIv() {
        if (mPlayOrPauseIv != null && mPlayOrPauseIv.getVisibility() != GONE) {
            mPlayOrPauseIv.setVisibility(GONE);
        }
    }


    /**
     * 显示底部布局
     */
    protected void showBottomProgress() {
        if (mPlayProgressLl != null && mPlayProgressLl.getVisibility() != VISIBLE && !mIsComplete && !mIsPlayError) {
            mPlayProgressLl.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏底部布局
     */
    protected void dismissBottomProgress() {
        if (mPlayProgressLl != null && mPlayProgressLl.getVisibility() != GONE) {
            mPlayProgressLl.setVisibility(GONE);
        }
    }

    /**
     * 播放出错
     *
     * @param errorMsg 错误信息
     */
    protected void showErrorLayout(String errorMsg) {
        if (mErrorRl != null && mErrorRl.getVisibility() != VISIBLE) {
            mErrorRl.setVisibility(VISIBLE);
        }
        if (mErrorTv != null) {
            mErrorTv.setText(errorMsg);
        }
    }

    /**
     * 隐藏播放出错
     */
    protected void dismissErrorLayout() {
        if (mErrorRl != null && mErrorRl.getVisibility() != GONE) {
            mErrorRl.setVisibility(GONE);
        }
    }


    /**
     * 快进、倒退提示框
     *
     * @param title
     * @param value
     */
    protected void showHintDialog(String title, String value) {
        if (mHintView != null) {
            mHintView.show(title, value);
        }
    }

    /**
     * 播放总时长设置
     *
     * @param totalTime
     */
    protected void setTotalTime(int totalTime) {
        Log.e(TAG, "总进度：" + totalTime);
        this.mTotalTime = totalTime;
        if (mSeekBar != null) {
            mSeekBar.setMax(totalTime);
        }
        if (mTotalTimeTv != null) {
            mTotalTimeTv.setText(VideoTimeUtils.updateTimeFormat(totalTime));
        }
    }

    /**
     * 当前播放进度设置
     *
     * @param currentProgress 当前进度
     */
    protected void setProgress(int currentProgress) {
        Log.e(TAG, "当前播放进度：" + currentProgress);
        this.mCurrentProgress = currentProgress;
        if (mSeekBar != null) {
            mSeekBar.setProgress(currentProgress);
        }
        if (mStartTimeTv != null) {
            mStartTimeTv.setText(VideoTimeUtils.updateTimeFormat(currentProgress));
        }
    }

    /**
     * 设置缓冲进度
     *
     * @param secondProgress 缓冲进度
     */
    protected void setSecondProgress(int secondProgress) {
        if (mSeekBar != null) {
            mSeekBar.setSecondaryProgress(secondProgress);
        }
    }

    /**
     * 控制布局延时显示、隐藏
     *
     * @param visibility 显示/隐藏
     */
    protected void delayControlVisibility(int visibility) {
        if (mHandler != null) {
            mHandler.removeCallbacks(mHideRunnable);
        }
        if (mIsPlayError) {//播放错误状态下不准显示这些东西
            isControlVisible = false;
            dismissPlayOrPauseIv();
            dismissBottomProgress();
            stopLoading();
        } else {
            if (visibility == VISIBLE) {
                isControlVisible = true;
                showPlayOrPauseIv();
                showBottomProgress();
                if (mNeedTopTitleAndBackLayout) {
                    showTitleAndBack();
                }
                if (mHandler != null) {
                    mHandler.postDelayed(mHideRunnable, delayHideMillis);
                }
            } else {
                if (!mIsComplete && !mIsPause) {
                    dismissPlayOrPauseIv();
                }
                if (!mIsLoading) {
                    dismissTitleAndBack();
                }
                dismissBottomProgress();
                isControlVisible = false;
            }
        }
    }

    /**
     * 获取当前屏幕状态
     *
     * @return 返回屏幕状态
     */
    public ScreenStatus getScreenStatus() {
        return this.mScreenStatus;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mControllerSimpleGestureListener != null && mControllerSimpleGestureListener.isHasFF_REW()) {
                onEndFF_REW(event);
                mControllerSimpleGestureListener.setHasFF_REW(false);
            }
        }
        //监听触摸事件
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onBrightnessGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        Log.e(TAG, "亮度");
    }

    @Override
    public void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        Log.e(TAG, "音量");
        int value = getHeight() / maxVolume;
        int newVolume = (int) ((e1.getY() - e2.getY()) / value + oldVolume);

        if (newVolume > maxVolume) {
            newVolume = maxVolume;
        } else if (newVolume < 0) {
            newVolume = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);


        //要强行转Float类型才能算出小数点，不然结果一直为0
        int volumeProgress = (int) (newVolume / Float.valueOf(maxVolume) * 100);
        if (mHintView != null) {
            mHintView.show("音量", volumeProgress + "%");
        }
    }

    @Override
    public void onFF_REWGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mIsPlayError || mIsPause || mIsComplete) {
            return;
        }
        float duration = mTotalTime / 1000;
        float offset = e2.getX() - e1.getX();
        //根据移动的正负决定快进还是快退
        if (offset > 0) {
//            scl.setImageResource(R.drawable.ff);
            int newFF_REWProgress = (int) (oldFF_REWProgress + offset / getWidth() * 100);
            if (newFF_REWProgress > 100) {
                newFF_REWProgress = 100;
            }
            newPlaybackTime = (newFF_REWProgress * duration) / 100;
            String newPlaybackTimeStr = String.format("%02d:%02d", ((int) newPlaybackTime / 60), ((int) newPlaybackTime % 60));
            String durationStr = String.format("%02d:%02d", ((int) duration / 60), ((int) duration % 60));
            showHintDialog("前进", newPlaybackTimeStr + "/" + durationStr);

        } else {
            int newFF_REWProgress = (int) (oldFF_REWProgress + offset / getWidth() * 100);
            if (newFF_REWProgress < 0) {
                newFF_REWProgress = 0;
            }
            newPlaybackTime = (newFF_REWProgress * duration) / 100;
            String newPlaybackTimeStr = String.format("%02d:%02d", ((int) newPlaybackTime / 60), ((int) newPlaybackTime % 60));
            String durationStr = String.format("%02d:%02d", ((int) duration / 60), ((int) duration % 60));
            showHintDialog("后退", newPlaybackTimeStr + "/" + durationStr);
        }
    }

    @Override
    public void onSingleTapGesture(MotionEvent e) {
        Log.e(TAG, "single tap");
        delayControlVisibility(isControlVisible ? GONE : VISIBLE);
    }

    @Override
    public void onDoubleTapGesture(MotionEvent e) {
        Log.e(TAG, "onDoubleTapGesture: ");
    }

    @Override
    public void onDown(MotionEvent e) {
        //每次按下的时候更新当前音量，还有进度
        if (mTotalTime != 0) {
            oldFF_REWProgress = ((100 * mCurrentProgress) / mTotalTime);
        } else {
            oldFF_REWProgress = 0;
        }
        oldVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onEndFF_REW(MotionEvent e) {
        if (mListener != null && !mIsPlayError && !mIsPause && !mIsComplete) {
            mListener.onSeekTo((int) newPlaybackTime * 1000);
        }
    }

    /*******处理视频实时信息*******/


    /**
     * 释放视频的处理
     */
    protected void onReleaseHandle() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        delayControlVisibility(GONE);
        dismissErrorLayout();
        showCover();
        if (mHintView != null) {
            mHintView.release();
        }
    }

    /**
     * 暂停视频的处理
     */
    protected void onPauseHandle() {
        mIsPause = true;
        if (mPlayOrPauseIv != null) {
            mPlayOrPauseIv.setImageResource(R.drawable.play_start);
        }
        delayControlVisibility(VISIBLE);
    }

    /**
     * 恢复播放视频的处理
     */
    protected void onResumeHandle() {
        mIsPause = false;
        if (mPlayOrPauseIv != null) {
            mPlayOrPauseIv.setImageResource(R.drawable.play_pause);
        }
        delayControlVisibility(GONE);
        dismissCover();
    }

    /**
     * 播放新视频的处理
     *
     * @param params 新视频参数
     */
    protected void onPlayNewHandle(Bundle params) {
        mIsPlayError = false;
        mIsComplete = false;
        if (params != null) {
            String coverUrl = params.getString(VideoStatus.Constants.PLAY_COVER_URL);
            setCover(coverUrl);
            String title = params.getString(VideoStatus.Constants.PLAY_TITLE);
            setTitle(title);
        }
        delayControlVisibility(GONE);
        dismissErrorLayout();
        showCover();
        startLoading();
    }

    /**
     * 开始播放的处理
     */
    protected void onStartPlayHandle() {
        if (mPlayOrPauseIv != null) {
            mPlayOrPauseIv.setImageResource(R.drawable.play_pause);
        }
        dismissCover();
        stopLoading();
        dismissErrorLayout();
        delayControlVisibility(GONE);
    }

    /**
     * 播放出错的处理
     *
     * @param params
     */
    protected void onErrorHandle(Bundle params) {
        mIsPlayError = true;
        String errorMsg = "视频播放出错";
        if (params != null) {
            errorMsg = params.getString(VideoStatus.Constants.PLAY_ERROR_MSG);
        }
        delayControlVisibility(GONE);
        stopLoading();
        showErrorLayout(errorMsg);
    }

    /**
     * loading开始处理
     */
    protected void onBufferStartHandle() {
        Log.d(TAG, "loading开始");
        startLoading();
    }

    /**
     * loading结束处理
     */
    protected void onBufferEndHandle() {
        Log.d(TAG, "loading结束");
        stopLoading();
    }

    /**
     * 处理播放完成的情况
     */
    protected void onCompleteHandle() {
        mIsComplete = true;
        if (mPlayOrPauseIv != null) {
            mPlayOrPauseIv.setImageResource(R.drawable.play_start);
        }
        delayControlVisibility(GONE);
        dismissErrorLayout();
        showCover();
        showPlayOrPauseIv();
    }

    /**
     * 播放进度处理
     *
     * @param params 进度参数
     */
    protected void onPlayProgressHandle(Bundle params) {
        if (params != null) {
            int progress = params.getInt(VideoStatus.Constants.PLAY_PROGRESS);
            setProgress(progress);
        }
    }

    /**
     * 缓冲进度处理
     *
     * @param params 缓冲进度参数
     */
    protected void onSecondPlayProgressHandle(Bundle params) {
        if (params != null) {
            int progress = params.getInt(VideoStatus.Constants.PLAY_SECOND_PROGRESS);
            setSecondProgress(progress);
        }
    }

    /**
     * 总时长处理
     *
     * @param params 总时长参数
     */
    protected void onTotalTimeHandle(Bundle params) {
        if (params != null) {
            int total = params.getInt(VideoStatus.Constants.PLAY_TOTAL_TIME);
            setTotalTime(total);
        }
    }
}
