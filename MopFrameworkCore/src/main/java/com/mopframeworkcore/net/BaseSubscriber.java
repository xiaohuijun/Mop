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
package com.mopframeworkcore.net;

import android.content.Context;

import com.mopframeworkcore.net.Exception.NovateException;
import com.mopframeworkcore.net.util.LogWraper;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * BaseSubscriber
 * Created by Tamic on 2016-08-03.
 */
public abstract class BaseSubscriber<T> implements Observer<T> {

    protected Context context;

    public BaseSubscriber(Context context) {
        this.context = context;
    }

    public BaseSubscriber() {
    }

    @Override
    public void onSubscribe(Disposable d) {
        LogWraper.v("Novate", "-->http is onSubscribe");
    }


    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(java.lang.Throwable e) {
        if (e == null) {
            LogWraper.v("Novate", "throwable is null");
            return;
        }
        String errorInfo = e.getMessage() == null ? "error message is null" : e.getMessage();
        LogWraper.v("Novate", e.getMessage());

        String detail = "";
        if (e.getCause() != null) {
            detail = e.getCause().getMessage();
        }
        LogWraper.e("Novate", "--> " + detail);

        onError(NovateException.handleException(new Exception(e)));
        onComplete();
    }

    @Override
    public void onComplete() {
        LogWraper.v("Novate", "-->http is Complete");
    }

    public abstract void onError(MThrowable e);
}
