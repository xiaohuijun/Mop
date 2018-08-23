/*
 *    Copyright (C) 2016 Tamic
 *
 *    link :https://github.com/Tamicer/Novate
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.mopframeworkcore.net.download;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.mopframeworkcore.net.BaseSubscriber;
import com.mopframeworkcore.net.MThrowable;
import com.mopframeworkcore.net.util.LogWraper;
import com.mopframeworkcore.net.util.Utils;

import io.reactivex.disposables.Disposable;


/**
 * DownSubscriber
 * Created by Tamic on 2016-08-03.
 */
public class DownSubscriber<ResponseBody extends okhttp3.ResponseBody> extends BaseSubscriber<ResponseBody> {
    private DownLoadCallBack callBack;
    private Context context;
    private String path;
    private String name;
    private String key;

    public DownSubscriber(String key, String path, String name, DownLoadCallBack callBack, Context context) {
        super(context);
        this.key = key;
        this.path = path;
        this.name = name;
        this.callBack = callBack;
        this.context = context;
    }

    @Override
    public void onSubscribe(Disposable d) {
        super.onSubscribe(d);
        if (callBack != null) {
           /* if (TextUtils.isEmpty(key)) {
                key = FileUtil.generateFileKey(path, name);
            }*/
            callBack.onStart(key);
        }
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
    }

    @Override
    public void onComplete() {
        if (callBack != null) {
            callBack.onCompleted();
        }
    }

    @Override
    public void onError(final MThrowable e) {
        LogWraper.e(NovateDownLoadManager.TAG, "DownSubscriber:>>>> onError:" + e.getMessage());
        if (callBack != null) {
            final MThrowable throwable = new MThrowable(e, -100, e.getMessage());
            if (Utils.checkMain()) {
                callBack.onError(throwable);
            } else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callBack.onError(throwable);
                    }
                });
            }
        }
    }

    @Override
    public void onNext(ResponseBody responseBody) {
        LogWraper.d(NovateDownLoadManager.TAG, "DownSubscriber:>>>> onNext");
        new NovateDownLoadManager(callBack).writeResponseBodyToDisk(key, path, name, context, responseBody);

    }
}
