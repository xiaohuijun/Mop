package com.test.mopshare.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.test.mopshare.DataDecoder;
import com.test.mopshare.share.ShareListener;
import com.test.mopshare.share.ShareType;
import com.test.mopshare.share.bean.ShareInfo;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DefaultShareInstance implements IShareInstance {
    private CompositeDisposable mDisposables;
    private ShareListener mShareListener;

    @Override
    public void share(Activity activity, ShareInfo shareInfo, ShareListener shareListener) {
        this.mShareListener = shareListener;
        mDisposables = new CompositeDisposable();
        switch (shareInfo.getShareType()) {
            case ShareType.TEXT:
                shareText(activity, shareInfo);
                break;
            case ShareType.WEB:
                shareMedia(activity, shareInfo);
                break;
            case ShareType.IMAGE:
                shareImage(activity, shareInfo);
                break;
            default:
                shareListener.shareFailure(new Exception("is not support shareType" + shareInfo.getShareType()));
                activity.finish();
                break;
        }
    }


    private void shareText(Activity activity, ShareInfo shareInfo) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareInfo.getText());
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, "分享"));
    }


    private void shareMedia(Activity activity, ShareInfo shareInfo) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, String.format("%s %s", shareInfo.getTitle(), shareInfo.getTargetUrl()));
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(sendIntent, "分享"));
    }


    private void shareImage(final Activity activity, final ShareInfo shareInfo) {

        mDisposables.add(Observable.create(new ObservableOnSubscribe<Uri>() {
            @Override
            public void subscribe(ObservableEmitter<Uri> emitter) throws Exception {
                try {
                    Uri uri = Uri.fromFile(new File(DataDecoder.decode(activity, shareInfo.getShareImageObject(), false)));
                    emitter.onNext(uri);
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Uri>() {
                    @Override
                    public void accept(Uri uri) throws Exception {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.setType("image/jpeg");
                        activity.startActivity(Intent.createChooser(shareIntent, "分享"));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mShareListener.shareFailure(new Exception(throwable));
                        activity.finish();
                    }
                }));
    }

    @Override
    public void handleResult(Intent data) {

    }

    @Override
    public boolean isInstall(Context context) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        return context.getPackageManager()
                .resolveActivity(shareIntent, PackageManager.MATCH_DEFAULT_ONLY) != null;
    }

    @Override
    public void recycle() {
        if (mDisposables != null) {
            mDisposables.clear();
            mDisposables = null;
        }
        mShareListener = null;
    }
}
