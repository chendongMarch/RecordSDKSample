package com.march.recordsdk;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.march.recordsdk.camera.VCamera;
import com.march.recordsdk.camera.util.DeviceUtils;
import com.march.recordsdk.common.RecordConfig;
import com.march.recordsdk.service.AssertService;

import java.io.File;
/**
 * Project  : RecordSDKSample
 * Package  : com.march.recordsdk.common
 * CreateAt : 16/9/20
 * Describe : 使用RecordSDK配置相关参数
 *
 * @author chendong
 */

public class RecordSDK {

    private static Application app;
    private static RecordConfig mConfig;
    private static SharedPreferences mSharePreference;

    public static void initialize(Application application, RecordConfig recordConfig) {
        app = application;
        mSharePreference = PreferenceManager.getDefaultSharedPreferences(app);
        mConfig = recordConfig;
        VCamera.setVideoCachePath(mConfig.getCachePath());
        VCamera.setDebugMode(true);
        VCamera.initialize(app);
        //开始拷贝资源
        startAssertTask();
    }

    public static void startAssertTask() {
        //解压assert里面的文件
        app.startService(new Intent(app, AssertService.class));
    }

    public static Context getContext() {
        return app;
    }

    public static RecordConfig getConfig() {
        return mConfig;
    }

    public static SharedPreferences getSharePreference() {
        return mSharePreference;
    }
}
