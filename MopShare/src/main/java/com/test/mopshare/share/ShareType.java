package com.test.mopshare.share;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ShareType {
    @IntDef({TEXT, IMAGE, MUSIC, VIDEO, IMAGES, WEB, APPLETS, APP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    public static final int TEXT = 1001;//文本
    public static final int IMAGE = 1002;//图片
    public static final int MUSIC = 1003;//音乐
    public static final int VIDEO = 1004;//视频
    public static final int IMAGES = 1005;//多图
    public static final int WEB = 1006;//网页 图文
    public static final int APPLETS = 1007;//微信小程序
    public static final int APP = 1008;//qq分享应用
}
