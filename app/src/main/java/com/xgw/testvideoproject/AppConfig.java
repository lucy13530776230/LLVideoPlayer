package com.xgw.testvideoproject;

import android.app.Application;
import android.os.Environment;

import com.lljy.custommediaplayer.utils.VideoManager;

import java.io.File;

public class AppConfig extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VideoManager.getInstance()
                .initApp(this)//初始化app上下文给播放器
                .setVideoExpireDays(1)//设置下载的离线文件超时时间，不设置默认30天会清理下载文件
                .enableDownloadEngine(false)//默认就是true
                .enableWifiCheck(true)//是否开启wifi检测
                .setVideoSavedPath(
                        Environment.getExternalStorageDirectory().getPath()
                                + File.separator + "llplayer" + File.separator);//设置播放地址，默认在该目录下
    }
}
