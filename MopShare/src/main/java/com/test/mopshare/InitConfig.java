package com.test.mopshare;

import android.content.Context;
import android.support.annotation.IdRes;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.AuthInfo;

public class InitConfig {
    private static InitConfig inStance;
    private String wxId;

    private String wxSecret;

    private String qqId;

    private String weiboId;

    private String weiboRedirectUrl = "https://api.weibo.com/oauth2/default.html";

    private String weiboScope = "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";

    private boolean debug;

    private int appLogoResId;//应用logo
    private String appName;//应用名

    public static InitConfig instance() {
        if (inStance == null) {
            synchronized (InitConfig.class) {
                if (inStance == null) {
                    inStance = new InitConfig();
                }
            }
        }
        return inStance;
    }

    public InitConfig wxId(String id) {
        wxId = id;
        return this;
    }

    public InitConfig wxSecret(String id) {
        wxSecret = id;
        return this;
    }

    public InitConfig qqId(String id) {
        qqId = id;
        return this;
    }

    public InitConfig weiboId(String id) {
        weiboId = id;
        return this;
    }

    public InitConfig weiboRedirectUrl(String url) {
        weiboRedirectUrl = url;
        return this;
    }

    public InitConfig weiboScope(String scope) {
        weiboScope = scope;
        return this;
    }

    public InitConfig debug(boolean isDebug) {
        debug = isDebug;
        return this;
    }

    public InitConfig inStallWbSdk(Context context) {
        WbSdk.install(context.getApplicationContext(), new AuthInfo(context.getApplicationContext(), weiboId, weiboRedirectUrl,
                weiboScope));
        return this;
    }

    public String getWxId() {
        return wxId;
    }

    public String getWxSecret() {
        return wxSecret;
    }

    public String getQqId() {
        return qqId;
    }

    public String getWeiboId() {
        return weiboId;
    }

    public String getWeiboRedirectUrl() {
        return weiboRedirectUrl;
    }

    public String getWeiboScope() {
        return weiboScope;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getAppLogoResId() {
        return appLogoResId;
    }

    public InitConfig appLogoResId(@IdRes int appLogoResId) {
        this.appLogoResId = appLogoResId;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public InitConfig appName(String appName) {
        this.appName = appName;
        return this;
    }
}
