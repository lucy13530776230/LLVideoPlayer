package com.lljy.custommediaplayer.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.lecloud.sdk.constant.PlayerEvent;
import com.lecloud.sdk.constant.PlayerParams;
import com.lecloud.sdk.constant.StatusCode;
import com.lecloud.sdk.videoview.IMediaDataVideoView;
import com.lecloud.sdk.videoview.VideoViewListener;
import com.lecloud.sdk.videoview.vod.VodVideoView;
import com.lljy.custommediaplayer.utils.VideoLayoutParams;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @desc: 乐视视频播放器：http://www.lecloud.com/zh-cn/help/sdk.html
 * 引入乐视视频播放器目的，公司使用的是乐视云存储存放视频，播放视频传入uuid+vuid的方式传入到乐视云sdk；
 * 播放url在低版本性能差的应用会丢帧，因此引入了腾讯视频播放器{@link TencentVideoPlayer}
 * 乐视视频支持的播放格式有：3gp,mp4,ts,mp3,flv，但我们用的是乐视sdk播放uuid+vuid，所以可忽略这些格式。
 * 乐视视频也自带离线下载功能
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 17:43
 */

public class LeVideoPlayer extends AbsVideoPlayer implements VideoViewListener {
    private static final String TAG = "xgw_video";
    private IMediaDataVideoView videoView;
    private static final int UPDATE_TIME_AND_PROGRESS = 1;
    private TimerHandler mTimerHandler;

    private static class TimerHandler extends Handler {
        private WeakReference<LeVideoPlayer> ref;

        public TimerHandler(LeVideoPlayer player) {
            this.ref = new WeakReference<>(player);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_TIME_AND_PROGRESS:
                    if (ref != null) {
                        LeVideoPlayer player = ref.get();
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
                    break;
            }
        }
    }

    public LeVideoPlayer(Context context) {
        super(context, null);
    }

    public LeVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public LeVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initPlayer() {
        release();
        mTimerHandler = new TimerHandler(this);
        //初始化点播播放器
        videoView = new VodVideoView(mContext);
        //设置播放回调监听
        videoView.setVideoViewListener(this);
        //添加播放器
        addView((View) videoView, VideoLayoutParams.computeContainerSize(mContext, 16, 9));
        if (mVideo != null && mVideo.getInfo() != null && !TextUtils.isEmpty(mVideo.getInfo().getUu()) && !TextUtils.isEmpty(mVideo.getInfo().getVu())) {
            String uuid = mVideo.getInfo().getUu();
            String vuid = mVideo.getInfo().getVu();
            Bundle bundle = new Bundle();
            bundle.putInt(PlayerParams.KEY_PLAY_MODE, PlayerParams.VALUE_PLAYER_VOD);
            bundle.putString(PlayerParams.KEY_PLAY_UUID, uuid);
            bundle.putString(PlayerParams.KEY_PLAY_VUID, vuid);
            bundle.putString(PlayerParams.KEY_PLAY_PU, "0");
            videoView.setDataSource(bundle);
        } else {
            Log.d(TAG, "无可用播放地址");
        }
    }

    @Override
    public void play() {
        if (videoView != null) {
            if (hasPaused) {
                videoView.onStart();
                hasPaused = false;
            }
        } else {
            initPlayer();
        }
    }

    @Override
    public void pause() {
        if (videoView != null) {
            if (videoView.isPlaying()) {
                videoView.onPause();
                hasPaused = true;
            }
        }
    }

    @Override
    public void release() {
        if (videoView != null) {
            videoView.onDestroy();
            videoView.setVideoViewListener(null);
            removeView((View) videoView);
            videoView = null;
        }
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
            mTimerHandler = null;
        }
        hasPaused = false;
    }

    @Override
    public void seekTo(int progress) {
        if (videoView != null) {
            videoView.seekTo(progress);
        }
    }

    @Override
    public boolean isPlaying() {
        return videoView != null && videoView.isPlaying();
    }

    @Override
    public int getDuration() {
        return videoView == null ? 0 : (int) videoView.getDuration();
    }

    @Override
    public int getCurrentProgress() {
        return videoView == null ? 0 : (int) videoView.getCurrentPosition();
    }

    @Override
    public void onStateResult(int i, Bundle bundle) {
        switch (i) {
            case PlayerEvent.PLAY_VIDEOSIZE_CHANGED:
                /**
                 * 获取到视频的宽高的时候，此时可以通过视频的宽高计算出比例，进而设置视频view的显示大小。
                 * 如果不按照视频的比例进行显示的话，(以surfaceView为例子)内容会填充整个surfaceView。
                 * 意味着你的surfaceView显示的内容有可能是拉伸的
                 */
                break;

            case PlayerEvent.PLAY_PREPARED:
                // 播放器准备完成，此刻调用start()就可以进行播放了
                if (videoView != null) {
                    videoView.onStart();
                }
                if (mListener != null) {
                    mListener.onPrepared();
                }
                if (mTimerHandler != null) {
                    mTimerHandler.removeMessages(UPDATE_TIME_AND_PROGRESS);
                    mTimerHandler.sendEmptyMessageDelayed(UPDATE_TIME_AND_PROGRESS, 500);
                }
                break;
            case PlayerEvent.PLAY_INFO:
                if (bundle != null && mListener != null) {
                    int code = bundle.getInt(PlayerParams.KEY_RESULT_STATUS_CODE);
                    if (code == StatusCode.PLAY_INFO_VIDEO_RENDERING_START) {
                        //播放第一帧，停止转菊花
                        mListener.onLoadingFinished();
                    } else if (code == StatusCode.PLAY_INFO_BUFFERING_START) {
                        //开始缓冲，开始转菊花
                        mListener.onLoadingStart();
                    } else if (code == StatusCode.PLAY_INFO_BUFFERING_END) {
                        //缓冲完成，停止转菊花
                        mListener.onLoadingFinished();
                    }
                }
                break;
            case PlayerEvent.PLAY_COMPLETION:
                //回调该方法说明播单个视频完成
                if (mListener != null) {
                    mListener.onComplete();
                }
                break;
            case PlayerEvent.PLAY_ERROR:
                //播放出错
                if (mListener != null) {
                    mListener.onError("播放出错");
                }
                break;
            case PlayerEvent.PLAY_BUFFERING:
                //缓冲进度
                int biffer = bundle.getInt(PlayerParams.KEY_PLAY_BUFFERPERCENT);
                if (mListener != null) {
                    mListener.onSecondProgress(biffer);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String onGetVideoRateList(LinkedHashMap<String, String> map) {
        if (map != null) {
            for (Map.Entry<String, String> rates : map.entrySet()) {
                if (rates.getValue().equals("高清")) {
                    return rates.getKey();
                }
            }
        }
        return "";
    }
}
