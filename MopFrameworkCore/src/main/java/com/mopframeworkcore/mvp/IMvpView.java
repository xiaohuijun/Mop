package com.mopframeworkcore.mvp;

public interface IMvpView {

    void showProgressView(boolean isShow);

    void showErrorView(boolean isShow);

    void showEmptyView(boolean isShow);
}
