package com.test.mopshare.share.bean;

import com.test.mopshare.share.SharePlatform;
import com.test.mopshare.share.ShareType;

public class ShareInfo {
    private int shareType;//分享类型
    private int platform;//分享平台
    private String text;//分享文本
    private ShareImageObject shareImageObject;//图片对象
    private String title;//标题
    private String summary;//描述
    private String targetUrl;//目标url
    private String mediaUrl;//音乐 视频url
    private String appId;// 小程序原始id
    private String appPath;//小程序页面路径

    public int getShareType() {
        return shareType;
    }

    public void setShareType(@ShareType.Type int shareType) {
        this.shareType = shareType;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(@SharePlatform.Platform int platform) {
        this.platform = platform;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ShareImageObject getShareImageObject() {
        return shareImageObject;
    }

    public void setShareImageObject(ShareImageObject shareImageObject) {
        this.shareImageObject = shareImageObject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppPath() {
        return appPath;
    }

    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    @Override
    public String toString() {
        return "ShareInfo{" +
                "shareType=" + shareType +
                ", platform=" + platform +
                ", text='" + text + '\'' +
                ", shareImageObject=" + shareImageObject +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", targetUrl='" + targetUrl + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", appId='" + appId + '\'' +
                ", appPath='" + appPath + '\'' +
                '}';
    }

    public void recycle() {
        // bitmap recycle
        if (shareImageObject != null
                && shareImageObject.getBitmap() != null
                && !shareImageObject.getBitmap().isRecycled()) {
            shareImageObject.getBitmap().recycle();
        }
        shareImageObject = null;
    }
}
