package com.mopframeworkcore.net.download;


import com.mopframeworkcore.net.MThrowable;
import com.mopframeworkcore.net.callback.ResponseCallback;

import okhttp3.Call;
import okhttp3.ResponseBody;

public abstract class UpLoadCallback extends ResponseCallback {

    public abstract void onProgress(Object tag, int progress, long speed, boolean done);

    @Override
    public void onProgress(Object tag, float progress, long downloaded, long total) {
        super.onProgress(tag, progress, downloaded, total);
        onProgress(tag, (int) progress, downloaded, downloaded == total);
    }

    @Override
    public Object onHandleResponse(ResponseBody response) throws Exception {
        return response;
    }

    @Override
    public void onError(Object tag, MThrowable e) {

    }

    @Override
    public void onCancel(Object tag, MThrowable e) {

    }

    @Override
    public void onNext(Object tag, Call call, Object response) {

    }
}


