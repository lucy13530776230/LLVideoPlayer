package com.lljy.custommediaplayer.view;

import android.content.Context;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.lljy.custommediaplayer.R;
import com.lljy.custommediaplayer.constants.VideoStatus;
import com.lljy.custommediaplayer.gesture.ControllerListener;
import com.lljy.custommediaplayer.gesture.ControllerSimpleGestureListener;
import com.lljy.custommediaplayer.gesture.GestureResultListener;
import com.lljy.custommediaplayer.utils.VideoTimeUtils;

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

public abstract class AbsController extends RelativeLayout implements View.OnTouchListener, GestureResultListener {
    private static final String TAG = "xgw_video";
    private GestureDetector mGestureDetector;//手势器
    private ControllerSimpleGestureListener mControllerSimpleGestureListener;//手势器回调接口

    private SeekBar mSeekBar;//进度条
    private TextView mStartTimeTv;//开始时间
    private TextView mTotalTimeTv;//总时间
    private ImageView mCoverIv;//封面
    private ImageView mPlayOrPauseIv;//开始/暂停图标
    private RelativeLayout mLoadingRl;//进度框
    private RelativeLayout mErrorRl;//播放出错布局
    private TextView mErrorTv;//错误内容
    private ShowControlView mHintView;//提示框

    private LinearLayout mPlayProgressLl;//底部进度条父布局

    private ControllerListener mListener;//控制页面回调接口


    private int oldFF_REWProgress = 0;//手指按下时老的播放进度
    private float newPlaybackTime;//拖动后新的播放进度

    private int mTotalTime;//总时间
    private int mCurrentProgress;//当前播放进度

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
    private void init(Context context) {
        View contentView = LayoutInflater.from(context).inflate(getLayoutId(), this, true);
        //底部
        mTotalTimeTv = contentView.findViewById(R.id.duration_tv);
        mStartTimeTv = contentView.findViewById(R.id.play_start_tv);
        mPlayProgressLl = contentView.findViewById(R.id.play_progress);
        //进度条
        mSeekBar = contentView.findViewById(R.id.seekbar);
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
        //封面
        mCoverIv = contentView.findViewById(R.id.cover_iv);
        //开始/暂停
        mPlayOrPauseIv = contentView.findViewById(R.id.play_iv);

        //加载
        mLoadingRl = contentView.findViewById(R.id.loading_rl);
        //错误布局
        mErrorRl = contentView.findViewById(R.id.error_rl);
        mErrorTv = contentView.findViewById(R.id.error_tv);

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


        initExtensionViews(contentView);
    }

    /**
     * 设置控制页面回调接口
     *
     * @param listener 控制页回调给播放器
     */
    public void setControllerListener(ControllerListener listener) {
        this.mListener = listener;
    }

    /**
     * 给子类初始化拓展控件用的
     *
     * @param contentView 内容view
     */
    protected abstract void initExtensionViews(View contentView);

    /**
     * 获取子控件布局id
     *
     * @return
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
        switch (state) {
            case VideoStatus.MEDIA_STATE_RELEASE:
                break;
            case VideoStatus.MEDIA_STATE_PAUSE://暂停
                if (mPlayOrPauseIv != null) {
                    mPlayOrPauseIv.setImageResource(R.drawable.play_start);
                }
                showPlayOrPauseIv();
                break;
            case VideoStatus.MEDIA_STATE_RESUME://恢复播放
                if (mPlayOrPauseIv != null) {
                    mPlayOrPauseIv.setImageResource(R.drawable.play_pause);
                }
                dismissPlayOrPauseIv();
                break;
            case VideoStatus.MEDIA_STATE_PLAY_NEW://播放新视频
                if (params != null) {
                    String coverUrl = params.getString(VideoStatus.Constants.PLAY_COVER_URL);
                    setCover(coverUrl);
                    showCover();
                }
                startLoading();
                break;
            case VideoStatus.MEDIA_STATE_START_PLAY://播放器准备好，即将播放
                dismissErrorLayout();
                dismissCover();
                stopLoading();
                setControlVisibility(GONE, GONE);
                break;
            case VideoStatus.MEDIA_STATE_ERROR:///播放出错，显示错误信息
                if (params != null) {
                    String errorMsg = params.getString(VideoStatus.Constants.PLAY_ERROR_MSG);
                    playError(errorMsg);
                }
                break;
            case VideoStatus.MEDIA_STATE_BUFFER_START://loading开始
                Log.d(TAG, "loading开始");
                startLoading();
                break;
            case VideoStatus.MEDIA_STATE_BUFFER_END://loading结束
                Log.d(TAG, "loading结束");
                stopLoading();
                break;
            case VideoStatus.MEDIA_STATE_COMPLETE://播放完成
                if (mPlayOrPauseIv != null) {
                    mPlayOrPauseIv.setImageResource(R.drawable.play_start);
                }
                dismissErrorLayout();
                dismissBottomProgressLayout();
                showPlayOrPauseIv();
                showCover();
                break;
            case VideoStatus.MEDIA_STATE_PLAY_PROGRESS://播放进度
                if (params != null) {
                    int progress = params.getInt(VideoStatus.Constants.PLAY_PROGRESS);
                    setProgress(progress);
                }
                break;
            case VideoStatus.MEDIA_STATE_PLAY_SECOND_PROGRESS://缓冲进度
                if (params != null) {
                    int progress = params.getInt(VideoStatus.Constants.PLAY_SECOND_PROGRESS);
                    setSecondProgress(progress);
                }
                break;
            case VideoStatus.MEDIA_STATE_PLAY_TOTAL_TIME://总时长
                if (params != null) {
                    int total = params.getInt(VideoStatus.Constants.PLAY_TOTAL_TIME);
                    setTotalTime(total);
                }
                break;
        }
    }

    /**
     * 设置封面
     *
     * @param cover 封面url
     */
    private void setCover(String cover) {
        if (mCoverIv != null) {
            Glide.with(mCoverIv.getContext())
                    .load(cover)
                    .into(mCoverIv);
        }
    }

    /**
     * 显示封面
     */
    private void showCover() {
        if (mCoverIv != null) {
            mCoverIv.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏封面
     */
    private void dismissCover() {
        if (mCoverIv != null) {
            mCoverIv.setVisibility(GONE);
        }
    }

    /**
     * 加载loading
     */
    private void startLoading() {
        if (mLoadingRl != null) {
            mLoadingRl.setVisibility(VISIBLE);
        }
        setControlVisibility(GONE, GONE);
    }

    /**
     * 停止加载
     */
    private void stopLoading() {
        if (mLoadingRl != null) {
            mLoadingRl.setVisibility(GONE);
        }
    }

    /**
     * 播放出错
     *
     * @param errorMsg 错误信息
     */
    private void playError(String errorMsg) {
        if (mErrorRl != null) {
            mErrorRl.setVisibility(VISIBLE);
        }
        if (mErrorTv != null) {
            mErrorTv.setText(errorMsg);
        }
    }

    /**
     * 隐藏播放出错
     */
    private void dismissErrorLayout() {
        if (mErrorRl != null) {
            mErrorRl.setVisibility(GONE);
        }
    }


    /**
     * 快进、倒退提示框
     *
     * @param title
     * @param value
     */
    private void showHintDialog(String title, String value) {
        if (mHintView != null) {
            mHintView.show(title, value);
        }
    }

    /**
     * 播放总时长设置
     *
     * @param totalTime
     */
    private void setTotalTime(int totalTime) {
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
    private void setProgress(int currentProgress) {
        Log.d(TAG, "当前进度：" + currentProgress);
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
    private void setSecondProgress(int secondProgress) {
        Log.d(TAG, "缓冲进度：" + secondProgress);
        if (mSeekBar != null) {
            mSeekBar.setSecondaryProgress(secondProgress);
        }
    }


    /**
     * 控制器是否可见
     *
     * @return 控制器是否可见
     */
    private boolean isControlVisible() {
        return mPlayProgressLl != null && mPlayProgressLl.getVisibility() == VISIBLE && mPlayOrPauseIv != null && mPlayOrPauseIv.getVisibility() == VISIBLE;
    }

    /**
     * 隐藏开始/暂停图标
     */
    private void dismissPlayOrPauseIv() {
        if (mPlayOrPauseIv != null) {
            mPlayOrPauseIv.setVisibility(GONE);
        }
    }

    /**
     * 显示开始/暂停图标
     */
    private void showPlayOrPauseIv() {
        if (mPlayOrPauseIv != null) {
            mPlayOrPauseIv.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏底部布局
     */
    private void dismissBottomProgressLayout() {
        if (mPlayProgressLl != null) {
            mPlayProgressLl.setVisibility(GONE);
        }
    }

    /**
     * 显示底部布局
     */
    private void showBottomProgressLayout() {
        if (mPlayProgressLl != null) {
            mPlayProgressLl.setVisibility(VISIBLE);
        }
    }

    /**
     * 显示/隐藏控制布局
     */
    private void showOrHideControl() {
        if (isControlVisible()) {
            dismissPlayOrPauseIv();
            dismissBottomProgressLayout();
        } else {
            showPlayOrPauseIv();
            showBottomProgressLayout();
        }
    }

    /**
     * 控制布局显示、隐藏
     */
    public void setControlVisibility(int btnPlayVisibility, int playProgressVisibility) {
        if (mPlayOrPauseIv != null) {
            mPlayOrPauseIv.setVisibility(btnPlayVisibility);
        }
        if (mPlayProgressLl != null) {
            mPlayProgressLl.setVisibility(playProgressVisibility);
        }
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
        Log.e("xgw", "亮度");
    }

    @Override
    public void onVolumeGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.e("xgw", "音量");
    }

    @Override
    public void onFF_REWGesture(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
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
            String durationStr = String.format("%02d:%02d", ((int) duration / 1000), ((int) duration % 60));
            showHintDialog("后退", newPlaybackTimeStr + "/" + durationStr);
        }
    }

    @Override
    public void onSingleTapGesture(MotionEvent e) {
        Log.e("xgw", "single tap");
        showOrHideControl();
        if (mListener != null) {
            mListener.onPlayOrPauseClick();
        }
    }

    @Override
    public void onDoubleTapGesture(MotionEvent e) {
        Log.e("xgw", "onDoubleTapGesture: ");
    }

    @Override
    public void onDown(MotionEvent e) {
        if (mTotalTime != 0) {
            oldFF_REWProgress = (int) ((100 * mCurrentProgress) / mTotalTime);
        } else {
            oldFF_REWProgress = 0;
        }
    }

    @Override
    public void onEndFF_REW(MotionEvent e) {
        if (mListener != null) {
            mListener.onSeekTo((int) newPlaybackTime * 1000);
        }
    }
}
