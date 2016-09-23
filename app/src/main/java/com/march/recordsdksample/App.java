package com.march.recordsdksample;

import android.app.Application;

import com.march.recordsdk.RecordSDK;
import com.march.recordsdk.common.RecordConfig;

/**
 * Project  : RecordSDKSample
 * Package  : com.march.recordsdksample
 * CreateAt : 16/9/19
 * Describe :
 *
 * @author chendong
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RecordConfig recordConfig = new RecordConfig();
        recordConfig.setCachePathInDCIM("chendong");
        RecordSDK.initialize(this, recordConfig);
    }
}
