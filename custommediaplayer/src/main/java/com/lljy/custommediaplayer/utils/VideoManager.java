package com.lljy.custommediaplayer.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.lecloud.sdk.api.stats.IAppStats;
import com.lecloud.sdk.api.stats.ICdeSetting;
import com.lecloud.sdk.config.LeCloudPlayerConfig;
import com.lecloud.sdk.listener.OnInitCmfListener;
import com.lljy.custommediaplayer.download.VideoDownloadManager;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by XieGuangwei on 2018/4/12.
 * 该类用来初始化上下文等相关配置
 */

public class VideoManager {
    private static final String TAG = "VideoManager";
    private static VideoManager instance;
    private Application app;
    private boolean cdeInitSuccess;

    public static VideoManager getInstance() {
        if (instance == null) {
            synchronized (VideoManager.class) {
                if (instance == null) {
                    instance = new VideoManager();
                }
            }
        }
        return instance;
    }

    public void initApp(Application app) {
        this.app = app;
        initLePlayer(app);
        VideoDownloadManager.getInstance().checkExpireVideos();
    }

    /**
     * 初始化乐视播放器
     *
     * @param application
     */
    private void initLePlayer(final Application application) {
        String processName = getProcessName(application, android.os.Process.myPid());
        if (application.getApplicationInfo().packageName.equals(processName)) {
            //TODO CrashHandler是一个抓取崩溃log的工具类（可选）
//            LeakCanary.install(this);
//            CrashReport.initCrashReport(getApplicationContext(), "900059604", true);
            try {
                PackageInfo packageInfo = application.getPackageManager().getPackageInfo(application.getPackageName(), 0);
                final LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
                parameters.put(ICdeSetting.HOST_TYPE, LeCloudPlayerConfig.HOST_DEFAULT + "");
                parameters.put(ICdeSetting.LOG_OUTPUT_TYPE, LeCloudPlayerConfig.LOG_LOGCAT + "");
                parameters.put(ICdeSetting.USE_CDE_PORT, false + "");
                parameters.put(ICdeSetting.SCHEME_TYPE, LeCloudPlayerConfig.SCHEME_HTTP + "");
                parameters.put(IAppStats.APP_VERSION_NAME, packageInfo.versionName);
                parameters.put(IAppStats.APP_VERSION_CODE, packageInfo.versionCode + "");
                parameters.put(IAppStats.APP_PACKAGE_NAME, application.getPackageName());
                parameters.put(IAppStats.APP_NAME, "bcloud_android");
//                parameters.put(IAppStats.APP_CHANNEL, "说明一下app渠道");
//                parameters.put(IAppStats.APP_CATEGORY, "视频");
                LeCloudPlayerConfig.setmInitCmfListener(new OnInitCmfListener() {

                    @Override
                    public void onCdeStartSuccess() {
                        //cde启动成功,可以开始播放
                        cdeInitSuccess = true;
                        Log.d(TAG, "onCdeStartSuccess: ");
                    }

                    @Override
                    public void onCdeStartFail() {
                        //cde启动失败,不能正常播放;如果使用remote版本则可能是remote下载失败;
                        //如果使用普通版本,则可能是so文件加载失败导致
                        cdeInitSuccess = false;
                        Log.d(TAG, "onCdeStartFail: ");
                    }

                    @Override
                    public void onCmfCoreInitSuccess() {
                        //不包含cde的播放框架需要处理
                    }

                    @Override
                    public void onCmfCoreInitFail() {
                        //不包含cde的播放框架需要处理
                    }

                    @Override
                    public void onCmfDisconnected() {
                        //cde服务断开,会导致播放失败,重启一次服务
                        try {
                            cdeInitSuccess = false;
                            LeCloudPlayerConfig.init(application.getApplicationContext(), parameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                LeCloudPlayerConfig.init(application.getApplicationContext(), parameters);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取当前进程名字
     *
     * @param cxt
     * @param pid
     * @return
     */
    private static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager)
                cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    return procInfo.processName;
                }
            }
        }
        return null;
    }

    public Application getApp() {
        return app;
    }
}
