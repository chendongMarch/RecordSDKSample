package com.march.recordsdk.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.march.recordsdk.common.Logger;
import com.march.recordsdk.common.ThemeHelper;
import com.march.recordsdk.camera.util.DeviceUtils;

import java.io.File;


/**
 * Project  : RecordSDKSample
 * Package  : com.march.recordsdk.common
 * CreateAt : 16/9/20
 * Describe : 预处理拷贝资源到sd卡,如果已经拷贝会优先结束
 *
 * @author chendong
 */

public class AssertService extends IntentService {

	/** 是否正在运行 */
	private static boolean mIsRunning;


	public AssertService() {
		super("AssertService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mIsRunning = true;

	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			File mThemeCacheDir;
			//获取传入参数
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && !isExternalStorageRemovable())
				mThemeCacheDir = new File(getExternalCacheDir(), "Theme");
			else
				mThemeCacheDir = new File(getCacheDir(), "Theme");
			ThemeHelper.prepareTheme(getApplication(), mThemeCacheDir);
		} catch (OutOfMemoryError | Exception e) {
			Logger.e(e);
		}
		mIsRunning = false;
		stopSelf();
	}


	public static boolean isRunning() {
		return mIsRunning;
	}

    public static boolean isExternalStorageRemovable() {
        if (DeviceUtils.hasGingerbread())
            return Environment.isExternalStorageRemovable();
        else
            return Environment.MEDIA_REMOVED.equals(Environment.getExternalStorageState());
    }
}
