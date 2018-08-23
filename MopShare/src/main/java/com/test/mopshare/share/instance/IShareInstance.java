package com.test.mopshare.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.test.mopshare.share.ShareListener;
import com.test.mopshare.share.bean.ShareInfo;

public interface IShareInstance {
    void share(Activity activity, ShareInfo shareInfo, ShareListener shareListener);

    void handleResult(Intent data);

    boolean isInstall(Context context);

    void recycle();
}
