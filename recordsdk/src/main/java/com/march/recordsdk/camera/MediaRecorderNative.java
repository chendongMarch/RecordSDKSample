package com.march.recordsdk.camera;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.march.recordsdk.camera.model.MediaObject;
import com.yixia.videoeditor.adapter.UtilityAdapter;


/**
 * 视频录制：边录制边底层处理视频（旋转和裁剪）
 *
 * @author yixia.com
 */
@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class MediaRecorderNative extends MediaRecorderBase implements MediaRecorder.OnErrorListener {


    /**
     * 视频后缀
     */
    private static final String VIDEO_SUFFIX = ".ts";

    /**
     * 开始录制
     */
    @Override
    public MediaObject.MediaPart startRecord() {
        //防止没有初始化的情况
        if (!UtilityAdapter.isInitialized()) {
            UtilityAdapter.initFilterParser();
        }

        MediaObject.MediaPart result = null;

        if (mMediaObject != null) {
            mRecording = true;
            result = mMediaObject.buildMediaPart(mCameraId, VIDEO_SUFFIX);
            String cmd = String.format("filename = \"%s\"; ", result.mediaPath);
            //如果需要定制非480x480的视频，可以启用以下代码，其他vf参数参考ffmpeg的文档：

            cmd += String.format("addcmd = %s; ", " -vf \"transpose=" + readTranspose() + ",crop=" + width + ":" + height + ":0:0\" ");
            UtilityAdapter.FilterParserAction(cmd, UtilityAdapter.PARSERACTION_START);
            if (mAudioRecorder == null && result != null) {
                mAudioRecorder = new AudioRecorder(this);
                mAudioRecorder.start();
            }
        }
        return result;
    }


    private int readTranspose() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * 停止录制
     */
    @Override
    public void stopRecord() {
        UtilityAdapter.FilterParserAction("", UtilityAdapter.PARSERACTION_STOP);
        super.stopRecord();
    }

    /**
     * 数据回调
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mRecording) {
            //底层实时处理视频，将视频旋转好，并剪切成480x480
            UtilityAdapter.RenderDataYuv(data);
        }
        super.onPreviewFrame(data, camera);
    }

    /**
     * 预览成功，设置视频输入输出参数
     */
    @Override
    protected void onStartPreviewSuccess() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Log.i("chendong", "后置摄像  640, 480, 0, UtilityAdapter.FLIPTYPE_NORMAL ");
            UtilityAdapter.RenderInputSettings(640, 480, 0, UtilityAdapter.FLIPTYPE_NORMAL);
        } else {
            Log.i("chendong", "前置摄像  640, 480, 180, UtilityAdapter.FLIPTYPE_HORIZONTAL ");
            UtilityAdapter.RenderInputSettings(640, 480, 0, UtilityAdapter.FLIPTYPE_HORIZONTAL);
        }
        Log.i("chendong", "height, width, mFrameRate,  " + height + "  " + width + "   " + mFrameRate);
        UtilityAdapter.RenderOutputSettings(height, width, mFrameRate, UtilityAdapter.OUTPUTFORMAT_YUV | UtilityAdapter.OUTPUTFORMAT_MASK_MP4 /*| UtilityAdapter.OUTPUTFORMAT_MASK_HARDWARE_ACC*/);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null)
                mr.reset();
        } catch (IllegalStateException e) {
            Log.w("Yixia", "stopRecord", e);
        } catch (Exception e) {
            Log.w("Yixia", "stopRecord", e);
        }
        if (mOnErrorListener != null)
            mOnErrorListener.onVideoError(what, extra);
    }

    /**
     * 接收音频数据，传递到底层
     */
    @Override
    public void receiveAudioData(byte[] sampleBuffer, int len) {
        if (mRecording && len > 0) {
            UtilityAdapter.RenderDataPcm(sampleBuffer);
        }
    }
}
