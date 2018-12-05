package com.lljy.custommediaplayer.view.engine;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;

import com.tencent.rtmp.ITXVodPlayListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXVodPlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * @desc: 腾讯视频播放器：https://cloud.tencent.com/document/product/881/20216#step-3.3A-.E5.90.AF.E5.8A.A8.E6.92.AD.E6.94.BE
 * 引入腾讯视频播放器目的：
 * ①播放url；
 * ②解决android原生播放{@link android.media.MediaPlayer}{@link android.widget.VideoView}视频格式限制；
 * ③解决乐视视频{@link LeVideoPlayer}播放sdk兼容低版本性能问题。
 * 腾讯视频sdk自带下载缓存视频、离线下载功能，可根据需求使用。
 * 腾讯视频播放器sdk支持视频格式有：3gp,mp4,flv,m3u8
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/29 17:41
 */


public class TencentVideoPlayer extends AbsVideoPlayer implements ITXVodPlayListener {
    private TXVodPlayer mLivePlayer;
    private TXCloudVideoView mPlayerView;

    public TencentVideoPlayer(Context context) {
        super(context, null);
    }

    public TencentVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TencentVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void play() {
        if (mLivePlayer != null) {
            if (hasPaused) {
                mLivePlayer.resume();
                hasPaused = false;
            } else {
                initPlayer();
            }
        }
    }

    @Override
    public void pause() {
        if (mLivePlayer != null) {
            if (mLivePlayer.isPlaying()) {
                mLivePlayer.pause();
                hasPaused = true;
            }
        }
    }

    @Override
    protected void initPlayer() {
        try {
            release();
            //初始化腾讯视频播放器
            mLivePlayer = new TXVodPlayer(mContext);
            mPlayerView = new TXCloudVideoView(mContext);
            mPlayerView.showLog(false);
            //添加播放器
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(mPlayerView, params);
            //设置播放
            mLivePlayer.setPlayerView(mPlayerView);
            //播放回调监听
            mLivePlayer.setVodListener(this);
            // 硬件加速在1080p解码场景下效果显著，但细节之处并不如想象的那么美好：
            // (1) 只有 4.3 以上android系统才支持
            // (2) 兼容性我们目前还仅过了小米华为等常见机型，故这里的返回值您先不要太当真
            mLivePlayer.enableHardwareDecode(false);
            mLivePlayer.setRenderRotation(TXLiveConstants.RENDER_ROTATION_PORTRAIT);
            mLivePlayer.setRenderMode(TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION);
            //是否自动播放，设置不自动不放
            mLivePlayer.setAutoPlay(true);
            if (mVideo == null || mVideo.getInfo() == null || (TextUtils.isEmpty(mVideo.getSrc()) && TextUtils.isEmpty(mVideo.getNativeSrc()))) {
                if (mListener != null) {
                    mListener.onError("播放出错，或者没有视频资源");
                }
                return;
            }
            Log.d(TAG, "腾讯视频播放器播放视频本地地址？：" + mVideo.getNativeSrc() + "\n网络地址：" + mVideo.getSrc());
            int result = mLivePlayer.startPlay(TextUtils.isEmpty(mVideo.getNativeSrc()) ? mVideo.getSrc() : mVideo.getNativeSrc());
            if (result != 0) {
                Log.d(TAG, "播放出错，或者没有视频资源");
                if (mListener != null) {
                    mListener.onError("播放出错，或者没有视频资源");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mListener != null) {
                mListener.onError("初始化资源出错");
            }
        }
    }

    /**
     * 释放播放器
     */
    public void release() {
        try {
            if (mLivePlayer != null) {
                mLivePlayer.setVodListener(null);
                mLivePlayer.stopPlay(true);
                mLivePlayer = null;
            }

            if (mPlayerView != null) {
                mPlayerView.onDestroy();
                removeView(mPlayerView);
                mPlayerView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            hasPaused = false;
        }

    }

    @Override
    public void seekTo(int progress) {
        if (mLivePlayer != null) {
            mLivePlayer.seek(progress / 1000);
        }
    }

    @Override
    public boolean isPlaying() {
        return mLivePlayer != null && mLivePlayer.isPlaying();
    }

    @Override
    public int getDuration() {
        return mLivePlayer == null ? 0 : (int) mLivePlayer.getDuration();
    }

    @Override
    public int getCurrentProgress() {
        return mLivePlayer == null ? 0 : (int) mLivePlayer.getCurrentPlaybackTime();
    }

    @Override
    public void onPlayEvent(TXVodPlayer txVodPlayer, int event, Bundle param) {
        if (event == TXLiveConstants.PLAY_EVT_VOD_PLAY_PREPARED || event == TXLiveConstants.PLAY_EVT_VOD_LOADING_END) {
            //视频准备完或者视频loading完了，回调准备完，停止转动菊花
            if (mListener != null) {
                mListener.onLoadingFinished();
            }

        }
        if (event == TXLiveConstants.PLAY_EVT_PLAY_BEGIN) {
            //视频开始播放，停止转动菊花
            if (mListener != null) {
                mListener.onLoadingFinished();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_PROGRESS) {
            //视频实时进度回调
            int progress = param.getInt(TXLiveConstants.EVT_PLAY_PROGRESS_MS);
            int playable = param.getInt(TXLiveConstants.EVT_PLAYABLE_DURATION_MS);
            int duration = param.getInt(TXLiveConstants.EVT_PLAY_DURATION_MS);
            if (mListener != null) {
                mListener.onProgress(progress);
                mListener.onSecondProgress(0);
                mListener.onTotalTime(duration);
            }
        } else if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT || event == TXLiveConstants.PLAY_ERR_FILE_NOT_FOUND) {
            //视频失去连接或者视频文件未找到，释放播放器资源
            String errorMsg;
            if (event == TXLiveConstants.PLAY_ERR_NET_DISCONNECT) {
                errorMsg = "视频断开连接";
            } else {
                errorMsg = "视频资源找不到";
            }
            if (mListener != null) {
                mListener.onError(errorMsg);
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_LOADING) {
            //视频开始loading，显示菊花
            if (mListener != null) {
                mListener.onLoadingStart();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_RCV_FIRST_I_FRAME) {
            //视频播放第一帧，停止转动菊花
            if (mListener != null) {
                mListener.onLoadingFinished();
                mListener.onPrepared();
            }
        } else if (event == TXLiveConstants.PLAY_ERR_HLS_KEY) {
            if (mListener != null) {
                mListener.onError("HLS解密key获取失败");
            }
        } else if (event == TXLiveConstants.PLAY_WARNING_RECONNECT) {
            if (mListener != null) {
                mListener.onLoadingStart();
            }
        } else if (event == TXLiveConstants.PLAY_EVT_PLAY_END) {
            if (mListener != null) {
                mListener.onComplete();
            }
        }
    }

    @Override
    public void onNetStatus(TXVodPlayer txVodPlayer, Bundle bundle) {

    }
}
