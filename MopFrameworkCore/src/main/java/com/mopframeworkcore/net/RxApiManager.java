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

import android.annotation.TargetApi;
import android.os.Build;
import android.util.ArrayMap;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Tamic on 2017-01-16.
 */

public class RxApiManager implements RxActionManager<Object> {

    private static RxApiManager sInstance = null;

    private ArrayMap<Object, Disposable> maps;
    private CompositeDisposable mDisposables;

    public static RxApiManager get() {

        if (sInstance == null) {
            synchronized (RxApiManager.class) {
                if (sInstance == null) {
                    sInstance = new RxApiManager();
                }
            }
        }
        return sInstance;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private RxApiManager() {
        maps = new ArrayMap<>();
        if (mDisposables == null)
            mDisposables = new CompositeDisposable();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void add(Object tag, Disposable disposable) {
        maps.put(tag, disposable);
        mDisposables.add(disposable);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void remove(Object tag) {
        if (!maps.isEmpty()) {
            maps.remove(tag);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void removeAll() {
        if (!maps.isEmpty()) {
            maps.clear();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void cancel(Object tag) {
        if (maps.isEmpty()) {
            return;
        }
        Disposable disposable = maps.get(tag);
        if (maps.get(tag) == null) {
            return;
        }
        if (disposable.isDisposed()) return;
        mDisposables.remove(disposable);
        maps.remove(tag);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void cancelAll() {
        if (maps.isEmpty()) {
            return;
        }
        mDisposables.clear();
        maps.clear();
    }

}
