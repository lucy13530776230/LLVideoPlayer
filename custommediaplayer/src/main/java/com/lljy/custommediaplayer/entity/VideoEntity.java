package com.lljy.custommediaplayer.entity;

import android.text.TextUtils;

import com.lljy.custommediaplayer.constants.VideoEngineType;

import java.io.Serializable;

/**
 * @desc: 视频实体类，管理视频各种信息
 * 如：视频顺序{@link #order}、视频名称{@link #videoName}、是否正在播放{@link #isPlaying}等
 * 播放器引擎类型{@link #videoEngineType}
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/12/5 10:16
 */

public class VideoEntity implements Comparable<VideoEntity>, Serializable, Cloneable {
    private static final long serialVersionUID = -7842663983256502058L;
    private String id;//视频id
    private String order;//视频顺序
    private String videoName;//视频名称
    private String videoEngineType = VideoEngineType.TYPE_TENCENT;//默认是腾讯视频播放引擎
    private String netUrl;//网络地址
    private String nativeUrl;//本地地址
    private String coverUrl;//封面地址
    private String uu;//乐视uuid
    private String vu;//乐视vuid

    private boolean isPlaying;

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoEngineType() {
        return videoEngineType;
    }

    public void setVideoEngineType(String videoEngineType) {
        this.videoEngineType = videoEngineType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNetUrl() {
        return netUrl;
    }

    public void setNetUrl(String netUrl) {
        this.netUrl = netUrl;
    }

    public String getNativeUrl() {
        return nativeUrl;
    }

    public void setNativeUrl(String nativeUrl) {
        this.nativeUrl = nativeUrl;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getUu() {
        return uu;
    }

    public void setUu(String uu) {
        this.uu = uu;
    }

    public String getVu() {
        return vu;
    }

    public void setVu(String vu) {
        this.vu = vu;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof VideoEntity && !TextUtils.isEmpty(((VideoEntity) obj).id) && (((VideoEntity) obj).id).equals(id) && !TextUtils.isEmpty(((VideoEntity) obj).videoEngineType) && ((VideoEntity) obj).videoEngineType.equals(videoEngineType);
    }

    @Override
    public int compareTo(VideoEntity o) {
        try {
            if (o == null) {
                return -1;
            }
            if (TextUtils.isEmpty(o.getOrder())) {
                return -1;
            }
            if (TextUtils.isEmpty(this.order)) {
                return 1;
            }
            int thisOrder = Integer.parseInt(this.order);
            int otherOrder = Integer.parseInt(o.getOrder());
            //order相同，则按照视频id排序
            if (thisOrder == otherOrder) {
                if (TextUtils.isEmpty(o.getId())) {
                    return -1;
                }
                if (TextUtils.isEmpty(this.id)) {
                    return 1;
                }
                int thisId = Integer.parseInt(this.id);
                int otherId = Integer.parseInt(o.getId());
                return otherId - thisId;
            } else {
                return thisOrder - otherOrder;
            }
        } catch (Exception e) {

        }
        return 0;
    }

    @Override
    public VideoEntity clone() {
        try {
            return (VideoEntity) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
