package com.lljy.custommediaplayer.view.engine;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.ViewGroup;

import com.lecloud.sdk.constant.PlayerEvent;
import com.lecloud.sdk.constant.PlayerParams;
import com.lecloud.sdk.constant.StatusCode;
import com.lecloud.sdk.videoview.VideoViewListener;
import com.lecloud.sdk.videoview.vod.VodVideoView;
import com.lljy.custommediaplayer.view.surfaceview.WrapContentSurfaceView;

import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @desc: 乐视视频播放器：http://www.lecloud.com/zh-cn/help/sdk.html
 * 引入乐视视频播放器目的，公司使用的是乐视云存储存放视频，播放视频传入uuid+vuid的方式传入到乐视云sdk，当然也可以用乐视视频播放器播放网络视频；
 * 播放url在低版本性能差的应用会丢帧，因此引入了腾讯视频播放器{@link TencentVideoPlayer}
 * 乐视视频支持的播放格式有：3gp,mp4,ts,mp3,flv，但我们用的是乐视sdk播放uuid+vuid，所以可忽略这些格式。
 * 乐视视频也自带离线下载功能
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 17:43
 */

public class LeVideoPlayer extends AbsVideoPlayer implements VideoViewListener {
    private static final String TAG = "LeVideoPlayer";
    private VodVideoView videoView;
    private static final int UPDATE_TIME_AND_PROGRESS = 1;
    private TimerHandler mTimerHandler;

    private WrapContentSurfaceView mSurfaceView;//播放器播放载体
    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //初始化点播播放器
            videoView = new VodVideoView(mContext);
            videoView.setSurface(holder.getSurface());
            //设置播放回调监听
            videoView.setVideoViewListener(LeVideoPlayer.this);
            //添加播放器
            String nativeUrl = mVideo.getNativeUrl();
            String netUrl = mVideo.getNetUrl();
            String uu = mVideo.getUu();
            String vu = mVideo.getVu();
            if (!TextUtils.isEmpty(nativeUrl)) {
                Log.d(TAG, "乐视播本地：" + nativeUrl);
                videoView.setDataSource(nativeUrl);
            } else if (!TextUtils.isEmpty(uu) && !TextUtils.isEmpty(vu)) {
                Log.d(TAG, "乐视播网络uuid:" + uu + ",vuid:" + vu);
                Bundle bundle = new Bundle();
                bundle.putInt(PlayerParams.KEY_PLAY_MODE, PlayerParams.VALUE_PLAYER_VOD);
                bundle.putString(PlayerParams.KEY_PLAY_UUID, uu);
                bundle.putString(PlayerParams.KEY_PLAY_VUID, vu);
                bundle.putString(PlayerParams.KEY_PLAY_PU, "0");
                videoView.setDataSource(bundle);
            } else if (!TextUtils.isEmpty(netUrl)) {
                Log.d(TAG, "乐视播网络url:" + netUrl);
                videoView.setDataSource(netUrl);
            } else {
                if (mListener != null) {
                    mListener.onError("无可用播放地址");
                }
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
     * 延时500ms重复获取进度和时间，达到实时更新进度条显示时间的目的
     */
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
                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
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
    protected void init(Context context) {
        super.init(context);
    }

    @Override
    protected void initPlayer() {
        try {
            release();
            if (mVideo == null ||
                    (TextUtils.isEmpty(mVideo.getNativeUrl()) &&
                            (TextUtils.isEmpty(mVideo.getUu()) && TextUtils.isEmpty(mVideo.getVu())) &&
                            TextUtils.isEmpty(mVideo.getNetUrl()))) {
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

    @Override
    public void play() {
        try {
            if (videoView != null) {
                if (hasPaused) {
                    videoView.onStart();
                    hasPaused = false;
                }
            } else {
                initPlayer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        try {
            if (videoView != null) {
                if (videoView.isPlaying()) {
                    videoView.onPause();
                    hasPaused = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void release() {
        try {
            if (videoView != null) {
                videoView.onDestroy();
                videoView.setVideoViewListener(null);
                videoView = null;
            }
            if (mSurfaceView != null) {
                removeView(mSurfaceView);
                mSurfaceView = null;
            }
            if (mTimerHandler != null) {
                mTimerHandler.removeCallbacksAndMessages(null);
                mTimerHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hasPaused = false;
        }
    }

    @Override
    public void seekTo(int progress) {
        try {
            if (videoView != null) {
                videoView.seekTo(progress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPlaying() {
        try {
            return videoView != null && videoView.isPlaying();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getDuration() {
        try {
            return videoView == null ? 0 : (int) videoView.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getCurrentProgress() {
        try {
            return videoView == null ? 0 : (int) videoView.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onStateResult(int i, Bundle bundle) {
        try {
            switch (i) {
                case PlayerEvent.PLAY_VIDEOSIZE_CHANGED:
                    /**
                     * 获取到视频的宽高的时候，此时可以通过视频的宽高计算出比例，进而设置视频view的显示大小。
                     * 如果不按照视频的比例进行显示的话，(以surfaceView为例子)内容会填充整个surfaceView。
                     * 意味着你的surfaceView显示的内容有可能是拉伸的
                     */
                    if (videoView != null) {
                        int videoWidth = videoView.getVideoWidth();
                        int videoHeight = videoView.getVideoHeight();
                        if (mSurfaceView != null) {
                            mSurfaceView.adaptVideoSize(videoWidth, videoHeight);
                        }
                    }
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
//                case PlayerEvent.PLAY_BUFFERING:
//                    //缓冲进度
//                    int biffer = bundle.getInt(PlayerParams.KEY_PLAY_BUFFERPERCENT);
//                    if (mListener != null) {
//                        mListener.onSecondProgress(biffer);
//                    }
//                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String onGetVideoRateList(LinkedHashMap<String, String> map) {
        try {
            if (map != null) {
                for (Map.Entry<String, String> rates : map.entrySet()) {
                    if (rates.getValue().equals("高清")) {
                        return rates.getKey();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
