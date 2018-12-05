package com.lljy.custommediaplayer.entity;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * @desc: 视频实体类，管理视频各种信息
 * 如：视频顺序{@link #video_order}、视频名称{@link #videoName}、是否正在播放{@link #isPlaying}等
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/5 10:16
 */

public class VideoBean implements Comparable<VideoBean>, Serializable {
    private static final long serialVersionUID = -7842663983256502058L;
    private String video_order;
    private String videoName;
    private String source;
    private String source_id;
    private String video_id;
    private String src;
    private String nativeSrc;
    private VideoInfo info;

    private boolean isPlaying;

    public String getVideo_order() {
        return video_order;
    }

    public void setVideo_order(String video_order) {
        this.video_order = video_order;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource_id() {
        return source_id;
    }

    public void setSource_id(String source_id) {
        this.source_id = source_id;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getNativeSrc() {
        return nativeSrc;
    }

    public void setNativeSrc(String nativeSrc) {
        this.nativeSrc = nativeSrc;
    }

    public VideoInfo getInfo() {
        return info;
    }

    public void setInfo(VideoInfo info) {
        this.info = info;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VideoBean && !TextUtils.isEmpty(((VideoBean) obj).video_id) && (((VideoBean) obj).video_id).equals(video_id);
    }

    @Override
    public int compareTo(VideoBean o) {
        try {
            if (o == null) {
                return -1;
            }
            if (TextUtils.isEmpty(o.getVideo_order())) {
                return -1;
            }
            if (TextUtils.isEmpty(this.video_order)) {
                return 1;
            }
            int thisOrder = Integer.parseInt(this.video_order);
            int otherOrder = Integer.parseInt(o.getVideo_order());
            //order相同，则按照视频id排序
            if (thisOrder == otherOrder) {
                if (TextUtils.isEmpty(o.getVideo_id())) {
                    return -1;
                }
                if (TextUtils.isEmpty(this.video_id)) {
                    return 1;
                }
                int thisId = Integer.parseInt(this.video_id);
                int otherId = Integer.parseInt(o.getVideo_id());
                return otherId - thisId;
            } else {
                return thisOrder - otherOrder;
            }
        } catch (Exception e) {

        }
        return 0;
    }
}
