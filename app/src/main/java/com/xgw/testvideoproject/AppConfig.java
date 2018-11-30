package com.xgw.testvideoproject;

import android.app.Application;

import com.lljy.custommediaplayer.utils.VideoManager;

public class AppConfig extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        VideoManager.getInstance().initApp(this);
    }
}
