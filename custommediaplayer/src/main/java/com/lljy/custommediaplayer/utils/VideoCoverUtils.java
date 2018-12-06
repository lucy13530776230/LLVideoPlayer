package com.lljy.custommediaplayer.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.lljy.custommediaplayer.entity.VideoEntity;

/**
 * Created by XieGuangwei on 2018/4/13.
 */

public class VideoCoverUtils {
    /**
     * 加载封面
     *
     * @param context
     * @param imageView
     * @param videoEntity
     */
    public static void load(Context context, ImageView imageView, VideoEntity videoEntity) {
        if (imageView == null || context == null || videoEntity == null) {
            return;
        }
        String cover;
        String coverUrl = videoEntity.getCoverUrl();
        String nativeUrl = videoEntity.getNativeUrl();
        String networkUrl = videoEntity.getNetUrl();
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
