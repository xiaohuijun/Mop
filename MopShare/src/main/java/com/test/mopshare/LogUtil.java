package com.test.mopshare;

import android.util.Log;

/**
 * Created by shaohui on 2016/12/8.
 */

public class LogUtil {

    private static final String TAG = "share_util_log";

    public static void i(String info) {
        if (InitConfig.instance().isDebug()) {
            Log.i(TAG, info);
        }
    }

    public static void d(String info) {
        if (InitConfig.instance().isDebug()) {
            Log.d(TAG, info);
        }
    }

    public static void w(String info) {
        if (InitConfig.instance().isDebug()) {
            Log.w(TAG, info);
        }
    }

    public static void e(String error) {
        if (InitConfig.instance().isDebug()) {
            Log.e(TAG, error);
        }
    }
}
