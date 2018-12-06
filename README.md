自定义视频播放器

整合了乐视视频播放器（播放乐视sdk）、腾讯视频播放器（播放url）两种，支持播放视频格式：3gp,mp4,flv,m3u8，可自定义皮肤，可添加到android项目，自带离线缓存功能。

### 使用说明

##### 1.添加权限

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

##### 2.添加乐视Service

```xml
<service
         android:name="com.letvcloud.cmf.MediaService"
         android:process=":cmf" />
```

##### 3.引入播放器库工程

project/build.gradle

```groovy
buildscript {
    
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.2.1'
        

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
        //下载仓库
        maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
    }
}
```

app/build.gradle

```groovy
android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "xxx"
        minSdkVersion 15
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
        multiDexEnabled true
        ndk {
            abiFilters "armeabi", "armeabi-v7a"
        }
    }



    sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

repositories {
    flatDir {
        dirs project(':custommediaplayer').file('libs')
        dirs project(':LePlayerSdk').file('libs')
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    //design
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    //test
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
    implementation project(':custommediaplayer')
}
```



##### 4.初始化信息（最好在Application.java的onCreate()方法初始化

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



##### 5.播放

​	播放时，只要设置好资源，自动选择播放本地视频还是网络视频，会自动选择用那个播放引擎播放本地视频还是网络视频。

**(1)列表设置资源**

图示：

![](http://git.cke123.com/XieGuangwei/CustomVideoPlayer/raw/master/screenshot/list_cap.png)

xml

```xml
<com.lljy.custommediaplayer.view.player.CustomListVideoPlayer
        android:id="@+id/video_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:needTouchControlProgress="true"
        app:needTouchControlVol="true"></com.lljy.custommediaplayer.view.player.CustomListVideoPlayer>
```

```java
参数说明：
needTouchControlProgress="true"//是否可手势滑动控制进度
needTouchControlVol="true"//是否可手势滑动控制音量
```

java

```java
mVideoView = findViewById(R.id.video_view);//初始化视频播放器
mVideoView.setController(new ListController(this));//设置皮肤为列表
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
mVideoView.setVideos(videos);//设置播放资源，并播放

//注意：info、video_id、source、一定要设置，否则不能播放和缓存
//回调，记录全屏，全屏需手动设置撑开布局
```

**（2）单个视频**

图示：

![](http://git.cke123.com/XieGuangwei/CustomVideoPlayer/raw/master/screenshot/single_cap.png)

xml

```xml
<com.lljy.custommediaplayer.view.player.SimpleVideoPlayer
        android:id="@+id/video_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:needTouchControlProgress="true"
        app:needTouchControlVol="true"></com.lljy.custommediaplayer.view.player.SimpleVideoPlayer>
```

```java
参数说明：
needTouchControlProgress="true"//是否可手势滑动控制进度
needTouchControlVol="true"//是否可手势滑动控制音量
```

java

```java
mVideoView = findViewById(R.id.video_view);//初始化视频播放器
mVideoView.setController(new ListController(this));//设置皮肤为简单皮肤
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
mVideoView.setVideo(video2);//设置播放资源，并播放
```

**(3)回调监听**

```java
mVideoView.setListener(new IVideoListener() {
            @Override
            public void onStartFullScreen() {

            }

            @Override
            public void onExitFullScreen() {

            }

            @Override
            public void onError(String msg) {

            }

            @Override
            public void onComplete() {

            }
        });
```



**(4)生命周期**

```java
@Override
protected void onPause() {
    super.onPause();
    mVideoView.onPause();//释放播放器资源
}

@Override
protected void onResume() {
    super.onResume();
    mVideoView.onResume();//恢复播放
}

@Override
protected void onDestroy() {
    super.onDestroy();
    VideoManager.getInstance().cancelAllDownloads();//取消所有下载任务（这个在退出app调用即可，不用每个activity或者fragment都添加）
}
```

##### 6.引擎选择机制

​	设置的VideoEngineType来选择播放引擎和下载引擎，默认会设置引擎类型为VideoEngineType.TYPE_TENCENT，如需使用乐视播放器引擎播放和下载，需设置引擎类型为videoEntity.setVideoEngineType(VideoEngineType.TYPE_LETV)，播放器会根据设置的类型选择播放和下载类型。