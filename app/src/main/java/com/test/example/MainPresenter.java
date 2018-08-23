package com.test.example;

import android.arch.lifecycle.LifecycleOwner;
import android.support.annotation.NonNull;

import com.mopframeworkcore.log.LogUtils;
import com.mopframeworkcore.mvp.BasePresenter;

public class MainPresenter extends BasePresenter<MainView> {

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        super.onResume(owner);
        LogUtils.w("onresume");
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        super.onPause(owner);
        LogUtils.w("onPause");
    }
}
