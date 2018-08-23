package com.test.mopshare.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.test.mopshare.DataDecoder;
import com.test.mopshare.InitConfig;
import com.test.mopshare.LogUtil;
import com.test.mopshare.share.ShareListener;
import com.test.mopshare.share.SharePlatform;
import com.test.mopshare.share.ShareType;
import com.test.mopshare.share.bean.ShareImageObject;
import com.test.mopshare.share.bean.ShareInfo;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

import org.reactivestreams.Subscription;

import java.io.File;
import java.util.ArrayList;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.LongConsumer;
import io.reactivex.schedulers.Schedulers;

public class QqShareInstance implements IShareInstance {
    private Tencent mTencent;
    private CompositeDisposable mDisposables;
    private ShareListener mShareListener;
    private static final int MAX_QQ_TITLE_LENGTH = 30;//分享的标题, 最长30个字符。
    private static final int MAX_QQ_SUMMARY_LENGTH = 40;//分享的消息摘要，最长40个字。
    private static final int MAX_QQZONE_TITLE_LENGTH = 200;//分享qq空间标题 最长200字
    private static final int MAX_QQZONE_SUMMARY_LENGTH = 600;//分享qq空间描述 最长600字
    private static final int MAX_VIDEO_SIZE = 100;//空间最大发布100M视频

    public QqShareInstance(Context context, String appid) {
        mTencent = Tencent.createInstance(appid, context.getApplicationContext());
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void share(@NonNull final Activity activity, @NonNull final ShareInfo shareInfo, @NonNull ShareListener shareListener) {
        this.mShareListener = shareListener;
        if (shareInfo.getPlatform() == SharePlatform.QZONE) {
            doShareQQZone(activity, shareInfo);
            return;
        }
        doShareQQ(activity, shareInfo);
    }

    @Override
    public void handleResult(Intent data) {
        Tencent.handleResultData(data, mUiListener);
    }

    @Override
    public boolean isInstall(Context context) {
        return mTencent.isQQInstalled(context);
    }

    @Override
    public void recycle() {
        if (mTencent != null) {
            mTencent.releaseResource();
            mTencent = null;
        }
        if (mDisposables != null) {
            mDisposables.clear();
            mDisposables = null;
        }
        mShareListener = null;
    }

    private void doShareQQZone(Activity activity, ShareInfo shareInfo) {
        switch (shareInfo.getShareType()) {
            case ShareType.WEB:
                shareQQZoneDefault(activity, shareInfo);
                break;
            case ShareType.IMAGE:
            case ShareType.IMAGES:
            case ShareType.VIDEO:
                publishQQZone(activity, shareInfo);
                break;
            default:
                mShareListener.shareFailure(new Exception("not support shareType " + shareInfo.getShareType() + " share to QQZONE "));
                activity.finish();
                break;
        }
    }

    private void shareQQZoneDefault(final Activity activity, final ShareInfo shareInfo) {
        mDisposables.add(Observable.create(new ObservableOnSubscribe<ArrayList<String>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<String>> emitter) throws Exception {
                ShareImageObject shareImageObject = shareInfo.getShareImageObject();
                ArrayList<String> pathList = shareImageObject.getmPathList();
                if (pathList != null && pathList.size() > 0) {
                    emitter.onNext(pathList);
                    return;
                }
                try {
                    String imagePath = DataDecoder.decode(activity, shareInfo.getShareImageObject(), true);
                    pathList = new ArrayList<>();
                    pathList.add(imagePath);
                    emitter.onNext(pathList);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> imgList) throws Exception {
                        Bundle params = new Bundle();
                        params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                        params.putString(QzoneShare.SHARE_TO_QQ_TITLE, getQzShareTitle(shareInfo.getTitle()));//必填
                        params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, getQzShareSummary(shareInfo.getSummary()));//选填
                        params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, shareInfo.getTargetUrl());//必填
                        params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imgList);
                        mTencent.shareToQzone(activity, params, mUiListener);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        activity.finish();
                        mShareListener.shareFailure(new Exception(throwable));
                    }
                }));
    }

    private void publishQQZone(final Activity activity, final ShareInfo shareInfo) {
        final boolean isPublishVideo = (shareInfo.getShareType() == ShareType.VIDEO);
        mDisposables.add(Observable.create(new ObservableOnSubscribe<ArrayList<String>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<String>> emitter) throws Exception {
                if (isPublishVideo) {
                    ArrayList<String> list = new ArrayList<>();
                    if (!TextUtils.isEmpty(shareInfo.getMediaUrl())) {
                        File file = new File(shareInfo.getMediaUrl());
                        if (file.exists() && file.isFile()) {
                            list.add(shareInfo.getMediaUrl());
                            emitter.onNext(list);
                        } else {
                            emitter.onError(new Exception("video file not exist"));
                        }
                    } else {
                        emitter.onError(new Exception("vieoPath is empty"));
                    }
                    return;
                }
                ShareImageObject shareImageObject = shareInfo.getShareImageObject();
                ArrayList<String> pathList = shareImageObject.getmPathList();
                if (pathList != null && pathList.size() > 0) {
                    emitter.onNext(pathList);
                    return;
                }
                try {
                    String imagePath = DataDecoder.decode(activity, shareInfo.getShareImageObject(), true);
                    pathList = new ArrayList<>();
                    pathList.add(imagePath);
                    emitter.onNext(pathList);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> list) throws Exception {
                        Bundle params = new Bundle();
                        params.putInt(QzonePublish.PUBLISH_TO_QZONE_KEY_TYPE, isPublishVideo ?
                                QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO : QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
                        params.putString(QzonePublish.PUBLISH_TO_QZONE_SUMMARY, getQzShareSummary(shareInfo.getSummary()));
                        if (!isPublishVideo)
                            params.putStringArrayList(QzonePublish.PUBLISH_TO_QZONE_IMAGE_URL, list);
                        if (isPublishVideo)
                            params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, list.get(0));
                        mTencent.publishToQzone(activity, params, mUiListener);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        activity.finish();
                        mShareListener.shareFailure(new Exception(throwable));
                    }
                }));
    }


    private void doShareQQ(final Activity activity, final ShareInfo shareInfo) {
        final boolean isSupportImageHttpUrl = (shareInfo.getShareType() != ShareType.IMAGE);//纯图分享需要本地地址
        mDisposables.add(Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(FlowableEmitter<String> emitter) {
                try {
                    emitter.onNext(DataDecoder.decode(activity, shareInfo.getShareImageObject(), isSupportImageHttpUrl));
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }, BackpressureStrategy.BUFFER).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnLifecycle(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) {

                    }
                }, new LongConsumer() {
                    @Override
                    public void accept(long t) {
                    }
                }, new Action() {
                    @Override
                    public void run() {
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String imgurl) {
                        LogUtil.w(imgurl);
                        switch (shareInfo.getShareType()) {
                            case ShareType.WEB:
                                shareQQDefault(activity, shareInfo, imgurl);
                            case ShareType.IMAGE:
                                shareQQImage(activity, shareInfo, imgurl);
                                break;
                            case ShareType.MUSIC:
                                shareQQMusic(activity, shareInfo, imgurl);
                                break;
                            case ShareType.APP:
                                shareQQApp(activity, shareInfo, imgurl);
                                break;
                            default:
                                mShareListener.shareFailure(new Exception("not support shareType " + shareInfo.getShareType() + " share to QQ "));
                                activity.finish();
                                break;
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        activity.finish();
                        mShareListener.shareFailure(new Exception(throwable));
                    }
                }));
    }


    private void shareQQDefault(Activity activity, ShareInfo shareInfo, String imgurl) {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, getShareTitle(shareInfo.getTitle()));
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, getShareSummary(shareInfo.getSummary()));
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareInfo.getTargetUrl());
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imgurl);
        if (!TextUtils.isEmpty(InitConfig.instance().getAppName()))
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, InitConfig.instance().getAppName());
        mTencent.shareToQQ(activity, params, mUiListener);
    }

    private void shareQQImage(Activity activity, ShareInfo shareInfo, String imgurl) {
        Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, imgurl);
        if (!TextUtils.isEmpty(InitConfig.instance().getAppName()))
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, InitConfig.instance().getAppName());
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
        mTencent.shareToQQ(activity, params, mUiListener);
    }

    private void shareQQMusic(Activity activity, ShareInfo shareInfo, String imgurl) {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, getShareTitle(shareInfo.getTitle()));
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, getShareSummary(shareInfo.getSummary()));
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareInfo.getTargetUrl());
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imgurl);
        params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL, shareInfo.getMediaUrl());
        if (!TextUtils.isEmpty(InitConfig.instance().getAppName()))
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, InitConfig.instance().getAppName());
        mTencent.shareToQQ(activity, params, mUiListener);
    }

    private void shareQQApp(Activity activity, ShareInfo shareInfo, String imgurl) {
        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
        params.putString(QQShare.SHARE_TO_QQ_TITLE, getShareTitle(shareInfo.getTitle()));
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, getShareTitle(shareInfo.getSummary()));
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imgurl);
        mTencent.shareToQQ(activity, params, mUiListener);
    }

    private IUiListener mUiListener = new IUiListener() {
        @Override
        public void onComplete(Object o) {
            LogUtil.w("qq share success");
            mShareListener.shareSuccess();
        }

        @Override
        public void onError(UiError uiError) {
            LogUtil.w("qq share fail");
            mShareListener.shareFailure(new Exception("errorCode:" + uiError.errorCode + " errorMessage:" + uiError.errorMessage
                    + " errorDetail:" + uiError.errorDetail));
        }

        @Override
        public void onCancel() {
            LogUtil.w("qq share cancel");
            mShareListener.shareCancel();
        }
    };

    private String getShareTitle(String title) {
        return title != null && title.length() > MAX_QQ_TITLE_LENGTH ? title.substring(0, MAX_QQ_TITLE_LENGTH) : title;
    }

    private String getShareSummary(String summary) {
        return summary != null && summary.length() > MAX_QQ_SUMMARY_LENGTH ? summary.substring(0, MAX_QQ_SUMMARY_LENGTH) : summary;
    }

    private String getQzShareTitle(String title) {
        return title != null && title.length() > MAX_QQZONE_TITLE_LENGTH ? title.substring(0, MAX_QQZONE_TITLE_LENGTH) : title;
    }

    private String getQzShareSummary(String summary) {
        return summary != null && summary.length() > MAX_QQZONE_SUMMARY_LENGTH ? summary.substring(0, MAX_QQZONE_SUMMARY_LENGTH) : summary;
    }
}
