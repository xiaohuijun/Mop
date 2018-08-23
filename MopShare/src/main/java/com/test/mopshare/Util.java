package com.test.mopshare;

import com.test.mopshare.login.LoginPlatform;
import com.test.mopshare.share.SharePlatform;

public class Util {
    public static String getSharePlatformName(@SharePlatform.Platform int shareId) {
        String platformName = "";
        switch (shareId) {
            case SharePlatform.QQ:
                platformName = "qq";
                break;
            case SharePlatform.QZONE:
                platformName = "qzone";
                break;
            case SharePlatform.WEIBO:
                platformName = "weibo";
                break;
            case SharePlatform.WX:
                platformName = "wx";
                break;
            case SharePlatform.WX_TIMELINE:
                platformName = "wx_timeline";
                break;
            case SharePlatform.DEFAULT:
                platformName = "default";
                break;
            default:
                platformName = "unkonw platform";
                break;
        }
        return platformName;
    }

    public static String getLoginPlatformName(@LoginPlatform.Platform int loginId) {
        String platformName = "";
        switch (loginId) {
            case LoginPlatform.QQ:
                platformName = "qq";
                break;
            case LoginPlatform.WEIBO:
                platformName = "weibo";
                break;
            case LoginPlatform.WX:
                platformName = "wx";
                break;
            default:
                platformName = "unkonw platform";
                break;
        }
        return platformName;
    }
}
