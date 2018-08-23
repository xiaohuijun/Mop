package com.test.mopshare.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.test.mopshare.login.LoginListener;
import com.test.mopshare.login.LoginPlatform;
import com.test.mopshare.login.bean.BaseToken;
import com.test.mopshare.login.bean.LoginResult;
import com.test.mopshare.login.bean.WeiboToken;
import com.test.mopshare.login.bean.WeiboUser;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.auth.WbConnectErrorMessage;
import com.sina.weibo.sdk.auth.sso.SsoHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WbLoginInstance implements ILoginInstance {
    private static final String USER_INFO = "https://api.weibo.com/2/users/show.json";
    private SsoHandler mSsoHandler;
    private LoginListener mLoginListener;
    private boolean isFetchUserInfo;
    private CompositeDisposable mDisposables;

    public WbLoginInstance(Activity activity, LoginListener loginListener, boolean isFetchUserInfo) {
        mSsoHandler = new SsoHandler(activity);
        this.mLoginListener = loginListener;
        this.isFetchUserInfo = isFetchUserInfo;
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        mSsoHandler.authorize(mWbAuthListener);
    }

    @Override
    public void fetchUserInfo(final BaseToken token) {
        mDisposables.add(Observable.create(new ObservableOnSubscribe<WeiboUser>() {
            @Override
            public void subscribe(ObservableEmitter<WeiboUser> emitter) throws Exception {
                OkHttpClient client = new OkHttpClient();
                Request request =
                        new Request.Builder().url(buildUserInfoUrl(token, USER_INFO)).build();
                try {
                    Response response = client.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WeiboUser user = WeiboUser.parse(jsonObject);
                    emitter.onNext(user);
                } catch (IOException | JSONException e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeiboUser>() {
                    @Override
                    public void accept(WeiboUser weiboUser) throws Exception {
                        mLoginListener.loginSuccess(
                                new LoginResult(LoginPlatform.WEIBO, token, weiboUser));
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
        if (mSsoHandler != null) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean isInstall(Context context) {
        return WbSdk.isWbInstall(context);
    }

    @Override
    public void recycle() {
        if (mDisposables != null) {
            mDisposables.clear();
            mDisposables = null;
        }
        mLoginListener = null;
        mSsoHandler = null;
    }

    private WbAuthListener mWbAuthListener = new WbAuthListener() {
        @Override
        public void onSuccess(Oauth2AccessToken oauth2AccessToken) {
            Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(oauth2AccessToken.getBundle());
            WeiboToken weiboToken = WeiboToken.parse(accessToken);
            if (isFetchUserInfo) {
                mLoginListener.beforeFetchUserInfo(weiboToken);
                fetchUserInfo(weiboToken);
            } else {
                mLoginListener.loginSuccess(new LoginResult(LoginPlatform.WEIBO, weiboToken));
            }
        }

        @Override
        public void cancel() {
            mLoginListener.loginCancel();
        }

        @Override
        public void onFailure(WbConnectErrorMessage wbConnectErrorMessage) {
            mLoginListener.loginFailure(new Exception(wbConnectErrorMessage.getErrorCode() + ":" + wbConnectErrorMessage.getErrorMessage()));
        }
    };

    private String buildUserInfoUrl(BaseToken token, String baseUrl) {
        return baseUrl + "?access_token=" + token.getAccessToken() + "&uid=" + token.getOpenid();
    }
}
