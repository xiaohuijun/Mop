package com.test.mopshare.login;

import com.test.mopshare.login.bean.BaseToken;
import com.test.mopshare.login.bean.LoginResult;

public interface LoginListener {

    void loginSuccess(LoginResult result);

    void beforeFetchUserInfo(BaseToken token);

    void loginFailure(Exception e);

    void loginCancel();
}
