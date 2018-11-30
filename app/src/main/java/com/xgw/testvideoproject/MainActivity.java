package com.xgw.testvideoproject;

import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.entity.VideoInfo;
import com.lljy.custommediaplayer.utils.VideoManager;
import com.lljy.custommediaplayer.view.CustomVideoView;
import com.lljy.custommediaplayer.view.SimpleController;

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
    private CustomVideoView mVideoView;
    private String[] videos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mVideoView = findViewById(R.id.video_view);
        mVideoView.setController(new SimpleController(this));
        List<String> videoList = StorageUtils.getVideoPaths(Environment.getExternalStorageDirectory().getPath() + "/Callby");
        videoList.add("乐视云视频");
        videos = new String[videoList.size()];
        videoList.toArray(videos);
        findViewById(R.id.select_btn).setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("请选择视频")
                    .setItems(videos, (dialog, which) -> {
                        if (which != videos.length - 1) {
                            VideoBean videoBean = new VideoBean();
                            videoBean.setSrc(videos[which]);
                            videoBean.setInfo(new VideoInfo());
                            mVideoView.setVideoSource(videoBean);
                        } else {
                            VideoBean videoBean = new VideoBean();
                            VideoInfo info = new VideoInfo();
                            info.setUu("nothf5qvkj");
                            info.setVu("16041dadc1");
                            videoBean.setInfo(info);
                            mVideoView.setVideoSource(videoBean);
                        }
                    }).show();
        });
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
}
