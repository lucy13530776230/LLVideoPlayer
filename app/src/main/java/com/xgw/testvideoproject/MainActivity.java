package com.xgw.testvideoproject;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lljy.custommediaplayer.constants.ScreenStatus;
import com.lljy.custommediaplayer.constants.VideoEngineType;
import com.lljy.custommediaplayer.entity.VideoEntity;
import com.lljy.custommediaplayer.interfs.IVideoListener;
import com.lljy.custommediaplayer.utils.VideoManager;
import com.lljy.custommediaplayer.view.controller.ListController;
import com.lljy.custommediaplayer.view.controller.SimpleController;
import com.lljy.custommediaplayer.view.player.CustomListVideoPlayer;
import com.lljy.custommediaplayer.view.player.SimpleVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * aaa
 *
 * @author XieGuangwei
 * @email 775743075@qq.com
 * create at 2018/11/29 16:55
 * @see {@link VideoManager}
 */


public class MainActivity extends AppCompatActivity {
    private SimpleVideoPlayer mVideoView;
    private String[] videoNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setListener(new IVideoListener() {

            /**
             * 点击了打开或者退出全屏
             *
             * @param currentScreenStatus 当前屏幕状态
             */
            @Override
            public void onStartOrExitFullScreenPressed(ScreenStatus currentScreenStatus) {
                //在这里处理全屏、退出全屏操作
//                if (currentScreenStatus == ScreenStatus.SCREEN_STATUS_FULL) {
//                    //...退出全屏代码...
//                    //执行完退出全屏代码记得给播放器设置当前屏幕为正常状态
//                    mVideoView.setScreenStatus(ScreenStatus.SCREEN_STATUS_NORMAL);
//                } else {
//                    //...打开全屏代码...
//                    //执行完打开全屏记得给播放器设置当前屏幕为全屏状态
//                    mVideoView.setScreenStatus(ScreenStatus.SCREEN_STATUS_FULL);
//                }
            }

            /**
             * 返回
             *
             * @param currentScreenStatus 当前屏幕状态
             */
            @Override
            public void onTitleBackPressed(ScreenStatus currentScreenStatus) {
                //点击了标题栏的返回按钮，执行的操作与点击手机返回按钮一样
//                onBackPressed();
            }

            /**
             * 播放出错
             *
             * @param msg 错误信息
             */
            @Override
            public void onError(String msg) {

            }

            /**
             * 播放完成
             */
            @Override
            public void onComplete() {

            }

            /**
             * 加载封面图片
             * @param imageView 图片控件
             * @param cover     图片地址
             */
            @Override
            public void onCoverLoad(ImageView imageView, String cover) {
                Glide.with(MainActivity.this)
                        .load(cover)
                        .into(imageView);
            }
        });

        mVideoView.setController(new SimpleController(this));
        List<VideoEntity> videos = new ArrayList<>();

        VideoEntity video1 = new VideoEntity();
        video1.setPlaying(true);
        video1.setNetUrl("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4");
        video1.setId("1");
        video1.setCoverUrl("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg");
        video1.setVideoName("办公室小野开番外了，居然在办公室开澡堂！老板还点赞？");
        videos.add(video1);


        VideoEntity video2 = new VideoEntity();
        video2.setPlaying(false);
        video2.setNetUrl("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-20-26.mp4");
        video2.setId("2");
        video2.setCoverUrl("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg");
        video2.setVideoName("小野在办公室用丝袜做茶叶蛋 边上班边看《外科风云》");
        videos.add(video2);


        VideoEntity video3 = new VideoEntity();
        video3.setPlaying(false);
        video3.setId("3");
        video3.setUu("nothf5qvkj");
        video3.setVu("9f1a891f09");
        video3.setVideoEngineType(VideoEngineType.TYPE_LETV);
        video3.setCoverUrl("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg");
        video3.setVideoName("Linux系统讲解");
        video3.setVideoEngineType(VideoEngineType.TYPE_LETV);
        videos.add(video3);

        videoNames = new String[videos.size()];

        videoNames[0] = "腾讯sdk下载载视频1";
        videoNames[1] = "腾讯sdk下载载视频2";
        videoNames[2] = "乐视sdk下载载视频2";

        mVideoView.setVideo(video3);
    }

    @Override
    public void onBackPressed() {
        if (mVideoView.getScreenStatus() == ScreenStatus.SCREEN_STATUS_FULL) {
            //执行退出全屏操作
            //退出全屏后设置屏幕状态为正常
            mVideoView.setScreenStatus(ScreenStatus.SCREEN_STATUS_NORMAL);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoManager.getInstance().cancelAllDownloads();
    }
}
