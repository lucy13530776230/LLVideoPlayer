package com.lljy.custommediaplayer.view.controller;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lljy.custommediaplayer.R;


/**
 * Created by XieGuangwei on 2018/1/4.
 * 声音、亮度、进度控制显示布局
 */

public class ShowControlView extends RelativeLayout {
    TextView titleTv;
    TextView valueTv;
    private HideRunnable mHideRunnable;
    private int duration = 1000;

    public ShowControlView(Context context) {
        this(context, null);
    }

    public ShowControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.show_control_layout, this, true);
        titleTv = contentView.findViewById(R.id.title_tv);
        valueTv = contentView.findViewById(R.id.value_tv);
        mHideRunnable = new HideRunnable();
        setVisibility(GONE);
    }

    public void show(String title, String value) {
        setVisibility(VISIBLE);
        titleTv.setText(title);
        valueTv.setText(value);
        removeCallbacks(mHideRunnable);
        postDelayed(mHideRunnable, duration);
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    //隐藏自己的Runnable
    private class HideRunnable implements Runnable {
        @Override
        public void run() {
            setVisibility(GONE);
            ShowControlView.this.removeCallbacks(mHideRunnable);
        }
    }
}
