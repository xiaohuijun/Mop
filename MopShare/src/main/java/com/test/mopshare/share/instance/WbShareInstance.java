package com.test.mopshare.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.test.mopshare.DataDecoder;
import com.test.mopshare.share.ShareListener;
import com.test.mopshare.share.ShareType;
import com.test.mopshare.share.bean.ShareImageObject;
import com.test.mopshare.share.bean.ShareInfo;
import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MultiImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoSourceObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;
import com.sina.weibo.sdk.utils.Utility;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class WbShareInstance implements IShareInstance {
    private WbShareHandler mShareHandler;
    private ShareListener mShareListener;
    private CompositeDisposable mDisposables;

    public WbShareInstance(Activity context, String appid) {
        mShareHandler = new WbShareHandler(context);
        mShareHandler.registerApp();
        mShareHandler.setProgressColor(0xff33b5e5);
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void share(Activity activity, ShareInfo shareInfo, ShareListener shareListener) {
        this.mShareListener = shareListener;
        switch (shareInfo.getShareType()) {
            case ShareType.TEXT:
                shareText(shareInfo);
                break;
            case ShareType.IMAGE:
                shareImage(activity, shareInfo);
                break;
            case ShareType.WEB:
                shareWeb(activity, shareInfo);
                break;
            case ShareType.IMAGES:
                if (!WbSdk.supportMultiImage(activity)) {
                    mShareListener.shareFailure(new Exception("weibo version must be than 7.8.0"));
                    activity.finish();
                } else {
                    shareImages(activity, shareInfo);
                }
                break;
            case ShareType.VIDEO:
                if (!WbSdk.supportMultiImage(activity)) {
                    mShareListener.shareFailure(new Exception("weibo version must be than 7.8.0"));
                    activity.finish();
                } else {
                    shareVideo(activity, shareInfo);
                }
                break;
            default:
                mShareListener.shareFailure(new Exception("weibo shareType is not support:" + shareInfo.getShareType()));
                activity.finish();
                break;
        }
    }

    @Override
    public void handleResult(Intent data) {
        mShareHandler.doResultIntent(data, mWbShareCallback);
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
        mShareListener = null;
        mShareHandler = null;
    }

    private WbShareCallback mWbShareCallback = new WbShareCallback() {
        @Override
        public void onWbShareSuccess() {
            mShareListener.shareSuccess();
        }

        @Override
        public void onWbShareCancel() {
            mShareListener.shareCancel();
        }

        @Override
        public void onWbShareFail() {
            mShareListener.shareFailure(new Exception("share fail"));
        }
    };

    private void shareText(ShareInfo shareInfo) {
        TextObject textObject = new TextObject();
        textObject.text = shareInfo.getText();
        textObject.title = shareInfo.getTitle();
        textObject.description = shareInfo.getSummary();
        textObject.actionUrl = shareInfo.getTargetUrl();
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = textObject;
        mShareHandler.shareMessage(weiboMessage, false);
    }

    private void shareImage(final Activity activity, final ShareInfo shareInfo) {
        mDisposables.add(Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                try {
                    String imgpath = DataDecoder.decode(activity, shareInfo.getShareImageObject(), false);
                    emitter.onNext(BitmapFactory.decodeFile(imgpath));
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        ImageObject imageObject = new ImageObject();
                        imageObject.setImageObject(bitmap);
                        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
                        weiboMessage.imageObject = imageObject;
                        mShareHandler.shareMessage(weiboMessage, false);
                        bitmap.recycle();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mShareListener.shareFailure(new Exception(throwable));
                        activity.finish();
                    }
                }));
    }

    private void shareWeb(final Activity activity, final ShareInfo shareInfo) {
        mDisposables.add(Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(ObservableEmitter<Bitmap> emitter) throws Exception {
                try {
                    String imgpath = DataDecoder.decode(activity, shareInfo.getShareImageObject(), false);
                    emitter.onNext(BitmapFactory.decodeFile(imgpath));
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Exception {
                        WebpageObject mediaObject = new WebpageObject();
                        mediaObject.identify = Utility.generateGUID();
                        mediaObject.title = shareInfo.getTitle();
                        mediaObject.description = shareInfo.getSummary();
                        mediaObject.setThumbImage(bitmap);
                        mediaObject.actionUrl = shareInfo.getTargetUrl();
                        mediaObject.defaultText = shareInfo.getText();
                        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
                        weiboMessage.mediaObject = mediaObject;
                        mShareHandler.shareMessage(weiboMessage, false);
                        bitmap.recycle();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mShareListener.shareFailure(new Exception(throwable));
                        activity.finish();
                    }
                }));
    }

    private void shareImages(Activity activity, ShareInfo shareInfo) {
        ShareImageObject shareImageObject = shareInfo.getShareImageObject();
        ArrayList<String> imgList = shareImageObject.getmPathList();
        if (imgList == null || imgList.size() == 0) {
            mShareListener.shareFailure(new Exception("imgList is empty"));
            activity.finish();
            return;
        }
        MultiImageObject multiImageObject = new MultiImageObject();
        //pathList设置的是本地本件的路径,并且是当前应用可以访问的路径，现在不支持网络路径（多图分享依靠微博最新版本的支持，所以当分享到低版本的微博应用时，多图分享失效
        // 可以通过WbSdk.hasSupportMultiImage 方法判断是否支持多图分享,h5分享微博暂时不支持多图）多图分享接入程序必须有文件读写权限，否则会造成分享失败
        ArrayList<Uri> pathList = new ArrayList<>();

        for (String path : imgList) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                pathList.add(Uri.fromFile(file));
            }
        }
        if (pathList.size() == 0) {
            mShareListener.shareFailure(new Exception("image path must be local path"));
            activity.finish();
            return;
        }
        multiImageObject.setImageList(pathList);
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.multiImageObject = multiImageObject;
        mShareHandler.shareMessage(weiboMessage, false);
    }

    private void shareVideo(Activity activity, ShareInfo shareInfo) {
        String videoUrl = shareInfo.getMediaUrl();
        if (TextUtils.isEmpty(videoUrl)) {
            mShareListener.shareFailure(new Exception("video path is empty"));
            return;
        }
        File file = new File(videoUrl);
        if (!file.exists() || !file.isFile()) {
            mShareListener.shareFailure(new Exception("video path must be local path"));
            activity.finish();
            return;
        }
        VideoSourceObject videoSourceObject = new VideoSourceObject();
        videoSourceObject.videoPath = Uri.fromFile(new File(videoUrl));
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.videoSourceObject = videoSourceObject;
        mShareHandler.shareMessage(weiboMessage, false);
    }
}
