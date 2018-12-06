package com.xgw.testvideoproject;

import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lljy.custommediaplayer.download.VideoDownloadManager;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.entity.VideoInfo;
import com.lljy.custommediaplayer.utils.VideoManager;
import com.lljy.custommediaplayer.view.player.CustomListVideoPlayer;
import com.lljy.custommediaplayer.view.controller.ListController;

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
    private CustomListVideoPlayer mVideoView;
    private String[] videoNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setController(new ListController(this));
        List<VideoBean> videos = new ArrayList<>();

        VideoBean video1 = new VideoBean();
        video1.setPlaying(true);
        video1.setSrc("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4");
        video1.setVideo_id("1");
        VideoInfo info = new VideoInfo();
        info.setThumbnails("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg");
        video1.setVideoName("办公室小野开番外了，居然在办公室开澡堂！老板还点赞？");
        video1.setInfo(info);
        video1.setSource("qiniu");
        videos.add(video1);


        VideoBean video2 = new VideoBean();
        video2.setPlaying(false);
        video2.setSrc("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-20-26.mp4");
        video2.setVideo_id("2");
        VideoInfo info2 = new VideoInfo();
        info2.setThumbnails("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg");
        video2.setVideoName("小野在办公室用丝袜做茶叶蛋 边上班边看《外科风云》");
        video2.setSource("qiniu");
        video2.setInfo(info2);
        videos.add(video2);


        VideoBean video3 = new VideoBean();
        video3.setPlaying(false);
        video3.setVideo_id("3");
        VideoInfo info3 = new VideoInfo();
        info3.setUu("nothf5qvkj");
        info3.setVu("9f1a891f09");
        info3.setThumbnails("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg");
        video3.setVideoName("Linux系统讲解");
        video3.setSource("letv");
        video3.setInfo(info3);
        videos.add(video3);

        videoNames = new String[videos.size()];

        videoNames[0] = "腾讯sdk下载载视频1";
        videoNames[1] = "腾讯sdk下载载视频2";
        videoNames[2] = "乐视sdk下载载视频2";

        mVideoView.setVideos(videos);
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
