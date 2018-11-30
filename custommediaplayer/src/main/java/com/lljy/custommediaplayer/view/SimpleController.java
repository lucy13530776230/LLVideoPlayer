package com.lljy.custommediaplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.lljy.custommediaplayer.R;

/**
 * @desc: 简单的播放器控制页
 * 实现的有：①手势右滑快进左滑倒退；②右半屏上滑加音量下滑降音量；③进度条实时显示，拖拽进度条控制进度。
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/30 10:27
 */

public class SimpleController extends AbsController {
    public SimpleController(Context context) {
        super(context);
    }

    public SimpleController(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 给子类初始化拓展控件用的
     *
     * @param contentView 内容view
     */
    @Override
    protected void initExtensionViews(View contentView) {

    }

    /**
     * 获取子控件布局id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.simple_controller_layout;
    }
}
