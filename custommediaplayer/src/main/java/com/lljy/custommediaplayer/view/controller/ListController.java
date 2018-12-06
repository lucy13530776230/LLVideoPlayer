package com.lljy.custommediaplayer.view.controller;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lljy.custommediaplayer.R;
import com.lljy.custommediaplayer.adapter.VideoListAdapter;
import com.lljy.custommediaplayer.constants.PlayMode;
import com.lljy.custommediaplayer.constants.ScreenStatus;
import com.lljy.custommediaplayer.constants.VideoStatus;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.interfs.ControllerListener;
import com.lljy.custommediaplayer.interfs.ListControllerListener;
import com.lljy.custommediaplayer.utils.PlayModeUtils;
import com.lljy.custommediaplayer.view.player.CustomListVideoPlayer;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @desc: 包含播放列表的控制器
 * 该控制器是给{@link CustomListVideoPlayer}播放器使用
 * 设置监听{@link #setListener(ControllerListener)}需传入{@link ListControllerListener}实例
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/3 10:09
 */

public class ListController extends AbsController<ListControllerListener> {
    private RelativeLayout mVideoListRl;//视频列表父布局
    private RecyclerView mVideoListRv;//视频列表
    private VideoListAdapter mAdapter;//视频列表适配器
    private ImageView playModeIv;//播放模式
    private ImageView fullScreenIv;//全屏按钮
    private PlayNextRunnable mPlayNextRunnable;

    public ListController(Context context) {
        super(context);
    }

    public ListController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        mPlayNextRunnable = new PlayNextRunnable(this);
        setPlayModelImage();
    }

    @Override
    protected void onPauseHandle() {
        super.onPauseHandle();
        dismissListRl();
    }

    @Override
    protected void onResumeHandle() {
        super.onResumeHandle();
        dismissListRl();
    }

    @Override
    protected void onPlayNewHandle(Bundle params) {
        super.onPlayNewHandle(params);
        dismissListRl();
    }

    @Override
    protected void onStartPlayHandle() {
        super.onStartPlayHandle();
        dismissListRl();
    }

    @Override
    protected void onErrorHandle(Bundle params) {
        super.onErrorHandle(params);
        dismissListRl();
        String errorMsg = "视频播放出错";
        boolean isPlayCache = false;
        if (params != null && !TextUtils.isEmpty(params.getString(VideoStatus.Constants.PLAY_ERROR_MSG))) {
            errorMsg = params.getString(VideoStatus.Constants.PLAY_ERROR_MSG);
            isPlayCache = params.getBoolean(VideoStatus.Constants.IS_PLAY_CACHE);
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mPlayNextRunnable);
        }
        if (mAdapter != null && mAdapter.getData().size() > 0) {
            for (int i = 0; i < mAdapter.getData().size(); i++) {
                VideoBean videoBean = mAdapter.getData().get(i);
                //找到正在播放的视频
                if (videoBean != null && videoBean.isPlaying()) {
                    if (isPlayCache) {
                        //有本地链接，需要重播，把本地视频清空，播网络视频
                        videoBean.setNativeSrc(null);
                        showErrorLayout(errorMsg + "，即将重新播放该视频");
                        delayPlayNextOrReplay(videoBean);
                    } else {
                        //判断是否只有一个视频，大于一个视频才播放下一个视频
                        showErrorLayout(errorMsg + "，即将播放下一个视频");
                        if (mAdapter.getData().size() > 1) {
                            if (i < mAdapter.getData().size() - 1) {
                                delayPlayNextOrReplay(mAdapter.getData().get(i + 1));
                                setPlay(i + 1);
                            } else {
                                delayPlayNextOrReplay(mAdapter.getData().get(0));
                                setPlay(0);
                            }
                        } else {
                            //否则提示无视频
                            showErrorLayout("暂无视频");
                        }
                    }
                    break;
                }
            }
        } else {
            showErrorLayout("暂无视频");
        }
    }

    /**
     * 播放下一个视频
     *
     * @param videoBean
     */
    private void delayPlayNextOrReplay(VideoBean videoBean) {
        if (mHandler != null && mPlayNextRunnable != null) {
            mPlayNextRunnable.setVideoBean(videoBean);
            mHandler.removeCallbacks(mPlayNextRunnable);
            mHandler.postDelayed(mPlayNextRunnable, delayMillis);
        }
    }

    @Override
    protected void onBufferStartHandle() {
        super.onBufferStartHandle();
        dismissListRl();
    }

    @Override
    protected void onBufferEndHandle() {
        super.onBufferEndHandle();
        dismissListRl();
    }

    @Override
    protected void onReleaseHandle() {
        super.onReleaseHandle();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        dismissListRl();
    }

    /**
     * 延时播放下一个视频任务
     */
    private static class PlayNextRunnable implements Runnable {
        private WeakReference<ListController> ref;
        private VideoBean videoBean;

        public PlayNextRunnable(ListController controller) {
            this.ref = new WeakReference<>(controller);
        }

        public void setVideoBean(VideoBean videoBean) {
            this.videoBean = videoBean;
        }

        @Override
        public void run() {
            if (ref != null && ref.get() != null) {
                ListController controller = ref.get();
                Handler handler = controller.mHandler;
                ListControllerListener listener = controller.mListener;
                if (handler != null) {
                    handler.removeCallbacks(this);
                }
                if (listener != null) {
                    listener.onVideoSelected(videoBean);
                }
            }
        }
    }

    @Override
    protected void delayControlVisibility(int visibility) {
        super.delayControlVisibility(visibility);
        if (visibility == VISIBLE) {
            showListRl();
        } else {
            dismissListRl();
        }
    }

    /**
     * 显示列表
     */
    private void showListRl() {
        if (mVideoListRl != null && mVideoListRl.getVisibility() != VISIBLE) {
            mVideoListRl.setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏列表
     */
    private void dismissListRl() {
        if (mVideoListRl != null && mVideoListRl.getVisibility() != GONE) {
            mVideoListRl.setVisibility(GONE);
        }
    }

    @Override
    public void setListener(ListControllerListener listener) {
        super.setListener(listener);
        if (mAdapter != null) {
            mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                if (mListener != null) {
                    setPlay(position);
                    mListener.onVideoSelected((VideoBean) adapter.getItem(position));
                }
            });
        }
    }

    /**
     * 给子类初始化拓展控件用的
     *
     * @param contentView 内容view
     */
    @Override
    protected void initExtensionViews(View contentView, Context context) {
        mVideoListRl = contentView.findViewById(R.id.video_list_rl);
        mVideoListRv = contentView.findViewById(R.id.video_list_rv);
        mVideoListRv.setLayoutManager(new LinearLayoutManager(context));
        mAdapter = new VideoListAdapter(null);
        mVideoListRv.setAdapter(mAdapter);
        playModeIv = contentView.findViewById(R.id.play_model_iv);
        playModeIv.setOnClickListener(v -> setPlayMode());
        fullScreenIv = contentView.findViewById(R.id.full_screen_iv);
        fullScreenIv.setOnClickListener(v -> startOrExitFullScreen());
    }

    /**
     * 获取子控件布局id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.list_control_layout;
    }

    @Override
    protected void startLoading() {
        super.startLoading();
        dismissListRl();
    }

    @Override
    protected void onCompleteHandle() {
        super.onCompleteHandle();
        Log.d(TAG, "onCompleteHandle start");
        if (mListener != null && mAdapter != null && mAdapter.getData().size() > 0) {
            int playMode = PlayModeUtils.getPlayMode();
            //单曲循环模式---一直播放该视频
            if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_SINGLE_CYCLE) {
                int playedPosition = 0;
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    VideoBean videoBean = mAdapter.getData().get(i);
                    if (videoBean != null && videoBean.isPlaying()) {
                        playedPosition = i;
                        break;
                    }
                }
                mListener.onVideoSelected(mAdapter.getData().get(playedPosition));
                setPlay(playedPosition);
            } else if (playMode == PlayMode.PLAY_MODE_LIST_CYCLE) {//列表循环模式---播放完最后一个视频播放第一个视频
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    VideoBean videoBean = mAdapter.getData().get(i);
                    if (videoBean != null && videoBean.isPlaying()) {
                        if (i < mAdapter.getData().size() - 1) {
                            mListener.onVideoSelected(mAdapter.getData().get(i + 1));
                            setPlay(i + 1);
                        } else {
                            mListener.onVideoSelected(mAdapter.getData().get(0));
                            setPlay(0);
                        }
                        break;
                    }
                }
            } else {//顺序播放-----播放完最后一个视频不再继续播放，点击播放按钮后再继续播放最后一个视频
                for (int i = 0; i < mAdapter.getData().size(); i++) {
                    if (mAdapter != null) {
                        VideoBean videoBean = mAdapter.getData().get(i);
                        if (videoBean != null && videoBean.isPlaying()) {
                            if (i < mAdapter.getData().size() - 1) {
                                mListener.onVideoSelected(mAdapter.getData().get(i + 1));
                                setPlay(i + 1);
                            } else {
                                showCover();
                                showPlayOrPauseIv();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 初始化视频列表
     *
     * @param videos 视频列表
     */
    public void setVideos(List<VideoBean> videos) {
        if (videos != null && videos.size() > 0 && mAdapter != null) {
            int playedPosition = 0;
            boolean hasPlayedPosition = false;
            //去空
            Iterator<VideoBean> iterator = videos.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == null) {
                    iterator.remove();
                }
            }
            for (int i = 0; i < videos.size(); i++) {
                VideoBean videoBean = videos.get(i);
                if (videoBean.isPlaying()) {
                    playedPosition = i;
                    hasPlayedPosition = true;
                    break;
                }
            }
            if (!hasPlayedPosition) {
                videos.get(0).setPlaying(true);
            }
            mAdapter.setNewData(videos);
            if (mListener != null) {
                mListener.onVideoSelected(videos.get(playedPosition));
            }
        } else {
            if (mListener != null) {
                mListener.onVideoSelected(null);
            }
        }
    }

    /**
     * 添加视频
     *
     * @param videos 添加的视频列表
     */
    public void addVideos(List<VideoBean> videos) {
        if (videos != null && videos.size() > 0 && mAdapter != null) {
            List<VideoBean> currentVideos = mAdapter.getData();
            for (int i = 0; i < videos.size(); i++) {
                VideoBean videoBean = videos.get(i);
                if (videoBean != null && !currentVideos.contains(videoBean)) {
                    mAdapter.addData(videoBean);
                }
            }
            Collections.sort(mAdapter.getData());
            mAdapter.notifyItemChanged(0, mAdapter.getData().size());
        }
    }

    /**
     * 删除视频
     *
     * @param videos 删除的视频列表
     */
    public void deleteVideos(List<VideoBean> videos) {
        if (mAdapter != null && mAdapter.getData().size() > 0 && videos != null && videos.size() > 0) {
            int deletePlayedPosition = -1;
            for (int i = 0; i < videos.size(); i++) {
                List<VideoBean> currentVideos = mAdapter.getData();
                VideoBean videoBean = videos.get(i);
                if (currentVideos.size() > 0 && currentVideos.contains(videoBean)) {
                    deletePlayedPosition = currentVideos.indexOf(videoBean);
                    if (videoBean.isPlaying()) {//删除了正在播放的视频，通知播放器停止播放
                        if (mListener != null) {
                            mListener.onPlayedVideoDeleted(videoBean);
                        }
                    }
                    if (deletePlayedPosition >= 0 && deletePlayedPosition <= currentVideos.size() - 1) {
                        mAdapter.remove(deletePlayedPosition);
                    }
                }
            }
            if (deletePlayedPosition > 0) {
                //播放第一个视频
                List<VideoBean> currentVideos = mAdapter.getData();
                if (currentVideos.size() > 0) {
                    if (mListener != null) {
                        mListener.onVideoSelected(currentVideos.get(0));
                        mAdapter.setPlay(0);
                    }
                } else {
                    Bundle params = new Bundle();
                    params.putString(VideoStatus.Constants.PLAY_ERROR_MSG, "暂无视频");
                    onErrorHandle(params);
                }
            }
        }
    }

    /**
     * 设置视频播放顺序
     *
     * @param orders 视频顺序
     */
    public void orderVideos(List<VideoBean> orders) {
        //调整播放顺序
        if (orders != null && orders.size() > 0 && mAdapter != null && mAdapter.getData().size() > 0) {
            final List<VideoBean> videos = mAdapter.getData();
            for (int i = 0; i < orders.size(); i++) {
                VideoBean order = orders.get(i);
                if (order != null && videos.contains(order)) {
                    int position = videos.indexOf(order);
                    VideoBean old = videos.get(position);
                    old.setVideo_order(order.getVideo_order());
                }
            }
            Collections.sort(videos);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 设置播放模式
     */
    private void setPlayMode() {
        String toast = null;
        //顺序播放、列表循环、单曲循环
        if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_LIST_ORDER) {
            PlayModeUtils.setPlayMode(PlayMode.PLAY_MODE_LIST_CYCLE);
            toast = "列表循环";
        } else if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_LIST_CYCLE) {
            PlayModeUtils.setPlayMode(PlayMode.PLAY_MODE_SINGLE_CYCLE);
            toast = "单曲循环";
        } else if (PlayModeUtils.getPlayMode() == PlayMode.PLAY_MODE_SINGLE_CYCLE) {
            PlayModeUtils.setPlayMode(PlayMode.PLAY_MODE_LIST_ORDER);
            toast = "顺序播放";
        }
        Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
        setPlayModelImage();
    }

    /**
     * 设置playmodel对应的按钮
     */
    private void setPlayModelImage() {
        if (playModeIv == null) {
            return;
        }
        int resourceId;
        switch (PlayModeUtils.getPlayMode()) {
            case PlayMode.PLAY_MODE_LIST_ORDER:
                resourceId = R.drawable.ic_play_model_order;
                break;
            case PlayMode.PLAY_MODE_LIST_CYCLE:
                resourceId = R.drawable.ic_play_model_list_recycle;
                break;
            case PlayMode.PLAY_MODE_SINGLE_CYCLE:
                resourceId = R.drawable.ic_play_model_single_recycle;
                break;
            default:
                resourceId = R.drawable.ic_play_model_order;
                break;
        }
        playModeIv.setImageResource(resourceId);
    }

    /**
     * 打开或者退出全屏
     */
    private void startOrExitFullScreen() {
        if (fullScreenIv != null && mListener != null) {
            if (mScreenStatus == ScreenStatus.SCREEN_STATUS_NORMAL) {
                //打开全屏
                mListener.onStartFullScreen();
                //图标改为退出全屏
                fullScreenIv.setImageResource(R.drawable.video_exit);
                //屏幕状态改为全屏
                super.setScreenStatus(ScreenStatus.SCREEN_STATUS_FULL);
            } else {
                //关闭全屏
                mListener.onExitFullScreen();
                //图标改为打开全屏
                fullScreenIv.setImageResource(R.drawable.video_full_screen);
                //屏幕状态改为正常
                super.setScreenStatus(ScreenStatus.SCREEN_STATUS_NORMAL);
            }
        }
    }

    /**
     * 正在播放的视频
     *
     * @param position
     */
    public void setPlay(int position) {
        if (mAdapter != null) {
            mAdapter.setPlay(position);
        }
    }
}