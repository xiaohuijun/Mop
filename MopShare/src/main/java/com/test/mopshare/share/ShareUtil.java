package com.test.mopshare.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.test.mopshare.InitConfig;
import com.test.mopshare.ProxyActivity;
import com.test.mopshare.Util;
import com.test.mopshare.share.bean.ShareInfo;
import com.test.mopshare.share.instance.DefaultShareInstance;
import com.test.mopshare.share.instance.IShareInstance;
import com.test.mopshare.share.instance.QqShareInstance;
import com.test.mopshare.share.instance.WbShareInstance;
import com.test.mopshare.share.instance.WxShareInstance;

public class ShareUtil {
    private static ShareUtil instance;

    private IShareInstance mShareInstance;
    private ShareInfo mShareInfo;
    private ShareListener mShareListener;

    public static ShareUtil getInstance() {
        if (instance == null) {
            synchronized (ShareUtil.class) {
                if (instance == null) {
                    instance = new ShareUtil();
                }
            }
        }
        return instance;
    }

    public void doAction(Activity activity) {
        mShareInstance = getShareInstance(activity);
        if (mShareInfo == null || mShareInstance == null || mShareListener == null) {
            activity.finish();
            return;
        }
        if (!mShareInstance.isInstall(activity)) {
            mShareListener.shareFailure(new Exception(Util.getSharePlatformName(mShareInfo.getPlatform()) + " is not installed"));
            activity.finish();
            return;
        }
        mShareInstance.share(activity, mShareInfo, mShareListener);
    }

    public void handleResult(Intent data) {
        if (mShareInstance != null && data != null) {
            mShareInstance.handleResult(data);
        }
    }

    public void share(@NonNull Context context, @NonNull ShareInfo shareInfo, @NonNull ShareListener shareListener) {
        this.mShareInfo = shareInfo;
        this.mShareListener = shareListener;
        context.startActivity(ProxyActivity.newInstance(context, ProxyActivity.ACTION_SHARE));
    }

    private IShareInstance getShareInstance(Activity context) {
        if (mShareInfo == null)
            return null;
        int platform = mShareInfo.getPlatform();
        switch (platform) {
            case SharePlatform.WX:
            case SharePlatform.WX_TIMELINE:
                return new WxShareInstance(context, InitConfig.instance().getWxId());
            case SharePlatform.QQ:
            case SharePlatform.QZONE:
                return new QqShareInstance(context, InitConfig.instance().getQqId());
            case SharePlatform.WEIBO:
                return new WbShareInstance(context, InitConfig.instance().getWeiboId());
            case SharePlatform.DEFAULT:
            default:
                return new DefaultShareInstance();
        }
    }

    public void recycle() {
        if (mShareInstance != null) {
            mShareInstance.recycle();
        }
        mShareInstance = null;
        if (mShareInfo != null)
            mShareInfo.recycle();
        mShareInfo = null;
        mShareListener = null;
    }
}
