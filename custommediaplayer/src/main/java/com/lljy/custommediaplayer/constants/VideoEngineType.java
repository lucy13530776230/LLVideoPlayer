package com.lljy.custommediaplayer.constants;
/**
 * @desc: 视频播放器引擎类型
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/6 14:50
 */

/**
 * @desc:
 * 播放器引擎类型
 * 播放资源类型决定使用哪种播放引擎和下载引擎，默认引擎为{@link #TYPE_TENCENT},如需乐视播放器，需设置类型为{@link #TYPE_LETV}
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/6 16:02
 */

public final class VideoEngineType {
    public static final String TYPE_TENCENT = "typeTencent";//通过url就可以播放的视频
    public static final String TYPE_LETV = "typeLeTv";//通过乐视视频播放
}
