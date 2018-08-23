package com.mopframeworkcore.mvp;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class BasePresenter <V>  implements IPresenter{

    protected Context mContext;
    protected V mView;

    protected void onCleared() {

    }

    public void attachView(Context context, V view) {
        this.mContext = context;
        this.mView = view;
    }

    public void detachView() {
        this.mView = null;
    }

    public boolean isAttachView() {
        return this.mView != null;
    }

    public void onCreatePresenter(@Nullable Bundle savedState) {

    }

    public void onDestroyPresenter() {
        this.mContext = null;
        detachView();
    }

    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {

    }

    @Override
    public void onLifecycleChanged(@NonNull LifecycleOwner owner, @NonNull Lifecycle.Event event) {

    }
}
