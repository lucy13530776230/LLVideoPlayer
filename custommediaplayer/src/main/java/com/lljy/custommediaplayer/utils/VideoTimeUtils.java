package com.lljy.custommediaplayer.utils;

/**
 * Created by XieGuangwei on 2018/9/5.
 */

public class VideoTimeUtils {
    /**
     * 时间格式化
     *
     * @param millisecond 总时间 毫秒
     * @return
     */
    public static String updateTimeFormat(int millisecond) {
        //将毫秒转换为秒
        int second = millisecond / 1000;
        //计算小时
        int hh = second / 3600;
        //计算分钟
        int mm = second % 3600 / 60;
        //计算秒
        int ss = second % 60;
        //判断时间单位的位数
        String str = null;
        if (hh != 0) {//表示时间单位为三位
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        return str;
    }
}
