## 自定义视频播放器

整合了乐视视频播放器（播放乐视sdk）、腾讯视频播放器（播放url）两种，支持播放视频格式：3gp,mp4,flv,m3u8，可自定义皮肤，可添加到android项目，自带离线缓存功能。

### 使用说明

1.添加权限

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.GET_TASKS"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_LOGS" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.RECEIVE_USER_PRESENT"/>
```

2.添加乐视Service

```xml
<service
         android:name="com.letvcloud.cmf.MediaService"
         android:process=":cmf" />
```

3.初始化信息（最好在Application.java的onCreate()方法初始化

```java
public class AppConfig extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VideoManager.getInstance()
                .initApp(this)//初始化全局Application
                .setVideoExpireDays(1)//初始化视频过期时间
                .setVideoSavedPath(Environment.getExternalStorageDirectory().getPath() + 	                                                    File.separator + "llplayer" + File.separator);//设置视频缓存目录
    }
}
```



4.layout里面添加播放器

```xml
<com.lljy.custommediaplayer.view.player.CustomListVideoPlayer
        android:id="@+id/video_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:needTouchControlProgress="true"
        app:needTouchControlVol="true"></com.lljy.custommediaplayer.view.player.CustomListVideoPlayer>
```

    参数说明：
    	app:needTouchControlProgress="true"//是否可手势滑动控制进度
        app:needTouchControlVol="true"//是否可手势滑动控制音量
5.设置播放资源

(1)列表

```java
mVideoView = findViewById(R.id.video_view);//初始化视频播放器
mVideoView.setController(new ListController(this));//设置皮肤为列表
//开始设置资源
List<VideoBean> videos = new ArrayList<>();

VideoBean video1 = new VideoBean();
video1.setPlaying(true);
video1.setSrc("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-33-30.mp4");
video1.setVideo_id("1");
VideoInfo info = new VideoInfo();
info.setThumbnails("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-17_17-30-43.jpg");
video1.setVideoName("视频1");
video1.setInfo(info);
video1.setSource("qiniu");
videos.add(video1);

VideoBean video2 = new VideoBean();
video2.setPlaying(false);
video2.setSrc("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-20-26.mp4");
video2.setVideo_id("2");
VideoInfo info2 = new VideoInfo();
info2.setThumbnails("http://tanzi27niu.cdsb.mobi/wps/wp-content/uploads/2017/05/2017-05-10_10-09-58.jpg");
video2.setVideoName("视频2");
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
video3.setVideoName("视频3");
video3.setSource("letv");
video3.setInfo(info3);
videos.add(video3);

//注意：info、video_id、source、一定要设置，否则不能播放和缓存
```


