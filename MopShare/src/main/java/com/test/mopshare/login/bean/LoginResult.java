package com.test.mopshare.login.bean;

public class LoginResult {
    private String mCode;

    private BaseToken mToken;

    private BaseUser mUserInfo;

    private int mPlatform;

    public LoginResult(int platform, BaseToken token) {
        mPlatform = platform;
        mToken = token;
    }

    public LoginResult(int platform, BaseToken token, String code) {
        mPlatform = platform;
        mToken = token;
        mCode = code;
    }

    public LoginResult(int platform, BaseToken token, BaseUser userInfo) {
        mPlatform = platform;
        mToken = token;
        mUserInfo = userInfo;
    }

    public String getCode() {
        return mCode;
    }

    public void setCode(String code) {
        this.mCode = code;
    }

    public int getPlatform() {
        return mPlatform;
    }

    public void setPlatform(int platform) {
        this.mPlatform = platform;
    }

    public BaseToken getToken() {
        return mToken;
    }

    public void setToken(BaseToken token) {
        mToken = token;
    }

    public BaseUser getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(BaseUser userInfo) {
        mUserInfo = userInfo;
    }
}
