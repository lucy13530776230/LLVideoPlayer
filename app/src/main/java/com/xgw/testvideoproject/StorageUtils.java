package com.xgw.testvideoproject;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by XieGuangwei on 2017/12/24.
 * 存储相关工具类
 */

public class StorageUtils {
    /**
     * 获取那个目录下的文件路径列表
     * 展会使用，完后删除
     *
     * @param dir
     * @return
     */
    public static List<String> getVideoPaths(String dir) {
        ArrayList<String> fileList = new ArrayList<String>();
        File extStorage = new File(dir);
        if (!extStorage.exists()) {
            return new ArrayList<>();
        }
        File[] files = extStorage.listFiles();
        if (files == null) {
            return fileList;
        }
        //获取路径
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                fileList.add(files[i].getAbsolutePath());
            }
        }
        return fileList;
    }
}
