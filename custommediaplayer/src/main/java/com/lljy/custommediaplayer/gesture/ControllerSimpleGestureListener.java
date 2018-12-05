package com.lljy.custommediaplayer.gesture;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.lljy.custommediaplayer.constants.ScrollMode;

/**
 * @desc: 控制器手势回调监听类
 * 监听手势类型：按下{@link #onDown(MotionEvent)}、滑动{@link #onScroll(MotionEvent, MotionEvent, float, float)}等等
 * @author: XieGuangwei
 * @email: 775743075@qq.com
 * create at 2018/11/30 11:11
 */


public class ControllerSimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = "ControllerSimpleGestureListener";
    private GestureResultListener mGestureResultListener;
    private ScrollMode mScrollMode;
    //横向偏移检测，让快进快退不那么敏感
    private int offsetX = 1;
    private boolean hasFF_REW = false;
    private View gestureLayout;

    public ControllerSimpleGestureListener(View gestureLayout) {
        this.gestureLayout = gestureLayout;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        hasFF_REW = false;
        //每次按下都重置为NONE
        mScrollMode = ScrollMode.NONE;
        if (mGestureResultListener != null) {
            mGestureResultListener.onDown(e);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (gestureLayout == null) {
            return true;
        }
        int width = gestureLayout.getWidth();
        switch (mScrollMode) {
            case NONE:
                Log.d(TAG, "NONE: ");
                //offset是让快进快退不要那么敏感的值
                if (Math.abs(distanceX) - Math.abs(distanceY) > offsetX) {
                    mScrollMode = ScrollMode.FF_REW;
                } else {
//                    if (e1.getX() < width / 2) {
//                        mScrollMode = ScrollMode.BRIGHTNESS;
//                    } else {
//                        mScrollMode = ScrollMode.VOLUME;
//                    }
                    if (e1.getX() >= width / 2) {
                        mScrollMode = ScrollMode.VOLUME;
                    }
                }
                break;
            case VOLUME:
                if (mGestureResultListener != null) {
                    mGestureResultListener.onVolumeGesture(e1, e2, distanceX, distanceY);
                }
                break;
            case BRIGHTNESS:
                if (mGestureResultListener != null) {
                    mGestureResultListener.onBrightnessGesture(e1, e2, distanceX, distanceY);
                }
                break;
            case FF_REW:
                if (mGestureResultListener != null) {
                    mGestureResultListener.onFF_REWGesture(e1, e2, distanceX, distanceY);
                }
                hasFF_REW = true;
                break;
        }
        return true;
    }


    @Override
    public boolean onContextClick(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (mGestureResultListener != null) {
            mGestureResultListener.onDoubleTapGesture(e);
        }
        return super.onDoubleTap(e);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return super.onDoubleTapEvent(e);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return super.onFling(e1, e2, velocityX, velocityY);
    }


    @Override
    public void onShowPress(MotionEvent e) {
        super.onShowPress(e);
    }


    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mGestureResultListener != null) {
            mGestureResultListener.onSingleTapGesture(e);
        }
        return super.onSingleTapConfirmed(e);
    }

    /**
     * 是否正在滑动快进/倒退
     *
     * @return
     */
    public boolean isHasFF_REW() {
        return hasFF_REW;
    }

    /**
     * 设置滑动状态
     *
     * @param hasFF_REW
     */
    public void setHasFF_REW(boolean hasFF_REW) {
        this.hasFF_REW = hasFF_REW;
    }

    public void setVideoGestureListener(GestureResultListener gestureResultListener) {
        this.mGestureResultListener = gestureResultListener;
    }
}
