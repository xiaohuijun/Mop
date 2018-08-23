package com.mopframeworkcore.config;

import android.annotation.SuppressLint;
import android.content.Context;

import com.mopframeworkcore.exception.CrashUtils;
import com.mopframeworkcore.log.LogUtils;
import com.mopframeworkcore.net.OkHttpUtil;
import com.mopframeworkcore.permission.PermissionHelper;
import com.mopframeworkcore.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/11.
 */

public class FrameworkConfig {
    private static FrameworkConfig instance;

    public static FrameworkConfig getInstance() {
        if (instance == null) {
            synchronized (FrameworkConfig.class) {
                if (instance == null) {
                    instance = new FrameworkConfig();
                }
            }
        }
        return instance;
    }

    public FrameworkConfig init(Context context, boolean isDebug) {
        Utils.init(context);
        initLog(isDebug);
        return this;
    }

    @SuppressLint("MissingPermission")
    public FrameworkConfig initCrash(final File crashDir, final CrashUtils.OnCrashListener onCrashListener) {
        PermissionHelper.requestStorage(new PermissionHelper.OnPermissionGrantedListener() {
            @Override
            public void onPermissionGranted() {
                if (crashDir == null) {
                    CrashUtils.init(onCrashListener);
                    return;
                }
                CrashUtils.init(crashDir, onCrashListener);
            }
        });
        return this;
    }

    public FrameworkConfig initNet(Context context, Map<String, String> headers, Map<String, Object> commonParameters, String baseUrl, boolean isDebug) {
        OkHttpUtil.getInstance().init(context, headers, commonParameters, baseUrl, isDebug);
        return this;
    }

    private void initLog(boolean isDebug) {
        LogUtils.getConfig()
                .setLogSwitch(isDebug)// 设置 log 总开关，包括输出到控制台和文件，默认开
                .setConsoleSwitch(isDebug)// 设置是否输出到控制台开关，默认开
                .setGlobalTag(Utils.getPackageName())// 设置 log 全局标签，默认为空
                // 当全局标签不为空时，我们输出的 log 全部为该 tag，
                // 为空时，如果传入的 tag 为空那就显示类名，否则显示 tag
                .setLogHeadSwitch(true)// 设置 log 头信息开关，默认为开
                .setLog2FileSwitch(false)// 打印 log 时是否存到文件的开关，默认关
                .setDir("")// 当自定义路径为空时，写入应用的/cache/log/目录中
                .setFilePrefix("")// 当文件前缀为空时，默认为"util"，即写入文件为"util-MM-dd.txt"
                .setBorderSwitch(true)// 输出日志是否带边框开关，默认开
                .setSingleTagSwitch(true)// 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
                .setConsoleFilter(LogUtils.V)// log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
                .setFileFilter(LogUtils.V)// log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
                .setStackDeep(1)// log 栈深度，默认为 1
                .setStackOffset(0)// 设置栈偏移，比如二次封装的话就需要设置，默认为 0
                .setSaveDays(3)// 设置日志可保留天数，默认为 -1 表示无限时长
                // 新增 ArrayList 格式化器，默认已支持 Array, MThrowable, Bundle, Intent 的格式化输出
                .addFormatter(new LogUtils.IFormatter<ArrayList>() {
                    @Override
                    public String format(ArrayList list) {
                        return "LogUtils Formatter ArrayList { " + list.toString() + " }";
                    }
                });
    }
}
