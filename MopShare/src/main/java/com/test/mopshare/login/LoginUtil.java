package com.test.mopshare.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.test.mopshare.ProxyActivity;
import com.test.mopshare.Util;
import com.test.mopshare.login.instance.ILoginInstance;
import com.test.mopshare.login.instance.QqLoginInstance;
import com.test.mopshare.login.instance.WbLoginInstance;
import com.test.mopshare.login.instance.WxLoginInstance;

public class LoginUtil {
    private static LoginUtil instance;

    private ILoginInstance mLoginInstance;
    private LoginListener mLoginListener;
    private int mPlatform;
    private boolean isFetchUserInfo;

    public static LoginUtil getInstance() {
        if (instance == null) {
            synchronized (LoginUtil.class) {
                if (instance == null) {
                    instance = new LoginUtil();
                }
            }
        }
        return instance;
    }

    public void doAction(Activity activity) {
        switch (mPlatform) {
            case LoginPlatform.QQ:
                mLoginInstance = new QqLoginInstance(activity, mLoginListener, isFetchUserInfo);
                break;
            case LoginPlatform.WEIBO:
                mLoginInstance = new WbLoginInstance(activity, mLoginListener, isFetchUserInfo);
                break;
            case LoginPlatform.WX:
                mLoginInstance = new WxLoginInstance(activity, mLoginListener, isFetchUserInfo);
                break;
            default:
                if (mLoginListener != null) {
                    mLoginListener.loginFailure(new Exception("unknow platform"));
                }
                activity.finish();
        }
        if (mLoginInstance != null) {
            if (!mLoginInstance.isInstall(activity)) {
                if (mLoginListener != null) {
                    mLoginListener.loginFailure(new Exception(Util.getLoginPlatformName(mPlatform) + " is not installed"));
                    activity.finish();
                }
                return;
            }
            mLoginInstance.doLogin(activity, mLoginListener, isFetchUserInfo);
        }
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (mLoginInstance != null) {
            mLoginInstance.handleResult(requestCode, resultCode, data);
        }
    }

    public void doLogin(@NonNull Context context, @LoginPlatform.Platform int mPlatform, boolean fetchUserInfo, @NonNull LoginListener listener) {
        this.mPlatform = mPlatform;
        this.isFetchUserInfo = isFetchUserInfo;
        this.mLoginListener = listener;
        context.startActivity(ProxyActivity.newInstance(context, ProxyActivity.ACTION_LOGIN));
    }

    public void recycle() {
        if (mLoginInstance != null) {
            mLoginInstance.recycle();
        }
        mLoginInstance = null;
        mLoginListener = null;
        mPlatform = 0;
        isFetchUserInfo = false;
    }
}
