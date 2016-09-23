package com.march.recordsdk.ui.record;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.march.recordsdk.R;
import com.march.recordsdk.ui.BaseRecordActivityWrap;
import com.march.recordsdk.ui.widget.ProgressView;
import com.march.recordsdk.ui.widget.VideoView;
import com.march.recordsdk.camera.FFMpegUtils;
import com.march.recordsdk.camera.model.MediaObject;
import com.march.recordsdk.camera.util.DeviceUtils;


/**
 * 视频导入
 */
public class ImportVideoActivity extends BaseRecordActivityWrap implements OnClickListener, OnPreparedListener, VideoView.OnPlayStateListener {

    /**
     * 视频预览
     */
    private VideoView mVideoView;
    /**
     * 暂停图标
     */
    private View mRecordPlay;
    /**
     * 视频总进度条
     */
    private ProgressView mProgressView;

    /**
     * 视频信息
     */
    private MediaObject mMediaObject;
    private MediaObject.MediaPart mMediaPart;
    /**
     * 窗体宽度
     */
    private int size;
    private String mVideoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 防止锁屏

        String obj = getIntent().getStringExtra("obj");
        mVideoPath = getIntent().getStringExtra("path");
        mMediaObject = restoreMediaObject(obj);
        if (mMediaObject == null) {
            Toast.makeText(this, R.string.record_read_object_faild, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        size = DeviceUtils.getScreenWidth(this);
        setContentView(R.layout.activity_import_video);

        // ~~~ 绑定控件
        mVideoView = getView(R.id.record_preview);
        mRecordPlay = getView(R.id.record_play);
        mProgressView = getView(R.id.record_progress);

        // ~~~ 绑定事件
        mVideoView.setOnClickListener(this);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnPlayStateListener(this);
        setClick(R.id.title_left, this);
        setClick(R.id.title_right, this);
        View view = getView(R.id.record_layout);
        if (view != null) {
            view.getLayoutParams().height = size;
        }
        mVideoView.setVideoPath(mVideoPath);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_left)
            finish();
        if (v.getId() == R.id.title_right)
            startEncoding();
    }

    /**
     * 开始转码
     */
    private void startEncoding() {
        //检测磁盘空间
//		if (FileUtils.showFileAvailable() < 200) {
//			Toast.makeText(this, R.string.record_camera_check_available_faild, Toast.LENGTH_SHORT).show();
//			return;
//		}

        if (!isFinishing() && mMediaObject != null && mMediaPart != null) {
            final int width = mVideoView.getVideoWidth();
            final int height = mVideoView.getHeight();

            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    showProgress("", getString(R.string.record_camera_progress_message));
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                    return FFMpegUtils.importVideo(mMediaPart, size, width, height, 0, 0, true);
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    hideProgress();
                    if (result) {
                        saveMediaObject(mMediaObject);
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(ImportVideoActivity.this, R.string.record_video_transcoding_faild, Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
    }

    @Override
    public void onStateChanged(boolean isPlaying) {
        if (isPlaying)
            mRecordPlay.setVisibility(View.GONE);
        else
            mRecordPlay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (!isFinishing()) {
            if (mVideoView.getVideoWidth() == 0 || mVideoView.getVideoHeight() == 0) {
                Toast.makeText(ImportVideoActivity.this, R.string.record_camera_import_video_faild, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            mVideoView.start();
            mVideoView.setLooping(true);

            int duration = mMediaObject.getMaxDuration() - mMediaObject.getDuration();
            if (duration > mVideoView.getDuration())
                duration = mVideoView.getDuration();

            mMediaPart = mMediaObject.buildMediaPart(mVideoPath, duration, MediaObject.MEDIA_PART_TYPE_IMPORT_VIDEO);
            mProgressView.setData(mMediaObject);
        }
    }
}
