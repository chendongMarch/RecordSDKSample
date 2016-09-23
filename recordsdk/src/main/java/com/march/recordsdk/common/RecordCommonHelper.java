package com.march.recordsdk.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.march.recordsdk.RecordSDK;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Project  : VideoRecorder
 * Package  : com.march.recordsdk.common
 * CreateAt : 16/9/18
 * Describe : 帮助类
 *
 * @author chendong
 */
public class RecordCommonHelper {


    public static int getColor(Context context, int colorId) {
        return ContextCompat.getColor(context, colorId);
    }

    //关闭输入输出流
    public static void closeStream(Closeable... closeable) {
        for (Closeable c : closeable) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // assert拷贝到sd卡
    public static boolean copyToSdcard(final Context ctx, String fileName, String target) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = ctx.getAssets().open(fileName);
            out = new FileOutputStream(target);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (Exception ex) {
            Logger.e(ex);
            return false;
        } finally {
            RecordCommonHelper.closeStream(in, out);
        }
        return true;
    }

    // 显示图片toast
    public static Toast showToastImage(Context ctx, int resID) {
        final Toast toast = Toast.makeText(ctx, "", Toast.LENGTH_SHORT);
        View mNextView = toast.getView();
        if (mNextView != null)
            mNextView.setBackgroundResource(resID);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        return toast;
    }

    // 判断两个字符串是否相等
    public static boolean equals(String str1, String str2) {
        return !(str1 == null || str2 == null) && str1.equals(str2);
    }

    // 获取颜色值
    public static int toColor(String str, int def) {
        if (isNullOrEmpty(str)) {
            return def;
        }
        try {
            return Color.parseColor(str);
        } catch (Exception e) {
            return def;
        }
    }


    public static int dipToPX(final Context ctx, float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, ctx.getResources().getDisplayMetrics());
    }

    public static boolean isNullOrEmpty(String str) {
        return (str == null || str.length() == 0);
    }

    // 获取昵称
    public static String getDefNikeName(String defValue) {
        return RecordSDK.getSharePreference()
                .getString(RecordConstant.THEME_LOGO_AUTHOR_NAME, defValue);
    }

    // 更新主题版本
    public static void updateThemeVersion(String name, int version) {
        SharedPreferences.Editor edit = RecordSDK.getSharePreference().edit();
        edit.putInt(RecordConstant.THEME_CURRENT_VERSION + "_" + name, version).apply();
    }

    public static int getInt(String key, int defValue) {
        if (RecordSDK.getContext() != null) {
            return PreferenceManager.getDefaultSharedPreferences(RecordSDK.getContext()).getInt(key, defValue);
        }
        return defValue;
    }

    public static void putInt(String key, int value) {
        if (RecordSDK.getContext() != null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RecordSDK.getContext());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(key, value);
            editor.apply();
        }
    }

    public static Bundle buildAnswerIntent(String videoPath, String thumbPath, int duration) {
        Bundle bundle = new Bundle();
        bundle.putString(RecordConstant.KEY_VIDEO_STORE_PATH, videoPath);
        bundle.putString(RecordConstant.KEY_VIDEO_THUMB_PATH, thumbPath);
        bundle.putInt(RecordConstant.KEY_VIDEO_DURATION, duration);
        return bundle;
    }
}
