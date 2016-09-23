package com.march.recordsdk.common;

import android.os.Environment;

import com.march.recordsdk.camera.util.DeviceUtils;

import java.io.File;

/**
 * Project  : VideoRecorder
 * Package  : com.march.recordsdk.common
 * CreateAt : 16/9/18
 * Describe :
 *
 * @author chendong
 */
public class RecordConfig {
    // 最长时长
    private int maxDuration = 10 * 1000;
    // 最短时长
    private int minDuration = 3 * 1000;
    // 帧率
    private int videoBitrate = 1500;
    // MV显示的作者
    private String author = "佚名";
    // 拍摄之后的存储路径
    private String cachePath = Environment.getDownloadCacheDirectory().getAbsolutePath();

    public int getMaxDuration() {
        return maxDuration;
    }

    public int getMinDuration() {
        return minDuration;
    }

    public int getVideoBitrate() {
        return videoBitrate;
    }

    public String getAuthor() {
        return author;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public void setMinDuration(int minDuration) {
        this.minDuration = minDuration;
    }

    public void setVideoBitrate(int videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public void setCachePathInDCIM(String keyName) {
        this.cachePath = initDefCachePath(keyName);
    }


    private String initDefCachePath(String keyName) {
        // 设置拍摄视频缓存路径
        if (keyName == null || keyName.length() == 0)
            keyName = "VCamera";
        String resultPath;
        File mDcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        if (DeviceUtils.isZte()) {
            if (mDcimDir.exists()) {
                resultPath = mDcimDir + "/Camera/" + keyName + "/";
            } else {
                resultPath = mDcimDir.getPath().replace("/sdcard/", "/sdcard-ext/") + "/Camera/" + keyName + "/";
            }
        } else {
            resultPath = mDcimDir + "/Camera/" + keyName + "/";
        }
        return resultPath;
    }
}
