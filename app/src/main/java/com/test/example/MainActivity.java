package com.test.example;

import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.test.mopshare.LogUtil;
import com.test.mopshare.share.ShareListener;
import com.test.mopshare.share.SharePlatform;
import com.test.mopshare.share.ShareType;
import com.test.mopshare.share.ShareUtil;
import com.test.mopshare.share.bean.ShareImageObject;
import com.test.mopshare.share.bean.ShareInfo;
import com.mopframeworkcore.config.FrameworkConfig;
import com.mopframeworkcore.exception.CrashUtils;
import com.mopframeworkcore.imageloader.ImageLoader;
import com.mopframeworkcore.imageloader.ImageLoaderConfiguration;
import com.mopframeworkcore.imageloader.progress.ProgressListener;
import com.mopframeworkcore.imageloader.progress.body.ProgressInfo;
import com.mopframeworkcore.log.LogUtils;
import com.mopframeworkcore.mvp.BaseMvpActivity;
import com.mopframeworkcore.mvp.CreatePresenter;
import com.mopframeworkcore.mvp.PresenterVariable;
import com.mopframeworkcore.permission.PermissionHelper;

import java.util.HashMap;
import java.util.Map;

@CreatePresenter(presenter = MainPresenter.class)
public class MainActivity extends BaseMvpActivity<MainPresenter> {
    private Map<String, Object> parameters = new HashMap<String, Object>();
    @PresenterVariable
    private MainPresenter mainPresenter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void init() {
        ImageLoader.getInstance().clearImageDiskCache(mContext);
        final Map<String, String> headers = new HashMap<>();
        headers.put("apikey", "27b6fb21f2b42e9d70cd722b2ed038a9");
        headers.put("Accept", "application/json");
        FrameworkConfig.getInstance().init(mContext, true)
                .initCrash(null, new CrashUtils.OnCrashListener() {
                    @Override
                    public void onCrash(String crashInfo, Throwable e) {

                    }
                })
                .initNet(mContext, headers, parameters, "https://apis.baidu.com/", true);

        ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder()
                .setShowPlace(true)
                .setBlur(true)
                .setBlurProgress(25)
                .build();
        ImageLoader.getInstance().displayImageWithProgress("https://pic1.ajkimg.com/display/xinfang/c505ba5db4576b9d72a1d542e8a0c6a1/860x10000.jpg",
                (ImageView) findViewById(R.id.ivTest), configuration, new ProgressListener() {
                    @Override
                    public void onProgress(ProgressInfo progressInfo) {
                        LogUtils.w(progressInfo);
                    }

                    @Override
                    public void onError(long id, Exception e) {

                    }
                });


        findViewById(R.id.btnTestNet).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestStorage(new PermissionHelper.OnPermissionGrantedListener() {
                    @Override
                    public void onPermissionGranted() {
                        ShareInfo shareInfo = new ShareInfo();
                        shareInfo.setPlatform(SharePlatform.DEFAULT);
                        shareInfo.setShareType(ShareType.IMAGE);
                        ShareImageObject shareImageObject = new ShareImageObject("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1534345888596&di=778835684a69ba712eb30405d9bc8b63&imgtype=0&src=http%3A%2F%2Ffj.ikafan.com%2Fattachment%2Fforum%2F201301%2F25%2F211926pf6g8x0n2vav1g6a.png");
                        shareInfo.setShareImageObject(shareImageObject);
                        ShareUtil.getInstance().share(mContext, shareInfo, new ShareListener() {
                            @Override
                            public void shareSuccess() {
                                Toast.makeText(mContext,"分享成功",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void shareFailure(Exception e) {
                                LogUtil.w(e.getMessage());
                                Toast.makeText(mContext,"分享失败",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void shareCancel() {
                                Toast.makeText(mContext,"分享取消",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });


//                OkHttpUtil.getInstance().downLoad(mContext, "https://pic1.ajkimg.com/display/xinfang/c505ba5db4576b9d72a1d542e8a0c6a1/860x10000.jpg", parameters, new RxFileCallBack() {
//                    @Override
//                    public void onNext(Object tag, File file) {
//                        LogUtils.w("file:" + file.getAbsolutePath());
//                    }
//
//                    @Override
//                    public void onProgress(Object tag, float progress, long downloaded, long total) {
//                        LogUtils.w("progress:" + progress);
//                    }
//
//                    @Override
//                    public void onError(Object tag, MThrowable e) {
//
//                    }
//
//                    @Override
//                    public void onCancel(Object tag, MThrowable e) {
//
//                    }
//                });

//                new Novate.Builder(mContext)
//                        .addHeader(headers)
//                        .addParameters(parameters)
//                        .baseUrl("https://apis.baidu.com/")
//                        .addHeader(headers)
//                        .addLog(true)
//                        .build().rxDownload(mContext, "https://pic1.ajkimg.com/display/xinfang/c505ba5db4576b9d72a1d542e8a0c6a1/860x10000.jpg"
//                        , new RxFileCallBack("test.jpg") {
//                            @Override
//                            public void onNext(Object tag, File file) {
//                                LogUtils.w("file:" + file.getAbsolutePath());
//                            }
//
//                            @Override
//                            public void onProgress(Object tag, float progress, long downloaded, long total) {
//                                LogUtils.w("progress:" + progress );
//                            }
//
//                            @Override
//                            public void onError(Object tag, MThrowable e) {
//
//                            }
//
//                            @Override
//                            public void onCancel(Object tag, MThrowable e) {
//
//                            }
//                        });
            }
        });
    }
}
