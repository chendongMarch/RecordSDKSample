package com.march.recordsdk.common;

import android.content.Intent;
import android.os.Bundle;

/**
 * Project  : RecordSDKSample
 * Package  : com.march.recordsdk.common
 * CreateAt : 16/9/20
 * Describe : 拍摄完成返回的资源
 *
 * @author chendong
 */
public class RecordResult {
    private String videoPath;
    private String thumbPath;
    private int duration;

    public RecordResult(Intent data) {
        Bundle internalBundle = data.getBundleExtra(RecordConstant.KEY_VIDEO_RESULT);
        this.thumbPath = internalBundle.getString(RecordConstant.KEY_VIDEO_STORE_PATH);
        this.videoPath = internalBundle.getString(RecordConstant.KEY_VIDEO_THUMB_PATH);
        this.duration = internalBundle.getInt(RecordConstant.KEY_VIDEO_DURATION);
    }

    public String getVideoPath() {
        return videoPath;
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
