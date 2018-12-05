package com.lljy.custommediaplayer.download;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SizeF;

import com.lecloud.sdk.download.control.DownloadCenter;
import com.lecloud.sdk.download.control.LeDownloadManager;
import com.lecloud.sdk.download.info.LeDownloadInfo;
import com.lecloud.sdk.download.observer.LeDownloadObserver;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.SpeedCalculator;
import com.liulishuo.okdownload.core.breakpoint.BlockInfo;
import com.liulishuo.okdownload.core.breakpoint.BreakpointInfo;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.listener.DownloadListener4WithSpeed;
import com.liulishuo.okdownload.core.listener.assist.Listener4SpeedAssistExtend;
import com.lljy.custommediaplayer.constants.VideoSpKey;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.utils.SPUtils;
import com.lljy.custommediaplayer.utils.VideoManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VideoDownloadManager {
    private static final String TAG = "VideoDownloadManager";
    private static VideoDownloadManager instance;
    private VideoDownloadListener mListener;
    private String downloadPath = Environment.getDownloadCacheDirectory().getPath() + File.separator + "langlangvideo" + File.separator;

    private int expireDays = 30;//视频过期时间默认三十天

    private List<DownloadTask> okTasks = new ArrayList<>();
    private List<LeDownloadInfo> leTasks = new ArrayList<>();

    /**
     * OkDownload下载回调
     */
    private DownloadListener4WithSpeed downloadListener4WithSpeed = new DownloadListener4WithSpeed() {
        @Override
        public void taskStart(@NonNull DownloadTask task) {
            try {
                if (!TextUtils.isEmpty((String) task.getTag())) {
                    String uniqueKey = (String) task.getTag();
                    if (!shouldDownloadVideo(uniqueKey)) {
                        //说明视频已经不需要下载了，取消下载并且同时删除文件
                        task.cancel();
                        task.setTag(null);
                        File file = task.getFile();
                        if (file != null && file.exists()) {
                            file.delete();
                        }
                    } else {
                        addDownloadRecord(uniqueKey, "1");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void connectStart(@NonNull DownloadTask task, int blockIndex, @NonNull Map<String, List<String>> requestHeaderFields) {

        }

        @Override
        public void connectEnd(@NonNull DownloadTask task, int blockIndex, int responseCode, @NonNull Map<String, List<String>> responseHeaderFields) {

        }

        @Override
        public void infoReady(@NonNull DownloadTask task, @NonNull BreakpointInfo info, boolean fromBreakpoint, @NonNull Listener4SpeedAssistExtend.Listener4SpeedModel model) {

        }

        @Override
        public void progressBlock(@NonNull DownloadTask task, int blockIndex, long currentBlockOffset, @NonNull SpeedCalculator blockSpeed) {

        }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset, @NonNull SpeedCalculator taskSpeed) {

        }

        @Override
        public void blockEnd(@NonNull DownloadTask task, int blockIndex, BlockInfo info, @NonNull SpeedCalculator blockSpeed) {

        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull SpeedCalculator taskSpeed) {
            try {
                if (!TextUtils.isEmpty((String) task.getTag())) {
                    String uniqueKey = (String) task.getTag();
                    File file = task.getFile();
                    if (cause == EndCause.COMPLETED) {
                        file = task.getFile();
                        if (file != null && file.exists()) {
                            Log.d(TAG, "url视频下载完成，存放地址：" + file.getPath());
                            //下载成功
                            if (shouldDownloadVideo(uniqueKey)) {
                                //视频下载完毕，通知可播放本地视频
                                addDownloadRecord(uniqueKey, file.getPath());
                            } else {
                                //数据库不存在，删除对应文件
                                file.delete();
                            }
                        }
                    } else {
                        Log.d(TAG, "url视频下载失败：" + (realCause != null ? realCause.getMessage() : "原因未知"));
                        //下载失败
                        if (file != null && file.exists()) {
                            file.delete();
                        }
                        removeDownloadRecord(uniqueKey);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 乐视视频下载回调监听
     */
    private LeDownloadObserver observer = new LeDownloadObserver() {
        @Override
        public void onDownloadStart(LeDownloadInfo leDownloadInfo) {
            try {
                if (leDownloadInfo != null && !TextUtils.isEmpty(leDownloadInfo.getUu()) && !TextUtils.isEmpty(leDownloadInfo.getVu())) {
                    String uniqueKey = leDownloadInfo.getUu() + leDownloadInfo.getVu();
                    if (!shouldDownloadVideo(uniqueKey)) {
                        //说明视频已经不需要下载了，取消下载并且同时删除文件
                        LeDownloadManager.getInstance(VideoManager.getInstance().getApp()).cancelDownload(leDownloadInfo, true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloadProgress(LeDownloadInfo leDownloadInfo) {

        }

        @Override
        public void onDownloadStop(LeDownloadInfo leDownloadInfo) {

        }

        @Override
        public void onDownloadSuccess(LeDownloadInfo leDownloadInfo) {
            try {
                if (leDownloadInfo != null && !TextUtils.isEmpty(leDownloadInfo.getUu()) && !TextUtils.isEmpty(leDownloadInfo.getVu())) {
                    Log.d(TAG, "乐视视频下载完成，存放地址：" + leDownloadInfo.getFileSavePath());
                    String uniqueKey = leDownloadInfo.getUu() + leDownloadInfo.getVu();
                    if (!shouldDownloadVideo(uniqueKey)) {
                        LeDownloadManager.getInstance(VideoManager.getInstance().getApp()).cancelDownload(leDownloadInfo, true);
                    } else {
                        addDownloadRecord(uniqueKey, leDownloadInfo.getFileSavePath());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloadFailed(LeDownloadInfo leDownloadInfo, String s) {
            try {
                if (leDownloadInfo != null && !TextUtils.isEmpty(leDownloadInfo.getUu()) && !TextUtils.isEmpty(leDownloadInfo.getVu())) {
                    String uniqueKey = leDownloadInfo.getUu() + leDownloadInfo.getVu();
                    Log.d(TAG, "乐视sdk下载引擎下载出错，错误信息：" + s);
                    LeDownloadManager.getInstance(VideoManager.getInstance().getApp()).cancelDownload(leDownloadInfo, true);
                    removeDownloadRecord(uniqueKey);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDownloadCancel(LeDownloadInfo leDownloadInfo) {

        }

        @Override
        public void onDownloadInit(LeDownloadInfo leDownloadInfo, String s) {

        }

        @Override
        public void onDownloadWait(LeDownloadInfo leDownloadInfo) {

        }

        @Override
        public void onGetVideoInfoRate(LeDownloadInfo leDownloadInfo, LinkedHashMap<String, String> linkedHashMap) {

        }
    };

    public static VideoDownloadManager getInstance() {
        if (instance == null) {
            synchronized (VideoDownloadManager.class) {
                if (instance == null) {
                    instance = new VideoDownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 设置回调监听
     *
     * @param listener
     */
    public void setListener(VideoDownloadListener listener) {
        this.mListener = listener;
    }

    /**
     * 视频过期时间
     *
     * @param days 过期时间
     */
    public void setExpireDays(int days) {
        if (days > 0) {
            this.expireDays = days;
        }
    }

    /**
     * 添加下载的视频
     *
     * @param videoBean
     */
    public void addDownloadVideo(VideoBean videoBean) {
        try {
            if (videoBean != null && videoBean.getInfo() != null && !TextUtils.isEmpty(downloadPath) && !TextUtils.isEmpty(videoBean.getSource()) && !TextUtils.isEmpty(videoBean.getVideo_id())) {
                String uniqueKey;
                String source = videoBean.getSource();
                String videoId = videoBean.getVideo_id();
                String uu = videoBean.getInfo().getUu();
                String vu = videoBean.getInfo().getVu();
                String url = videoBean.getSrc();
                //判断本地是否已下载过该视频
                if (!TextUtils.isEmpty(uu) && !TextUtils.isEmpty(vu)) {
                    //使用乐视sdk下载引擎
                    Log.d(TAG, "使用乐视sdk下载引擎");
                    uniqueKey = uu + vu;
                    if ("1".equals(getNativeUrl(uniqueKey))) {
                        Log.d(TAG, "视频正在下载，不用重新下载");
                        return;
                    }
                    if (isVideoExits(getNativeUrl(uniqueKey))) {
                        Log.d(TAG, "视频已存在，不用重新下载");
                        return;
                    }
                    //添加下载记录，表示该视频需要下载
                    addDownloadRecord(uniqueKey, "0");
                    DownloadCenter downloadCenter = DownloadCenter.getInstances(VideoManager.getInstance().getApp());
                    downloadCenter.setDownloadSavePath(downloadPath);
                    downloadCenter.registerDownloadObserver(observer);
                    LeDownloadInfo info = new LeDownloadInfo();
                    info.setUu(uu);
                    info.setVu(vu);
                    downloadCenter.downloadVideo(info);
                    if (leTasks != null) {
                        leTasks.add(info);
                    }
                } else if (!TextUtils.isEmpty(url)) {
                    //使用OkDownloader下载引擎
                    Log.d(TAG, "使用OkDownloader下载引擎");
                    uniqueKey = source + videoId;
                    addDownloadRecord(uniqueKey, "0");
                    //父目录不能存在则创建下载目录
                    File parentFile = new File(downloadPath);
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    //配置下载路径、名称
                    final DownloadTask downloadTask = new DownloadTask.Builder(url, parentFile)
                            .setMinIntervalMillisCallbackProcess(16)
                            .setPassIfAlreadyCompleted(false).build();
                    //设置tag
                    downloadTask.setTag(videoBean.getSource() + videoBean.getVideo_id());
                    //开始下载
                    downloadTask.enqueue(downloadListener4WithSpeed);
                    if (okTasks != null) {
                        okTasks.add(downloadTask);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加下载记录
     *
     * @param key       视频唯一标识码（视频id或者uuid+vuid）
     * @param nativeUrl 视频完整地址（如果刚开始下载，还没地址，则为0）
     */
    private void addDownloadRecord(String key, String nativeUrl) {
        SPUtils.getInstance(VideoSpKey.VIDEO_DOWNLOAD_SP_NAME).put(key, nativeUrl);
    }

    /**
     * 移除下载记录
     *
     * @param key
     */
    public void removeDownloadRecord(String key) {
        SPUtils.getInstance(VideoSpKey.VIDEO_DOWNLOAD_SP_NAME).remove(key);
    }

    /**
     * 获取指定视频本地路径
     *
     * @param uniqueKey 视频唯一标识码（视频id或者uuid+vuid）
     * @return 返回0表示还没下载完，返回url表示已下载
     */
    public String getNativeUrl(String uniqueKey) {
        if (!TextUtils.isEmpty(uniqueKey)) {
            return SPUtils.getInstance(VideoSpKey.VIDEO_DOWNLOAD_SP_NAME).getString(uniqueKey);
        }
        return null;
    }

    /**
     * 是否需要下载该视频
     *
     * @param uniqueKey 视频唯一标识码（视频source+video_id或者uuid+vuid）
     * @return 返回true表示需要下载，false表示不需要下载
     */
    private boolean shouldDownloadVideo(String uniqueKey) {
        return !TextUtils.isEmpty(getNativeUrl(uniqueKey));
    }

    /**
     * 指定视频是否存在
     *
     * @param uniqueKey 保存的路径
     * @return true则存在
     */
    public boolean isVideoExits(String uniqueKey) {
        String nativeUrl = getNativeUrl(uniqueKey);
        if (!TextUtils.isEmpty(nativeUrl) && !"0".equals(nativeUrl) && !"1".equals(nativeUrl)) {
            File file = new File(nativeUrl);
            return file.exists();
        }
        return false;
    }

    /**
     * 设置视频下载地址
     *
     * @param path 视频下载地址
     */
    public void setDownloadSavePath(String path) {
        this.downloadPath = path;
    }

    /**
     * 取消所有下载
     */
    public void cancelAllDownloads() {
        if (okTasks != null) {
            for (int i = 0; i < okTasks.size(); i++) {
                DownloadTask task = okTasks.get(i);
                if (task != null) {
                    String uniqueKey = (String) task.getTag();
                    String nativeUrl = getNativeUrl(uniqueKey);
                    task.cancel();
                    if ("0".equals(nativeUrl) || "1".equals(nativeUrl)) {
                        File file = task.getFile();
                        if (file != null && file.exists()) {
                            file.delete();
                        }
                        removeDownloadRecord(uniqueKey);
                    }
                }
            }
        }
        if (leTasks != null) {
            for (int i = 0; i < leTasks.size(); i++) {
                LeDownloadInfo info = leTasks.get(i);
                if (info != null) {
                    String uniqueKey = info.getUu() + info.getVu();
                    String nativeUrl = getNativeUrl(uniqueKey);
                    if ("0".equals(nativeUrl) || "1".equals(nativeUrl)) {
                        DownloadCenter.getInstances(VideoManager.getInstance().getApp()).cancelDownload(info, true);
                        removeDownloadRecord(uniqueKey);
                    } else {
                        DownloadCenter.getInstances(VideoManager.getInstance().getApp()).cancelDownload(info, false);
                    }
                }
            }
        }
    }

    /**
     * 检测视频是否过期
     */
    public void checkExpireVideos() {
        try {
            Log.d(TAG, "检测视频是否过期");
            //检测下载的视频是否需要删除（定期清理视频）
            Map<String, String> videosMap = (Map<String, String>) SPUtils.getInstance(VideoSpKey.VIDEO_DOWNLOAD_SP_NAME).getAll();
            //删除过期视屏
            if (videosMap != null && videosMap.size() > 0) {
                long now = System.currentTimeMillis();
                for (String key : videosMap.keySet()) {
                    String value = videosMap.get(key);
                    Log.d(TAG, "检测下载的视频url地址：" + value);
                    File file = new File(value);
                    if (file.exists()) {
                        long time = file.lastModified();
                        long createTimeMillis = Math.abs(now - time);
                        long createDays = createTimeMillis / (24 * 3600 * 1000);
                        if (createDays >= expireDays) {
                            Log.d(TAG, "视频过期，删除");
                            file.delete();
                            removeDownloadRecord(key);
                        }
                    } else {
                        removeDownloadRecord(key);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
