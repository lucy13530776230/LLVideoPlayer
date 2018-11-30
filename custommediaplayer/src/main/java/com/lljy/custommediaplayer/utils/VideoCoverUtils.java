package com.lljy.custommediaplayer.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.lljy.custommediaplayer.entity.VideoBean;
import com.lljy.custommediaplayer.entity.VideoInfo;

/**
 * Created by XieGuangwei on 2018/4/13.
 */

public class VideoCoverUtils {
    /**
     * 加载封面
     *
     * @param context
     * @param imageView
     * @param videoBean
     */
    public static void load(Context context, ImageView imageView, VideoBean videoBean) {
        if (imageView == null || context == null || videoBean == null || videoBean.getInfo() == null) {
            return;
        }
        VideoInfo info = videoBean.getInfo();
        String cover;
        String coverUrl = info.getThumbnails();
        String nativeUrl = videoBean.getNativeSrc();
        String networkUrl = videoBean.getSrc();
        if (!TextUtils.isEmpty(coverUrl)) {
            cover = coverUrl;
        } else if (!TextUtils.isEmpty(nativeUrl)) {
            cover = nativeUrl;
        } else {
            cover = networkUrl;
        }
        RequestBuilder<Drawable> builder = Glide.with(context).load(cover);
        if (!TextUtils.isEmpty(coverUrl)) {
            builder.into(imageView);
        } else {
            builder.thumbnail(0.3f).into(imageView);
        }
    }


}
