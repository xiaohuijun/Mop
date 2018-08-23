package com.test.mopshare.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.test.mopshare.login.LoginListener;
import com.test.mopshare.login.bean.BaseToken;

public interface ILoginInstance {
    void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo);

    void fetchUserInfo(BaseToken token);

    void handleResult(int requestCode, int resultCode, Intent data);

    boolean isInstall(Context context);

    void recycle();
}
