package com.test.mopshare.share.instance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.test.mopshare.DataDecoder;
import com.test.mopshare.LogUtil;
import com.test.mopshare.share.ShareListener;
import com.test.mopshare.share.SharePlatform;
import com.test.mopshare.share.ShareType;
import com.test.mopshare.share.bean.ShareInfo;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXMiniProgramObject;
import com.tencent.mm.opensdk.modelmsg.WXMusicObject;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXVideoObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.reactivestreams.Subscription;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.LongConsumer;
import io.reactivex.schedulers.Schedulers;

public class WxShareInstance implements IShareInstance {
    private IWXAPI mIWXAPI;
    private static final int THUMB_SIZE = 32 * 1024 * 8;
    private static final int TARGET_SIZE = 100;
    private CompositeDisposable mDisposables;
    private ShareListener mShareListener;

    public WxShareInstance(Activity activity, String wxId) {
        mIWXAPI = WXAPIFactory.createWXAPI(activity.getApplicationContext(), wxId, true);
        mIWXAPI.registerApp(wxId);
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void share(@NonNull Activity activity, @NonNull ShareInfo shareInfo, @NonNull ShareListener shareListener) {
        int shareType = shareInfo.getShareType();
        this.mShareListener = shareListener;
        switch (shareType) {
            case ShareType.TEXT:
                sendText(shareInfo);
                break;
            case ShareType.IMAGE:
                sendImage(activity, shareInfo);
                break;
            case ShareType.IMAGES:
                shareListener.shareFailure(new Exception("wx is not support share imageList"));
                activity.finish();
                break;
            case ShareType.WEB:
                sendWeb(activity, shareInfo);
                break;
            case ShareType.MUSIC:
                sendMusic(activity, shareInfo);
                break;
            case ShareType.VIDEO:
                sendVideo(activity, shareInfo);
                break;
            case ShareType.APPLETS:
                sendWxApplets(activity, shareInfo);
                break;
            default:
                shareListener.shareFailure(new Exception("wx is not support shareType" + shareInfo.getShareType()));
                activity.finish();
                break;
        }
    }

    @Override
    public void handleResult(Intent data) {
        mIWXAPI.handleIntent(data, new IWXAPIEventHandler() {
            @Override
            public void onReq(BaseReq baseReq) {
                LogUtil.w("baseReq" + baseReq.openId + "," + baseReq.getType());
            }

            @Override
            public void onResp(BaseResp baseResp) {
                LogUtil.w("baseResp" + baseResp.openId + "," + baseResp.errCode + "," + baseResp.getType() + "," + baseResp.errStr);
                switch (baseResp.errCode) {
                    case BaseResp.ErrCode.ERR_OK:
                        mShareListener.shareSuccess();
                        break;
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                        mShareListener.shareCancel();
                        break;
                    default:
                        String message = TextUtils.isEmpty(baseResp.errStr) ? "share fail" : baseResp.errStr;
                        mShareListener.shareFailure(new Exception(message));
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
        mShareListener = null;
    }

    private void sendText(@NonNull ShareInfo shareInfo) {
        WXTextObject textObj = new WXTextObject();
        textObj.text = shareInfo.getText();
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        msg.title = shareInfo.getTitle();
        msg.description = shareInfo.getSummary();
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = getTargetScene(shareInfo.getPlatform());
        mIWXAPI.sendReq(req);
    }

    private void sendImage(@NonNull final Activity activity, @NonNull final ShareInfo shareInfo) {
        mDisposables.add(Flowable.create(new FlowableOnSubscribe<Pair<Bitmap, byte[]>>() {
            @Override
            public void subscribe(FlowableEmitter<Pair<Bitmap, byte[]>> emitter) {
                try {
                    String imagePath = DataDecoder.decode(activity, shareInfo.getShareImageObject(), false);
                    emitter.onNext(Pair.create(BitmapFactory.decodeFile(imagePath),
                            DataDecoder.compress2Byte(imagePath, TARGET_SIZE, THUMB_SIZE)));
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
                .subscribe(new Consumer<Pair<Bitmap, byte[]>>() {
                    @Override
                    public void accept(Pair<Bitmap, byte[]> pair) {
                        WXImageObject imageObject = new WXImageObject(pair.first);
                        WXMediaMessage message = new WXMediaMessage(imageObject);
                        message.title = shareInfo.getTitle();
                        message.description = shareInfo.getSummary();
                        message.thumbData = pair.second;
                        pair.first.recycle();
                        sendMessage(message, buildTransaction("img"), shareInfo.getPlatform());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        activity.finish();
                        mShareListener.shareFailure(new Exception(throwable));
                    }
                }));
    }

    private void sendMusic(final Activity activity, final ShareInfo shareInfo) {
        WXMusicObject music = new WXMusicObject();
        music.musicUrl = shareInfo.getMediaUrl();
        doShare(activity, shareInfo, music, "music");
    }

    private void sendVideo(final Activity activity, final ShareInfo shareInfo) {
        WXVideoObject video = new WXVideoObject();
        video.videoUrl = shareInfo.getMediaUrl();
        doShare(activity, shareInfo, video, "video");
    }

    private void sendWeb(final Activity activity, final ShareInfo shareInfo) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareInfo.getTargetUrl();
        doShare(activity, shareInfo, webpage, "webpage");
    }

    /**
     * 分享小程序 若客户端版本低于6.5.6，小程序类型分享将自动转成网页类型分享。开发者必须填写网页链接字段，确保低版本客户端能正常打开网页链接。
     */
    private void sendWxApplets(final Activity activity, final ShareInfo shareInfo) {
        WXMiniProgramObject miniProgramObj = new WXMiniProgramObject();
        miniProgramObj.webpageUrl = shareInfo.getTargetUrl(); // 兼容低版本的网页链接
        miniProgramObj.miniprogramType = WXMiniProgramObject.MINIPTOGRAM_TYPE_RELEASE;// 正式版:0，测试版:1，体验版:2
        miniProgramObj.userName = shareInfo.getAppId();     // 小程序原始id
        miniProgramObj.path = shareInfo.getAppPath();       //小程序页面路径
        shareInfo.setPlatform(SharePlatform.WX);
        doShare(activity, shareInfo, miniProgramObj, "webpage");
    }

    private void doShare(final Activity activity, final ShareInfo shareInfo, final WXMediaMessage.IMediaObject mediaObject, final String type) {
        mDisposables.add(Flowable.create(new FlowableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(FlowableEmitter<byte[]> emitter) {
                try {
                    String imagePath = DataDecoder.decode(activity, shareInfo.getShareImageObject(), false);
                    emitter.onNext(DataDecoder.compress2Byte(imagePath, TARGET_SIZE, THUMB_SIZE));
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
                .subscribe(new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] thumbData) {
                        WXMediaMessage msg = new WXMediaMessage(mediaObject);
                        msg.title = shareInfo.getTitle();
                        msg.description = shareInfo.getSummary();
                        msg.thumbData = thumbData;
                        sendMessage(msg, type, shareInfo.getPlatform());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        activity.finish();
                        mShareListener.shareFailure(new Exception(throwable));
                    }
                }));
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    private int getTargetScene(int platfrom) {
        return platfrom == SharePlatform.WX_TIMELINE ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
    }

    private void sendMessage(WXMediaMessage msg, String type, int platfrom) {
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction(type);
        req.message = msg;
        req.scene = getTargetScene(platfrom);
        mIWXAPI.sendReq(req);
    }
}
