package com.march.recordsdk.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Project  : VideoRecorder
 * Package  : com.march.recordsdk.ui
 * CreateAt : 16/9/18
 * Describe : 基类,主要负责业务处理和公共方法
 *
 * @author chendong
 */
public class BaseRecordActivity extends AppCompatActivity {

    protected Context mContext;

    protected <V extends View> V getView(int id) {
        return (V) findViewById(id);
    }

    protected void setClick(int id, View.OnClickListener listener) {
        View viewById = findViewById(id);
        if (viewById != null) {
            viewById.setOnClickListener(listener);
        }
    }
    protected void setClick(View.OnClickListener listener, int ... ids) {
        for (int id : ids) {
            setClick(id,listener);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }
}
