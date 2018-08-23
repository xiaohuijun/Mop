package com.test.mopshare.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.test.mopshare.InitConfig;
import com.test.mopshare.login.LoginListener;
import com.test.mopshare.login.LoginPlatform;
import com.test.mopshare.login.bean.BaseToken;
import com.test.mopshare.login.bean.LoginResult;
import com.test.mopshare.login.bean.QQToken;
import com.test.mopshare.login.bean.QQUser;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QqLoginInstance implements ILoginInstance {
    private static final String SCOPE = "get_simple_userinfo";
    private static final String URL = "https://graph.qq.com/user/get_user_info";
    private Tencent mTencent;
    private IUiListener mIUiListener;
    private LoginListener mLoginListener;
    private CompositeDisposable mDisposables;
    private boolean fetchUserInfo;

    public QqLoginInstance(@NonNull Context activity, @NonNull LoginListener loginListener, boolean isFetchUserInfo) {
        this.mTencent = Tencent.createInstance(InitConfig.instance().getQqId(),
                activity.getApplicationContext());
        this.mLoginListener = loginListener;
        this.fetchUserInfo = isFetchUserInfo;
        mDisposables = new CompositeDisposable();
        this.mIUiListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                try {
                    QQToken token = QQToken.parse((JSONObject) o);
                    if (fetchUserInfo) {
                        mLoginListener.beforeFetchUserInfo(token);
                        fetchUserInfo(token);
                    } else {
                        mLoginListener.loginSuccess(new LoginResult(LoginPlatform.QQ, token));
                    }
                } catch (JSONException e) {
                    mLoginListener.loginFailure(e);
                }
            }

            @Override
            public void onError(UiError uiError) {
                mLoginListener.loginFailure(
                        new Exception("QQError: " + uiError.errorCode + uiError.errorDetail));
            }

            @Override
            public void onCancel() {
                mLoginListener.loginCancel();
            }
        };
    }

    @Override
    public void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        mTencent.login(activity, SCOPE, mIUiListener);
    }

    @Override
    public void fetchUserInfo(final BaseToken token) {
        mDisposables.add(Flowable.create(new FlowableOnSubscribe<QQUser>() {
            @Override
            public void subscribe(FlowableEmitter<QQUser> qqUserEmitter) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(buildUserInfoUrl(token, URL)).build();

                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    QQUser user = QQUser.parse(token.getOpenid(), jsonObject);
                    qqUserEmitter.onNext(user);
                } catch (Exception e) {
                    qqUserEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<QQUser>() {
                    @Override
                    public void accept(QQUser qqUser) throws Exception {
                        mLoginListener.loginSuccess(
                                new LoginResult(LoginPlatform.QQ, token, qqUser));
                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mLoginListener.loginFailure(new Exception(throwable));
                    }
                }));
    }

    @Override
    public void handleResult(int requestCode, int resultCode, Intent data) {
        Tencent.handleResultData(data, mIUiListener);
    }

    @Override
    public boolean isInstall(Context context) {
        return mTencent.isQQInstalled(context);
    }

    @Override
    public void recycle() {
        mTencent.releaseResource();
        mIUiListener = null;
        mLoginListener = null;
        mTencent = null;
        if (mDisposables != null) {
            mDisposables.clear();
            mDisposables = null;
        }
    }

    private String buildUserInfoUrl(BaseToken token, String base) {
        return base
                + "?access_token="
                + token.getAccessToken()
                + "&oauth_consumer_key="
                + InitConfig.instance().getQqId()
                + "&openid="
                + token.getOpenid();
    }
}
