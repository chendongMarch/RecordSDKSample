package com.march.recordsdk.common;

import android.util.Log;

import com.march.recordsdk.BuildConfig;


public class Logger {
    /**
     * 程序是否Debug版本
     */
    public static final boolean IsDebug = BuildConfig.DEBUG;
    private static final String TAG = "[VCameraDemo]";


    public static void e(Throwable tr) {
        if (IsDebug) {
            Log.e(TAG, "", tr);
        }
    }

    public static void e(String tag, String msg) {
        if (IsDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg) {
        if (IsDebug) {
            Log.e(TAG, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (IsDebug) {
            Log.e(tag, msg, tr);
        }
    }

    public static void e(String msg, Throwable tr) {
        if (IsDebug) {
            Log.e(TAG, msg, tr);
        }
    }
}
