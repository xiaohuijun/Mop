package com.test.mopshare;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.test.mopshare.login.LoginUtil;
import com.test.mopshare.share.ShareUtil;

public class ProxyActivity extends AppCompatActivity {
    private static final String ACTION_TYPE = "action_type";
    public static final int ACTION_LOGIN = 10001;
    public static final int ACTION_SHARE = 10002;

    private int mType;
    private boolean isFirstIn;

    public static Intent newInstance(Context context, int actionType) {
        Intent intent = new Intent(context, ProxyActivity.class);
        if (context instanceof Application) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(ACTION_TYPE, actionType);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirstIn = true;
        // init data
        mType = getIntent().getIntExtra(ACTION_TYPE, 0);
        switch (mType) {
            case ACTION_SHARE:
                //分享
                ShareUtil.getInstance().doAction(this);
                break;
            case ACTION_LOGIN:
                //登录
                LoginUtil.getInstance().doAction(this);
                break;
            default:
                // handle 微信回调
                LoginUtil.getInstance().handleResult(-1, -1, getIntent());
                ShareUtil.getInstance().handleResult(getIntent());
                finish();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstIn) {
            isFirstIn = false;
            return;
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleResult(0, 0, intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleResult(requestCode, resultCode, data);
    }

    private void handleResult(int requestCode, int resultCode, Intent data) {
        // 处理回调
        if (mType == ACTION_LOGIN) {
            LoginUtil.getInstance().handleResult(requestCode, resultCode, data);
        } else if (mType == ACTION_SHARE) {
            ShareUtil.getInstance().handleResult(data);
        }
        finish();
    }
}
