package com.test;

import android.app.Application;

import com.test.mopshare.InitConfig;

public class MopApplication extends Application {
    public static final String LOCAL_STORE_DIR = "mdata";
    public static String WEIBO_ID = "xxxxx";
    public static String WEIBO_SECRET = "xxxxxxx";
    public static String QQ_ID = "xxxxxx";
    public static String QQ_KEY = "xxxxxxx";
    public static String WEIXIN_ID = "xxxxxx";
    public static String WEIXIN_SECRET = "xxxxx";

    @Override
    public void onCreate() {
        super.onCreate();
        InitConfig.instance()
                .qqId(QQ_ID)
                .wxId(WEIXIN_ID)
                .weiboId(WEIBO_ID)
                .weiboScope(WEIBO_SECRET)  // 下面两个，如果不需要登录功能，可不填写
                .weiboRedirectUrl("https://openapi.baidu.com/social/oauth/2.0/receiver")
                .wxSecret(WEIXIN_SECRET)
                .appName("xxxxx")
                .inStallWbSdk(getApplicationContext())
                .debug(true);
    }
}
