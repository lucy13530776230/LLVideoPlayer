package com.lljy.custommediaplayer.entity;

public class VideoBean {
    private String video_order;
    private String videoName;
    private String source;
    private String source_id;
    private String video_id;
    private String src;
    private String nativeSrc;
    private VideoInfo info;

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
}
