package com.ningkangyuan;

import android.app.Application;

/**
 * Created by xuchun on 2016/8/15.
 */
public class MyApplication extends Application {

    public static boolean isRefreshMain = false;

    public static MyApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

}