package com.test.mopshare.login.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.test.mopshare.InitConfig;
import com.test.mopshare.login.LoginListener;
import com.test.mopshare.login.LoginPlatform;
import com.test.mopshare.login.bean.BaseToken;
import com.test.mopshare.login.bean.LoginResult;
import com.test.mopshare.login.bean.WxToken;
import com.test.mopshare.login.bean.WxUser;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

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

public class WxLoginInstance implements ILoginInstance {
    private static final String SCOPE_USER_INFO = "snsapi_userinfo";
    private static final String SCOPE_BASE = "snsapi_base";
    private static final String BASE_URL = "https://api.weixin.qq.com/sns/";

    private IWXAPI mIWXAPI;
    private LoginListener mLoginListener;
    private OkHttpClient mClient;
    private boolean fetchUserInfo;
    private CompositeDisposable mDisposables;

    public WxLoginInstance(Context activity, LoginListener loginListener, boolean isFetchUserInfo) {
        this.mLoginListener = loginListener;
        mIWXAPI = WXAPIFactory.createWXAPI(activity, InitConfig.instance().getWxId());
        mClient = new OkHttpClient();
        mIWXAPI.registerApp(InitConfig.instance().getWxId());
        this.fetchUserInfo = isFetchUserInfo;
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void doLogin(Activity activity, LoginListener listener, boolean fetchUserInfo) {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = SCOPE_USER_INFO;
        req.state = String.valueOf(System.currentTimeMillis());
        mIWXAPI.sendReq(req);
    }

    private void getToken(final String code) {
        mDisposables.add(Flowable.create(new FlowableOnSubscribe<WxToken>() {
            @Override
            public void subscribe(FlowableEmitter<WxToken> wxTokenFlowableEmitter) {
                Request request = new Request.Builder().url(buildTokenUrl(code)).build();
                try {
                    Response response = mClient.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WxToken token = WxToken.parse(jsonObject);
                    wxTokenFlowableEmitter.onNext(token);
                } catch (Exception e) {
                    wxTokenFlowableEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WxToken>() {
                    @Override
                    public void accept(WxToken wxToken) throws Exception {
                        if (fetchUserInfo) {
                            mLoginListener.beforeFetchUserInfo(wxToken);
                            fetchUserInfo(wxToken);
                        } else {
                            mLoginListener.loginSuccess(new LoginResult(LoginPlatform.WX, wxToken));
                        }
                    }

                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mLoginListener.loginFailure(new Exception(throwable.getMessage()));

                    }
                }));
    }

    @Override
    public void fetchUserInfo(final BaseToken token) {
        mDisposables.add(Flowable.create(new FlowableOnSubscribe<WxUser>() {
            @Override
            public void subscribe(FlowableEmitter<WxUser> wxUserFlowableEmitter) {
                Request request = new Request.Builder().url(buildUserInfoUrl(token)).build();
                try {
                    Response response = mClient.newCall(request).execute();
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    WxUser user = WxUser.parse(jsonObject);
                    wxUserFlowableEmitter.onNext(user);
                } catch (Exception e) {
                    wxUserFlowableEmitter.onError(e);
                }
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WxUser>() {
                    @Override
                    public void accept(WxUser wxUser) throws Exception {
                        mLoginListener.loginSuccess(
                                new LoginResult(LoginPlatform.WX, token, wxUser));
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
        mIWXAPI.handleIntent(data, new IWXAPIEventHandler() {
            @Override
            public void onReq(BaseReq baseReq) {
            }

            @Override
            public void onResp(BaseResp baseResp) {
                if (baseResp instanceof SendAuth.Resp && baseResp.getType() == 1) {
                    SendAuth.Resp resp = (SendAuth.Resp) baseResp;
                    switch (resp.errCode) {
                        case BaseResp.ErrCode.ERR_OK:
                            getToken(resp.code);
                            break;
                        case BaseResp.ErrCode.ERR_USER_CANCEL:
                            mLoginListener.loginCancel();
                            break;
                        case BaseResp.ErrCode.ERR_SENT_FAILED:
                            mLoginListener.loginFailure(new Exception("WX_ERR_SENT_FAILED"));
                            break;
                        case BaseResp.ErrCode.ERR_UNSUPPORT:
                            mLoginListener.loginFailure(new Exception("INFO.WX_ERR_UNSUPPORT"));
                            break;
                        case BaseResp.ErrCode.ERR_AUTH_DENIED:
                            mLoginListener.loginFailure(new Exception("INFO.WX_ERR_AUTH_DENIED"));
                            break;
                        default:
                            mLoginListener.loginFailure(new Exception("INFO.WX_ERR_AUTH_ERROR"));
                    }
                }
            }
        });
    }

    @Override
    public boolean isInstall(Context context) {
        return mIWXAPI.isWXAppInstalled();
    }

    @Override
    public void recycle() {
        if (mIWXAPI != null)
            mIWXAPI.detach();
        if (mDisposables != null) {
            mDisposables.clear();
            mDisposables = null;
        }
    }

    private String buildTokenUrl(String code) {
        return BASE_URL
                + "oauth2/access_token?appid="
                + InitConfig.instance().getWxId()
                + "&secret="
                + InitConfig.instance().getWxSecret()
                + "&code="
                + code
                + "&grant_type=authorization_code";
    }

    private String buildUserInfoUrl(BaseToken token) {
        return BASE_URL
                + "userinfo?access_token="
                + token.getAccessToken()
                + "&openid="
                + token.getOpenid();
    }
}
