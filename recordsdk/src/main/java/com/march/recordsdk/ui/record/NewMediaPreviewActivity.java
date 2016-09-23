package com.march.recordsdk.ui.record;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.march.quickrvlibs.RvViewHolder;
import com.march.quickrvlibs.SimpleRvAdapter;
import com.march.recordsdk.R;
import com.march.recordsdk.RecordSDK;
import com.march.recordsdk.common.RecordCommonHelper;
import com.march.recordsdk.common.POThemeSingle;
import com.march.recordsdk.common.RecordConstant;
import com.march.recordsdk.common.ThemeHelper;
import com.march.recordsdk.service.AssertService;
import com.march.recordsdk.ui.BaseRecordActivityWrap;
import com.march.recordsdk.ui.widget.BitmapImageView;
import com.march.recordsdk.ui.widget.ThemeSurfaceView;
import com.march.recordsdk.camera.model.MediaObject;
import com.march.recordsdk.camera.model.MediaThemeObject;
import com.march.recordsdk.camera.util.DeviceUtils;
import com.march.recordsdk.camera.util.FileUtils;
import com.march.recordsdk.camera.util.Log;
import com.march.recordsdk.camera.util.StringUtils;
import com.yixia.videoeditor.adapter.UtilityAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * 视频预览
 *
 * @author tangjun@yixia.com
 */
public class NewMediaPreviewActivity extends BaseRecordActivityWrap implements OnClickListener, UtilityAdapter.OnNativeListener {

    /**
     * 开始转码
     */
    private static final int HANDLER_ENCODING_START = 100;
    /**
     * 转码进度
     */
    private static final int HANDLER_ENCODING_PROGRESS = 101;
    /**
     * 转码结束
     */
    private static final int HANDLER_ENCODING_END = 102;
    /**
     * 无主题放的位置
     */
    private final static int NO_THEME_INDEX = 0;

    /**
     * 播放按钮、主题音量按钮
     */
    private ImageView mPlayStatus;
    /**
     * 上一步、下一步
     */
    private TextView mVideoPreviewMusic;
    /**
     * 主题音乐，原声音
     */
    private CheckBox mThemeVolumeCb, mVideoVolumeCb;
    /**
     * 正在加载
     */
    private View mLoadingView;
    /**
     * MV主题
     */
    private ThemeSurfaceView mThemeSurfaceView;


    private RecyclerView mThemeRv;
    /**
     * 主题缓存的目录
     */
    private File mThemeCacheDir;
    /**
     * 当前主题
     */
    private POThemeSingle mCurrentTheme;

    /**
     * 主题列表
     */
    private ArrayList<POThemeSingle> mThemeList;
    /**
     * 滤镜列表
     */
    private ArrayList<POThemeSingle> mFilterList;

    /**
     * 导演签名图片
     */
    private String mAuthorBitmapPath;
    /**
     * 导出视频，导出封面
     */
    private String mVideoPath, mCoverPath;
    /**
     * 临时合并ts流
     */
    private String mVideoTempPath;
    /**
     * 当前音乐路径
     */
    private String mCurrentMusicPath;
    /**
     * 当前音乐名称
     */
    private String mCurrentMusicTitle;
    /**
     * 当前音乐名称
     */
    private String mCurrentMusicName;
    /**
     * 是否需要回复播放
     */
    private boolean mNeedResume;
    /**
     * 是否停止播放
     */
    private boolean mStopPlayer;
    /**
     * 是否正在转码
     */
    private boolean mStartEncoding;
    /**
     * 视频信息
     */
    private MediaObject mMediaObject;
    private SimpleRvAdapter<POThemeSingle> editAdapter;
    private POThemeSingle currentTheme;
    private POThemeSingle currentFilter;
    private boolean isEditTheme = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("chendong", "create NewMediaPreviewActivity");
        mMediaObject = (MediaObject) getIntent().getSerializableExtra(RecordConstant.EXTRA_MEDIA_OBJECT);
        if (mMediaObject == null) {
            Toast.makeText(this, R.string.record_read_object_faild, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 防止锁屏
        prepareActivity();
        prepareViews();
    }

    /**
     * 预处理参数
     */
    private boolean prepareActivity() {
        //加载默认参数
        //获取传入参数
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && !isExternalStorageRemovable())
            mThemeCacheDir = new File(getExternalCacheDir(), "Theme");
        else
            mThemeCacheDir = new File(getCacheDir(), "Theme");

        mVideoPath = mMediaObject.getOutputVideoPath();
        if (StringUtils.isNotEmpty(mVideoPath)) {
            mCoverPath = mVideoPath.replace(".mp4", ".jpg");
        }
        mVideoTempPath = getIntent().getStringExtra("output");

        return true;
    }

    /**
     * 预处理UI相关
     */
    private void prepareViews() {
        setContentView(R.layout.activity_new_media_preview);
        //绑定控件
        mPlayStatus = getView(R.id.play_status);
        mThemeSurfaceView = getView(R.id.preview_theme);
        setClick(this, R.id.titleLeft, R.id.titleRight, R.id.titleText);

        mVideoPreviewMusic = getView(R.id.video_preview_music);
        mThemeRv = getView(R.id.rv_theme);
        mThemeVolumeCb = getView(R.id.video_preview_theme_volume);
        mVideoVolumeCb = getView(R.id.video_preview_video_volume);
        mLoadingView = getView(R.id.loading);

        mThemeSurfaceView.setOnComplateListener(mOnCompleteListener);
        mThemeSurfaceView.setOnClickListener(this);
        setClick(R.id.tab_theme, this);
        setClick(R.id.tab_filter, this);
        mThemeVolumeCb.setOnClickListener(this);
        mVideoVolumeCb.setOnClickListener(this);

        //设置主题预览默认参数
        mThemeSurfaceView.setIntent(getIntent());
        mThemeSurfaceView.setOutputPath(mVideoPath);//输出文件
        mThemeSurfaceView.setMediaObject(mMediaObject);
        if (FileUtils.checkFile(mThemeCacheDir)) {
            mThemeSurfaceView.setFilterCommonPath(new File(mThemeCacheDir, ThemeHelper.THEME_VIDEO_COMMON).getAbsolutePath());
        }
        /** 设置播放区域 */
        View preview_layout = getView(R.id.preview_layout);
        if (preview_layout != null) {
            preview_layout.getLayoutParams().height = DeviceUtils.getScreenWidth(this);
        }
        loadThemes();
    }

    @Override
    public void onResume() {
        super.onResume();
        UtilityAdapter.registerNativeListener(this);
        if (mThemeSurfaceView != null && mNeedResume && mCurrentTheme != null) {
            restart();
        }
        mNeedResume = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        UtilityAdapter.registerNativeListener(null);
        if (mThemeSurfaceView != null && mThemeSurfaceView.isPlaying()) {
            mNeedResume = true;
            releaseVideo();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.titleLeft) {
            finish();
        } else if (id == R.id.titleRight) {
            startEncoding();
        } else if (id == R.id.preview_theme) {//点击暂停视频播放
            if (isPlaying())
                stopVideo();
            else
                startVideo();
        } else if (id == R.id.video_preview_theme_volume) {//静音主题音
            //隐藏动画
            RecordCommonHelper.showToastImage(this, mThemeVolumeCb.isChecked() ? R.drawable.priview_theme_volumn_close : R.drawable.priview_theme_volumn_open);
            mThemeSurfaceView.setThemeMute(mThemeVolumeCb.isChecked());
            restart();
        } else if (id == R.id.video_preview_video_volume) {//静音原声
            RecordCommonHelper.showToastImage(this, mVideoVolumeCb.isChecked() ? R.drawable.priview_orig_volumn_close : R.drawable.priview_orig_volumn_open);
            mThemeSurfaceView.setOrgiMute(mVideoVolumeCb.isChecked());
            restart();
        } else if (id == R.id.tab_theme) {
            if (!isEditTheme) {
                editAdapter.updateData(mThemeList);
                isEditTheme = true;
            }
        } else if (id == R.id.tab_filter) {
            if (isEditTheme) {
                editAdapter.updateData(mFilterList);
                isEditTheme = false;
            }
        }
    }

    /**
     * 开始转码
     */

    private void startEncoding() {
        stopVideo();

        //更新静音
        if (mMediaObject != null && mMediaObject.mThemeObject != null) {
            mMediaObject.mThemeObject.mThemeMute = mThemeVolumeCb.isChecked();
            mMediaObject.mThemeObject.mOrgiMute = mVideoVolumeCb.isChecked();
        }
        //检测是否需要重新编译
        mStartEncoding = true;
        mHandler.removeMessages(HANDLER_ENCODING_START);
        mHandler.removeMessages(HANDLER_ENCODING_PROGRESS);
        mHandler.removeMessages(HANDLER_ENCODING_END);
        mHandler.sendEmptyMessage(HANDLER_ENCODING_START);
    }

    private void buildEditAdapter() {
        editAdapter = new SimpleRvAdapter<POThemeSingle>(this, mThemeList, R.layout.view_theme_item) {
            @Override
            public void onBindView(RvViewHolder holder, POThemeSingle data, int pos, int type) {
                super.onBindView(holder, data, pos, type);
                BitmapImageView mIcon = (BitmapImageView) holder.getView(R.id.icon);
                ImageView mSelectedIcon = (ImageView) holder.getView(R.id.selected);
                TextView mTitle = (TextView) holder.getView(R.id.title);
                mTitle.setText(data.themeDisplayName);
                mIcon.setImagePath(data.themeIcon);
                if (!data.isMV()) {
                    mSelectedIcon.setImageResource(R.drawable.record_theme_square_selected);
                } else {
                    mSelectedIcon.setImageResource(R.drawable.record_theme_selected);
                }
                if ((isEditTheme
                        && (currentTheme == null && pos == 0) || data.equals(currentTheme))
                        || !isEditTheme
                        && ((currentFilter == null && pos == 0) || data.equals(currentFilter))) {
                    mSelectedIcon.setVisibility(View.VISIBLE);
                } else {
                    mSelectedIcon.setVisibility(View.GONE);
                }
            }
        };
        editAdapter.setOnChildClickListener(new com.march.quickrvlibs.inter.OnClickListener<POThemeSingle>() {
            @Override
            public void onItemClick(int pos, RvViewHolder holder, POThemeSingle data) {
                if (isEditTheme)
                    currentTheme = data;
                else
                    currentFilter = data;
                editAdapter.notifyDataSetChanged();
                editVideo(data);
            }
        });
        mThemeRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mThemeRv.setAdapter(editAdapter);
    }


    /** 加载主题 */
    /**
     * 加载主题
     */
    private void loadThemes() {
        if (isFinishing() || mStartEncoding)
            return;

        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... params) {
                // 预处理主题
                while (AssertService.isRunning()) {
                    SystemClock.sleep(500);
                }
                // 预处理主题（解压更新主题包）
                File result = ThemeHelper.prepareTheme(mContext, mThemeCacheDir);
                if (result != null) {
                    // 主题列表
                    mThemeList = ThemeHelper.parseTheme(mContext, mThemeCacheDir, ThemeHelper.THEME_MUSIC_VIDEO_ASSETS, R.array.theme_order);
                    //空主题
                    POThemeSingle originalTheme = ThemeHelper.loadThemeJson(mThemeCacheDir, new File(mThemeCacheDir, ThemeHelper.THEME_EMPTY));
                    if (originalTheme != null)
                        mThemeList.add(NO_THEME_INDEX, originalTheme);
                }
                // 加载滤镜
                mFilterList = ThemeHelper.parseTheme(mContext, mThemeCacheDir, ThemeHelper.THEME_FILTER_ASSETS, R.array.theme_filter_order);
                // 生成签名
                mAuthorBitmapPath = ThemeHelper.updateVideoAuthorLogo(mThemeCacheDir, getString(R.string.record_camera_author, RecordSDK.getConfig().getAuthor()), false);
                return result;
            }

            @Override
            protected void onPostExecute(File result) {
                super.onPostExecute(result);
                if (result != null && !isFinishing() && mThemeList != null && mThemeList.size() > 1) {
                    buildEditAdapter();
                    editVideo(mThemeList.get(0));
                }
            }

        }.execute();
    }

    /**
     * 重新播放
     */
    private synchronized void restart() {
        mStopPlayer = false;
        mHandler.removeMessages(UtilityAdapter.NOTIFYVALUE_PLAYFINISH);
        mHandler.sendEmptyMessageDelayed(UtilityAdapter.NOTIFYVALUE_PLAYFINISH, 100);
    }

    private void releaseVideo() {
        mThemeSurfaceView.pauseClearDelayed();
        mThemeSurfaceView.release();
        mPlayStatus.setVisibility(View.GONE);
    }

    /**
     * 开始播放
     */
    private void startVideo() {
        mStopPlayer = false;
        mThemeSurfaceView.start();
        mPlayStatus.setVisibility(View.GONE);
    }

    /**
     * 暂停播放
     */
    private void stopVideo() {
        mStopPlayer = true;
        mThemeSurfaceView.pause();
        mPlayStatus.setVisibility(View.VISIBLE);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLER_ENCODING_START:
                    if (!isFinishing()) {
                        showProgress("", getString(R.string.record_preview_encoding));
                        releaseVideo();
                        mThemeSurfaceView.startEncoding();
                        sendEmptyMessage(HANDLER_ENCODING_PROGRESS);
                    }
                    break;
                case HANDLER_ENCODING_PROGRESS://读取进度
                    int progress = UtilityAdapter.FilterParserInfo(UtilityAdapter.FILTERINFO_PROGRESS);
                    if (mProgressDialog != null) {
                        mProgressDialog.setMessage(getString(R.string.record_preview_encoding_format, progress));
                    }
                    if (progress < 100)
                        sendEmptyMessageDelayed(HANDLER_ENCODING_PROGRESS, 200);
                    else {
                        sendEmptyMessage(HANDLER_ENCODING_END);
                    }
                    break;
                case HANDLER_ENCODING_END:
                    int mDuration = UtilityAdapter.FilterParserInfo(UtilityAdapter.FILTERINFO_TOTALMS);
                    mThemeSurfaceView.release();
                    onEncodingEnd();
                    break;
                case UtilityAdapter.NOTIFYVALUE_BUFFEREMPTY:
                    showLoading();
                    break;
                case UtilityAdapter.NOTIFYVALUE_BUFFERFULL:
                    hideLoading();
                    break;
                case UtilityAdapter.NOTIFYVALUE_PLAYFINISH:
                    /** 播放完成时报告 */
                    if (!isFinishing() && !mStopPlayer) {
                        showLoading();
                        mThemeSurfaceView.release();
                        mThemeSurfaceView.initFilter();
                        mPlayStatus.setVisibility(View.GONE);
                    }
                    break;
                case UtilityAdapter.NOTIFYVALUE_HAVEERROR:
                    /** 无法播放时报告 */
                    if (!isFinishing()) {
                        Toast.makeText(mContext, R.string.record_preview_theme_load_faild, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 转码完成
     */
    private void onEncodingEnd() {
        hideProgress();
        mStartEncoding = false;
        startActivity(new Intent(this, VideoPlayerActivity.class).putExtra("path", mVideoPath));
    }

    /**
     * 显示加载中
     */
    private void showLoading() {
        if (mLoadingView != null)
            mLoadingView.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏加载中
     */
    private void hideLoading() {
        if (mLoadingView != null)
            mLoadingView.setVisibility(View.GONE);
    }

    /**
     * 是否正在播放
     */
    private boolean isPlaying() {
        return mThemeSurfaceView.isPlaying();
    }

    private void editVideo(POThemeSingle theme) {
        if (theme == null || mMediaObject == null)
            return;
        if (!StringUtils.isNotEmpty(mAuthorBitmapPath))
            return;
        if (StringUtils.isNotEmpty(mAuthorBitmapPath) && (mCurrentTheme == null
                || !RecordCommonHelper.equals(mCurrentTheme.themeName, theme.themeName))) {
            mCurrentTheme = theme;
            if (mMediaObject.mThemeObject == null)
                mMediaObject.mThemeObject = new MediaThemeObject();

            if (theme.isMV()) {
                mMediaObject.mThemeObject.mMVThemeName = theme.themeName;
                mMediaObject.mThemeObject.mMusicThemeName = theme.musicName;
                mThemeSurfaceView.reset();
                mThemeSurfaceView.setMVPath(theme.themeFolder);
                mThemeSurfaceView.setTheme(theme);
                mThemeSurfaceView.setVideoEndPath(mAuthorBitmapPath);//签名
                mThemeSurfaceView.setInputPath(mVideoTempPath);//输入文件
                //添加音乐
                mCurrentMusicPath = mCurrentTheme.musicPath;
                mCurrentMusicTitle = mCurrentTheme.musicTitle;
                mCurrentMusicName = mCurrentTheme.musicName;
                mThemeSurfaceView.setMusicPath(mCurrentMusicPath);

                updateMusicTextView();

                //清空静音状态
                mThemeVolumeCb.setChecked(false);
            }

            //滤镜
            if (theme.isFilter()) {
                mMediaObject.mThemeObject.mFilterThemeName = theme.themeName;
                mThemeSurfaceView.setFilterPath(theme.getFilterPath());
            }
            restart();
        }
    }

    /**
     * 更新音乐名称
     */

    private void updateMusicTextView() {
        if (StringUtils.isEmpty(mCurrentMusicTitle)) {
            mVideoPreviewMusic.setText(R.string.record_preview_music_nothing);
            mThemeVolumeCb.setVisibility(View.GONE);
        } else {
            mVideoPreviewMusic.setText(mCurrentMusicTitle);
            mThemeVolumeCb.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 播放完成
     */
    private ThemeSurfaceView.OnCompleteListener mOnCompleteListener = new ThemeSurfaceView.OnCompleteListener() {
        @Override
        public void onComplete() {
            if (!isFinishing()) {
                mThemeSurfaceView.release();
            }
        }
    };

    public static boolean isExternalStorageRemovable() {
        if (DeviceUtils.hasGingerbread())
            return Environment.isExternalStorageRemovable();
        else
            return Environment.MEDIA_REMOVED.equals(Environment.getExternalStorageState());
    }

    @Override
    public void ndkNotify(int key, int value) {
        if (!isFinishing())
            mHandler.sendEmptyMessage(value);
    }
}
