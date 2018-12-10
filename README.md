# 自定义视频播放器

整合了乐视视频播放器（播放乐视sdk）、腾讯视频播放器（播放url）两种，支持播放视频格式：3gp,mp4,flv,m3u8，可自定义皮肤，可添加到android项目，自带离线缓存功能。

## 使用说明

### 1.添加权限

```xml
<uses-permission android:name="android.permission.INTERNET" /><!--必须-->
<uses-permission android:name="android.permission.GET_TASKS"/><!--必须-->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /><!--必须-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /><!--必须-->
<uses-permission android:name="android.permission.READ_LOGS" /><!--必须-->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.RECEIVE_USER_PRESENT"/>
```

### 2.添加乐视Service

```xml
<service
         android:name="com.letvcloud.cmf.MediaService"
         android:process=":cmf" />
```

### 3.引入播放器库工程

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



### 4.初始化信息（最好在Application.java的onCreate()方法初始化

```java
public class AppConfig extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VideoManager.getInstance()
                .initApp(this)//初始化全局Application
            	.enableDownloadEngine(true)//默认就是true，不设置为true，播放时播放网络资源，并且不会下载
                .setVideoExpireDays(1)//初始化视频过期时间
                .setVideoSavedPath(Environment.getExternalStorageDirectory().getPath() + 	                                                    File.separator + "llplayer" + File.separator);//设置视频缓存目录
    }
}
```



### 5.播放

​	播放时，只要设置好资源，自动选择播放本地视频还是网络视频，会自动选择用那个播放引擎播放本地视频还是网络视频。

#### (1)列表设置资源

图示：

![](http://git.cke123.com/XieGuangwei/CustomVideoPlayer/raw/master/screenshot/list_cap.png)

xml

```xml
<com.lljy.custommediaplayer.view.player.CustomListVideoPlayer
        android:id="@+id/video_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:needBackButtonOnFullScreenStatus="true"
        app:needBackButtonOnNormalScreenStatus="true"
        app:needStartOrExitFullScreenButton="true"
        app:needTitle="true"
        app:needTopTitleAndBackLayout="true"
        app:needTouchControlProgress="true"
        app:needTouchControlVol="true"></com.lljy.custommediaplayer.view.player.CustomListVideoPlayer>
```

参数说明：

| 参数名                             | 参数类型 | 默认值 | 参数说明                                   |
| ---------------------------------- | -------- | ------ | ------------------------------------------ |
| needBackButtonOnFullScreenStatus   | boolean  | true   | 全屏时是否需要显示顶部返回按钮             |
| needBackButtonOnNormalScreenStatus | boolean  | false  | 非全屏时是否需要显示顶部返回按钮           |
| needStartOrExitFullScreenButton    | boolean  | true   | 是否需要全屏/退出全屏按钮                  |
| needTitle                          | boolean  | true   | 是否需要视屏标题                           |
| needTopTitleAndBackLayout          | boolean  | true   | 是否需要顶部布局（目前包含返回按钮和标题） |
| needTouchControlProgress           | boolean  | true   | 是否需要手势控制播放进度（手势左右滑动）   |
| needTouchControlVol                | boolean  | true   | 是否需要手动控制音量（手势上下滑动）       |



初始化列表

```java
mVideoView = findViewById(R.id.video_view);
mVideoView.setController(new ListController(this));
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
videos.add(video3);
//设置播放列表
mVideoView.setVideos(videos);
```

#### （2）单个视频

图示：

![](http://git.cke123.com/XieGuangwei/CustomVideoPlayer/raw/master/screenshot/single_cap.png)

xml

```xml
<com.lljy.custommediaplayer.view.player.SimpleVideoPlayer
        android:id="@+id/video_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:needBackButtonOnFullScreenStatus="true"
        app:needBackButtonOnNormalScreenStatus="true"
        app:needStartOrExitFullScreenButton="true"
        app:needTitle="true"
        app:needTopTitleAndBackLayout="true"
        app:needTouchControlProgress="true"
        app:needTouchControlVol="true"></com.lljy.custommediaplayer.view.player.SimpleVideoPlayer>
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
mVideoView.setVideo(video2);//设置播放资源，并播放
```

#### （3）播放出错

图示：

![](http://git.cke123.com/XieGuangwei/CustomVideoPlayer/raw/master/screenshot/error.png)

### 6.回调监听（回调监听注册一定要加载mVideoView.setController之前）

```java
//回调监听注册一定要加载mVideoView.setController之前，回调监听注册一定要加载mVideoView.setController之前，回调监听注册一定要加载mVideoView.setController之前，
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
```



### 7.生命周期

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
```

### 8.引擎选择机制

VideoEntity.setVideoEngineType(VideoEngineType)

| 类型                               | 说明                 |
| ---------------------------------- | -------------------- |
| VideoEngineType.TYPE_ANDROID_MEDIA | 原生播放器（默认值） |
| VideoEngineType.TYPE_TENCENT       | 腾讯视频播放器       |
| VideoEngineType.TYPE_LETV          | 乐视视频播放器       |

实体类默认使用原生播放器，设置腾讯视频播放器和乐视播放器播放链接可以支持多种格式，原生的支持格式相对较少。