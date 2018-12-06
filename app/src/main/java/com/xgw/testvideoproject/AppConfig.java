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
                .initApp(this)
                .setVideoExpireDays(1)
                .setVideoSavedPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "llplayer" + File.separator);
    }
}
