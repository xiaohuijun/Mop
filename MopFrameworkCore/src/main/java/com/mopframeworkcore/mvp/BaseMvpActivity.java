package com.mopframeworkcore.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;

import com.mopframeworkcore.net.OkHttpUtil;


public abstract class BaseMvpActivity<P extends BasePresenter> extends AppCompatActivity implements IMvpView {

    private PresenterProviders mPresenterProviders;
    private PresenterDispatch mPresenterDispatch;
    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        this.mContext = this;
        mPresenterProviders = PresenterProviders.inject(this);
        mPresenterDispatch = new PresenterDispatch(mPresenterProviders);

        mPresenterDispatch.attachView(this, this);
        mPresenterDispatch.onCreatePresenter(savedInstanceState);
        if (getPresenter() != null)
            getLifecycle().addObserver(getPresenter());//添加LifecycleObserver
        init();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenterDispatch.onSaveInstanceState(outState);
    }

    @LayoutRes
    protected abstract int getLayoutId();

    public abstract void init();

    protected P getPresenter() {
        return mPresenterProviders.getPresenter(0);
    }

    public PresenterProviders getPresenterProviders() {
        return mPresenterProviders;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenterDispatch.detachView();
        OkHttpUtil.getInstance().cancelRequestByTag(mContext);
    }

    @Override
    public void showProgressView(boolean isShow) {

    }

    @Override
    public void showErrorView(boolean isShow) {

    }

    @Override
    public void showEmptyView(boolean isShow) {

    }
}
